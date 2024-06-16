package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${ui.base.url}")
    private String baseUrl;

    @Value("${ui.shipment-details.url}")
    private String shipmentDetailsUrl;

    @FindBy(how = How.ID, using = "viewPickListBtn")
    private WebElement viewPickListButton;

    @FindBy(id = "fillShipmentBtn")
    private WebElement fillProductButton;

    @FindBy(id = "prodTableId")
    private WebElement productTable;

    @FindBy(id = "detailsBtn")
    private WebElement productShippingDetailsSection;

    @FindBy(id = "percentageId")
    private WebElement amountOfProductsFilled;

    @FindBy(id = "prodTableId")
    private WebElement orderCriteriaTable;

    @FindBy(id = "informationDetails-Labeling Product Category")
    private WebElement productCategory;

    @FindBy(id = "informationDetails-Shipping Method")
    private WebElement shippingMethodElement;

    @FindBy(id = "informationDetails-Order Number")
    private WebElement orderNumber;

    @FindBy(id = "informationDetails-Priority")
    private WebElement orderPriority;

    @FindBy(id = "informationDetails-Customer ID")
    private WebElement customerId;

    @FindBy(id = "informationDetails-Customer Name")
    private WebElement customerName;

    @FindBy(id = "informationDetails-Ship Date")
    private WebElement shipDate;

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

    @FindBy(id = "viewShippingLabelBtn")
    private WebElement printShippingLabelButton;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(productTable);
    }

    @Value("${testing.browser}")
    private String browser;

    private int getExpectedWindowsNumber() {
        return "chrome".equals(browser) ? 3 : 2;
    }

    public void openViewPickListModal() {
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
        sharedActions.waitForVisible(fillProductButton);
    }

    public void viewPageContent() {
        waitForElementsVisible(quantityColumn, productFamilyColumn, bloodTypeColumn, shippingMethodElement, orderCriteriaTable, productCategory, orderNumber, orderPriority, customerId, customerName, orderStatus, shipDate);
    }

    private void waitForElementsVisible(WebElement... elements) {
        for (WebElement element : elements) {
            sharedActions.waitForVisible(element);
        }
    }

    public void goTo(Long shipmentId) {
        var url = baseUrl + shipmentDetailsUrl.replace("{shipmentId}", String.valueOf(shipmentId));
        this.driver.get(url);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(productTable);
    }

    public void clickViewPackingSlip() {
        log.info("Clicking on the View Packing Slip button.");
        sharedActions.clickElementAndMoveToNewTab(driver, viewPackingListButton, getExpectedWindowsNumber());
    }

    public void clickPrintShippingLabel() {
        log.info("Clicking on the Print Shipping Label button.");
        sharedActions.clickElementAndMoveToNewTab(driver, printShippingLabelButton, getExpectedWindowsNumber());
    }

    public void ensureViewPackingSlipButtonIsNotVisible() {
        sharedActions.waitForNotVisible(viewPackingListButton);
    }

    public void ensureViewShippingLabelButtonIsNotVisible() {
        sharedActions.waitForNotVisible(printShippingLabelButton);
    }
}
