package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateShipmentPage {

    private final SharedActions sharedActions;

    // Page elements mapped as By objects
    private By customerSelect = By.id("customer-select");
    private By originLocationSelect = By.id("origin-location-select");
    private By destinationLocationSelect = By.id("destination-location-select");
    private By shipmentDateInput = By.id("shipment-date");
    private By addProductButton = By.id("add-product-btn");
    private By productSelect = By.id("product-select");
    private By quantityInput = By.id("quantity-input");
    private By submitButton = By.id("submit-shipment");
    private By cancelButton = By.id("cancel-shipment");

    @Autowired
    public CreateShipmentPage(SharedActions sharedActions) {
        this.sharedActions = sharedActions;
    }

    public void selectCustomer(String customer) {
        sharedActions.waitForVisible(customerSelect);
        sharedActions.click(customerSelect);
        sharedActions.sendKeys(customerSelect, customer);
    }

    public void selectOriginLocation(String location) {
        sharedActions.waitForVisible(originLocationSelect);
        sharedActions.click(originLocationSelect);
        sharedActions.sendKeys(originLocationSelect, location);
    }

    public void selectDestinationLocation(String location) {
        sharedActions.waitForVisible(destinationLocationSelect);
        sharedActions.click(destinationLocationSelect);
        sharedActions.sendKeys(destinationLocationSelect, location);
    }

    public void setShipmentDate(String date) {
        sharedActions.waitForVisible(shipmentDateInput);
        sharedActions.sendKeys(shipmentDateInput, date);
    }

    public void addProduct(String product, String quantity) {
        sharedActions.waitForVisible(addProductButton);
        sharedActions.click(addProductButton);

        sharedActions.waitForVisible(productSelect);
        sharedActions.click(productSelect);
        sharedActions.sendKeys(productSelect, product);

        sharedActions.waitForVisible(quantityInput);
        sharedActions.sendKeys(quantityInput, quantity);
    }

    public void submitShipment() {
        sharedActions.waitForVisible(submitButton);
        sharedActions.click(submitButton);
    }

    public void cancelShipment() {
        sharedActions.waitForVisible(cancelButton);
        sharedActions.click(cancelButton);
    }

    public boolean isPageLoaded() {
        return sharedActions.isElementVisible(customerSelect) &&
            sharedActions.isElementVisible(originLocationSelect) &&
            sharedActions.isElementVisible(destinationLocationSelect);
    }
}
