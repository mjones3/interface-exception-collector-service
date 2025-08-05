package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Component
@Slf4j
public class ShipmentDetailPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${ui.shipment-details.url}")
    private String shipmentDetailsUrl;

    @FindBy(how = How.ID, using = "viewPickListBtn")
    private WebElement viewPickListButton;

    @FindBy(id = "detailsBtn")
    private WebElement productShippingDetailsSection;

    @FindBy(id = "percentageId")
    private WebElement amountOfProductsFilled;

    @FindBy(id = "prodTableId")
    private WebElement orderCriteriaTable;

    @FindBy(xpath = "//*[@id='informationDetails-Labeling-Product-Category']")
    private WebElement productCategory;

    @FindBy(xpath = "//*[@id='informationDetails-Shipping-Method']")
    private WebElement shippingMethodElement;

    @FindBy(xpath = "//*[@id='informationDetails-BioPro-Order-ID']")
    private WebElement orderNumber;

    @FindBy(id = "informationDetails-Priority")
    private WebElement orderPriority;

    @FindBy(xpath = "//*[@id='informationDetails-Customer-Code']")
    private WebElement customerId;

    @FindBy(xpath = "//*[@id='informationDetails-Customer-Name']")
    private WebElement customerName;

    @FindBy(id = "informationDetails-Status")
    private WebElement orderStatus;

    @FindBy(id = "bloodTypeColumn")
    private WebElement bloodTypeColumn;

    @FindBy(id = "productFamilyColumn")
    private WebElement productFamilyColumn;

    @FindBy(id = "quantityColumn")
    private WebElement quantityColumn;

    @FindBy(id = "viewPackingListBtn")
    private WebElement viewPackingListButton;

//    Static locators
    private final static String shipmentStatusValue = "//*[@id=\"informationDetails-Status\"]/following-sibling::span";
    private final static String productTable = "//*[@id='prodTableId']";
    private final static String verifyProductsBtn = "//*[@id='verifyProductsBtn']";
    private final static String pendingPercentage = "//*[@id='percentageId']";
    private final static String externalId = "//*[@id='informationDetails-External-Order-ID']";
    private final static String completeShipmentButton = "//*[@id='completeShipmentBtn']";
    private final static String printShippingLabelButton = "//*[@id='viewShippingLabelBtn']";

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(By.xpath(productTable));
    }

    @Value("${testing.browser}")
    private String browser;

    //Static By Locators

    private By orderCommentAccordion = By.id("orderInfoComment");
    private By backButnLocator = By.id("backBtnId");
