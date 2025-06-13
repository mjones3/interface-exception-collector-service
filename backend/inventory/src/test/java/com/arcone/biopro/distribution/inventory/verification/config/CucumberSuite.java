package com.arcone.biopro.distribution.inventory.verification.config;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;

/**
 * Cucumber test suite configuration.
 * Tags can be specified via command line using -Dcucumber.filter.tags="@tag1 or @tag2 and not @tag3"
 * The default tags "not @disabled and not @skipOnPipeline" will be used if no command line tags are specified.
 * Command line tags will override the default tags when provided.
 */
@Suite
@IncludeEngines("cucumber")
@SelectDirectories("src/test/java/com/arcone/biopro/distribution/inventory/verification/features")
@ConfigurationParameters({
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"),
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.inventory.verification"),
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @disabled and not @skipOnPipeline"),
    @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true"),
    @ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long")
})
public class CucumberSuite {
}
