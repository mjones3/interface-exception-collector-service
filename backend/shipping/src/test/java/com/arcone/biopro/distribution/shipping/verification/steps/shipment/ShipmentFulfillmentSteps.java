package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.FillProductsPage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shipping.verification.support.StaticValuesMapper;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shipping.verification.support.types.ListShipmentsResponseType;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentRequestDetailsResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
public class ShipmentFulfillmentSteps {

    private List<ListShipmentsResponseType> result;

    private Map resultMap;

    private List<ListShipmentsResponseType> orders;
    private ShipmentRequestDetailsResponseType order;
    private long orderNumber;

    private long shipmentId;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    @Autowired
    private HomePage homePage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;


    @Autowired
    private FillProductsPage fillProductsPage;

    private ShipmentRequestDetailsResponseType shipmentDetailType;

    private String unitNumber;
    private String checkDigit;
    private String productCode;

    private ShipmentRequestDetailsResponseType setupOrderFulfillmentRequest(String orderNumber, String customerId, String customerName, String quantities, String bloodTypes
        , String productFamilies, String unitNumbers, String productCodes) {
        return shipmentTestingController.buildShipmentRequestDetailsResponseType(Long.valueOf(orderNumber),
            "ASAP",
            "OPEN",
            customerId,
            0L,
            "123456789",
            "TEST",
            "TEST",
            "Frozen",
            LocalDate.now(),
            "Blood Bank",
            customerName,
            "",
            "3056778756",
            "FL",
            "33016",
            "US",
            "1",
            "Miami",
            "Miami-Dade",
            "36544 SW 27th St",
            "North Miami",
            quantities,
            bloodTypes,
            productFamilies, unitNumbers, productCodes);
    }

    private void goToDetailsPage(long orderNumber) throws Exception {
        Long shipmentId = shipmentTestingController.getOrderShipmentId(orderNumber);
        homePage.goTo();
        this.shipmentDetailPage.goTo(shipmentId);
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }


    @Given("I have no shipment fulfillment requests.")
    public void noOrderFulfillmentRequest() throws Exception {
        // No action needed. Just logging the initial number of orders.
        this.result = shipmentTestingController.listShipments();
        var size = result.size();
        log.info("Initial order count: {}", size);
    }

    @When("I receive a shipment fulfillment request event.")
    public void receiveFulfillmentOrderRequest() throws Exception {
        this.orderNumber = shipmentTestingController.createShippingRequest();
    }

    @Then("The shipment request will be available in the Distribution local data store and I can fill the shipment.")
    public void verifyOrderPersistence() throws Exception {
        // Getting the order
        this.orders = shipmentTestingController.listShipments();
        setShipmentId();
        this.order = shipmentTestingController.parseShipmentRequestDetail(shipmentTestingController.getShipmentRequestDetails(this.shipmentId));

        assertNotNull(this.order, "Failed to get order fulfillment details.");
        assertEquals(this.orderNumber, this.order.getOrderNumber(), "Failed to get order by number.");

    }

    private void setShipmentId() {
        var orderFilter = this.orders.stream().filter(x -> x.getOrderNumber().equals(this.orderNumber)).findAny().orElse(null);
        if (orderFilter != null) {
            this.shipmentId = orderFilter.getId();
            log.info("Found Shipment by Order Number");
        }
    }

    @Given("I have a shipment request persisted.")
    public void orderRequestPersisted() throws Exception {
        // Add an order request.
        this.orderNumber = shipmentTestingController.createShippingRequest();
    }

    @When("I retrieve the shipment list.")
    public void retrieveOrderPendingList() throws Exception {
        this.result = shipmentTestingController.listShipments();
        this.orders = result;
        log.info("Order list: {}", this.orders);

        assertNotNull(result, "Failed to get order fulfillment requests.");
    }

    @Then("I am able to see the requests.")
    public void verifyOrderPendingList() {
        assertFalse(this.orders.isEmpty(), "The order pending list is empty.");
        log.info("Order list is not empty. Total orders: {}", this.orders.size());
    }

    @When("I retrieve one shipment by shipment id.")
    public void retrieveOneOrder() throws Exception {
        setShipmentId();
        this.resultMap = shipmentTestingController.getShipmentRequestDetails(this.shipmentId);
        this.order = shipmentTestingController.parseShipmentRequestDetail(this.resultMap);

        assertNotNull(result, "Failed to get order fulfillment requests.");
    }

