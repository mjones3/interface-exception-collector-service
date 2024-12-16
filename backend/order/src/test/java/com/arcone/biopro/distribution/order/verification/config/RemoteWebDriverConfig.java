package com.arcone.biopro.distribution.order.verification.config;

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
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Lazy
@Configuration
@Profile("remote")
public class RemoteWebDriverConfig {

    @Value("${selenium.grid.url}")
    private String seleniumGridUrl;

    @Value("${selenium.headless.execution}")
    private boolean headless;


    @Bean
    @Lazy
    @ConditionalOnProperty(name = "testing.browser", havingValue = "firefox")
    @Scope("browserscope")
    public WebDriver firefoxDriver() throws MalformedURLException, URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        } // Execution without GUI.
        return new RemoteWebDriver(new URI(seleniumGridUrl).toURL(), options);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    @Scope("browserscope")
    public WebDriver chromeDriver() throws URISyntaxException, MalformedURLException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-infobars"); // Disabling infobars.
        options.addArguments("--disable-extensions"); // Disabling extensions.
        options.addArguments("--disable-gpu"); // Applicable to Windows OS only.
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems.
        options.addArguments("--no-sandbox"); // Bypass OS security model.
        options.addArguments("--disable-client-side-phishing-detection"); // Disables the client-side phishing detection feature.
        options.addArguments("--disable-default-apps"); // Disables installation of default apps on first run. This is used during automated testing.
        options.addArguments("--enable-automation"); // Enables indication that browser is controlled by automation.
        if (headless) {
            options.addArguments("--headless");
        } // Execution without GUI.
        return new RemoteWebDriver(new URI(seleniumGridUrl).toURL(), options);
    }

}
