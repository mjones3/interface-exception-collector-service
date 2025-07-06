package com.arcone.biopro.distribution.irradiation.verification.config;

import com.arcone.biopro.testing.frontend.config.WebDriverConfig;
import com.arcone.biopro.testing.frontend.core.WebNavigator;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Slf4j
public class CucumberHooks {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebDriverConfig webDriverConfig;


    @Before
    public void setup(Scenario scenario) throws Exception {
        if (scenario.getSourceTagNames().contains("@ui")) {
            webDriverConfig.getWebDriver(); // Initialize only for @ui scenarios
        }
    }

    @After
    public void afterEachScenario(Scenario scenario) throws MalformedURLException, URISyntaxException {
        if (scenario.getSourceTagNames().contains("@ui")) {
            WebNavigator webNavigator = applicationContext.getBean(WebNavigator.class);
            webDriverConfig.cleanup();
        }
    }
}
