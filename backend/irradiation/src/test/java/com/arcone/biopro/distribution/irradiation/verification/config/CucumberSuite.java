package com.arcone.biopro.distribution.irradiation.verification.config;

import org.junit.platform.suite.api.*;

import static io.cucumber.core.options.Constants.*;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectDirectories({
    "src/test/java/com/arcone/biopro/distribution/irradiation/verification/api/features",
    "src/test/java/com/arcone/biopro/distribution/irradiation/verification/ui/features"
})
@ConfigurationParameters({
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.irradiation.verification"),
    @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true"),
    @ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long")
})
public class CucumberSuite {
}
