package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Component
@Slf4j
public class SearchOrderPage extends CommonPageFactory {

    private static final String ORDER_STATUS_SELECT_ID = "orderStatusSelect";
    private static final String ORDER_STATUS_PANEL_ID = "orderStatusSelect-panel";
    private static final String ORDER_PRIORITY_SELECT_ID = "deliveryTypesSelect";
    private static final String ORDER_PRIORITY_PANEL_ID = "deliveryTypesSelect-panel";
    private static final String ORDER_SHIP_TO_CUSTOMER_SELECT_ID = "customersSelect";
    private static final String ORDER_SHIP_TO_CUSTOMER_PANEL_ID = "customersSelect-panel";

    OrderController orderController = new OrderController();

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

    @FindBy(id = "orderStatusSelect")
    private WebElement orderStatusField;

    @FindBy(id = "deliveryTypesSelect")
    private WebElement orderPrioritiesField;

    @FindBy(id = "customersSelect")
    private WebElement customersField;

    @FindBy(id = "createDateFrom")
    private WebElement createDateFromField;

    @FindBy(id = "createDateTo")
    private WebElement createDateToField;

    @FindBy(id = "desiredShipmentDateFrom")
    private WebElement desiredShippingDateFromField;

    @FindBy(id = "desiredShipmentDateTo")
    private WebElement desiredShippingDateToField;

    @FindBy(id = "applyBtn")
    private WebElement filterApplyButton;

    @FindBy(id = "resetFilterBtn")
    private WebElement filterResetButton;

    @FindBy(xpath = "//button[@id='filtersButtonId']//span[contains(@class, 'mat-badge-content')]")
    private WebElement filterCountBadge;





    private static final String tableRows = "//biopro-table//tbody/tr";

    @FindBy(xpath = tableRows)
    private List<WebElement> tableRowsList;

    @FindAll({
        @FindBy(xpath = "//td[starts-with(@id,'orderPriorityReport.priorityRow')]")
    })
    private List<WebElement> orderPriorityList;

    //    Dynamic locators
    private String orderDetailsButtonXpath(String externalId) {
        return String.format("//td[starts-with(@id,'externalIdRow')]//span[contains(text(),'%s')]/../../..//button[@id='detailsBtn']", externalId);
    }

    private String orderIdXpath(String orderId) {
        return String.format("//td[starts-with(@id,'orderNumberRow')]//span[contains(text(),'%s')]", orderId);
    }

    private String externalIdXpath(String externalId) {
        return String.format("//td[starts-with(@id,'externalIdRow')]//span[contains(text(),'%s')]", externalId);
    }

    private String orderStatusXpath(String orderId, String status) {
        return String.format("//td[starts-with(@id,'externalIdRow')]//span[contains(text(),'%s')]/../../..//span[contains(text(),'%s')]", orderId, status.toUpperCase());
    }

    private String orderPriorityXpath(String orderId, String priority) {
        return String.format("//td[starts-with(@id,'externalIdRow')]//span[contains(text(),'%s')]/../../..//span[contains(text(),'%s')]", orderId, priority.toUpperCase());
    }