    @Then("I am able to view the shipment fulfillment details.")
    public void verifyOrderDetails() {
        log.info("Asserting if the order is not null.");
        assertNotNull(this.order, "Failed to get order fulfillment details.");
    }

    @And("The item attribute {string} contains {string}.")
    public void itemAttributeContains(String attribute, String value) {
        log.info("Asserting if the item attribute {} contains {}.", attribute, value);
        var orderItems = this.order.getItems();
        var attributeKey = new StaticValuesMapper().shipmentItemAttributes().get(attribute);

        // Check if the expected attribute's value is in the order items.
        boolean match = orderItems.stream().anyMatch(item -> {
            try {
                var actualValue = FieldUtils.readDeclaredField(item, attributeKey, true).toString();
                log.info("Item attribute's {} current value: {}", attribute, actualValue);

                return actualValue.equals(value);
            } catch (Exception e) {
                log.debug(e.toString());
            }
            return false;
        });
        assertTrue(match, "Failed to find the product attribute.");
    }

    @And("The short date item attribute {string} contains {string}.")
    public void shortDateItemAttributeContains(String attribute, String value) {
        log.info("Asserting if the short date item attribute {} contains {}.", attribute, value);
        var orderItems = this.order.getItems();
        var attributeKey = new StaticValuesMapper().shipmentItemShortDateAttributes().get(attribute);

        // Check if any of the items has a short date product with the expected attribute's value.
        boolean match = orderItems.stream().anyMatch(item -> {
            try {
                var shortDateProducts = item.getShortDateProducts();
                return shortDateProducts.stream().anyMatch(shortDateProduct -> {
                    try {
                        var actualValue = FieldUtils.readDeclaredField(shortDateProduct, attributeKey, true);
                        log.info("Found a {}: {}", attribute, actualValue);
                        return actualValue.equals(value);
                    } catch (Exception e) {
                        log.debug(e.toString());
                    }
                    return false;
                });
            } catch (Exception e) {
                log.debug(e.toString());
            }
            return false;
        });
        assertTrue(match, "Failed to find the short date product attribute.");
    }

    @And("The item attribute {string} is not empty.")
    public void itemAttributeNotEmpty(String attribute) {
        log.info("Asserting if the item attribute {} is not empty.", attribute);
        var orderItems = this.order.getItems();
        var attributeKey = new StaticValuesMapper().shipmentItemAttributes().get(attribute);

        // Check if the expected attribute is not empty in the order items.
        boolean match = orderItems.stream().anyMatch(item -> {
            try {
                var actualValue = FieldUtils.readDeclaredField(item, attributeKey, true);
                log.info("Attribute {} current value: {}", attribute, actualValue);
                return actualValue != null;
            } catch (Exception e) {
                log.debug(e.toString());
            }
            return false;
        });
        assertTrue(match, "Failed to find the product attribute.");
    }

    @And("The fulfillment request attribute {string} is not empty.")
    public void fulfillmentAttributeNotEmpty(String attribute) {
        log.info("Asserting if the request attribute {} is not empty.", attribute);
        var currentOrder = this.order;
        var attributeKey = new StaticValuesMapper().shipmentFulfillmentRequestAttributes().get(attribute);

        // Check if the expected attribute is not empty in the order.
        try {
            var actualValue = FieldUtils.readDeclaredField(currentOrder, attributeKey, true);
            log.info("Attribute's {} current value: {}", attribute, actualValue);
            assertNotNull(actualValue, "Failed to find the product attribute.");
        } catch (Exception e) {
            log.debug(e.toString());
            fail("Failed to find the product attribute.");
        }
    }

    @And("I choose to fill product of family {string} and blood type {string}.")
    public void iHaveFilledTheShipment(String family, String bloodType) {
        shipmentDetailPage.clickFillProduct(family, bloodType);
    }

    @When("I add the unit {string} with product code {string}.")
    public void addUnitWithProductCode(String unit, String productCode) throws InterruptedException {
        fillProductsPage.addUnitWithProductCode(unit, productCode);
        this.unitNumber = unit;
        this.productCode = productCode;
    }

    @And("I define visual inspection as {string}.")
    public void defineVisualInspection(String visualInspection) throws InterruptedException {
        fillProductsPage.defineVisualInspection(visualInspection);
    }

