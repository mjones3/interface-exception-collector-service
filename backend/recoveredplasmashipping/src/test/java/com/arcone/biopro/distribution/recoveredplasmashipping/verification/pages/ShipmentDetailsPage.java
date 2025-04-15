package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShipmentDetailsPage extends CommonPageFactory {

    private final By shipmentDetailsHeader = By.xpath("//h3//span[contains(text(),'Shipment Details')]");
    private final By shipmentNumber = By.id("informationDetails-Shipment-Number-value");
    private final By customerCode = By.id("informationDetails-Customer-Code-value");
    private final By customerName = By.id("informationDetails-Customer-Name-value");
    private final By status = By.id("informationDetails-Shipment-Status-value");
    private final By productType = By.id("informationDetails-Product-Type-value");
    private final By shipmentDate = By.id("informationDetails-Shipment-Date-value");
    private final By transportationNumber = By.id("informationDetails-Transportation-#-value");
    private final By totalCartons = By.id("informationDetails-Total-Cartons-value");
    private final By totalProducts = By.id("informationDetails-Total-Products-value");
    private final By totalVolume = By.id("informationDetails-Total-Volume-value");
    private final By addCartonBtn = By.xpath("//button[@id='btnAddCarton']");
    private final By backToSearchBtn = By.id("backActionBtn");

    private By addedCartonRow(String cartonNumberPrefix, String sequence, String status) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]",
                cartonNumberPrefix, sequence, status));
    }

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(shipmentDetailsHeader);
    }

    public void goTo(String id) throws InterruptedException {
        if (!homePage.isLoaded()) {
            homePage.goTo();
        }
        sharedActions.navigateTo("/recovered-plasma/" + id + "/shipment-details");
        sharedActions.waitForVisible(shipmentDetailsHeader);
    }

    public String getShipmentNumber() {
        return sharedActions.getText(shipmentNumber);
    }

    public String getCustomerCode() {
        return sharedActions.getText(customerCode);
    }

    public String getCustomerName() {
        return sharedActions.getText(customerName);
    }

    public String getShipmentStatus() {
        return sharedActions.getText(status);
    }

    public String getProductType() {
        return sharedActions.getText(productType);
    }

    public String getShipmentDate() {
        return sharedActions.getText(shipmentDate);
    }

    public String getTransportationNumber() {
        return sharedActions.getText(transportationNumber);
    }

    public String getTotalCartons() {
        return sharedActions.getText(totalCartons);
    }

    public String getTotalProducts() {
        return sharedActions.getText(totalProducts);
    }

    public String getTotalVolume() {
        return sharedActions.getText(totalVolume);
    }

    public boolean isAddCartonButtonVisible() {
        return sharedActions.isElementVisible(addCartonBtn);
    }

    public boolean isBackToSearchButtonVisible() {
        return sharedActions.isElementVisible(backToSearchBtn);
    }

    public void clickBackToSearchButton() {
        sharedActions.click(backToSearchBtn);
    }

    public void clickAddCarton() {
        sharedActions.click(addCartonBtn);
    }

    public boolean isAddCartonButtonEnabled() {
        return sharedActions.isElementEnabled(driver, addCartonBtn);
    }

    public void verifyCartonIsListed(String cartonNumberPrefix, String sequence, String status) {
        sharedActions.waitForVisible(addedCartonRow(cartonNumberPrefix, sequence, status));
    }
}
