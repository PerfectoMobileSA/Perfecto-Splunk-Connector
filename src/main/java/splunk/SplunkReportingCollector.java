package splunk;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SplunkReportingCollector extends Reporting {
	public static ArrayList<HashMap> reportCollector = new ArrayList<HashMap>();
	public  ArrayList<HashMap> reportFinal = new ArrayList<HashMap>();
	public  ArrayList<Pair> pairArrayList = new ArrayList<Pair>();


	public void addReport(Object o) {
		reportCollector.add((HashMap) o);
	}

	// initializing splunk connection values
	public SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken);
	}

	public SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken, Proxy proxy) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken, proxy);
	}

	public SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
									String splunkToken, String splunkChannel) {
		super(sla, splunkScheme, splunkHost, splunkPort, splunkToken, splunkChannel);
	}

	public SplunkReportingCollector(long sla, String splunkScheme, String splunkHost, String splunkPort,
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
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void loadParallelReports()throws Exception{
		System.out.println("report collector size: "+reportCollector.size());
		int counter =1;
		for (int i = 0; i < reportCollector.size(); i++) {
			System.out.println( "each test method: " +reportCollector.get(i).get("methodName"));
			HashMap singleTestReport = reportCollector.get(i);
			if(i == 0){
				new Pair<HashMap,HashMap>(singleTestReport,null);
				counter++;
			}else{
				boolean setTest = false;

				for (int j = 0; j <this.pairArrayList.size() ; j++) {
					if(this.pairArrayList.get(j).test_2 == null){
						String methodName = (String)singleTestReport.get("methodName");
						String model = (String )singleTestReport.get("model");

						HashMap pair_1 = (HashMap)this.pairArrayList.get(j).getTest_1();

						String pair_1_methodName = (String)pair_1.get("methodName");
						String pair_1_model = (String)pair_1.get("model");

						if(methodName.contentEquals(pair_1_methodName) && model.contentEquals(pair_1_model)){
							this.pairArrayList.get(j).setTest_2(singleTestReport);
							counter++;
							setTest = true;
						}
					}
				}if(!setTest){
					new Pair<HashMap,HashMap>(singleTestReport,null);
					counter++;
				}
			}
		}
		System.out.println("Created Pair objects: " + counter);
	}

	/**
	 * Creates JSON event per parallel test case or single test case to send to Splunk HEC.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void createJSON()throws Exception{
		Gson gson = (new GsonBuilder()).enableComplexMapKeySerialization().disableHtmlEscaping().serializeNulls().create();
		System.out.println("pairArrayList size: " + this.pairArrayList.size());

		for (int i = 0; i < this.pairArrayList.size(); i++) {
			HashMap jsonObject = new HashMap<String,HashMap>();
			if(this.pairArrayList.get(i).test_2 == null) {
				jsonObject.put("Primary", this.pairArrayList.get(i).getTest_1());

			}else{
				HashMap test_1 = (HashMap) this.pairArrayList.get(i).getTest_1();
				HashMap test_2 = (HashMap) this.pairArrayList.get(i).getTest_2();
				if(gson.toJson(test_1).contains("\"testStatus\":\"Pass\"")){
					jsonObject.put("Primary",test_1);
					jsonObject.put("Secondary",test_2);
				}else if(gson.toJson(test_2).contains("\"testStatus\":\"Pass\"")){
					jsonObject.put("Primary",test_2);
					jsonObject.put("Secondary",test_1);
				}else{
					jsonObject.put("Primary",test_1);
					jsonObject.put("Secondary",test_2);
				}
			}
			this.reportFinal.add(jsonObject);
		}
		for (int i = 0; i <this.reportFinal.size() ; i++) {
			System.out.println("reportFinal size: " +reportFinal.size());
			String jsonReport = gson.toJson(this.reportFinal.get(i));
			if (this.splunk.getSplunkHost() != null) {
				this.splunk.splunkFeed(jsonReport);
			}
		}
	}

	// stores the individual reports into the json collector; the param is Scenario name in quantum feature file
	public void submitReporting(String testMethodName) {
		this.steps.put("Steps", this.stepCollector);
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().serializeNulls()
				.create();

		HashMap<String, HashMap> methodDetails = new HashMap<String, HashMap>();
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
	public  class Pair<Test_1, Test_2>{
		private Test_1 test_1;
		private Test_2 test_2;

		public Pair(Test_1 test_1,Test_2 test_2){
			this.test_1 = test_1;
			this.test_2 = test_2;
			pairArrayList.add(this);
		}
		public Test_1 getTest_1(){return test_1;}
		public Test_2 getTest_2(){return test_2;}
		public void setTest_1(Test_1 test_1){this.test_1 = test_1;}
		public void setTest_2(Test_2 test_2){this.test_2 = test_2;}
	}
}
