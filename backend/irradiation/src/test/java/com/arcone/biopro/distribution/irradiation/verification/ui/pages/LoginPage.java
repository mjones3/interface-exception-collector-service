package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.common.utils.Retry;
import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Lazy
@Component
public class LoginPage extends CommonPageFactory {

    // locators
    private final By loginFormLocator = By.id("kc-form");
    private final By usernameInputLocator = By.id("username");
    private final By passwordInputLocator = By.id("password");
    private final By loginButtonLocator = By.id("kc-login");

    // Injecting username and password from environment variables
    @Value("${distribution.login.username}")
    private String username;

    @Value("${distribution.login.password}")
    private String password;


    //    Page Actions
    @Override
    public boolean isLoaded() {
        try{
            Retry.retryOnException(()->{
                driver.getDriver();
                PageElement loginForm = driver.waitForElement(loginFormLocator,5);
                loginForm.waitForVisible(5);
            }, WebDriverException.class);
            PageElement loginForm = driver.waitForElement(loginFormLocator);
            loginForm.waitForVisible();
            return loginForm.isDisplayed();
        } catch (Exception ex) {
            log.error("Unable to login", ex);
            throw new RuntimeException(ex);
        }
    }

    public void login() {
        try {
            assertTrue(this.isLoaded());
            PageElement usernameInput = driver.waitForElement(usernameInputLocator,5);
            usernameInput.waitForVisible(5);
            usernameInput.waitForClickable();
            usernameInput.sendKeys(username);
            PageElement passwordInput = driver.waitForElement(passwordInputLocator,3);
            passwordInput.waitForVisible();
            passwordInput.waitForClickable();
            passwordInput.sendKeys(password);
            PageElement loginButton = driver.waitForElement(loginButtonLocator);
            loginButton.waitForVisible();
            loginButton.click();
        } catch (Exception e) {
            log.error("Unable to login", e);
            throw e;
        }
    }
}
