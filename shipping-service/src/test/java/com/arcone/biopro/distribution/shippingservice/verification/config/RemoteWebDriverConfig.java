package com.arcone.biopro.distribution.shippingservice.verification.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.event.annotation.AfterTestExecution;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Lazy
@Configuration
@Profile("remote")
public class RemoteWebDriverConfig {

    @Value("${selenium.grid.url}")
    private String seleniumGridUrl;


    @Bean
    @ConditionalOnProperty(name = "browser", havingValue = "firefox")
    public WebDriver firefoxDriver() throws MalformedURLException, URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        return new RemoteWebDriver(new URI(seleniumGridUrl).toURL(), options);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebDriver chromeDriver() throws URISyntaxException, MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        return new RemoteWebDriver(new URI(seleniumGridUrl).toURL(), options);
    }

    @AfterTestExecution
    public void afterTestExecution() {
        WebDriverManager.getInstance().quit();
    }
}
