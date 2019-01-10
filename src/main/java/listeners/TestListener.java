package listeners;

import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.quantum.listeners.QuantumReportiumListener;
import com.quantum.utils.ConsoleUtils;
import cucumber.api.java.en.Then;
import org.testng.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * TestNG listener class that collects the Splunk host data upon test suite start. Further collects test data after each test
 * method finishes, and lastly, upon test suite finish, arranges the test data into JSON events and sends to Splunk.
 */
public class TestListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite arg0) { SplunkHelper.setSplunk();}

    @Override
    public void onTestStart(ITestResult result) { SplunkHelper.getCollector().testExecutionStart(); }

    @Override
    public void onStart(ITestContext context) { SplunkHelper.setSplunk();}

	@Override
	public void onTestSuccess(ITestResult testResult) {
		setDetails("Pass", testResult);
	}

	@Override
	public void onTestFailure(ITestResult testResult) { setDetails("Fail", testResult); }

	@Override
	public void onTestSkipped(ITestResult result) {
		setDetails("Skip", result);
	}

	private void setDetails(String result, ITestResult testResult) {

		SplunkHelper.getCollector().reporting.put("testStatus", result);

		SplunkHelper.getCollector().reporting.put("className",
				testResult.getMethod().getInstance().getClass().getName());

		try {
			SplunkHelper.getCollector().reporting.put("hostName", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!result.equalsIgnoreCase("Skip")) {

			SplunkHelper.getCollector().reporting.put("model", SplunkHelper.getDeviceInfo("model"));
			SplunkHelper.getCollector().reporting.put("device", SplunkHelper.getDeviceInfo("deviceID"));
			SplunkHelper.getCollector().reporting.put("os", SplunkHelper.getDeviceInfo("os"));
			SplunkHelper.getCollector().reporting.put("location", SplunkHelper.getDeviceInfo("location"));
			SplunkHelper.getCollector().reporting.put("description", SplunkHelper.getDeviceInfo("description"));
			try {
				SplunkHelper.getCollector().reporting.put("monitorTag", SplunkHelper.getMonitorTag());
			} catch (Exception ex) {
                ConsoleUtils.logInfoBlocks("This test does not contain a monitor tag.");
			}
		}

		if (result.equalsIgnoreCase("Fail")) {
			if (testResult.getThrowable() != null)
				if (testResult.getThrowable().getStackTrace() != null) {
					StringWriter sw = new StringWriter();
					testResult.getThrowable().printStackTrace(new PrintWriter(sw));
					SplunkHelper.getCollector().reporting.put("stackTrace", sw.toString());
				}
		}

		if (!result.equalsIgnoreCase("Skip")) {

    // Report logger that accomplishes 3 things.
    //  1. Sets the end time of the test.
    //  2. Divides the start and end time to create a test duration in seconds.
    //  3. Converts the start/end time to real date formats.
			SplunkHelper.getCollector().testExecutionEnd();
		}

		SplunkHelper.getCollector().reporting.put("testName", testResult.getTestContext().getName());
		SplunkHelper.getCollector().reporting.put("methodName", testResult.getMethod().getMethodName());
		SplunkHelper.getCollector().reporting.put("executionID", SplunkHelper.getQAFDriver().getCapabilities().getCapability("executionId"));
		SplunkHelper.getCollector().reporting.put("reportiumReport", QuantumReportiumListener.getReportClient().getReportUrl());
		SplunkHelper.getCollector().submitReporting(testResult.getMethod().getMethodName());
	}
// Arranges test data into JSON events.
	@Override
	public void onFinish(ISuite arg0) {
		try {
			SplunkHelper.getCollector().commitSplunk();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

	@Override
	public void onFinish(ITestContext context) {}

    /**
     * The PerfectoSplunkSteps methods are intended to be used in a Quantum Cucumber implementation for text or image checkpoint timers.
     */

	@QAFTestStepProvider
    public static class PerfectoSplunkSteps {

        @Then("^I perform a Splunk transaction using the name \"([^\"]*)\" and description of \"([^\"]*)\" with an SLA of \"([^\"]*)\" - I'm also utilizing an OCR checkpoint for the word \"([^\"]*)\" with a timeout of \"([^\"]*)\" and threshold of \"([^\"]*)\"$")
        public void visualOCRTimer(String name, String desc, String SLA, String text, String timeout, String threshold)
                throws Exception {

            SplunkHelper.testStepStart(name, desc);

            Map<String, Object> params1 = new HashMap<>();
            params1.put("content", text);
            params1.put("source", "camera");
            params1.put("timeout", timeout);
            params1.put("measurement", "accurate");
            params1.put("threshold", threshold);
            params1.put("analysis", "automatic");
            Object result1 = SplunkHelper.getQAFDriver().executeScript("mobile:checkpoint:text", params1);

            if (!result1.toString().contains("true")) {
                throw new Exception("Text not found!");
            }
            SplunkHelper.testStepEnd(Long.parseLong(SLA), name);
        }

        @Then("^I perform a Splunk transaction using the name \"([^\"]*)\" and description of \"([^\"]*)\" with an SLA of \"([^\"]*)\" - I'm also utilizing an Image checkpoint for the image \"([^\"]*)\" with a timeout of \"([^\"]*)\" and threshold of \"([^\"]*)\"$")
        public void visualImageTimer(String name, String desc, String SLA, String repo, String timeout, String threshold)
                throws Exception {

            SplunkHelper.testStepStart(name, desc);

            Map<String, Object> params1 = new HashMap<>();
            params1.put("content", repo);
            params1.put("match", "identical");
            params1.put("source", "camera");
            params1.put("measurement", "accurate");
            params1.put("timeout", timeout);
            params1.put("threshold", threshold);
            Object result1 = SplunkHelper.getQAFDriver().executeScript("mobile:checkpoint:image", params1);

            if (!result1.toString().contains("true")) {
                throw new Exception("Image not found!");
            }
            SplunkHelper.testStepEnd(Long.parseLong(SLA), name);
        }
    }
}
