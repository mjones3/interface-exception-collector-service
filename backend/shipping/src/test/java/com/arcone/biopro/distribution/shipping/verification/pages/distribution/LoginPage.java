package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${UI_KC_USERNAME:admin}")
    private String username;

    @Value("${UI_KC_PASSWORD:admin}")
    private String password;

    //    Page Locators
    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "kc-login")
    private WebElement loginButton;

    //    Page Actions

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(usernameField) && sharedActions.isElementVisible(passwordField);
    }

    public void login() throws InterruptedException {
        this.driver.manage().window().maximize();
        sharedActions.sendKeys(usernameField, username);
        sharedActions.sendKeys(passwordField, password);
        sharedActions.click(loginButton);
    }
}
