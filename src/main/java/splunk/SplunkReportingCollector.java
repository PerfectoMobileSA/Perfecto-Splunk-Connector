package splunk;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SplunkReportingCollector extends Reporting {
	private static ArrayList<HashMap> reportCollector = new ArrayList<>();
	private ArrayList<HashMap> reportFinal = new ArrayList<>();
	private ArrayList<Pair> pairArrayList = new ArrayList<>();

	 private void addReport(Object o) {
		reportCollector.add((HashMap) o);
	}

	// initializing Splunk connection values
	 SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken);
	}

	 SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken, Proxy proxy) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken, proxy);
	}

	 SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken, String splunkChannel) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken, splunkChannel);
	}

	 SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken, String splunkChannel, Proxy proxy) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken, splunkChannel, proxy);
	}

	public void commitSplunk()throws Exception{
		loadParallelReports();
		createJSON();
	}

	/**
	 * Pairs test data by model and method name for parallel testing,
	 * if there is no parallel test case the test case will not be paired.
	 */
	@SuppressWarnings("unchecked")
	private void loadParallelReports(){

		for (int i = 0; i < reportCollector.size(); i++) {
			HashMap singleTestReport = reportCollector.get(i);
			if(i == 0){
				new Pair<HashMap,HashMap>(singleTestReport,null);
			}else{
				boolean setTest = false;

				for (Pair pair : this.pairArrayList) {
					if (pair.test_2 == null) {
						String methodName = (String) singleTestReport.get("methodName");
						String model = (String) singleTestReport.get("model");

						HashMap pair_1 = (HashMap) pair.getTest_1();

						String pair_1_methodName = (String) pair_1.get("methodName");
						String pair_1_model = (String) pair_1.get("model");

						if (methodName.contentEquals(pair_1_methodName) && model.contentEquals(pair_1_model)) {
							pair.setTest_2(singleTestReport);
							setTest = true;
						}
					}
				}
				if(!setTest){
					new Pair<HashMap,HashMap>(singleTestReport,null);
				}
			}
		}
	}

	/**
	 * Creates JSON event per parallel test case or single test case to send to Splunk HEC.
	 * @throws Exception if the JSON is not formatted correctly.
	 */
	@SuppressWarnings("unchecked")
	private void createJSON()throws Exception{
		Gson gson = (new GsonBuilder()).enableComplexMapKeySerialization().disableHtmlEscaping().serializeNulls().create();

		for (Pair pair : this.pairArrayList) {
			HashMap jsonObject = new HashMap<String, HashMap>();
			if (pair.test_2 == null) {
				jsonObject.put("Primary", pair.getTest_1());

			} else {
				HashMap test_1 = (HashMap) pair.getTest_1();
				HashMap test_2 = (HashMap) pair.getTest_2();
				if (gson.toJson(test_1).contains("\"testStatus\":\"Pass\"")) {
					jsonObject.put("Primary", test_1);
					jsonObject.put("Secondary", test_2);
				} else if (gson.toJson(test_2).contains("\"testStatus\":\"Pass\"")) {
					jsonObject.put("Primary", test_2);
					jsonObject.put("Secondary", test_1);
				} else {
					jsonObject.put("Primary", test_1);
					jsonObject.put("Secondary", test_2);
				}
			}
			this.reportFinal.add(jsonObject);
		}
        for (HashMap aReportFinal : this.reportFinal) {
            String jsonReport = gson.toJson(aReportFinal);
            if (this.splunk.getSplunkHost() != null) {
                this.splunk.splunkFeed(jsonReport);
            }
        }
	}

	// stores the individual reports into the json collector; the parameter is the Scenario name in quantum feature file.
	public void submitReporting(String testMethodName) {
		this.steps.put("Steps", this.stepCollector);
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().serializeNulls()
				.create();

		HashMap<String, HashMap> methodDetails = new HashMap<>();
		methodDetails.put(testMethodName, this.steps);

		this.reporting.put("methods", methodDetails);

		String stepsJson = gson.toJson(this.steps);
		if (stepsJson.contains("Fail")) {
			this.reporting.put("performanceStatus", "Fail");
		} else if (stepsJson.contains("Pass")) {
			this.reporting.put("performanceStatus", "Pass");
		}
		System.out.println(gson.toJson(this.reporting));

		HashMap<String, Object> arrEntry = new HashMap<>();
		arrEntry.putAll(this.reporting);
		addReport(arrEntry);
	}
//	Helper class to create tuple object for parallel tests.
	public  class Pair<Test_1, Test_2>{
		private Test_1 test_1;
		private Test_2 test_2;

		Pair(Test_1 test_1,Test_2 test_2){
			this.test_1 = test_1;
			this.test_2 = test_2;
			pairArrayList.add(this);
		}
		Test_1 getTest_1(){return test_1;}
		Test_2 getTest_2(){return test_2;}
		void setTest_2(Test_2 test_2){this.test_2 = test_2;}
	}
}
