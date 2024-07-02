package com.arcone.biopro.distribution.shippingservice.verification.steps.shipment;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.FillProductsPage;
import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shippingservice.verification.support.StaticValuesMapper;
import com.arcone.biopro.distribution.shippingservice.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.types.ListShipmentsResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.types.ShipmentRequestDetailsResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    private FillProductsPage fillProductsPage;

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
    }

    @And("I define visual inspection as {string}.")
    public void defineVisualInspection(String visualInspection) {
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
        log.info("stop");
    }
}

