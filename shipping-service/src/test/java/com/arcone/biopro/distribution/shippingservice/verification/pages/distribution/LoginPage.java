package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${UI_KC_USERNAME}")
    private String username;

    @Value("${UI_KC_PASSWORD}")
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

    public void login() {
        sharedActions.sendKeys(usernameField, username);
        sharedActions.sendKeys(passwordField, password);
        sharedActions.click(loginButton);
    }
}
