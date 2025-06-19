package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShippingSummaryReportPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    private static final By dialog = By.id("ViewShippingSummaryDialog");
    private static final By testingStatement = By.xpath("//div[@data-testid='testing-statement']");
    private static final By shipToTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='ship-to-blood-center']");
    private static final By shipFromTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='ship-from-blood-center']");
    private static final By shipmentDetailsTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='shipment-details']");
    private static final By productShippedTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='product-shipped']");
    private static final By shipmentInformationTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='shipment-information']");
    private static final By closingDetailsTable = By.xpath("//div[@id='viewShippingSummaryReport']//table[@data-testid='shipment-closing-details']");

    private By cartonInformationRow(String cartonNumberPrefix, String productCode , String productDescription , String totalProducts) {
        return By.xpath(
            String.format(
                "//div[@id='viewShippingSummaryReport']//table[@data-testid='carton-information']//td[contains(.,'%s')]//following-sibling::td[contains(.,'%s')]//following-sibling::td[contains(.,'%s')]//following-sibling::td[contains(.,'%s')]",
                cartonNumberPrefix, productCode, productDescription,totalProducts));
    }


    private By shippingSummaryTitle(String reportTile) {
        return By.xpath(String.format("//div[contains(text(),'%s')]",  reportTile));
    }

    private By shippingSummaryHeader(String text) {
        return By.xpath(String.format("//div[contains(text(),'%s')]",  text));
    }

    public void verifyShippingSummaryReportIsVisible() {
        sharedActions.isElementVisible(dialog);
        sharedActions.isElementVisible(shippingSummaryTitle("Plasma Shipment Summary Report"));
    }

    public boolean verifyShippingSummaryReportTitle(String reportTitle) {
        return sharedActions.isElementVisible(shippingSummaryTitle(reportTitle));
    }

    public boolean verifyShippingSummaryReportHeader(String headerTxt) {
        return sharedActions.isElementVisible(shippingSummaryHeader(headerTxt));
    }

    public String getShippingSummaryReportTestingStatement() {
        return sharedActions.getText(testingStatement);
    }

    public String getShippingSummaryReportShipToCustomerName() {
        var table = driver.findElement(shipToTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getShippingSummaryReportShipToAddress() {
        var table = driver.findElement(shipToTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getShippingSummaryReportShipFromBloodCenterName() {
        var table = driver.findElement(shipFromTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getShippingSummaryReportShipFromAddress() {
        var table = driver.findElement(shipFromTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getShippingSummaryReportShipFromPhoneNumber() {
        var table = driver.findElement(shipFromTable);
        var data = table.getText().split("\n");
        return data[2];
    }

    public String getShipmentDetailsTransportationNumber() {
        var table = driver.findElement(shipmentDetailsTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getShipmentDetailsShipNumber() {
        var table = driver.findElement(shipmentDetailsTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getShipmentDetailsShipDateTime() {
        var table = driver.findElement(shipmentDetailsTable);
        var data = table.getText().split("\n");
        return data[2];
    }

    public String getProductShippedProductType() {
        var table = driver.findElement(productShippedTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getProductShippedProductCode() {
        var table = driver.findElement(productShippedTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getShipmentInformationTotalCartons() {
        var table = driver.findElement(shipmentInformationTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getShipmentInformationTotalProducts() {
        var table = driver.findElement(shipmentInformationTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getClosingDetailsEmployeeName() {
        var table = driver.findElement(closingDetailsTable);
        var data = table.getText().split("\n");
        return data[0];
    }

    public String getClosingDetailsEmployeeSignature() {
        var table = driver.findElement(closingDetailsTable);
        var data = table.getText().split("\n");
        return data[1];
    }

    public String getClosingDetailsDate() {
        var table = driver.findElement(closingDetailsTable);
        var data = table.getText().split("\n");
        return data[2];
    }

    public boolean verifyCartonDetailRow(String cartonNumberPrefix, String productCode , String productDescription , String totalProducts){
        return sharedActions.isElementVisible( cartonInformationRow(cartonNumberPrefix,productCode,productDescription,totalProducts));
    }



    @Override
    public boolean isLoaded() {
        return false;
    }
}
