package com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class PopUpPage extends CommonPageFactory {

    // Define locators
    private final By popUpLocator = By.id("toast-container");
    private final By popUpTitleLocator = By.cssSelector("#toast-container .fuse-alert-title span");
    private final By popUpMessageLocator = By.cssSelector("#toast-container .fuse-alert-message");

    private By getSpecificPopUpLocator(String title, String message) {
        String xpath = String.format(
        ".//fuse-alert//div[contains(@class, 'fuse-alert-message') and contains(normalize-space(text()), '%s')]" +
            "//ancestor::fuse-alert//div[contains(@class, 'fuse-alert-title')]//span[normalize-space(text())='%s']",
            message,
            title
        );

        log.debug("Generated XPath for pop-up locator: {}", xpath);
        return By.xpath(xpath);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    public PageElement getPopUpTitle() {
        PageElement title = driver.waitForElement(popUpTitleLocator, 5);
        title.waitForVisible();
        return title;
    }

    public PageElement getPopUpMessage() {
        PageElement message = driver.waitForElement(popUpMessageLocator, 5);
        message.waitForVisible();
        return message;
    }

    public void closePopUp() {
        int maxRetries = 3;
        int attempts = 0;
        boolean clicked = false;
        while (attempts < maxRetries && !clicked) {
            try {
                PageElement popUpElement = driver.waitForElement(popUpLocator, 5);
                popUpElement.waitForVisible();
                popUpElement.waitForClickable();
                popUpElement.scrollIntoView();
                popUpElement.click();
                clicked = true;
            } catch (ElementClickInterceptedException e) {
                attempts++;
                log.warn("Click intercepted on attempt {}. Retrying...", attempts, e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry delay", ie);
                }
            } catch (Exception e) {
                log.error("Unexpected error while closing pop-up", e);
                throw e;
            }
        }
    }

    public void waitForPopUp() {
        try {
            var popup = driver.waitForElement(popUpLocator, 5);
            popup.waitForVisible();
            popup.waitForClickable();
        } catch (Exception e) {
            log.info("Error while waiting for pop-up");
            throw e;
        }
    }

    public boolean waitForSpecificPopUpMessage(String title, String message, int timeoutSeconds) {
        try {
            PageElement popUp = driver.waitForElement(getSpecificPopUpLocator(title, message), timeoutSeconds );
            if (popUp.isDisplayed()) {
                log.info("Pop-up with title '{}' and message '{}' is displayed.", title, message);
                popUp.waitForClickable();
                popUp.click();
                log.info("Closed the pop-up with title '{}' and message '{}'.", title, message);
                return true;
            } else {
                log.info("Pop-up with title '{}' and message '{}' is not displayed.", title, message);
                return false;
            }
        } catch (TimeoutException e) {
            log.info("Pop-up with title '{}' and message '{}' did not appear within {} seconds.", title, message, timeoutSeconds);
            return false;
        } catch (Exception e) {
            log.error("Error while waiting for pop-up with title '{}' and message '{}'.", title, message, e);
            return false;
        }
    }
}
