package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Lazy
@Component
public class HomePage extends CommonPageFactory {

    @Value("${ui.base.url}")
    private String baseUrl;

    // locators
    private final By filterInputLocator = By.id("filterInput");
    private final By companyLogoLocator = By.id("defaultLogo");
    protected By pageNameLocator = By.xpath("//h3");
    protected By moduleNameLocator = By.xpath("//h1");


    private By getMenuItemLocator(String process) {
        String subMenuLocatorTemplate = "//span[text()=' %s ']";
        return By.xpath(String.format(subMenuLocatorTemplate, process.replace('_', ' ')));
    }

    public By getLocationItemLocator(String location) {
        String locationListElement = "//*[@id='filteredList']//descendant::span[contains(text(),'%s')]";
        return By.xpath(String.format(locationListElement, location));
    }

    @Autowired
    private LoginPage loginPage;

    //    Page Actions
    @Override
    public boolean isLoaded() {
        try {
            PageElement companyLogo = driver.waitForElement(companyLogoLocator, 5);
            companyLogo.waitForVisible();
            return companyLogo.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            log.info("Home page not loaded: {}", e.getMessage());
            return false;
        }
    }

    public void goTo() {
        try {
            if (driver == null || driver.getDriver() == null) {
                throw new IllegalStateException("WebDriver instance is not initialized or has been quit.");
            }
            driver.get(baseUrl);
            loginPage.login();
            assertTrue(isLoaded());
        } catch (Exception e) {
            log.info("Unable to open Homepage");
            throw e;
        }
    }

    public void selectLocation(String location) {
        try {
            // Input the location value into the location filter input
            PageElement filterInput = driver.waitForElement(filterInputLocator);
            filterInput.sendKeys(location.toUpperCase());

            // Look for the location element with the specified location name and click it
            PageElement locationOption = driver.waitForElement(getLocationItemLocator(location.toUpperCase()));
            locationOption.click();
        } catch (Exception e) {
            log.info("Unable to select location {}", location);
            throw e;
        }
    }

    public void goToProcess(String process) throws Exception {
        try {
            PageElement menu = driver.waitForElement(getMenuItemLocator(process));
            menu.safeClick();
        } catch (Exception e) {
            log.info("Unable to go to {} process",process);
            throw e;
        }
    }

    public String getModuleName() {
        PageElement pageTitle = this.driver.waitForElement(this.moduleNameLocator);
        pageTitle.waitForVisible();
        return pageTitle.getText();
    }

    public String getPageName() {
        PageElement pageSubTitle = this.driver.waitForElement(this.pageNameLocator);
        pageSubTitle.waitForVisible();
        return pageSubTitle.getText();
    }

    public void waitForPageToLoad(String expectedTitle, String expectedSubTitle, int timeoutSeconds) {
        try {
            this.driver.Wait().withTimeout(Duration.ofSeconds((long) timeoutSeconds)).pollingEvery(Duration.ofMillis(500L)).ignoring(StaleElementReferenceException.class).until((drv) -> {
                try {
                    String moduleName = this.getModuleName();
                    String pageName = this.getPageName();
                    log.debug("Waiting for page to load. Expected Title: '{}', Actual Title: '{}', Expected SubTitle: '{}', Actual SubTitle: '{}'", new Object[]{expectedTitle, moduleName, expectedSubTitle, pageName});
                    return expectedTitle.equals(moduleName) && expectedSubTitle.equals(pageName);
                } catch (StaleElementReferenceException var6) {
                    log.warn("StaleElementReferenceException encountered. Retrying...");
                    return false;
                }
            });
            log.info("Page loaded successfully with Title: '{}' and SubTitle: '{}'.", expectedTitle, expectedSubTitle);
        } catch (TimeoutException var5) {
            TimeoutException e = var5;
            log.error("Timeout waiting for page to load. Expected Title: '{}', Expected SubTitle: '{}'", expectedTitle, expectedSubTitle);
            throw e;
        }
    }
}

