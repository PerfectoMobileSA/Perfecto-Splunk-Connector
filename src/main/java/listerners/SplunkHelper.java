package listerners;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import splunk.ReportingCollectorFactory;
import splunk.SplunkReportingCollector;

import java.util.HashMap;
import java.util.Map;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

public abstract class SplunkHelper {

	public static SplunkReportingCollector getCollector() {
		return ((SplunkReportingCollector) getBundle().getObject("splunkCollector"));
	}

	public static QAFExtendedWebDriver getQAFDriver() {
		return new WebDriverTestBase().getDriver();
	}

	public static void testStepStart(String stepName, String stepDesc) throws Exception {
		getCollector().startTransaction(stepName, stepDesc);
	}

	public static String getMonitorTag() {
		return ConfigurationManager.getBundle().getString("monitorTag");
	}

	public static void testStepEnd(long sla, String stepName) throws Exception {
		getCollector().endTransaction(sla, stepName, getUXTimer());
	}

	public static long getUXTimer() {
		return timerGet("ux");
	}

	public static long timerGet(String timerType) {
		String command = "mobile:timer:info";
		long result = 0;
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", timerType);
		try {
			result = (long) getQAFDriver().executeScript(command, params);
		} catch (Exception ex) {

		}
		return result;
	}

	public static void setSplunk() {
		SplunkReportingCollector reporting;
		try {
			if (!((String) getBundle().getProperty("splunkChannel")).equalsIgnoreCase("")) {
				reporting = ReportingCollectorFactory.createInstance(
						Long.parseLong((String) getBundle().getProperty("globalSLA")),
						(String) getBundle().getProperty("splunkSchema"),
						(String) getBundle().getProperty("splunkHost"),
						(String) getBundle().getProperty("splunkPort"),
						(String) getBundle().getProperty("splunkToken"),
						(String) getBundle().getProperty("splunkChannel"));
			} else {
//				System.out.println("\n"+"Setting Splunk paramaters"+"\n");

				reporting = ReportingCollectorFactory.createInstance(
						Long.parseLong((String) getBundle().getProperty("globalSLA")),
						(String) getBundle().getProperty("splunkSchema"),
						(String) getBundle().getProperty("splunkHost"),
						(String) getBundle().getProperty("splunkPort"),
						(String) getBundle().getProperty("splunkToken"));

			}
		} catch (Exception ex) {
//			System.out.println("\n"+"Else block - Setting Splunk paramaters"+"\n");

			reporting = ReportingCollectorFactory.createInstance(
					Long.parseLong((String) getBundle().getProperty("globalSLA")),
					(String) getBundle().getProperty("splunkSchema"),
					(String) getBundle().getProperty("splunkHost"),
					(String) getBundle().getProperty("splunkPort"),
					(String) getBundle().getProperty("splunkToken"));
		}

		ReportingCollectorFactory.setReporting(reporting);

		getBundle().setProperty("splunkCollector", reporting);
	}

	public static String getDeviceInfo(String value) {
		Map<String, Object> params1 = new HashMap<>();
		params1.put("property", value);
		String result1 = (String) getQAFDriver().executeScript("mobile:handset:info", params1);
		return result1;
	}

}
