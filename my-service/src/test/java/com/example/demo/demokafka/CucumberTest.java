package com.example.demo.demokafka;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"json:target/cucumber.json"},
        features = "classpath:features",
        glue = {"com.example.demo.demokafka", "com.decathlon.tzatziki.steps", "com.decathlon.tzatziki.steps.custom"},
        tags = "not @ignore")
public class CucumberTest {
}