package com.arcone.biopro.distribution.shippingservice.verification.steps.orderSteps;

import com.arcone.biopro.distribution.shippingservice.verification.support.Controllers.OrderTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ListOrdersResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.OrderDetailsResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OrderFulfilmentSteps {

    private EntityExchangeResult<String> result;
    private List<ListOrdersResponseType> orders;
    private OrderDetailsResponseType order;
    private long orderNumber;

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

    @Then("The shipment request will be available in the Distribution local data store and I can fill the order.")
    public void verifyOrderPersistence() throws Exception {
        // Getting the order
        this.result = orderTestingController.getOrderDetails(this.orderNumber);
        this.order = orderTestingController.parseOrderDetail(result);

        assertNotNull(this.order, "Failed to get order fulfillment details.");
        assertEquals(this.orderNumber, this.order.getOrderNumber(), "Failed to get order by number.");

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

    @When("I retrieve one shipment by order number.")
    public void retrieveOneOrder() throws Exception {
        this.result = orderTestingController.getOrderDetails(this.orderNumber);
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
            default:
                fail("Invalid attribute.");
        }
    }

    @And("The attribute {string} is not empty.")
    public void attributeNotEmpty(String attribute) {
        log.info("Asserting if the attribute {} is not empty.", attribute);
        var orderItems = this.order.getItems();
        switch (attribute) {
            case "Product Family":
                assertTrue(orderItems.stream().noneMatch(item -> item.getProductFamily().describeConstable().isEmpty()), "Failed to find product family.");
                break;
            case "Blood Type":
                assertTrue(orderItems.stream().noneMatch(item -> item.getBloodType().name().describeConstable().isEmpty()), "Failed to find blood type.");
                break;
            case "Product Quantity":
                assertTrue(orderItems.stream().noneMatch(item -> item.getQuantity().describeConstable().isEmpty()), "Failed to find quantity.");
                break;
            case "Order Number":
                assertTrue(orderItems.stream().noneMatch(item -> item.getOrderId().describeConstable().isEmpty()), "Failed to find order number.");
                break;
            default:
                fail("Invalid attribute.");
        }
    }
}
