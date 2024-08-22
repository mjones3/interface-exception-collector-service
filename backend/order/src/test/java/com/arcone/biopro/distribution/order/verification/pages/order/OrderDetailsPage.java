package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderDetailsPage extends CommonPageFactory {
    @Autowired
    private SharedActions sharedActions;

    @Value("${ui.base.url}")
    private String baseUrl;

    //Static locators and elements
    @FindBy(xpath = "//h3/*[text()='Order Details']")
    private WebElement orderDetailsTitle;

    @FindBy(id = "orderInfoComment")
    private WebElement orderInfoComment;

    @FindBy(xpath = "//*[@class='p-datatable-loading-overlay']")
    private WebElement tableLoadingOverlay;

    @FindBy(how = How.ID, using = "ViewPickListDialog")
    private WebElement ViewPickListDialog;

    public boolean isPicklistDialogLoaded() {
        return sharedActions.isElementVisible(ViewPickListDialog);
    }

    @FindBy(how = How.ID, using = "generatePickListButton")
    private WebElement viewPickListButton;

    //Dynamic locators
    private String orderInformationDetail(String param) {
        return String.format("//*[@id='orderInfoDescriptions']/*//span[normalize-space()='%s']", param);
    }

    private String shippingInformationDetail(String param) {
        return String.format("//*[@id='shippingInfoDescriptions']/*//span[normalize-space()='%s']", param);
    }

    private String billInformationDetail(String param) {
        return String.format("//*[@id='billInfoDescriptions']/*//span[normalize-space()='%s']", param);
    }

    private String orderComments(String comment) {
        return String.format("//*[@id='orderInfoComment']//*[text()='%s']", comment.toUpperCase());
    }

    private String productDetails(String productFamily, String bloodType, Integer quantity) {
        return String.format("//*[@id='prodTableId']/*//tbody//tr//td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']", productFamily.toUpperCase(), bloodType.toUpperCase(), quantity);
    }


    @Override
    public boolean isLoaded() {
        sharedActions.waitForVisible(orderDetailsTitle);
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        return sharedActions.isElementVisible(orderDetailsTitle);
    }

    public void goToOrderDetails(Integer orderId) {
        var orderDetailsUrl = baseUrl + "/orders/" + orderId + "/order-details";
        driver.get(orderDetailsUrl);
        Assert.assertTrue(isLoaded());
    }

    public void verifyOrderDetailsCard(String externalId, Integer orderId, String orderPriority, String orderStatus, String orderComments) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderInformationDetail(externalId))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderInformationDetail(orderId.toString()))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderInformationDetail(orderPriority))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderInformationDetail(orderStatus))));

        sharedActions.click(orderInfoComment);
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderComments(orderComments))));
    }

    public void verifyShippingInformationCard(String shippingCustomerCode, String customerName, String shippingMethod) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(shippingInformationDetail(shippingMethod))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(shippingInformationDetail(shippingCustomerCode))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(shippingInformationDetail(customerName.toUpperCase()))));
    }

    public void verifyBillingInformationCard(String billingCustomerCode, String customerName) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(billInformationDetail(billingCustomerCode))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(billInformationDetail(customerName.toUpperCase()))));
    }

    public void verifyProductDetailsSection(String productFamily, String bloodType, Integer quantity, String comments) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(productDetails(productFamily, bloodType, quantity))));
    }

    public Map<String, String> getShipmentDetailsTableContent() {
        var pickListTable = this.ViewPickListDialog.findElement(By.id("pickListTable"));

        List<WebElement> rowElements = pickListTable.findElements(By.xpath(".//tr"));

        List<WebElement> cellElements = rowElements.get(1).findElements(By.xpath(".//td"));

        var shipmentDetails = new HashMap<String, String>();
        shipmentDetails.put("orderNumber", cellElements.get(0).getText());
        shipmentDetails.put("shippingCustomerCode", cellElements.get(1).getText());
        shipmentDetails.put("customerName", cellElements.get(2).getText());

        return shipmentDetails;
    }

    public Map<String, String> getProductDetailsTableContent() {
        var pickListTable = this.ViewPickListDialog.findElement(By.id("productDetailsTable"));

        List<WebElement> rowElements = pickListTable.findElements(By.xpath(".//tr"));

        var productDetails = new HashMap<String, String>();
        for (int i = 1; i < rowElements.size(); i++) {
            var cellElements = rowElements.get(i).findElements(By.xpath(".//td"));
            var contentLine = cellElements.get(0).getText() + ":" + cellElements.get(1).getText() + ":" + cellElements.get(2).getText();
            productDetails.put(contentLine, contentLine);
        }

        return productDetails;
    }

    public Map<String, String> getShortDateProductDetailsTableContent() {
        var shortDateDetailsTable = this.ViewPickListDialog.findElement(By.id("shortDateDetailsTable"));

        List<WebElement> rowElements = shortDateDetailsTable.findElements(By.xpath(".//tr"));

        var shortDateProductDetails = new HashMap<String, String>();
        for (int i = 1; i < rowElements.size(); i++) {
            var cellElements = rowElements.get(i).findElements(By.xpath(".//td"));
            var contentLine = cellElements.get(0).getText() + ":" + cellElements.get(1).getText() + ":" + cellElements.get(2).getText();
            shortDateProductDetails.put(contentLine, contentLine);
        }

        return shortDateProductDetails;
    }

    public String getNoShortDateMessageContent() {
        var noShortDateDateMessage = this.ViewPickListDialog.findElement(By.id("shortDateDetailsEmpty"));
        return noShortDateDateMessage.getText();
    }

    public void openViewPickListModal() {
        sharedActions.waitForVisible(viewPickListButton);
        sharedActions.click(viewPickListButton);
    }

    public void viewPickListButton() {
        sharedActions.waitForVisible(viewPickListButton);
    }
}
