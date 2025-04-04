package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.FilterShipmentsController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class FilterShipmentsSteps {
    @Autowired
    private FilterShipmentsController filterShipmentsController;

    @Autowired
    private SharedContext context;

    @Given("I requested the list of shipments filtering by {string} as {string}.")
    public void iRequestedTheListOfShipmentsFilteringByAs(String filter, String value) {
        value = value.replace("<today>", LocalDate.now().toString());
        value = value.replace("<tomorrow>", LocalDate.now().plusDays(1).toString());
        if (value.equals("<currentShipmentNumber>")) {
            value = context.getShipmentCreateResponse().get("shipmentNumber").toString();
        }


        if (filter.equals("locationCodeList")) {
            filterShipmentsController.filterShipmentsByLocationList(value);
        } else {
            filterShipmentsController.filterShipmentsByLocationListAndCustomAttribute(context.getLocationCode(), filter, value);
        }
    }

    @Then("The list shipment response should contains {string} items.")
    public void theListShipmentResponseShouldContainsItems(String expectedQuantity) {
        Assert.assertEquals(Integer.parseInt(expectedQuantity),context.getApiShipmentListResponse().size());
    }

    @When("I receive the shipment list response.")
    public void iReceiveTheShipmentListResponse() {
        // Empty step on purpose
    }
}
