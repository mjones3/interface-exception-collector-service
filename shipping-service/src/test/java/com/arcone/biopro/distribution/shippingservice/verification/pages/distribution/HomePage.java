package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class HomePage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private LoginPage loginPage;

    @Value("${ui.base.url}")
    private String baseUrl;

    //    Page Locators
    @FindBy(id = "companyLogo")
    private List<WebElement> aoLogo;

    @FindBy(xpath = "//h5[normalize-space()='distribution.label']")
    private WebElement distributionMenuLabel;

    //    Page Actions
    @Override
    public boolean isLoaded() {
        return !aoLogo.isEmpty();
    }

    public void goTo() {
        this.driver.get(baseUrl);
        if (!isLoaded()) {
            loginPage.login();
        }
        sharedActions.waitForVisible(distributionMenuLabel);
        assertTrue(isLoaded());
    }
}
