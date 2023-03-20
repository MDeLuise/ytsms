package com.github.mdeluise.ytsms.integration;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    glue = {
        "com.github.mdeluise.ytsms.integration",
        "com.github.mdeluise.ytsms.integration.steps"
    },
    plugin = {"pretty"}
)
public class IntegrationRunnerTest {
}
