package com.arcone.biopro.distribution.order.verification.config;

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

    @After
    public void afterScenario() {
        this.ctx.getBean(WebDriver.class).quit();
    }

}
