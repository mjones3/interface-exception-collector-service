package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateShipmentPage {

    @Autowired
    private HomePage homePage;

    private final SharedActions sharedActions;

    // Page elements mapped as By objects
    private final By header = By.xpath("//h1[contains(text(),'Recovered Plasma Shipping')]");
    private final By customerSelect = By.id("customerSelectIdSelect");
    private final By customerSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Customer Name']");
    private final By scheduledShipmentDate = By.id("scheduledShipmentDateId");
    private final By productTypeSelect = By.id("productTypeId");
    private final By productTypeSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Product Type']");
    private final By submitButton = By.id("btnSubmit");
    private final By cancelButton = By.id("btnCancel");
    private final By createShipmentButton = By.id("createShipmentBtnId");
    private final By cartonTareWeightInput = By.id("cartonTareWeightId");
    private final By transportationRefNumberInput = By.id("transportationReferenceNumberId");

    private By selectInputOption(String customer) {
        return By.xpath(String.format("//mat-option//*[contains(text() , '%s')]", customer));
    }

    @Autowired
    public CreateShipmentPage(SharedActions sharedActions) {
        this.sharedActions = sharedActions;
    }

    public void goTo() throws InterruptedException {
        String createShipmentUrl = "/recovered-plasma";

        homePage.goTo();
        sharedActions.navigateTo(createShipmentUrl);
        sharedActions.waitForVisible(header);
    }

    public void selectCustomer(String customer) {
        sharedActions.waitForVisible(customerSelect);
        sharedActions.click(customerSelect);
        sharedActions.sendKeys(customerSelectFilter, customer);
        sharedActions.click(selectInputOption(customer));
    }

    public void selectProductType(String productType) {
        sharedActions.waitForVisible(productTypeSelect);
        sharedActions.click(productTypeSelect);
        sharedActions.sendKeys(productTypeSelectFilter, productType);
        sharedActions.click(selectInputOption(productType));
    }

    public void setCartonTareWeight(String weight) {
        sharedActions.waitForVisible(cartonTareWeightInput);
        sharedActions.sendKeys(cartonTareWeightInput, weight);
    }

    public void setShipmentDate(String date) {
        sharedActions.waitForVisible(scheduledShipmentDate);
        sharedActions.sendKeys(scheduledShipmentDate, date);
    }

    public void setTransportationRefNumber(String refNumber) {
        sharedActions.waitForVisible(transportationRefNumberInput);
        sharedActions.sendKeys(transportationRefNumberInput, refNumber);
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
        return sharedActions.isElementVisible(header);
    }

    public void clickCreateShipment() {
        sharedActions.waitForVisible(createShipmentButton);
        sharedActions.click(createShipmentButton);
    }
}
