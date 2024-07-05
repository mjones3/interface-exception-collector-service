package com.arcone.biopro.distribution.shippingservice.verification;

import org.junit.platform.suite.api.*;

import static io.cucumber.core.options.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectDirectories("src/test/java/com/arcone/biopro/distribution/shippingservice/verification/features")
@ConfigurationParameters({
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.shippingservice.verification.steps"),
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "(@api or @ui) and not @disabled"),
    @ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    ),
    @ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "true"),
    @ConfigurationParameter(key = "cucumber.execution.execution-mode.feature", value = "same_thread"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy", value = "fixed"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism", value = "4"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.max-pool-size", value = "4")
})
public class CucumberSuite {
}
