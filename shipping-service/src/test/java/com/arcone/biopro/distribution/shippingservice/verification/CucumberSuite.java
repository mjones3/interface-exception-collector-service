package com.arcone.biopro.distribution.shippingservice.verification;

import org.junit.platform.suite.api.*;

import static io.cucumber.core.options.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectDirectories("src/test/java/com/arcone/biopro/distribution/shippingservice/verification/features")
@ConfigurationParameters({
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.arcone.biopro.distribution.shippingservice.verification.steps"),
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @Disabled"),
    @ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    )
})
public class CucumberSuite {
}