    @Then("I should see the list of packed products added including {string} and {string}.")
    public void verifyPackedProducts(String family, String bloodType) {
        fillProductsPage.ensureProductIsAdded(family, bloodType);
    }

    @When("I choose to return to the shipment details page.")
    public void returnToShipmentDetails() {
        fillProductsPage.clickBackButton();
    }

    @And("I should not see the unit {string} with product code {string} added to the filled products table.")
    public void verifyProductNotAdded(String unitNumber, String productCode) throws InterruptedException {
        fillProductsPage.ensureProductIsNotAdded(unitNumber, productCode);
    }

    @Given("The shipment details are order Number {string}, customer ID {string}, Customer Name {string}, Product Details: Quantities {string}, Blood Types: {string}, Product Families {string}.")
    public void buildOrderFulfilmentRequest(String orderNumber, String customerId, String customerName
        , String quantities, String bloodTypes, String productFamilies) {

        this.shipmentDetailType = setupOrderFulfillmentRequest(orderNumber, customerId, customerName, quantities, bloodTypes, productFamilies, null, null);

        Assert.assertNotNull(this.shipmentDetailType);

    }

    @And("I have received a shipment fulfillment request with above details.")
    public void triggerOrderFulfillmentEvent() throws Exception {
        this.orderNumber = shipmentTestingController.createShippingRequest(this.shipmentDetailType);
        Assert.assertNotNull(this.orderNumber);
    }

    @And("I am on the Shipment Fulfillment Details page.")
    public void goToShipmentDetailsPage() throws Exception {
        this.goToDetailsPage(this.orderNumber);
    }

    @And("I am on the Shipment Fulfillment Details page for order {int}.")
    public void goToShipmentDetailsPageByOrderNumber(long customOrderNumber) throws Exception {
        this.goToDetailsPage(customOrderNumber);
    }

    @And("I enter the Shipment Fulfillment Details page for order {int}.")
    public void enterShipmentDetailsPageByOrderNumber(long customOrderNumber) throws Exception {
        this.goToDetailsPage(customOrderNumber);
    }

    @When("I type the unit {string}, digit {string}, and product code {string}.")
    public void iTypeTheUnitDigitAndProductCode(String unitNumber, String checkDigit, String productCode) throws InterruptedException {
        boolean checkDigitEnabled = shipmentTestingController.getCheckDigitConfiguration();
        fillProductsPage.addUnitWithDigitAndProductCode(unitNumber, checkDigit, productCode, checkDigitEnabled);
        this.unitNumber = unitNumber;
        this.checkDigit = checkDigit;
        this.productCode = productCode;
    }

    @And("The visual inspection field is {string}.")
    public void theVisualInspectionFieldIs(String status) {
        fillProductsPage.assertVisualInspectionIs(status);
    }

    @Then("I can {string} message {string}.")
    public void iCanMessage(String conditional, String message) {
        if (conditional.contains("not")) { // not
            fillProductsPage.assertCheckDigitErrorIs("");
        } else {
            fillProductsPage.assertCheckDigitErrorIs(message);
        }
    }

    @And("The visual inspection configuration is {string}.")
    public void setVisualInspectionConfig(String status) {
        shipmentTestingController.setVisualInspectionConfiguration(status);
    }

    @And("I define visual inspection as {string}, if needed.")
    public void iDefineVisualInspectionAsIfNeeded(String inspection) throws InterruptedException {
        boolean visualInspectionEnabled = shipmentTestingController.getCheckVisualInspectionConfig();
        if (visualInspectionEnabled) {
            fillProductsPage.defineVisualInspection(inspection);
        } else {
            log.info("Visual inspection is not enabled.");
        }
    }

    @And("I am able to proceed with the product filling process.")
    public void iAmAbleToProceedWithTheProductFillingProcess() {
        boolean visualInspectionEnabled = shipmentTestingController.getCheckVisualInspectionConfig();
        if (visualInspectionEnabled) {
            fillProductsPage.assertVisualInspectionIs("enabled");
        } else {
            fillProductsPage.ensureProductIsAdded(this.unitNumber, this.productCode);
        }
    }

    @When("I type the unit {string}, digit {string}.")
    public void iTypeTheUnitDigit(String unitNumber, String checkDigit) throws InterruptedException {
        fillProductsPage.addUnitWithDigit(unitNumber, checkDigit);
        this.unitNumber = unitNumber;
        this.checkDigit = checkDigit;
    }
}

