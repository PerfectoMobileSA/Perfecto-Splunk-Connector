# Perfecto-Splunk-Connector
This project collects test data during executions and exports to Splunk HEC

This project was intended to be used with the Quantum Framework although can easily be adapted into any Java TestNG Framework as well.

The test data is collected after each execution and arranged into JSON format prior to exporting to Splunk. 

#Parallel Testing: 
If the test suite included parallel test executions, i.e. the same device model and the same method name, the parallel tests will be grouped togther into one JSON. Each test will be a value of a key called either "Primary" or "Secondary". If only one of the tests failed, the test data of the failed test will be the value a key called "Secondary" and the test that succeeded will be the value of a key called "Primary". The intention is to eliminate false coverage in the event of a flaky test execution. This can be easily implemented in a Splunk query searching for "Primary.testStatus" = "Fail", which inherently will only have the status of "Fail" if its 'Secondary' pair also failed.

#Single Test Executions: 
For single test executions, each single test of the test suite will be formatted into a individual JSON event, and will also be the value of a "Primary" key with no Secondary key.
