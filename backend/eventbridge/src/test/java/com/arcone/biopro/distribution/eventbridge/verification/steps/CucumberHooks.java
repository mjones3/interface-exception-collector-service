package com.arcone.biopro.distribution.eventbridge.verification.steps;

import io.cucumber.java.After;
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

    @Autowired
    public ApplicationContext ctx;

    @After(value = "@ui")
    public void afterScenario() {
        this.ctx.getBean(WebDriver.class).quit();
    }

}
