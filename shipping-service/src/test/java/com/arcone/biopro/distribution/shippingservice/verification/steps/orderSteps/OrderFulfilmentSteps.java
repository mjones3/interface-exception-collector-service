package com.arcone.biopro.distribution.shippingservice.verification.steps.orderSteps;

import com.arcone.biopro.distribution.shippingservice.verification.support.Endpoints;
import com.arcone.biopro.distribution.shippingservice.verification.support.payloads.OrderFulfillRequestPayload;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

public class OrderFulfilmentSteps {

    private EntityExchangeResult<String> result;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderFulfillRequestPayload fulfillPayload;


    @Given("I have no order fulfillment requests.")
    public void noOrderFulfillmentRequest() {
        // No action needed here as the order fulfillment does not exist yet.
//        add get
        System.out.println("No order fulfillment was found.");
    }

    @When("I receive an order fulfillment request event.")
    public void receiveFulfillmentOrderRequest() {
        // Kafka event will be sent here. To be implemented.
        System.out.println("Order fulfillment request sent (mock).");
    }

    @Then("The order request will be available in the Distribution local data store and I can fill the order.")
    public void verifyOrderPersistence() {
        result = webTestClient.get()
            .uri(Endpoints.LIST_ORDER)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
        // Asserting that the order fulfill request list is not empty.
        assert(!Objects.requireNonNull(result.getResponseBody()).isEmpty());
    }
}
