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


    private static final String externalTransferHeaderLocator = "//h3[normalize-space()='Product Selection and Transfer Information']";
    private static final String hospitalTransferIdInput = "hospitalTransferId";
    private static final String transferDateInput = "TransferdateId";
    private static final String CUSTOMER_SELECT_ID = "transferCustomerSelectId";
    private static final String CUSTOMER_PANEL_ID = "transferCustomerSelectIdSelect-panel";
    private static final String unitNumberInput = "unitNumberId";
    private static final String productCodeInput = "productCodeId";

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(By.xpath(externalTransferHeaderLocator));
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(By.xpath(externalTransferHeaderLocator));
    }

    public void goTo() throws InterruptedException {
        homePage.goTo();

        log.debug("Navigating to the External Transfer page: {}", externalTransferPageUrl);
        sharedActions.navigateTo(externalTransferPageUrl);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void selectCustomer(String customerName) throws InterruptedException {
        sharedActions.selectValuesFromDropdown(this.driver,CUSTOMER_SELECT_ID, CUSTOMER_PANEL_ID, List.of(customerName) );
    }

    public void defineHospitalTransferIdAndTransferDate(String hospitalTransferId, String transferDate) throws InterruptedException {
        log.debug("Adding hospital Transfer ID {} with Transfer Date {}.", hospitalTransferId, transferDate);
        sharedActions.sendKeys(this.driver, By.id(hospitalTransferIdInput), hospitalTransferId);
        sharedActions.sendKeys(this.driver, By.id(transferDateInput), transferDate);
        sharedActions.waitLoadingAnimation();
    }

    public void checkUnitNumberProductCodeFieldVisible(){
        assertTrue(sharedActions.isElementVisible(By.id(unitNumberInput)));
        assertTrue(sharedActions.isElementVisible(By.id(productCodeInput)));
    }
    public void checkUnitNumberProductCodeFieldNotVisible(){
        assertFalse(sharedActions.isElementVisible(By.id(unitNumberInput)));
        assertFalse(sharedActions.isElementVisible(By.id(productCodeInput)));
    }
}
