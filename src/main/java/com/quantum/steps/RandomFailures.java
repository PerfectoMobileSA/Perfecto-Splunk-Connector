package com.quantum.steps;

import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import cucumber.api.java.en.Given;

import java.util.Random;

@QAFTestStepProvider
public class RandomFailures {

    @Given("Test_1")
    public void Test_1()throws Exception{
//        new QAFExtendedWebDriver();
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_2")
    public void Test_2()throws Exception{
//        new QAFExtendedWebDriver();
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_3")
    public void Test_3()throws Exception{
//        new QAFExtendedWebDriver();
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_4")
    public void Test_4()throws Exception{
//        new QAFExtendedWebDriver();
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
    @Given("Test_5")
    public void Test_5()throws Exception{
//        new QAFExtendedWebDriver();
        boolean random = new Random().nextBoolean();
        if(random){
            throw new Exception();
        }
    }
}
