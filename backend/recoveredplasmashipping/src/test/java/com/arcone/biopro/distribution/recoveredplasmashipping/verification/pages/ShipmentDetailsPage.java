package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
    private final By closeShipmentBtn = By.id("closeShipmentBtnId");
    private final By confirmationShipmentDate = By.id("shipmentDateId");
    private final By confirmCloseShipmentBtn = By.id("btnContinue");
    private final By unacceptableReportLastRunDate = By.id("informationDetails-Last-Run-value");
    private final By unacceptableReportBtn = By.id("reportBtnId");
    private final By viewUnacceptableProductsDialog = By.id("viewUnacceptableProductsDialog");
    private final By viewUnacceptableProductsDialogHeader = By.xpath("//h2[contains(text(),'Unacceptable Product Report')]");
    private final By unacceptableProductsTable = By.id("unacceptableProductsTable");



    private By addedCartonRow(String cartonNumberPrefix, String sequence, String status) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]",
                cartonNumberPrefix, sequence, status));
    }

    private By addedCartonRow(String sequence) {
        return By.xpath(
            String.format(
                "//td[contains(@id,'cartonSequenceRow')]//*[.='%s']",
                sequence));
    }

    private By addedUnitInCartonTable(String cartonSequence, String unitNumber) {
        return By.xpath(
            String.format(
                "//td[contains(@id,'cartonSequenceRow')]//*[.='%s']/ancestor::tr/following-sibling::tr//*[contains(text(),'%s')]",
                cartonSequence, unitNumber));
    }

    private By unacceptableReportRow(String unitNumber , String productCode , String cartonNumberPrefix, String sequence, String reason) {
        return By.xpath(
            String.format(
                "//table[@id='unacceptableProductsTable']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]",
                unitNumber, productCode, cartonNumberPrefix,sequence,reason));
    }

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Autowired
    private TestUtils testUtils;

    @Value("${testing.browser}")
    private String browser;

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



    public void verifyCartonsAreVisible(List<Map> createCartonResponseList) {
        for (Map carton : createCartonResponseList) {
            verifyCartonIsListed(carton.get("cartonNumber").toString(), carton.get("cartonSequence").toString(), carton.get("status").toString());
        }
    }

    public void clickExpandCarton(String sequenceNumber) {
        sharedActions.click(addedCartonRow(sequenceNumber));
    }

    public void verifyUnitsAreListed(String cartonSequence, String unitNumberList) {
        clickExpandCarton(cartonSequence);
        for (String unitNumber : testUtils.getCommaSeparatedList(unitNumberList)) {
            sharedActions.waitForVisible(addedUnitInCartonTable(cartonSequence, testUtils.removeUnitNumberScanDigits(unitNumber)));
        }
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(shipmentDetailsHeader);
    }

    public void viewCartonPackingSlip() throws InterruptedException {
        Thread.sleep(500);
        sharedActions.moveToNewTab(driver, getExpectedWindowsNumber());
    }

    private int getExpectedWindowsNumber() {
        return "chrome".equals(browser) ? 3 : 2;
    }

    public void clickCloseShipment() {
        sharedActions.click(closeShipmentBtn);
    }

    public boolean isCloseShipmentButtonEnabled() {
        sharedActions.waitForVisible(closeShipmentBtn);
        return sharedActions.isElementEnabled(driver, closeShipmentBtn);
    }

    public void setShipmentConfirmationDate(String date) {
        sharedActions.sendKeys(confirmationShipmentDate, date);
    }

    public String getShipmentConfirmationDate() {
        return sharedActions.getInputValue(confirmationShipmentDate);
    }

    public void clickConfirmCloseShipment() {
        sharedActions.click(confirmCloseShipmentBtn);
    }

    public String getLastUnacceptableRunDate() {
        return sharedActions.getText(unacceptableReportLastRunDate);
    }

    public boolean isUnacceptableReportButtonEnabled() {
        sharedActions.waitForVisible(unacceptableReportBtn);
        return sharedActions.isElementEnabled(driver, unacceptableReportBtn);
    }

    public void clickUnacceptableReportButton() {
        sharedActions.click(unacceptableReportBtn);
    }

    public void verifyUnacceptableProductsReportIsVisible() {
        sharedActions.isElementVisible(viewUnacceptableProductsDialog);
        sharedActions.isElementVisible(viewUnacceptableProductsDialogHeader);
        sharedActions.isElementVisible(unacceptableProductsTable);
    }

    public String getUnacceptableTableHeader() {
        var productsTable = driver.findElement(unacceptableProductsTable);
        var rows = productsTable.getText().split("\n");
        return rows[0];
    }

    public void verifyProductIsListed(String unitNumber , String productCode , String cartonNumberPrefix, String sequence, String reason) {
        sharedActions.waitForVisible(unacceptableReportRow(unitNumber,productCode,cartonNumberPrefix,sequence,reason));
    }
}
