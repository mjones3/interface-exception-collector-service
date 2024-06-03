package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shippingservice.verification.support.ScreenshotService;
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

    @FindBy(xpath = "//h5[normalize-space()='Distribution']")
    private WebElement distributionMenuLabel;

    @Autowired
    private ScreenshotService screenshot;

    //    Page Actions
    @Override
    public boolean isLoaded() {
        return !aoLogo.isEmpty();
    }

    public void goTo() throws InterruptedException {
        this.driver.get(baseUrl);
        Thread.sleep(1000);
        if (!isLoaded()) {
            screenshot.attachScreenshot();
            loginPage.login();
        }
        sharedActions.waitForVisible(distributionMenuLabel);
        assertTrue(isLoaded());
    }
}
