package com.arcone.biopro.distribution.customer.verification.config.driver;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebDriverWaitUtil {

    public static final Long DEFAULT_TIMEOUT = 5L;

    private final WebDriver driver;

    public FluentWait<WebDriver> forHowLong(Long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(1000))
            .ignoring(StaleElementReferenceException.class);
    }

    public FluentWait<WebDriver> byDefault() {
        return forHowLong(DEFAULT_TIMEOUT);
    }

}
