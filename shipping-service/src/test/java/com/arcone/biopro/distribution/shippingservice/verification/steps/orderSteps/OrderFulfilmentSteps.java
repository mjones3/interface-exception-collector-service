package com.arcone.biopro.distribution.shippingservice.verification.steps.orderSteps;

import com.arcone.biopro.distribution.shippingservice.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shippingservice.verification.support.Endpoints;
import com.arcone.biopro.distribution.shippingservice.verification.support.payloads.OrderFulfillRequestPayload;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFulfilmentSteps {

    private EntityExchangeResult<String> result;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private OrderFulfillRequestPayload fulfillPayload;


    @Given("I have no order fulfillment requests.")
    public void noOrderFulfillmentRequest() {
        result = apiHelper.getRequest(Endpoints.LIST_ORDER);
        assertEquals(200, result.getStatus().value(), "Failed to get order fulfillment requests.");
        assertEquals("[]", Objects.requireNonNull(result.getResponseBody()).trim(), "Order fulfillment requests are not empty.");
    }

    @When("I receive an order fulfillment request event.")
    public void receiveFulfillmentOrderRequest() {
        // Kafka event will be sent here. To be implemented.
    }

    @Then("The order request will be available in the Distribution local data store and I can fill the order.")
    public void verifyOrderPersistence() {
        // Getting the order fulfillment request list.
        result = apiHelper.getRequest(Endpoints.LIST_ORDER);
        assertEquals(200, result.getStatus().value(), "Failed to get order fulfillment requests.");

        // assert that the order fulfillment request list is not empty.
        assertFalse(Objects.requireNonNull(result.getResponseBody()).isEmpty(), "Order fulfillment requests are empty.");
    }
}
