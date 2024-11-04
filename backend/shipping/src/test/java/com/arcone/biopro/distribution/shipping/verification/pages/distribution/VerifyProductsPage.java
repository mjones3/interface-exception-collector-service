package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VerifyProductsPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${ui.base.url}")
    private String baseUrl;

    @Value("${ui.shipment-verify-products.url}")

    private String verifyProductsUrl;

    private static final String verifiedProductsTable = "//*[@id='prodTableId']";
    private static final String productCategory = "//*[@id='informationDetails-Labeling-Product-Category']";
    private static final String shippingMethodElement = "//*[@id='informationDetails-Shipping-Method']";
    private static final String orderNumber = "//*[@id='informationDetails-BioPro-Order-ID']";
    private static final String orderPriority = "//*[@id='informationDetails-Priority']";
    private static final String customerId = "//*[@id='informationDetails-Customer-Code']";
    private static final String customerName = "//*[@id='informationDetails-Customer-Name']";
    private static final String orderStatus = "//*[@id='informationDetails-Status']";
    private static final String externalId = "//*[@id='informationDetails-External-Order-ID']";
    private static final String completeShipmentButton = "completeActionBtn";
    private static final String inputUnitNumber = "inUnitNumber";
    private static final String inputProductCode = "productCodeId";
    private static final String progressLog = "numberOfUnitAdded";
    private static final String closeWarningButton = "//*[@id='toast-container']//fuse-alert//button";

    private String formatUnitLocator(String unit) {
        unit = TestUtils.removeUnitNumberScanDigits(unit);
        return String.format("//biopro-unit-number-card[@ng-reflect-unit-number='%s']", unit);
    }

    private String formatProductCodeLocator(String productCode) {
        productCode = TestUtils.removeProductCodeScanDigits(productCode);
        return String.format("//biopro-unit-number-card[@ng-reflect-product-name='%s']", productCode);
    }


    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(By.xpath(verifiedProductsTable));
    }

    public void isPageOpen(String shipmentId) {
        sharedActions.isAtPage(verifyProductsUrl.replace("{shipmentId}", shipmentId));
    }

    public void viewPageContent() {
        sharedActions.waitForElementsVisible(By.xpath(shippingMethodElement),
            By.xpath(orderNumber),
            By.xpath(orderPriority),
            By.xpath(customerId),
            By.xpath(customerName),
            By.xpath(orderStatus),
            By.xpath(externalId));
    }

    public void scanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        unitNumber = String.format("=%s00", unitNumber);
        productCode = String.format("=<%s", productCode);
        sharedActions.sendKeysAndEnter(this.driver, By.id(inputUnitNumber), unitNumber);
        sharedActions.sendKeysAndEnter(this.driver, By.id(inputProductCode), productCode);
    }

    public boolean isProductVerified(String unitNumber, String productCode) {
        try {
            sharedActions.waitForElementsVisible(By.xpath(formatUnitLocator(unitNumber)),
                By.xpath(formatProductCodeLocator(productCode)));
            return true;
        } catch (Exception e) {
            log.debug("Product not found in verified products table");
            return false;
        }
    }

    public boolean isProductNotVerified(String unitNumber, String productCode) {
        try {

            sharedActions.waitForNotVisible(By.xpath(formatUnitLocator(unitNumber)));
            sharedActions.waitForNotVisible(By.xpath(formatProductCodeLocator(productCode)));
            return true;
        } catch (Exception e) {
            log.debug("Product not found in verified products table");
            return false;
        }
    }

    public String getProgressLog() {
        return sharedActions.getText(By.id(progressLog));
    }

    public boolean isCompleteShipmentButtonEnabled() {
        try {
            sharedActions.waitForEnabled(By.id(completeShipmentButton));
            return true;
        } catch (Exception e) {
            log.debug("Complete Shipment button is not enabled");
            return false;
        }
    }

    public boolean isCompleteShipmentButtonDisabled() {
        try {
            sharedActions.waitForDisabled(By.id(completeShipmentButton));
            return true;
        } catch (Exception e) {
            log.debug("Complete Shipment button is not disabled");
            return false;
        }
    }

    public void closeWarningMessage() throws InterruptedException {
        sharedActions.click(driver,By.xpath(closeWarningButton));
    }
}
