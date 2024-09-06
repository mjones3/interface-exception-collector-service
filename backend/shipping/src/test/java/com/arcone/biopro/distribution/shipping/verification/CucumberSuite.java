package com.arcone.biopro.distribution.shipping.verification;

import org.junit.platform.suite.api.*;
import org.springframework.test.context.ActiveProfiles;

import static io.cucumber.core.options.Constants.*;

@Suite
@IncludeEngines("cucumber")
@ActiveProfiles("AUTOMATION")
@SelectDirectories("src/test/java/com/arcone/biopro/distribution/shipping/verification/features")
@ConfigurationParameters({
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.shipping.verification.steps"),
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "(@api or @ui) and not @disabled"),
    @ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    ),
    @ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "true"),
    @ConfigurationParameter(key = "cucumber.execution.execution-mode.feature", value = "same_thread"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy", value = "fixed"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism", value = "1"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.max-pool-size", value = "1")
})
public class CucumberSuite {
}
