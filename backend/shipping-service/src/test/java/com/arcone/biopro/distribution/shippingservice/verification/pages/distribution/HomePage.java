package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shippingservice.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Component
public class HomePage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private LoginPage loginPage;

    @Autowired
    private TestUtils testUtils;

    @Value("${ui.base.url}")
    private String baseUrl;

    //    Page Locators
    @FindBy(id = "companyLogo")
    private List<WebElement> aoLogo;

    @FindBy(xpath = "//h5[normalize-space()='Distribution']")
    private WebElement distributionMenuLabel;

    //    Page Actions
    @Override
    public boolean isLoaded() {
        return !aoLogo.isEmpty();
    }

    public void goTo() throws InterruptedException {
        log.info("Navigating to the home page: {}", baseUrl);
        this.driver.get(baseUrl);
        Thread.sleep(1000);
        if (!isLoaded()) {
            loginPage.login();
        }
        sharedActions.waitForVisible(distributionMenuLabel);
        assertTrue(isLoaded());
        testUtils.setFacilityCookie(this.driver);
        driver.navigate().refresh();
    }
}
