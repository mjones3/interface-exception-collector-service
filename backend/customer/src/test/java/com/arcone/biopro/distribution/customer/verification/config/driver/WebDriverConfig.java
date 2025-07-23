package com.arcone.biopro.distribution.customer.verification.config.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Lazy
@Configuration
@Profile("!remote")
public class WebDriverConfig {

    @Value("${selenium.headless.execution}")
    private boolean headlessExecution;

    public ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-default-apps");
        options.addArguments("--enable-automation");
        options.addArguments("--disable-notifications");
        if (headlessExecution) {
            options.addArguments("--headless");
        }
        options.addArguments("--guest");
        return options;
    }

    @Lazy
    @Bean
    @Scope(value = "cucumber-glue", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @ConditionalOnProperty(name = "testing.browser", havingValue = "firefox")
    public WebDriver firefoxDriver() {
        return WebDriverManager.firefoxdriver()
            .create();
    }

    @Lazy
    @Bean
    @Scope(value = "cucumber-glue", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @ConditionalOnMissingBean
    public WebDriver chromeDriver() {
        return WebDriverManager.chromedriver()
            .capabilities(getChromeOptions())
            .create();
    }
}
