package com.arcone.biopro.distribution.shipping.verification.pages;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfWindowsToBe;


@Component
@Slf4j
public class SharedActions {

    @Autowired
    @Lazy
    private WebDriverWait wait;

    @Value("${ui.base.url}")
    private String baseUrl;

    public void waitForVisible(WebElement element) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to be visible.", element.toString());
                return element.isDisplayed();
            });
            log.debug("Element {} is visible now.", element.toString());
        } catch (NoSuchElementException e) {
            log.error("Element {} not found.", element.toString());
            throw e;
        }
    }

    public void waitForVisible(By locator) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to be visible.", locator);
                try {
                    return e.findElement(locator).isDisplayed();
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    try {
                        log.debug("Waiting for element {} to be visible for the second time.", locator);
                        return e.findElement(locator).isDisplayed();
                    } catch (NoSuchElementException | StaleElementReferenceException ex2) {
                        // Element not found, consider it as not visible
                        log.debug("Element {} not found after two tries, considering it as not visible.", locator);
                        return false;
                    }
                }
            });
            log.debug("Element {} is visible now.", locator);
        } catch (Exception e) {
            log.error("Element {} is not visible after the specified timeout.", locator);
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
                    log.debug("Element {} not found, considering it as not visible.", element);
                    return true;
                }
            });
            log.debug("Element {} is not visible now.", element.toString());
        } catch (Exception e) {
            log.error("Element {} is visible after the specified timeout.", element.toString());
            throw e;
        }
    }

    public void waitForNotVisible(By locator) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to not be visible.", locator);
                try {
                    return !e.findElement(locator).isDisplayed();
                } catch (NoSuchElementException ex) {
                    // Element not found, consider it as not visible
                    log.debug("Element {} not found, considering it as not visible.", locator);
                    return true;
                }
            });
            log.debug("Element {} is not visible now.", locator);
        } catch (Exception e) {
            log.error("Element {} is visible after the specified timeout.", locator);
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

    public void click(WebDriver driver, By locator) throws InterruptedException {
        waitForVisible(locator);
        Thread.sleep(500);
        driver.findElement(locator).click();
    }

    public void clickElementAndMoveToNewTab(WebDriver driver, WebElement element, int expectedWindowsNumber) {
        this.click(element);
        log.info("Waiting for {} windows to be open. Currently: {}", expectedWindowsNumber, driver.getWindowHandles().size());
        wait.until(numberOfWindowsToBe(expectedWindowsNumber));
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
    }

    public void clickElementAndMoveToNewTab(WebDriver driver, WebElement element){
        // When not specified, the expected quantity of windows will be 3
        // First tab (original), second tab (after click), and print dialog.
        this.clickElementAndMoveToNewTab(driver, element, 3);
    }

    public void locateXpathAndWaitForVisible(String locator, WebDriver driver) {
        try {
            wait.until(e -> {
                log.debug("Waiting for element {} to be visible.", locator);
                return driver.findElement(By.xpath(locator)).isDisplayed();
            });
            log.debug("Element {} is visible now.", locator);
        } catch (NoSuchElementException e) {
            log.error("Element {} is not visible after the specified timeout.", locator);
            throw e;
        }
    }

    public void verifyMessage(String header, String message) {
        log.info("Verifying message: {}", message);
        var bannerMessageLocator = "";
        if(header.startsWith("Acknowledgment")){
            bannerMessageLocator = "//*[@id='mat-mdc-dialog-0']//fuse-confirmation-dialog";
        }else{
            bannerMessageLocator = "//*[@id='toast-container']";
        }

        String finalBannerMessageLocator = bannerMessageLocator;
        String msg = wait.until(e -> e.findElement(By.xpath(finalBannerMessageLocator))).getText();

        // Split the message at line break to get header and message
        String[] msgParts = msg.split("\n");
        Assert.assertEquals(header.toUpperCase(), msgParts[0].toUpperCase());
        Assert.assertEquals(message.toUpperCase(), msgParts[1].toUpperCase());

        // dialog "//*[@id='mat-mdc-dialog-0']/div/div/fuse-confirmation-dialog"


    }

    public void waitLoadingAnimation() throws InterruptedException {
        String loadingAnimationLocator = "rsa.loading";
        Thread.sleep(500);
        wait.until(e -> {
            log.debug("Waiting for loading animation to disappear.");
            return e.findElements(By.cssSelector(loadingAnimationLocator)).isEmpty();
        });
    }

    public void isAtPage(String url) {
        wait.until(e -> {
            String fullUrl = baseUrl + url;
            log.debug("Waiting for the URL to be: {}", fullUrl);
            return e.getCurrentUrl().equals(fullUrl);
        });
    }
}
