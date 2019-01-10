package splunk;

import java.net.Proxy;

//Creating the Thread local instance of SplunkReportingCollector
public class ReportingCollectorFactory {
	
	private static ThreadLocal<SplunkReportingCollector> reporting = new ThreadLocal<SplunkReportingCollector>();

	public static SplunkReportingCollector getCollector() {
		return reporting.get();
	}

	public static void setReporting(SplunkReportingCollector report) {
		reporting.set(report);
	}
	
	public static SplunkReportingCollector createInstance(long sla, String splunkScheme, String splunkHost, String splunkPort,  String splunkToken) {
		return new SplunkReportingCollector(sla, splunkScheme, splunkHost, splunkPort,  splunkToken);
	}
	public static SplunkReportingCollector createInstance(long sla, String splunkScheme, String splunkHost, String splunkPort,  String splunkToken, Proxy proxy) {
		return new SplunkReportingCollector(sla, splunkScheme, splunkHost, splunkPort,  splunkToken, proxy);
	}
	public static SplunkReportingCollector createInstance(long sla, String splunkScheme, String splunkHost, String splunkPort,  String splunkToken, String splunkChannel) {
		return new SplunkReportingCollector(sla, splunkScheme, splunkHost, splunkPort,  splunkToken, splunkChannel);
	}
	public static SplunkReportingCollector createInstance(long sla, String splunkScheme, String splunkHost, String splunkPort,  String splunkToken, String splunkChannel, Proxy proxy) {
		return new SplunkReportingCollector(sla, splunkScheme, splunkHost, splunkPort,  splunkToken, splunkChannel, proxy);
	}
}