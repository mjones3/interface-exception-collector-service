package com.arcone.biopro.distribution.shippingservice.verification.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.openqa.selenium.NoSuchElementException;


@Component
@Slf4j
public class SharedActions {

    @Autowired
    @Lazy
    private WebDriverWait wait;

    public void waitForVisible(WebElement element) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to be visible.", element.toString());
                return element.isDisplayed();
            });
            log.debug("Element {} is visible now.", element.toString());
        } catch (Exception e) {
            log.error("Element {} is not visible in the specified timeout.", element.toString());
            throw e;
        }
    }


    public void waitForNotVisible(WebElement element) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to not be visible.", element.toString());
                try {
                    return !element.isDisplayed();
                } catch (NoSuchElementException ex) {
                    // Element not found, consider it as not visible
                    log.debug("Element {} not found, considering it as not visible.", element.toString());
                    return true;
                }
            });
            log.debug("Element {} is not visible now.", element.toString());
        } catch (Exception e) {
            log.error("Element {} is visible after the specified timeout.", element.toString());
            throw e;
        }
    }

    public boolean isElementVisible(WebElement element) {
        return element.isDisplayed();
    }

    public void sendKeys(WebElement element, String text) {
        waitForVisible(element);
        element.sendKeys(text);
    }

    public void click(WebElement element) {
        waitForVisible(element);
        element.click();
    }
}
