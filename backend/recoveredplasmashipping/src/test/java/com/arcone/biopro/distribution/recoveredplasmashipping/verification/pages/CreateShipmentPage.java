package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateShipmentPage extends CommonPageFactory {

    @Autowired
    private HomePage homePage;

    @Autowired
    private TestUtils  testUtils;

    private final SharedActions sharedActions;

    // Page elements mapped as By objects
    private final By header = By.xpath("//h1[contains(text(),'Recovered Plasma Shipping')]");

    // Create shipment panel
    private final By customerSelect = By.id("customerSelectIdSelect");
    private final By customerSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Customer Name']");
    private final By shipmentDate = By.id("shipmentDateId");
    private final By productTypeSelect = By.id("productTypeSelectIdSelect");
    private final By productTypeSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Product Type']");
    private final By submitButton = By.id("btnSubmit");
    private final By cancelButton = By.id("btnCancel");
    private final By createShipmentButton = By.id("createShipmentBtnId");
    private final By cartonTareWeightInput = By.id("cartonTareWeightId");
    private final By transportationRefNumberInput = By.xpath("//biopro-create-shipment//input[@id=\"transportationReferenceNumberId\"]");

    // Filter panel
    private final By filterPanelButton = By.id("filtersButtonId");
    private final By applyFiltersButton = By.id("applyBtn");
    private final By resetFiltersButton = By.id("resetFilterBtn");
    private final By customerSelectOnFilter = By.id("customerSelectSelect");
    private final By filterCustomerSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Customer']");
    private final By productTypeSelectOnFilter = By.id("productTypeSelectSelect");
    private final By filterProductTypeSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Product Type']");
    private final By shipmentStatusSelectOnFilter = By.id("shipmentStatusSelectSelect");
    private final By filterStatusSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Shipment Status']");
    private final By locationSelectOnFilter = By.id("locationSelectSelect");
    private final By filterLocationSelectFilter = By.cssSelector("input[ng-reflect-placeholder='Filter Location']");
    private final By dateFromOnFilter = By.id("shipmentDateFrom");
    private final By dateToOnFilter = By.id("shipmentDateTo");
    private final By filterTransportationRefNumberInput = By.xpath("//form[@id=\"searchFormId\"]//input[@id=\"transportationReferenceNumberId\"]");
    private final By filtersAppliedCount(int quantity){
        return By.xpath(String.format("//button[@id='filtersButtonId']//span[text() = '%s']", quantity));
    }

    // Shipment table
    private final By shipmentTableRow(String location, String transportationRefNumber, String customer, String productType, String status){
        return By.xpath(String.format("//td/span[contains(text(),'%s')]/../../td/span[contains(text(),'%s')]/../../td/span[contains(text(),'%s')]/../../td/span[contains(text(),'%s')]/../../td/span[contains(text(),'%s')]", location, transportationRefNumber, customer, productType, status.toUpperCase()));

    }

    private By selectInputOption(String customer) {
        return By.xpath(String.format("//mat-option//*[contains(text() , '%s')]", customer));
    }

    // Edit panel
    private final By editCommentsTextArea = By.cssSelector("textarea[data-testid='edit-shipment-comments']");

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
        sharedActions.click(customerSelect);
        sharedActions.sendKeys(customerSelectFilter, customer);
        sharedActions.click(selectInputOption(customer));
    }

    public void selectProductType(String productType) {
        sharedActions.click(productTypeSelect);
        sharedActions.sendKeys(productTypeSelectFilter, productType);
        sharedActions.click(selectInputOption(productType));
    }

    public void setCartonTareWeight(String weight) {
        sharedActions.sendKeys(cartonTareWeightInput, weight);
    }

    public void setShipmentDate(String date) {
        sharedActions.clearAndSendKeys(shipmentDate, date);
    }

    public void setTransportationRefNumber(String refNumber) {
        sharedActions.clearAndSendKeys(transportationRefNumberInput, refNumber);
    }

    public void submitShipment() {
        sharedActions.click(submitButton);
    }

    public void cancelShipment() {
        sharedActions.click(cancelButton);
    }

    public boolean isLoaded() {
        return sharedActions.isElementVisible(header);
    }

    public void waitForLoad(){
        sharedActions.waitForVisible(header);
    }

    public void clickCreateShipment() {
        sharedActions.click(createShipmentButton);
    }

    public void openFilterPanel() {
        sharedActions.click(filterPanelButton);
    }

    public boolean isFilterApplyButtonEnabled() {
        return sharedActions.isElementEnabled(driver, applyFiltersButton);
    }

    public void selectCustomers(String customers) {
        var customerList = testUtils.getCommaSeparatedList(customers);
        sharedActions.click(customerSelectOnFilter);
        for (String customer : customerList) {
            sharedActions.clearAndSendKeys(filterCustomerSelectFilter, customer);
            sharedActions.click(selectInputOption(customer));
        }
        sharedActions.pressESC();
    }

    public void selectProductTypes(String productTypes) {
        var productTypeList = testUtils.getCommaSeparatedList(productTypes);
        sharedActions.click(productTypeSelectOnFilter);
        for (String productType : productTypeList) {
            sharedActions.clearAndSendKeys(filterProductTypeSelectFilter, productType);
            sharedActions.click(selectInputOption(productType));
        }
        sharedActions.pressESC();
    }

    public void selectShipmentStatuses(String shipmentStatuses) {
        var shipmentStatusList = testUtils.getCommaSeparatedList(shipmentStatuses);
        sharedActions.click(shipmentStatusSelectOnFilter);
        for (String shipmentStatus : shipmentStatusList) {
            sharedActions.clearAndSendKeys(filterStatusSelectFilter, shipmentStatus);
            sharedActions.click(selectInputOption(shipmentStatus));
        }
        sharedActions.pressESC();
    }

    public void selectLocations(String locations) {
        var locationList = testUtils.getCommaSeparatedList(locations);
        sharedActions.click(locationSelectOnFilter);
        for (String location : locationList) {
            sharedActions.clearAndSendKeys(filterLocationSelectFilter, location);
            sharedActions.click(selectInputOption(location));
        }
        sharedActions.pressESC();
    }

    public void setFilterDateRange(String dateFrom, String dateTo) {
        sharedActions.sendKeys(dateFromOnFilter, dateFrom);
        sharedActions.sendKeys(dateToOnFilter, dateTo);
    }

    public void clickFilterApplyButton() {
        sharedActions.click(applyFiltersButton);
    }

    public void setFilterTransportationReferenceNumber(String transportationReferenceNumber) {
        sharedActions.sendKeys(filterTransportationRefNumberInput, transportationReferenceNumber);
    }

    public void verifyFilterResult(String location, String transportationReferenceNumber, String customer, String productType, String shipmentStatus) {
        sharedActions.waitForVisible(shipmentTableRow(location, transportationReferenceNumber, customer, productType, shipmentStatus));
    }

    public void verifyFilterCriteriaApplied(int quantity) {
        sharedActions.waitForVisible(filtersAppliedCount(quantity));
    }

    public void setEditComments(String comments) {
        sharedActions.sendKeys(editCommentsTextArea, comments);
    }
}
