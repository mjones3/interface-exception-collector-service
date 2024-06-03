package com.arcone.biopro.distribution.shippingservice.verification.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.test.context.event.annotation.AfterTestExecution;

@Lazy
@Configuration
@Profile("!remote")
public class LocalWebDriverConfig {

    @AfterTestExecution
    public void afterTestExecution() {
        WebDriverManager.getInstance().quit();
    }


    @Bean
    @Lazy
    @ConditionalOnProperty(name = "testing.browser", havingValue = "firefox")
    @Scope("browserscope")
    public WebDriver firefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        return new FirefoxDriver();
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    @Scope("browserscope")
    public WebDriver chromeDriver() {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver();
    }

}
