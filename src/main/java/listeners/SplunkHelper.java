package listeners;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import splunk.ReportingCollectorFactory;
import splunk.SplunkReportingCollector;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Map;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

 abstract class SplunkHelper {

//     Retrieves the SplunkReportingCollector instance to add test data.
	static SplunkReportingCollector getCollector() {
		return ((SplunkReportingCollector) getBundle().getObject("splunkCollector"));
	}

//	Local helper driver.
	static QAFExtendedWebDriver getQAFDriver() {
		return new WebDriverTestBase().getDriver();
	}

//	Report logger.
	static void testStepStart(String stepName, String stepDesc) throws Exception {
		getCollector().startTransaction(stepName, stepDesc);
	}

//	If a monitorTag was implemented in the test code, here it will be retrieved to be added to the test data.
	static String getMonitorTag() {
		return ConfigurationManager.getBundle().getString("monitorTag");
	}

//	Report logger.
	static void testStepEnd(long sla, String stepName) throws Exception {
		getCollector().endTransaction(sla, stepName, getUXTimer());
	}
	 /**
	  * The default timer is ux, please change the paramater in line 41 to one of the other getTimer methods (lines 62-67) if you need a different timer type.
	  * See <a href="https://developers.perfectomobile.com/display/PD/Get+timer">https://developers.perfectomobile.com/display/PD/Get+timer</a>
	  * @see <a href="https://developers.perfectomobile.com/display/TT/Measuring+User+Experience+Timing">https://developers.perfectomobile.com/display/TT/Measuring+User+Experience+Timing</a>
	  * @return Elapsed time from the start of the checkpoint execution until the text or image was validated.
	  */
	 private static long timerGet(String timerType) {
		String command = "mobile:timer:info";
		long result = 0;
		Map<String, String> params = new HashMap<>();
		params.put("type", timerType);
		try {
			result = (long) getQAFDriver().executeScript(command, params);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

     private static long getUXTimer() {
         return timerGet("ux");
     }
     private static long getElapsedTimer(){return timerGet("elapsed");}
     private static long getSystemTimer(){return timerGet("system");}
     private static long getDeviceTimer(){return timerGet("device");}

//	Collects the Splunk host details into a SplunkReportingCollector instance, to be further enhanced after the test suite finishes.
	 static void setSplunk() {
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
			}
			else if (((String) getBundle().getProperty("splunkProxy")).equalsIgnoreCase("true") &&
                    (!((String) getBundle().getProperty("splunkChannel")).equalsIgnoreCase(""))){

                String proxyType = (String) getBundle().getProperty("proxyType");
                String proxyIP = (String) getBundle().getProperty("proxyIP");
                Integer proxyPort = (Integer) getBundle().getProperty("proxyPort");
                Proxy.Type type= proxyType.contentEquals("HTTP")? Type.HTTP : Type.SOCKS;
                Proxy proxy = new Proxy(type, new InetSocketAddress(proxyIP, proxyPort));

                reporting = ReportingCollectorFactory.createInstance(
                        Long.parseLong((String) getBundle().getProperty("globalSLA")),
                        (String) getBundle().getProperty("splunkSchema"),
                        (String) getBundle().getProperty("splunkHost"),
                        (String) getBundle().getProperty("splunkPort"),
                        (String) getBundle().getProperty("splunkToken"),
                        (String) getBundle().getProperty("splunkChannel"),
                        proxy);
            }
            else if (((String) getBundle().getProperty("splunkProxy")).equalsIgnoreCase("true") &&
                    (((String) getBundle().getProperty("splunkChannel")).equalsIgnoreCase(""))){

                String proxyType = (String) getBundle().getProperty("proxyType");
                String proxyIP = (String) getBundle().getProperty("proxyIP");
                Integer proxyPort = (Integer) getBundle().getProperty("proxyPort");
                Proxy.Type type= proxyType.contentEquals("HTTP")? Type.HTTP : Type.SOCKS;
                Proxy proxy = new Proxy(type, new InetSocketAddress(proxyIP, proxyPort));

                reporting = ReportingCollectorFactory.createInstance(
                        Long.parseLong((String) getBundle().getProperty("globalSLA")),
                        (String) getBundle().getProperty("splunkSchema"),
                        (String) getBundle().getProperty("splunkHost"),
                        (String) getBundle().getProperty("splunkPort"),
                        (String) getBundle().getProperty("splunkToken"),
                        proxy);
            }
			else {
				reporting = ReportingCollectorFactory.createInstance(
						Long.parseLong((String) getBundle().getProperty("globalSLA")),
						(String) getBundle().getProperty("splunkSchema"),
						(String) getBundle().getProperty("splunkHost"),
						(String) getBundle().getProperty("splunkPort"),
						(String) getBundle().getProperty("splunkToken"));

			}
		} catch (Exception ex) {
			reporting = ReportingCollectorFactory.createInstance(
					Long.parseLong((String) getBundle().getProperty("globalSLA")),
					(String) getBundle().getProperty("splunkSchema"),
					(String) getBundle().getProperty("splunkHost"),
					(String) getBundle().getProperty("splunkPort"),
					(String) getBundle().getProperty("splunkToken"));
		}

		ReportingCollectorFactory.setReporting(reporting);

//		Stores the splunk host details in a local quantum variable to avoid threading issues later on in the test.
		getBundle().setProperty("splunkCollector", reporting);
	}

//	Helper method for retrieving device data information that will be included in the Splunk JSON event.
	 static String getDeviceInfo(String value) {
		Map<String, Object> params1 = new HashMap<>();
		params1.put("property", value);
		return (String) getQAFDriver().executeScript("mobile:handset:info", params1);
	}
}
