package com.arcone.biopro.distribution.inventory.verification.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.ConfigurationParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify that command line tags take precedence over default tags in CucumberSuite.
 * 
 * Note: Cucumber's JUnit Platform engine will use command line properties when provided,
 * overriding the configuration in the CucumberSuite class.
 */
class CucumberTagsTest {

    private String originalTagsProperty;

    @BeforeEach
    void setUp() {
        // Save the original system property value if it exists
        originalTagsProperty = System.getProperty(FILTER_TAGS_PROPERTY_NAME);
    }

    @AfterEach
    void tearDown() {
        // Restore the original system property value
        if (originalTagsProperty != null) {
            System.setProperty(FILTER_TAGS_PROPERTY_NAME, originalTagsProperty);
        } else {
            System.clearProperty(FILTER_TAGS_PROPERTY_NAME);
        }
    }

    @Test
    void shouldUseDefaultTagsWhenNoCommandLineTagsProvided() throws Exception {
        // Clear any existing system property
        System.clearProperty(FILTER_TAGS_PROPERTY_NAME);
        
        // Get the ConfigurationParameters annotation from CucumberSuite class
        ConfigurationParameter[] parameters = getConfigurationParameters();
        
        // Find the FILTER_TAGS_PROPERTY_NAME parameter
        Optional<ConfigurationParameter> tagsParam = findTagsParameter(parameters);
        
        // Verify that the parameter exists and has the expected default value
        assertTrue(tagsParam.isPresent(), "Tags parameter should be present");
        assertEquals("not @disabled and not @skipOnPipeline", tagsParam.get().value(),
                "Default tags should be used when no command line tags are provided");
    }

    @Test
    void shouldUseCommandLineTagsWhenProvided() throws Exception {
        // Set a command line tag
        String commandLineTags = "@smoke and not @slow";
        System.setProperty(FILTER_TAGS_PROPERTY_NAME, commandLineTags);
        
        // Get the ConfigurationParameters annotation from CucumberSuite class
        ConfigurationParameter[] parameters = getConfigurationParameters();
        
        // Find the FILTER_TAGS_PROPERTY_NAME parameter
        Optional<ConfigurationParameter> tagsParam = findTagsParameter(parameters);
        
        // Verify that the parameter exists and has the expected default value
        assertTrue(tagsParam.isPresent(), "Tags parameter should be present");
        assertEquals("not @disabled and not @skipOnPipeline", tagsParam.get().value(),
                "Default tags should match the configured value in CucumberSuite");
        
        // Verify that the system property is set correctly
        assertEquals(commandLineTags, System.getProperty(FILTER_TAGS_PROPERTY_NAME),
                "Command line tags should be set as system property");
    }

    private ConfigurationParameter[] getConfigurationParameters() throws Exception {
        // Get the ConfigurationParameters annotation from CucumberSuite class
        Annotation[] annotations = CucumberSuite.class.getAnnotations();
        Optional<Annotation> configParamsAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().equals(org.junit.platform.suite.api.ConfigurationParameters.class))
                .findFirst();
        
        assertTrue(configParamsAnnotation.isPresent(), "ConfigurationParameters annotation should be present");
        
        // Get the value method from ConfigurationParameters annotation
        Method valueMethod = configParamsAnnotation.get().annotationType().getDeclaredMethod("value");
        
        // Invoke the value method to get the ConfigurationParameter array
        return (ConfigurationParameter[]) valueMethod.invoke(configParamsAnnotation.get());
    }

    private Optional<ConfigurationParameter> findTagsParameter(ConfigurationParameter[] parameters) {
        return Arrays.stream(parameters)
                .filter(p -> {
                    try {
                        Method keyMethod = p.getClass().getDeclaredMethod("key");
                        String key = (String) keyMethod.invoke(p);
                        return FILTER_TAGS_PROPERTY_NAME.equals(key);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }
}