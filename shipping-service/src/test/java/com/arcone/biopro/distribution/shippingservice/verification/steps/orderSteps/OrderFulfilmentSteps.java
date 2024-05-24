package com.arcone.biopro.distribution.shippingservice.verification.steps.orderSteps;

import com.arcone.biopro.distribution.shippingservice.verification.support.Controllers.OrderTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ListOrdersResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.OrderDetailsResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.OrderItemShortDateResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class OrderFulfilmentSteps {

    private EntityExchangeResult<String> result;
    private List<ListOrdersResponseType> orders;
    private OrderDetailsResponseType order;
    private long orderNumber;

    private long shipmentId;

    @Autowired
    private OrderTestingController orderTestingController;

    @Given("I have no shipment fulfillment requests.")
    public void noOrderFulfillmentRequest() throws Exception {
        // No action needed. Just logging the initial number of orders.
        this.result = orderTestingController.listOrders();
        var size = orderTestingController.parseOrderList(result).size();
        log.info("Initial order count: {}", size);
    }

    @When("I receive a shipment fulfillment request event.")
    public void receiveFulfillmentOrderRequest() throws Exception {
        this.orderNumber = orderTestingController.createOrderRequest();
    }

    @Then("The shipment request will be available in the Distribution local data store and I can fill the shipment.")
    public void verifyOrderPersistence() throws Exception {
        // Getting the order
        this.orders = orderTestingController.parseOrderList(orderTestingController.listOrders());
        setShipmentId();

        this.result = orderTestingController.getOrderDetails(this.shipmentId);
        this.order = orderTestingController.parseOrderDetail(result);

        assertNotNull(this.order, "Failed to get order fulfillment details.");
        assertEquals(this.orderNumber, this.order.getOrderNumber(), "Failed to get order by number.");

    }

    private void setShipmentId() {
        var orderFilter =  this.orders.stream().filter(x -> x.getOrderNumber().equals(this.orderNumber)).findAny().orElse(null);
        if(orderFilter != null){
            this.shipmentId = orderFilter.getId();
            log.info("Found Shipment by Order Number");
        }
    }

    @Given("I have a shipment request persisted.")
    public void orderRequestPersisted() throws Exception {
        // Add an order request.
        this.orderNumber = orderTestingController.createOrderRequest();
    }

    @When("I retrieve the shipment list.")
    public void retrieveOrderPendingList() throws Exception {
        this.result = orderTestingController.listOrders();
        this.orders = orderTestingController.parseOrderList(result);
        log.info("Order list: {}", this.orders);

        assertEquals(200, result.getStatus().value(), "Failed to get order fulfillment requests.");
    }

    @Then("I am able to see the requests.")
    public void verifyOrderPendingList() {
        assertFalse(this.orders.isEmpty(), "The order pending list is empty.");
        log.info("Order list is not empty. Total orders: {}", this.orders.size());
    }

    @When("I retrieve one shipment by shipment id.")
    public void retrieveOneOrder() throws Exception {
        setShipmentId();
        this.result = orderTestingController.getOrderDetails(this.shipmentId);
        this.order = orderTestingController.parseOrderDetail(result);

        assertEquals(200, result.getStatus().value(), "Failed to get order fulfillment requests.");
    }

    @Then("I am able to view the shipment fulfillment details.")
    public void verifyOrderDetails() {
        log.info("Asserting if the order is not null.");
        assertNotNull(this.order, "Failed to get order fulfillment details.");
    }

    @And("The attribute {string} contains {string}.")
    public void attributeContains(String attribute, String value) {
        log.info("Asserting if the attribute {} contains {}.", attribute, value);
        var orderItems = this.order.getItems();
        switch (attribute) {
            case "Product Family":
                assertTrue(orderItems.stream().anyMatch(item -> item.getProductFamily().equals(value)), "Failed to find product family.");
                log.info("Product family found: {}", value);
                break;
            case "Blood Type":
                assertTrue(orderItems.stream().anyMatch(item -> item.getBloodType().name().equals(value)), "Failed to find blood type.");
                log.info("Blood type found: {}", value);
                break;
            case "Product Quantity":
                assertTrue(orderItems.stream().anyMatch(item -> item.getQuantity().equals(Integer.parseInt(value))), "Failed to find quantity.");
                log.info("Quantity found: {}", value);
                break;
            case "Unit Number":
                orderItems.stream().forEach(item -> childAttributeValueMatch("Unit Number",value,item.getShortDateProducts()));
                break;
            case "Product Code":
                orderItems.stream().forEach(item -> childAttributeValueMatch("Product Code",value,item.getShortDateProducts()));
                break;
            default:
                fail("Invalid attribute.");
        }
    }

    @And("The attribute {string} is not empty.")
    public void attributeNotEmpty(String attribute) {
        log.info("Asserting if the attribute {} is not empty.", attribute);
        var orderItems = this.order.getItems();
        switch (attribute) {
            case "Order Number":
                assertFalse(this.order.getOrderNumber().describeConstable().isEmpty(), "Failed to find Order Number.");
                break;
            case "Product Family":
                assertTrue(orderItems.stream().noneMatch(item -> item.getProductFamily().describeConstable().isEmpty()), "Failed to find product family.");
                break;
            case "Blood Type":
                assertTrue(orderItems.stream().noneMatch(item -> item.getBloodType().name().describeConstable().isEmpty()), "Failed to find blood type.");
                break;
            case "Product Quantity":
                assertTrue(orderItems.stream().noneMatch(item -> item.getQuantity().describeConstable().isEmpty()), "Failed to find quantity.");
                break;
            case "Shipment id":
                assertTrue(orderItems.stream().noneMatch(item -> item.getShipmentId().describeConstable().isEmpty()), "Failed to find shipment id.");
                break;
            default:
                fail("Invalid attribute.");
        }
    }

    public void childAttributeValueMatch(String attribute , String valueMatch, List<OrderItemShortDateResponseType> items){
        if(!valueMatch.equals("") && !valueMatch.isEmpty() && items != null && !items.isEmpty()){
            switch (attribute) {
                case "Unit Number":
                    log.info("Comparing Unit : {}" , valueMatch);
                    assertTrue(items.stream().anyMatch(x -> x.getUnitNumber().equals(valueMatch)),"Failed to find Unit Number.");
                    break;
                case "Product Code":
                    log.info("Product Code : {}" , valueMatch);
                    assertTrue(items.stream().anyMatch(x -> x.getProductCode().equals(valueMatch)),"Failed to find Product Code.");
                    break;

            }
        }
    }
}
