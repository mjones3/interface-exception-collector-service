package com.arcone.biopro.distribution.shippingservice.verification.steps;

import com.arcone.biopro.distribution.shippingservice.verification.support.ScreenshotService;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

@Lazy
@SpringBootTest
@Slf4j
public class CucumberHooks {

    @Lazy
    @Autowired
    private ScreenshotService screenshot;

    @Autowired
    public ApplicationContext ctx;

    @AfterStep
    public void afterStep(Scenario scenario) {
        // Take screenshot if error
        if (scenario.isFailed()) {
            log.info("Taking screenshot for failed scenario: {}", scenario.getName());
            screenshot.attachScreenshot();
        }

    }

    @After
    public void afterScenario(){
        this.ctx.getBean(WebDriver.class).quit();
    }

}
