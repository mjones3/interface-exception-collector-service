package com.arcone.biopro.distribution.shippingservice.verification.steps.orderSteps;

import com.arcone.biopro.distribution.shippingservice.domain.model.Order;
import com.arcone.biopro.distribution.shippingservice.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shippingservice.verification.support.Endpoints;
import com.arcone.biopro.distribution.shippingservice.verification.support.TestUtils;
import com.arcone.biopro.distribution.shippingservice.verification.support.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

public class OrderFulfilmentSteps {

    private EntityExchangeResult<String> result;
    private int initialOrderCount;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private TestUtils utils;

    @Autowired
    ObjectMapper objectMapper;

    @Given("I have no order fulfillment requests.")
    public void noOrderFulfillmentRequest() throws JsonProcessingException, InterruptedException {

        result = apiHelper.getRequest(Endpoints.LIST_ORDER, null);
        var orders = List.of(objectMapper.readValue(result.getResponseBody(), Order[].class));
        this.initialOrderCount = orders.size();

        System.out.println("Initial order count: " + this.initialOrderCount);
    }

    @When("I receive an order fulfillment request event.")
    public void receiveFulfillmentOrderRequest() throws Exception {
        utils.kafkaSender("order-fulfilled.json", Topics.ORDER_FULFILLED);
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(10000);
    }

    @Then("The order request will be available in the Distribution local data store and I can fill the order.")
    public void verifyOrderPersistence() throws JsonProcessingException, InterruptedException {
        // Getting the order fulfillment request list.
        result = apiHelper.getRequest(Endpoints.LIST_ORDER, null);
        var orders = List.of(objectMapper.readValue(result.getResponseBody(), Order[].class));
        System.out.println("Current order count: " + orders.size());

        assertEquals(200, result.getStatus().value(), "Failed to get order fulfillment requests.");
        assertTrue(orders.size() > this.initialOrderCount, "Failed to persist order fulfillment request.");
    }
}
