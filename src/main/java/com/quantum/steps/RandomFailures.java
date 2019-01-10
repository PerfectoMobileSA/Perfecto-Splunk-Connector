package com.quantum.steps;

import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import cucumber.api.java.en.Given;
import java.util.Random;

@QAFTestStepProvider
public class RandomFailures {

//    These tests are intended to illustrate how the test data will be organized by 'Primary' and 'Secondary' keys when exported to Splunk after execution.
    @Given("Test_1")
    public void Test_1()throws Exception{
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_2")
    public void Test_2()throws Exception{
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_3")
    public void Test_3()throws Exception{
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
}