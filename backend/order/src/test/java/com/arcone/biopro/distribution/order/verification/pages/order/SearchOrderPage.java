package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Component
@Slf4j
public class SearchOrderPage extends CommonPageFactory {

    OrderController orderController = new OrderController();
    public static final int SECONDS_TO_WAIT_FOR_COMPONENT_TO_SHOW = 5;

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Value("${ui.base.url}")
    private String baseUrl;


    //    Static locators and elements
    @FindBy(xpath = "//h3/*[text()='Search Orders']")
    private WebElement searchOrdersTitle;

    @FindBy(xpath = "//*[@class='p-datatable-loading-overlay']")
    private WebElement tableLoadingOverlay;

    @FindBy(id = "filtersButtonId")
    private WebElement filterToggleButton;

    @FindBy(id = "orderNumberInput")
    private WebElement orderNumberField;

    @FindBy(id = "orderStatusId")
    private WebElement orderStatusField;

    @FindBy(id = "orderPrioritiesId")
    private WebElement orderPrioritiesField;

    @FindBy(id = "customersId")
    private WebElement customersField;

    @FindBy(id = "createDateId")
    private WebElement createDateField;

    @FindBy(id = "desiredShippingDateId")
    private WebElement desiredShippingDateField;

    @FindBy(id = "applyBtn")
    private WebElement filterApplyButton;

    @FindBy(id = "resetFilterBtn")
    private WebElement filterResetButton;

    private static final String tableRows = "//*[@id='ordersTableId']//tbody/tr";

    @FindBy(xpath = tableRows)
    private List<WebElement> tableRowsList;

    @FindAll({
        @FindBy(xpath = "//tr[contains(@id,'order-table-row')]//td[position()=3]")
    })
    private List<WebElement> orderPriorityList;