//    private By manageProductsBtn = By.cssSelector("[id^='fillShipmentBtn']");
    private By manageProductsBtn = By.cssSelector("[id^='manageProductBtn']");

    // Dynamic By Locators

    private By orderComment(String comment) {
        return By.xpath(String.format("//*[@id='orderInfoComment']//div[contains(text(),'%s')]", comment));
    }

    private By manageProductButton(String family, String bloodType) {
        return By.xpath(String.format("//td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']/following-sibling::td//button", family.toUpperCase().replace("_", " "), bloodType.toUpperCase()));
    }

    private int getExpectedWindowsNumber() {
        return "chrome".equals(browser) ? 3 : 2;
    }

    public void openViewPickListModal() throws InterruptedException {
        sharedActions.waitForVisible(viewPickListButton);
        sharedActions.click(viewPickListButton);
    }

    public void viewAmountOfProductsFilled() {
        sharedActions.waitForVisible(amountOfProductsFilled);
        wait.until((ExpectedCondition<Boolean>) wd ->
            ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        String value = amountOfProductsFilled.getText();
        log.info("Checking if the amount of products filled is zero. Current value: {}", value);
        boolean isZero = "0%".equals(value);
        assertTrue(isZero, "The amount of products filled is not zero. Current value: " + value);
    }

    public void viewPickListButton() {
        sharedActions.waitForVisible(viewPickListButton);
    }

    public void viewProductShippingDetails() {
        sharedActions.waitForNotVisible(productShippingDetailsSection);
    }

    public void viewFillProduct() {
        sharedActions.waitForVisible(manageProductsBtn);
    }

    public void viewPageContent() {
        waitForElementsVisible(quantityColumn, productFamilyColumn, bloodTypeColumn, shippingMethodElement, orderCriteriaTable, productCategory, orderNumber, orderPriority, customerId, customerName, orderStatus, sharedActions.getElement(this.driver, By.xpath(externalId)));
    }

    private void waitForElementsVisible(WebElement... elements) {
        for (WebElement element : elements) {
            sharedActions.waitForVisible(element);
        }
    }

    public void goTo(Long shipmentId) {
        var url = shipmentDetailsUrl.replace("{shipmentId}", String.valueOf(shipmentId));
        log.info("Navigating to the shipment details page: {}", url);
        sharedActions.navigateTo(url);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(By.xpath(productTable));
    }

    public void clickViewPackingSlip() throws InterruptedException {
        log.info("Clicking on the View Packing Slip button.");
        sharedActions.clickElementAndMoveToNewTab(driver, viewPackingListButton, getExpectedWindowsNumber());
    }

    public void clickPrintShippingLabel() throws InterruptedException {
        log.info("Clicking on the Print Shipping Label button.");
        sharedActions.clickElementAndMoveToNewTab(driver, sharedActions.getElement(this.driver, By.xpath(printShippingLabelButton)), getExpectedWindowsNumber());
    }

    public void ensureViewPackingSlipButtonIsNotVisible() {
        sharedActions.waitForNotVisible(viewPackingListButton);
    }

    public void ensureViewShippingLabelButtonIsNotVisible() {
        sharedActions.waitForNotVisible(By.xpath(printShippingLabelButton));
    }

    public void clickFillProduct(String familyName, String bloodType) throws InterruptedException {
        var family = familyName.split(",")[0].trim();
        var blood = bloodType.split(",")[0].trim();
        log.info("Filling product with family {} and blood type {}.", family, blood);
        String locator = String.format("//td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']/following-sibling::td//button", family.toUpperCase(), blood.toUpperCase());
        sharedActions.waitForVisible(By.xpath(locator));
        sharedActions.click(driver.findElement(By.xpath(locator)));
    }

    public void completeShipment() throws InterruptedException {
        log.info("Completing shipment.");
        sharedActions.click(By.xpath(completeShipmentButton));
    }

    public void checkTotalProductsShipped(int totalProductsShipped) {
        log.info("Checking if the total of products shipped is {}.", totalProductsShipped);
        String locator = String.format("//p-table[@id='shippedProdTableId']//tfoot//td[.='(%s) Total Components']", totalProductsShipped);
        sharedActions.locateXpathAndWaitForVisible(locator, driver);
    }

    public void checkPendingLogNotVisible() {
        log.info("Checking if the pending log is not visible.");
        sharedActions.waitForNotVisible(By.xpath(pendingPercentage));
    }

    public void checkOrderComment(String comment) throws InterruptedException {
        log.info("Checking if the order comment is {}.", comment);
        sharedActions.click(driver, orderCommentAccordion);
        sharedActions.waitForVisible(orderComment(comment));
    }

    public void clickBackBtn() throws InterruptedException {
        sharedActions.click(driver, backButnLocator);
    }

    public void checkCompleteButtonIsNotVisible() {
        log.debug("checking Complete shipment button is visible.");
        sharedActions.waitForNotVisible(By.xpath(completeShipmentButton));
    }

    public void checkVerifyProductsButtonIsNotVisible(){
        log.debug("checking Verify products button is not visible.");
        sharedActions.waitForNotVisible(By.xpath(verifyProductsBtn));
    }

    public void checkVerifyProductsButtonIsVisible(){
        log.debug("checking Verify products button is visible.");
        sharedActions.waitForVisible(By.xpath(verifyProductsBtn));
    }

    public void clickVerifyProductsBtn() throws InterruptedException {
        sharedActions.click(By.xpath(verifyProductsBtn));
    }

    public void verifyShippingStatusIs(String status) {
        log.debug("Verifying that the shipping status is {}", status);
        sharedActions.waitForVisible(By.xpath(shipmentStatusValue));
        var actualStatus = sharedActions.getText(By.xpath(shipmentStatusValue));
        assertTrue(actualStatus.contains(status), "The shipping status is not as expected. Expected: " + status + ", Actual: " + actualStatus);
    }

    public void verifyManageProductsButtonIsVisible(boolean visible, String family, String bloodType) {
        log.debug("Verifying that the Manage Products button is {}", visible ? "visible" : "not visible");
        if (visible) {
            sharedActions.waitForVisible(manageProductButton(family, bloodType));
        } else {
            sharedActions.waitForNotVisible(manageProductButton(family, bloodType));
        }
    }

    private String shippingInformationDetail(String param) {
        return String.format("//*[@id='shippingInfoDescriptions']/*//span[normalize-space()='%s']", param);
    }

    private String orderInformationDetail(String param) {
        return String.format("//*[@id='orderInfoDescriptions']/*//span[normalize-space()='%s']", param);
    }

    public void verifyInternalTransferInformation(String shipmentType, String labelStatus, String quarantinedProducts) {
        sharedActions.waitForVisible(By.xpath(shippingInformationDetail(shipmentType)));
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(labelStatus)));
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(quarantinedProducts)));
    }
}
