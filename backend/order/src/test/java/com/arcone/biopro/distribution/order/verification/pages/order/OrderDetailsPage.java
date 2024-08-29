package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import org.json.JSONException;
import org.json.JSONObject;
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

    @FindBy(id = "shortDateDetailsEmpty")
    private WebElement noShortDateDetailsMessage;

    @FindBy(css = "#shortDateDetailsTable tbody tr")
    private List<WebElement> shortDateDetailsTableRows;

    @FindBy(css = "button[title='Close']")
    private WebElement closeViewPickListDialogButton;

    @FindBy(id = "shipmentsTableId")
    private WebElement shipmentDetailsTable;

    private static final By shipmentTableLocator = By.id("shipmentsTableId");
    private static final By shipmentDateLocator = By.xpath("//p-table[@id='shipmentsTableId']//td[5]");
    private static final By shipmentDetailsBtn = By.id("goToShipmentBtn");
    private static final By shipmentDetailsTableRows = By.xpath("//p-table[@id='shipmentsTableId']//tbody//tr");
    private static final By filledProductsCountLabel = By.id("filledOrdersCount");
    private static final By totalProductsCountLabel = By.id("totalOrderProducts");


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

    private String availableInventory(String productFamily, String bloodType, Integer quantity) {
        return String.format("//*[@id='prodTableId']/*//tbody//tr//td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']/following-sibling::td[normalize-space()='%s']/following-sibling::td[1]", productFamily.toUpperCase(), bloodType.toUpperCase(), quantity);
    }

    private String pickListHeaderDetails(String detail) {
        return String.format("//*[@id='viewPickListReport']//table[@id='pickListTable']//td[contains(normalize-space(),'%s')]", detail);
    }

    private String pickListProductDetails(String detail) {
        return String.format("//*[@id='viewPickListReport']//table[@id='productDetailsTable']//td[contains(normalize-space(),'%s')]", detail);
    }

    private String pickListShortDateTableHeader(String detail) {
        return String.format("//*[@id='viewPickListReport']//table[@id='shortDateDetailsTable']//th[contains(normalize-space(),'%s')]", detail);
    }

    private String shipmentTableDetails(String detail) {
        return String.format("//p-table[@id='shipmentsTableId']//td[text()='%s']", detail);
    }

    // Strings mappers

    private Map<String, String> productFamilyDescription = Map.of(
        "PLASMA_TRANSFUSABLE", "Plasma Transfusable"
    );


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
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(externalId)));
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(orderId.toString())));
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(orderPriority)));
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(orderStatus)));

        sharedActions.click(orderInfoComment);
        sharedActions.waitForVisible(By.xpath(orderComments(orderComments)));
    }

    public void verifyShippingInformationCard(String shippingCustomerCode, String customerName, String shippingMethod) {
        sharedActions.waitForVisible(By.xpath(shippingInformationDetail(shippingMethod)));
        sharedActions.waitForVisible(By.xpath(shippingInformationDetail(shippingCustomerCode)));
        sharedActions.waitForVisible(By.xpath(shippingInformationDetail(customerName.toUpperCase())));
    }

    public void verifyBillingInformationCard(String billingCustomerCode, String customerName) {
        sharedActions.waitForVisible(By.xpath(billInformationDetail(billingCustomerCode)));
        sharedActions.waitForVisible(By.xpath(billInformationDetail(customerName.toUpperCase())));
    }

    public void verifyProductDetailsSection(String productFamily, String bloodType, Integer quantity, String comments) {
        sharedActions.waitForVisible(By.xpath(productDetails(productFamily, bloodType, quantity)));
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

    public void checkAvailableInventory(String[] productFamily, String[] bloodType, String[] quantity) {
        for (int i = 0; i < productFamily.length; i++) {
            String productFamilyDescription = productFamily[i].replace("_", " ");
            sharedActions.waitForVisible(By.xpath(availableInventory(productFamilyDescription, bloodType[i], Integer.valueOf(quantity[i]))));
            Assert.assertFalse(sharedActions.isElementEmpty(driver.findElement(By.xpath(availableInventory(productFamilyDescription, bloodType[i], Integer.valueOf(quantity[i]))))));

        }
    }

    public void verifyPickListHeaderDetails(String orderNumber, String shippingCustomerCode, String customerName, String comments) {
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        sharedActions.waitForVisible(By.xpath(pickListHeaderDetails(orderNumber)));
        sharedActions.waitForVisible(By.xpath(pickListHeaderDetails(shippingCustomerCode)));
        sharedActions.waitForVisible(By.xpath(pickListHeaderDetails(customerName)));
        sharedActions.waitForVisible(By.xpath(pickListHeaderDetails(comments)));
    }

    public void verifyPickListProductDetails(String[] productFamily, String[] bloodType, String[] quantity, String[] comments) {
        for (int i = 0; i < productFamily.length; i++) {
            sharedActions.waitForVisible(By.xpath(pickListProductDetails(productFamilyDescription.get(productFamily[i]))));
            sharedActions.waitForVisible(By.xpath(pickListProductDetails(bloodType[i])));
            sharedActions.waitForVisible(By.xpath(pickListProductDetails(quantity[i])));
            sharedActions.waitForVisible(By.xpath(pickListProductDetails(comments[i])));
        }
    }

    public void verifyShortDateProductDetails(boolean isThereShortDateProduct) {
        if (isThereShortDateProduct) {
            sharedActions.waitForVisible(shortDateDetailsTableRows.getFirst());
            sharedActions.waitForVisible(By.xpath(pickListShortDateTableHeader("Unit Number")));
            sharedActions.waitForVisible(By.xpath(pickListShortDateTableHeader("Product Code")));
            sharedActions.waitForVisible(By.xpath(pickListShortDateTableHeader("Blood Type")));
            sharedActions.waitForVisible(By.xpath(pickListShortDateTableHeader("Storage Location")));
            Assert.assertFalse(shortDateDetailsTableRows.isEmpty());
        } else {
            sharedActions.waitForVisible(noShortDateDetailsMessage);
        }
    }

    public void closePickListModal() {
        sharedActions.waitForVisible(closeViewPickListDialogButton);
        sharedActions.click(closeViewPickListDialogButton);
    }

    public void verifyShipmentTable(JSONObject shipmentDetails) throws JSONException {
        JSONObject payload = (JSONObject) shipmentDetails.get("payload");

        sharedActions.waitForVisible(shipmentTableLocator);
        sharedActions.waitForVisible(By.xpath(shipmentTableDetails(payload.get("shipmentId").toString())));
        sharedActions.waitForVisible(By.xpath(shipmentTableDetails(payload.get("shipmentStatus").toString())));
        Assert.assertFalse(
            sharedActions.isElementEmpty(driver.findElement(shipmentDateLocator))
        );
    }

    public void verifyShipmentDetailsButton() {
        sharedActions.waitForVisible(shipmentDetailsBtn);
    }

    public void verifyOrderStatus(String orderStatus) {
        sharedActions.waitForVisible(By.xpath(orderInformationDetail(orderStatus)));
    }

    public boolean verifyHasMultipleShipments() {
        return driver.findElements(shipmentDetailsTableRows).size() > 1;
    }

    public void assertFilledProductIs(Integer filledProducts) {
        sharedActions.waitForVisible(filledProductsCountLabel);
        Assert.assertEquals(filledProducts, Integer.valueOf(driver.findElement(filledProductsCountLabel).getText()));
    }

    public void assertTotalProductIs(Integer totalProducts) {
        sharedActions.waitForVisible(totalProductsCountLabel);
        Assert.assertEquals(totalProducts, Integer.valueOf(driver.findElement(totalProductsCountLabel).getText()));
    }
}