    //    Dynamic locators
    private String orderDetailsButtonXpath(String orderId) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td/button[@id='detailsBtn']", orderId);
    }

    private String orderIdXpath(String orderId) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]", orderId);
    }

    private String orderStatusXpath(String orderId, String status) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td[contains(text(),'%s')]", orderId, status);
    }

    private String orderPriorityXpath(String orderId, String priority) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td/span[contains(text(),'%s')]", orderId, priority);
    }

    @Override
    public boolean isLoaded() {
        sharedActions.waitForVisible(searchOrdersTitle);
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        return sharedActions.isElementVisible(searchOrdersTitle);
    }

    public void goTo() {
        driver.get(baseUrl + "/orders/search");
        Assert.assertTrue(isLoaded());
    }

    public void validateOrderDetails(String externalId, String orderStatus, String orderPriority) throws InterruptedException {
        sharedActions.waitLoadingAnimation();
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderIdXpath(externalId))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderStatusXpath(externalId, orderStatus))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderPriorityXpath(externalId, orderPriority))));
    }

    public WebElement getPriorityElement(String externalId, String orderPriority) {
        return driver.findElement(By.xpath(orderPriorityXpath(externalId, orderPriority)));
    }

    public void verifyOrderDetailsOption(String externalId) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderDetailsButtonXpath(externalId))));
    }

    public void verifyOrderNotExists(String externalId) {
        try {
            sharedActions.waitForVisible(driver.findElement(By.xpath(orderIdXpath(externalId))));
            Assert.fail("Order " + externalId + " exists in the list of orders.");
        } catch (Exception e) {
            log.info("Order " + externalId + " does not exist in the list of orders.");
        }
    }

    public void verifyPriorityOrderList() {

        sharedActions.waitForVisible(orderPriorityList.getFirst());

        var priorityList = orderPriorityList.stream().toList().stream().map(WebElement::getText)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        log.info("Configured priority is: {}", orderController.priorities.keySet());
        log.info("Priority shown in UI: {}", priorityList);

        Assert.assertTrue(orderController.isPriorityOrdered(priorityList));
    }


    public List<WebElement> getOrderPriorityList() {
        return orderPriorityList.stream().toList();
    }

    public int tableRowsCount() {
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        sharedActions.waitForVisible(By.xpath(tableRows));
        return tableRowsList.size();
    }

    public void searchOrder(String value) throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        sharedActions.sendKeys(orderNumberField, value);
    }

    public void openTheSearchPanel() throws InterruptedException {
        sharedActions.waitForVisible(filterToggleButton);
        sharedActions.click(filterToggleButton);
    }

    public void iChooseApplyOption() throws InterruptedException {
        sharedActions.waitForVisible(filterApplyButton);
        sharedActions.click(filterApplyButton);
    }

    public void iChooseResetOption() throws InterruptedException {
        sharedActions.waitForVisible(filterResetButton);
        sharedActions.click(filterResetButton);
    }

    public void checkIfDetailsPageIsOpened() {
        sharedActions.waitForRedirectTo("order-details");
    }

    public void theOrderFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        assertFalse("Order field should be displayed",orderNumberField.isDisplayed());
    }

    public void theOrderFieldIsIsRequiredField() {
        assertIsRequiredField("Order field should be displayed", orderNumberField);
    }

    public void theOrderFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        assertFalse("Order field should be disabled",orderNumberField.isEnabled());
    }

    public void theOrderFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        assertTrue("Order field should be enabled",orderNumberField.isEnabled());
    }

    public void theResetOptionIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(filterResetButton);
        assertFalse("Reset button should be disabled",filterResetButton.isEnabled());
    }

    public void theApplyOptionIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(filterApplyButton);
        assertFalse("Apply button should be disabled",filterApplyButton.isEnabled());
    }

    public void theResetOptionIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(filterResetButton);
        assertTrue("Reset button should be enabled",filterResetButton.isEnabled());
    }

    public void theApplyOptionIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(filterApplyButton);
        assertTrue("Apply button should be enabled",filterApplyButton.isEnabled());
    }

    public void assertIsAriaDisabled(String message, WebElement element) {
        assertTrue(message, element.getDomAttribute("aria-disabled").equals("true"));
    }

    public void assertIsAriaEnabled(String message, WebElement element) {
        assertTrue(message, element.getDomAttribute("aria-disabled").equals("false"));
    }

    public void assertIsOptionalField(String message, WebElement element) {
        assertTrue(message, element.getDomAttribute("aria-required").equals("false"));
    }

    public void assertIsRequiredField(String message, WebElement element) {
        assertTrue(message, element.getDomAttribute("aria-required").equals("true"));
    }

    public WebElement findElementById(String fieldId) {
        return findElementWithByWaiting(By.id(fieldId), SECONDS_TO_WAIT_FOR_COMPONENT_TO_SHOW);
    }

    public WebElement findElementWithByWaiting(By field, int seconds) {
        var wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(field));
    }

    public void closeDropdownIfOpen(WebElement dropdown) {
        if ("true".equals(dropdown.getAttribute("aria-expanded"))) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('aria-expanded', 'false');", dropdown);
            log.info("Closed dropdown using JavaScript");
        }
    }

    public void iShouldSeeAValidationMessage(String message) {
        assertTrue(
            "%s error message not found".formatted(message),
            driver.getPageSource().contains("%s".formatted(message))
        );
    }

    public void theOrderNumberFieldShouldHaveEmptyValue() {
        theFieldShouldHaveEmptyValue(orderNumberField);
    }

    public void theFieldShouldHaveEmptyValue(WebElement fieldInput) {
        assertTrue("The field is blank", fieldInput.getAttribute("value").isBlank());
    }

    public void setCreateDateField(String value) throws InterruptedException {
        sharedActions.waitForVisible(createDateField);
        sharedActions.sendKeys(createDateField, value);
    }

    public void setDesireDateField(String value) throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateField);
        sharedActions.sendKeys(desiredShippingDateField, value);
    }
}