    private String labelXpath(String fieldLabel) {
        return "//mat-label[contains(text(), '%s')]/parent::*".formatted(fieldLabel);
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
        sharedActions.waitForVisible(By.xpath(externalIdXpath(externalId)));
        sharedActions.waitForVisible(By.xpath(orderStatusXpath(externalId, orderStatus)));
        sharedActions.waitForVisible(By.xpath(orderPriorityXpath(externalId, orderPriority)));
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

    public void verifyOrderExists(String externalId) {
        try {
            sharedActions.waitForNotVisible(tableLoadingOverlay);
            sharedActions.waitForVisible(By.xpath(orderIdXpath(externalId)));
            log.info("Order " + externalId + " exists in the list of orders.");
        } catch (Exception e) {
            Assert.fail("Order " + externalId + " does not exist in the list of orders.");
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
        var a = tableRowsList.size();
        return tableRowsList.size();
    }

    private void openDropDownIfClosed(WebElement dropdown) {
        if (!Boolean.parseBoolean(dropdown.getDomAttribute("aria-expanded"))) {
            sharedActions.click(dropdown);
        }
        sharedActions.waitForAttribute(dropdown, "aria-expanded","true");
    }

    private void closeDropdownIfOpen(WebElement dropdown) {
        try {
            if (dropdown.isDisplayed() && Boolean.parseBoolean(dropdown.getDomAttribute("aria-expanded"))) {
                sharedActions.click(dropdown);
            }
        } catch (Exception ignored) {}
    }

    public void checkSelectedValuesFromDropdown(String dropdownId, String panelId, List<String> valuesToCheck) {
        WebElement dropdown = driver.findElement(By.id(dropdownId));
        openDropDownIfClosed(dropdown);
        WebElement dropdownPanel = driver.findElement(By.id(panelId));

        List<WebElement> options = dropdownPanel.findElements(By.xpath(".//mat-option"));
        for (var option: options)
        {
            sharedActions.waitForVisible(option);
        }

        var selectedOptions = options.stream().filter(option -> option.getDomAttribute("aria-selected").equals("true")).toList();
        for (String value : valuesToCheck) {

            if (!value.isEmpty()) {
                Assert.assertTrue("Selected value: " + value, selectedOptions.stream()
                    .anyMatch((option -> option.getText().trim().equalsIgnoreCase(value))));
            }
        }
        sharedActions.sendKeys(dropdown, Keys.ESCAPE.toString());
        closeDropdownIfOpen(dropdown);
    }

    public void checkRequiredFields(String valueFields) {
        Arrays.stream(valueFields.split(",")).map(String::trim).forEach
            (valueField -> {
                switch (valueField) {
                    case "create date":
                        theCreateDateIsRequiredField();
                        break;
                    case "order number":
                        theOrderFieldIsRequiredField();
                        break;
                    default:
                        Assert.fail("Field not found: " + valueField);
                }
            });
    }

    public void checkIfEnabledOrDisabled(String valueField, boolean isEnabled) throws InterruptedException {
        if (!isEnabled) {
            switch(valueField) {
                case "create date from": theCreateDateFromFieldIsDisabled(); break;
                case "create date to": theCreateDateToFieldIsDisabled(); break;
                case "desired shipment date from": theDesiredShippingDateFromFieldIsDisabled(); break;
                case "desired shipment date to": theDesiredShippingDateToFieldIsDisabled(); break;
                case "order status": theOrderStatusFieldIsDisabled(); break;
                case "priority": theOrderPrioritiesFieldIsDisabled(); break;
                case "ship to customer": theCustomersFieldIsDisabled(); break;
                case "order number": theOrderFieldIsDisabled(); break;
                default:
                    Assert.fail("Field not found: " + valueField);
            }
        } else {
            switch(valueField) {
                case "create date from": theCreateDateFromFieldIsEnabled(); break;
                case "create date to": theCreateDateToFieldIsEnabled(); break;
                case "desired shipment date from": theDesiredShippingDateFromFieldIsEnabled(); break;
                case "desired shipment date to": theDesiredShippingDateToFieldIsEnabled(); break;
                case "order status": theOrderStatusFieldIsEnabled(); break;
                case "priority": theOrderPrioritiesFieldIsEnabled(); break;
                case "ship to customer": theCustomersFieldIsEnabled(); break;
                case "order number": theOrderFieldIsEnabled(); break;
                default:
                    Assert.fail("Field not found: " + valueField);
            }
        }
    }

    public void checkIfOptionHasStatus(String valueOption, String valueStatus) throws InterruptedException {
        if (valueStatus.equalsIgnoreCase("disabled")) {
            if (valueOption.equalsIgnoreCase("reset")) {
                theResetOptionIsDisabled();
            } else {
                theApplyOptionIsDisabled();
            }
        } else {
            if (valueOption.equalsIgnoreCase("reset")) {
                theResetOptionIsEnabled();
            } else {
                theApplyOptionIsEnabled();
            }
        }
    }

    public void checkSelectedValuesFromDropdownDescription(String values, String dropdownName) {
        switch(dropdownName) {
            case "order status": checkSelectedValuesFromDropdown(ORDER_STATUS_SELECT_ID, ORDER_STATUS_PANEL_ID, Arrays.asList(values.split(",\\s*"))); break;
            case "priority": checkSelectedValuesFromDropdown(ORDER_PRIORITY_SELECT_ID, ORDER_PRIORITY_PANEL_ID, Arrays.asList(values.split(",\\s*"))); break;
            case "ship to customer": checkSelectedValuesFromDropdown(ORDER_SHIP_TO_CUSTOMER_SELECT_ID, ORDER_SHIP_TO_CUSTOMER_PANEL_ID, Arrays.asList(values.split(",\\s*"))); break;
            default:
                Assert.fail("Field not found: " + dropdownName);
        }
    }

    public void checkNumberOfUsedFiltersForTheSearch(String expectedValue) {
        sharedActions.waitForVisible(filterCountBadge);
        var actualValue = filterCountBadge.getText();
        assertEquals("The number of used filters for the search should match.", expectedValue, actualValue);
    }

    public void selectOptionsForDropdownDescription(String values, String dropdownDescription) {
        switch(dropdownDescription) {
            case "order status": selectValuesFromDropdown(ORDER_STATUS_SELECT_ID, ORDER_STATUS_PANEL_ID, Arrays.asList(values.split(","))); break;
            case "priority": selectValuesFromDropdown(ORDER_PRIORITY_SELECT_ID, ORDER_PRIORITY_PANEL_ID, Arrays.asList(values.split(","))); break;
            case "ship to customer": selectValuesFromDropdown(ORDER_SHIP_TO_CUSTOMER_SELECT_ID, ORDER_SHIP_TO_CUSTOMER_PANEL_ID, Arrays.asList(values.split(","))); break;
            default:
                Assert.fail("Field not found: " + dropdownDescription);
        }
    }

    public void setValueForField(String value, String fieldName) throws InterruptedException {
        WebElement fieldToChange;
        switch (fieldName) {
            case "create date from": fieldToChange = createDateFromField;   break;
            case "desired shipping date from": fieldToChange = desiredShippingDateFromField;   break;
            case "create date to": fieldToChange = createDateToField;   break;
            case "desired shipping date to":  fieldToChange = desiredShippingDateToField;   break;
            default:
                Assert.fail("Field not found: " + fieldName);
                return;
        }
        sharedActions.waitForVisible(fieldToChange);
        sharedActions.sendKeys(fieldToChange, value);

    }



    public void selectValuesFromDropdown(String dropdownId, String panelId, List<String> valuesToSelect) {

        WebElement dropdown = driver.findElement(By.id(dropdownId));

        openDropDownIfClosed(dropdown);

        WebElement dropdownPanel = driver.findElement(By.id(panelId));

        List<WebElement> options = dropdownPanel.findElements(By.xpath(".//mat-option"));
        for (var option: options)
        {
            sharedActions.waitForVisible(option);
        }
        for (String value : valuesToSelect) {


            options.stream()
                .filter(option -> option.getText().trim().equalsIgnoreCase(value))
                .findFirst()
                .ifPresent(element -> {
                    sharedActions.click(element);
                    sharedActions.waitForAttribute(element, "aria-selected", "true");
                });

            log.info("Selected value: {}", value);
        }
        sharedActions.sendKeys(dropdown, Keys.ESCAPE.toString());
        closeDropdownIfOpen(dropdown);
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
        assertTrue("Order field should be displayed", orderNumberField.isDisplayed());
    }

    public void theOrderStatusFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(orderStatusField);
        assertTrue("OrderStatus field should be displayed",orderStatusField.isDisplayed());
    }

    public void theOrderPrioritiesFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(orderPrioritiesField);
        assertTrue("OrderPriorities field should be displayed",orderPrioritiesField.isDisplayed());
    }

