package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@Slf4j
public class ExternalTransferPage extends CommonPageFactory {

    @Value("${ui.external-transfer.url}")
    private String externalTransferPageUrl;

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;


    private static final By externalTransferHeaderLocator = By.xpath("//h3[normalize-space()='Transfer Product Information']");
    private static final By hospitalTransferIdInput = By.id("hospitalTransferId");
    private static final By transferDateSelect = By.id("TransferDateId");
    private static final By CUSTOMER_SELECT_ID = By.id("transferCustomerSelectId");
    private static final By CUSTOMER_PANEL_ID = By.id("transferCustomerSelectIdSelect-panel");
    private static final By unitNumberInput = By.id("unitNumberId");
    private static final By enterProducts = By.id("enterProductsId");
    private static final By productCodeInput = By.id("productCodeId");

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(externalTransferHeaderLocator);
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(externalTransferHeaderLocator);
    }

    public void goTo() throws InterruptedException {
        homePage.goTo();

        log.debug("Navigating to the External Transfer page: {}", externalTransferPageUrl);
        sharedActions.navigateTo(externalTransferPageUrl);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void selectCustomer(String customerName) throws InterruptedException {
        sharedActions.selectValuesFromDropdown(this.driver, CUSTOMER_SELECT_ID, CUSTOMER_PANEL_ID, List.of(customerName));
    }

    public void defineHospitalTransferIdAndTransferDate(String hospitalTransferId, String transferDate) throws InterruptedException {
        log.debug("Adding hospital Transfer ID {} with Transfer Date {}.", hospitalTransferId, transferDate);
        sharedActions.sendKeys(this.driver, hospitalTransferIdInput, hospitalTransferId);
        sharedActions.sendKeys(this.driver, transferDateInput, transferDate);
        sharedActions.waitLoadingAnimation();
    }

    public void checkUnitNumberProductCodeFieldVisibilityIs(boolean visible) {
        if (visible) {
            assertTrue(sharedActions.isElementVisible(enterProducts));
            assertTrue(sharedActions.isElementVisible(unitNumberInput));
            assertTrue(sharedActions.isElementVisible(productCodeInput));
        } else {
            assertFalse(sharedActions.isElementVisible(enterProducts));
            assertFalse(sharedActions.isElementVisible(unitNumberInput));
            assertFalse(sharedActions.isElementVisible(productCodeInput));
        }
    }
}
