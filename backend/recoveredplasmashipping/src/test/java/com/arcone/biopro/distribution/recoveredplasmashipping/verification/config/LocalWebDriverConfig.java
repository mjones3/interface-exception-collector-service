package com.arcone.biopro.distribution.recoveredplasmashipping.verification.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Lazy
@Configuration
@Profile("!remote")
public class LocalWebDriverConfig {

    @Value("${selenium.headless.execution}")
    private boolean headless;

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
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-infobars"); // Disabling infobars.
        options.addArguments("--disable-extensions"); // Disabling extensions.
        options.addArguments("--disable-gpu"); // Applicable to Windows OS only.
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems.
        options.addArguments("--no-sandbox"); // Bypass OS security model.
        options.addArguments("--disable-client-side-phishing-detection"); // Disables the client-side phishing detection feature.
        options.addArguments("--disable-default-apps"); // Disables installation of default apps on first run. This is used during automated testing.
        options.addArguments("--enable-automation"); // Enables indication that browser is controlled by automation.
        if (headless){options.addArguments("--headless");} // Execution without GUI.
        return new ChromeDriver(options);
    }

}
