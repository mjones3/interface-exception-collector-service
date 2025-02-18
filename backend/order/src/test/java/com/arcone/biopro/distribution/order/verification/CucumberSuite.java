package com.arcone.biopro.distribution.order.verification;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.ActiveProfiles;

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectDirectories("src/test/java/com/arcone/biopro/distribution/order/verification/features")
@ConfigurationParameters({
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "json:target/cucumber/cucumber.json"),
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.order.verification"),
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @disabled"),
    @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true"),
    @ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "true"),
    @ConfigurationParameter(key = "cucumber.execution.execution-mode.feature", value = "same_thread"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy", value = "fixed"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism", value = "5"),
    @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.max-pool-size", value = "15")
})
@ActiveProfiles("AUTOMATION")
public class CucumberSuite {
}