    public void theCustomersFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(customersField);
        assertTrue("Customers field should be displayed",customersField.isDisplayed());
    }

    public void theCreateDateFromFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(createDateFromField);
        assertTrue("CreateDateFrom field should be displayed",createDateFromField.isDisplayed());
    }

    public void theCreateDateToFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(createDateToField);
        assertTrue("CreateDateTo field should be displayed",createDateToField.isDisplayed());
    }

    public void theDesiredShipmentDateFromFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateFromField);
        assertTrue("DesiredShipmentDateFrom field should be displayed",desiredShippingDateFromField.isDisplayed());
    }

    public void theDesiredShipmentDateToFieldIsDisplayed() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateToField);
        assertTrue("DesiredShipmentDateTo field should be displayed",desiredShippingDateToField.isDisplayed());
    }

    public void theOrderFieldIsRequiredField() {
        sharedActions.waitForVisible(orderNumberField);
        assertIsRequiredField("Order field should be required", orderNumberField);
    }

    public void theCreateDateIsRequiredField() {
        sharedActions.waitForVisible(createDateFromField);
        createDateFromField.click();
        createDateToField.click();
        assertContainsFieldAsRequired("Create Date");
    }

    public void theCreateDateFromIsRequiredField() {
        sharedActions.waitForVisible(createDateFromField);
        assertIsRequiredField("CreateDateFrom field should be required", createDateFromField);
    }

    public void theCreateDateToIsRequiredField() {
        sharedActions.waitForVisible(createDateToField);
       assertIsRequiredField("CreateDateTo field should be required", createDateToField);
    }

    public void theOrderFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        assertFalse("Order field should be disabled",orderNumberField.isEnabled());
    }

    public void theOrderFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(orderNumberField);
        assertTrue("Order field should be enabled",orderNumberField.isEnabled());
    }

    public void theOrderStatusFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(orderStatusField);
        assertIsAriaDisabled("OrderStatus field should be disabled", orderStatusField);
    }

    public void theOrderStatusFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(orderStatusField);
        assertTrue("OrderStatus field should be enabled",orderStatusField.isEnabled());
    }

    public void theOrderPrioritiesFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(orderPrioritiesField);
        assertIsAriaDisabled("OrderPriorities field should be disabled", orderPrioritiesField);
    }

    public void theOrderPrioritiesFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(orderPrioritiesField);
        assertTrue("OrderPriorities field should be enabled",orderPrioritiesField.isEnabled());
    }

    public void theCustomersFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(customersField);
        assertIsAriaDisabled("Customers field should be disabled", customersField);
    }

    public void theCustomersFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(customersField);
        assertTrue("Customers field should be enabled",customersField.isEnabled());
    }

    public void theCreateDateFromFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(createDateFromField);
        assertFalse("CreateDateFrom field should be disabled",createDateFromField.isEnabled());
    }

    public void theCreateDateFromFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(createDateFromField);
        assertTrue("CreateDateFrom field should be enabled",createDateFromField.isEnabled());
    }

    public void theCreateDateToFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(createDateToField);
        assertFalse("CreateDateTo field should be disabled",createDateToField.isEnabled());
    }

    public void theCreateDateToFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(createDateToField);
        assertTrue("CreateDateTo field should be enabled",createDateToField.isEnabled());
    }

    public void theDesiredShippingDateFromFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateFromField);
        assertFalse("DesiredShippingDateFrom field should be disabled",desiredShippingDateFromField.isEnabled());
    }

    public void theDesiredShippingDateFromFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateFromField);
        assertTrue("DesiredShippingDateFrom field should be enabled",desiredShippingDateFromField.isEnabled());
    }

    public void theDesiredShippingDateToFieldIsDisabled() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateToField);
        assertFalse("DesiredShippingDateTo field should be disabled",desiredShippingDateToField.isEnabled());
    }

    public void theDesiredShippingDateToFieldIsEnabled() throws InterruptedException {
        sharedActions.waitForVisible(desiredShippingDateToField);
        assertTrue("DesiredShippingDateTo field should be enabled",desiredShippingDateToField.isEnabled());
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

    public void assertContainsFieldAsRequired(String fieldLabel) {
        WebElement labelElement = driver.findElement(By.xpath(labelXpath(fieldLabel)));
        WebElement asteriskSpanElement = labelElement.findElement(
            By.xpath("span[contains(@class, 'mat-mdc-form-field-required-marker')]"));
        assertTrue(
            "Label '%s' not visible in page".formatted(fieldLabel),
            labelElement.isDisplayed()
        );
        assertTrue(
            "Label '%s' has no visible 'asterisk' (required marker)".formatted(fieldLabel),
            asteriskSpanElement.isDisplayed()
        );
    }

//    public void closeDropdownIfOpen(WebElement dropdown) {
//        if ("true".equals(dropdown.getAttribute("aria-expanded"))) {
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript("arguments[0].setAttribute('aria-expanded', 'false');", dropdown);
//            log.info("Closed dropdown using JavaScript");
//        }
//    }

    public void iShouldSeeAValidationMessage(String message) {
        assertTrue(
            "%s".formatted(message),
            driver.getPageSource().contains("%s".formatted(message))
        );
    }

    public void theOrderNumberFieldShouldHaveEmptyValue() {
        theFieldShouldHaveEmptyValue(orderNumberField);
    }

    public void theFieldShouldHaveEmptyValue(WebElement fieldInput) {
        assertTrue("The field is Empty", fieldInput.getAttribute("value").isEmpty());
    }

    public void checkForFieldsVisibility(String valueFields) {
        Arrays.stream(valueFields.split(",")).map(String::trim).forEach
            (valueField -> {
                switch (valueField) {
                    case "create date from":
                        try {
                            theCreateDateFromFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "create date to":
                        try {
                            theCreateDateToFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "desired shipment date from":
                        try {
                            theDesiredShipmentDateFromFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "desired shipment date to":
                        try {
                            theDesiredShipmentDateToFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "order status":
                        try {
                            theOrderStatusFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "priority":
                        try {
                            theOrderPrioritiesFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "ship to customer":
                        try {
                            theCustomersFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    case "order number":
                        try {
                            theOrderFieldIsDisplayed();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        Assert.fail("Field not found: " + valueField);
                }
            });
    }
}
