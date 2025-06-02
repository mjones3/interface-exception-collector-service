package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
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
    private final By confirmRepackCartonBtn = By.id("btnContinue");
    private final By unacceptableReportLastRunDate = By.id("informationDetails-Last-Run-value");
    private final By unacceptableReportBtn = By.id("reportBtnId");
    private final By viewUnacceptableProductsDialog = By.id("viewUnacceptableProductsDialog");
    private final By viewUnacceptableProductsDialogHeader = By.xpath("//h2[contains(text(),'Unacceptable Products Report')]");
    private final By unacceptableProductsTable = By.id("unacceptableProductsTable");
    private final By cancelBtn = By.id("btnCancel");
    private final By repackComments = By.id("reasonCommentsId");
    private final By reportsBtn = By.id("reportsDialogBtnId");
    private final By confirmRemoveCartonBtn = By.id("confirmation-dialog-confirm-btn");
    private final By editShipmentBtn = By.xpath("//button//*[contains(text(),'Edit')]");
    private final By customerNameEditSelect = By.id("customerSelectIdSelect");
    private final By searchCustomerNameEditSelect = By.xpath("//biopro-search-select[@id='customerNameId']/mat-form-field");
    private final By productTypeEditSelect = By.id("productTypeSelectIdSelect");
    private final By searchProductTypeEditSelect = By.xpath("//biopro-search-select/mat-form-field[@id='productTypeSelectId']");
    private final By transportationNumberEditInput = By.id("transportationReferenceNumberId");
    private final By shipmentDateEditInput = By.id("shipmentDateId");
    private final By commentsTab = By.xpath("//a/span[contains(.,'Comments')]");


    private By addedCartonRow(String cartonNumberPrefix, String sequence, String status) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]",
                cartonNumberPrefix, sequence, status));
    }
 private By removeCartonButton(String cartonNumberPrefix, String sequence, String status) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/parent::tr//button[@data-testid='remove-carton']",
                cartonNumberPrefix, sequence, status));
    }

    private By repackCartonButton(String cartonNumberPrefix, String sequence, String status) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td/*[@id='repackCartonBtn']",
                cartonNumberPrefix, sequence, status));
    }

    private By addedCartonRow(String sequence) {
        return By.xpath(
            String.format(
                "//td[contains(@id,'cartonSequenceRow')]//*[.='%s']/../parent::tr/td/button[contains(@id, 'ExpandBtn')]",
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

    private By cartonStatusRow(String cartonNumberPrefix, String sequence) {
        return By.xpath(
            String.format(
                "//table[@id='cartonListTableId']//td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/following-sibling::td[starts-with(@id,'statusRow')]",
                cartonNumberPrefix, sequence, status));
    }

    private By shipmentHistoryRow(String user, String Date, String comments) {
        return By.xpath(
            String.format(
                "//table[@id='shipmentInfoCommentsTableId']//td[contains(., '%s')]/following-sibling::td[contains(., '%s')]/following-sibling::td[contains(., '%s')]",
                user, Date, comments));
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
        sharedActions.waitForVisible(shipmentNumber);
        return sharedActions.getText(shipmentNumber);
    }

    public String getCustomerCode() {
        return sharedActions.getText(customerCode);
    }

    public String getCustomerName() {
        return sharedActions.getText(customerName);
    }

    public String getShipmentStatus() {
        sharedActions.waitForVisible(status);
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

    public boolean isRepackButtonEnabled(String cartonNumberPrefix, String sequence, String status) {
        var id = repackCartonButton(cartonNumberPrefix, sequence, status);
        sharedActions.waitForVisible(id);
        return sharedActions.isElementVisible(id);
    }

    public void clickRepackButton(String cartonNumberPrefix, String sequence, String status) {
        sharedActions.click(repackCartonButton(cartonNumberPrefix, sequence, status));
    }

    public void clickCancelButton() {
        sharedActions.click(cancelBtn);
    }

    public String getCartonStatus(String cartonNumberPrefix, String sequence) {
        var cartonRow = cartonStatusRow(cartonNumberPrefix, sequence);
        sharedActions.waitForVisible(cartonRow);
        return sharedActions.getText(cartonRow);
    }

    public void enterRepackComments(String comments) {
        sharedActions.sendKeys(repackComments, comments);
    }

    public void clickConfirmRepackCarton() {
        sharedActions.click(confirmRepackCartonBtn);
    }

    public boolean isReportsButtonEnabled() {
        sharedActions.waitForVisible(reportsBtn);
        return sharedActions.isElementEnabled(driver, reportsBtn);
    }

    public void clickPrintReportBtn() {
        sharedActions.click(reportsBtn);
    }


    public void checkRemoveCartonOptionIsAvailable(String prefix, String sequenceNumber, String status) {
        sharedActions.waitForVisible(removeCartonButton(prefix, sequenceNumber, status));
        Assert.assertTrue(sharedActions.isElementEnabled(driver, removeCartonButton(prefix, sequenceNumber, status)));
    }

    public void removeCarton(String prefix, String sequenceNumber, String status) {
        sharedActions.click(removeCartonButton(prefix, sequenceNumber, status));
    }

    public void confirmRemoveCarton() {
        sharedActions.click(confirmRemoveCartonBtn);
    }

    public boolean isEditShipmentButtonEnabled() {
        sharedActions.waitForVisible(editShipmentBtn);
        return sharedActions.isElementEnabled(driver, editShipmentBtn);
    }

    public void clickEditShipmentButton() {
        sharedActions.click(editShipmentBtn);
    }

    public void verifyEditShipmentFields(String field, String value, String status) throws InterruptedException {
        boolean expectEnabled = status.equals("enabled");

        // Wait for items to load in the form
        Thread.sleep(500);
        switch (field) {
            case "Customer":
                Assert.assertEquals(value, sharedActions.getText(customerNameEditSelect));
                Assert.assertEquals(expectEnabled, !sharedActions.hasElementCssClass(searchCustomerNameEditSelect, "mat-form-field-disabled"));
                break;
            case "Product Type":
                Assert.assertEquals(value, sharedActions.getText(productTypeEditSelect));
                Assert.assertEquals(expectEnabled, !sharedActions.hasElementCssClass(searchProductTypeEditSelect, "mat-form-field-disabled"));
                break;
            case "Transportation Reference Number":
                Assert.assertEquals(value, sharedActions.getInputValue(transportationNumberEditInput));
                Assert.assertEquals(expectEnabled, sharedActions.isElementEnabled(driver, transportationNumberEditInput));
                break;
            case "Shipment Date":
                Assert.assertEquals(testUtils.parseDateKeyword(value), sharedActions.getInputValue(shipmentDateEditInput));
                Assert.assertEquals(expectEnabled, sharedActions.isElementEnabled(driver, shipmentDateEditInput));
                break;
            default:
                throw new IllegalArgumentException("Invalid field: " + field);
        }

    }

    public void switchToCommentsTab() {
        sharedActions.click(commentsTab);
    }

    public void verifyShipmentHistoryRow(String user, String date, String comments) {
        var row = shipmentHistoryRow(user, testUtils.parseDateKeyword(date), comments);
        sharedActions.waitForVisible(row);
    }
}
