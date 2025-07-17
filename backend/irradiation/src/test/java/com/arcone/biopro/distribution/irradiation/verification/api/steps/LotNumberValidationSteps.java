package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class LotNumberValidationSteps {

    private final GraphQlHelper graphQlHelper;

    private String lotNumber;
    private String type;
    private Boolean validationResult;
    private String notificationMessage;

    @Given("I have a valid lot number {string} and supply type {string}")
    public void iHaveAValidLotNumberAndSupplyType(String lotNumber, String type) {
        this.lotNumber = lotNumber;
        this.type = type;
    }

    @Given("I have a lot number {string} and supply type {string}")
    public void iHaveALotNumberAndSupplyType(String lotNumber, String type) {
        this.lotNumber = lotNumber;
        this.type = type;
    }

    @When("I call the supply service to validate the lot number")
    public void iCallTheSupplyServiceToValidateTheLotNumber() {
        Map<String, Object> variables = Map.of(
            "lotNumber", lotNumber,
            "type", type
        );
        try {
            this.validationResult = graphQlHelper.executeQuery("validateLotNumber", variables, "validateLotNumber", Boolean.class).getData();
            this.notificationMessage = validationResult ? "Validation success" : "Lot number validation failed";
        } catch (Exception e) {
            this.validationResult = false;
            this.notificationMessage = "Error: " + e.getMessage();
        }
    }

    @Then("the supply service should return true")
    public void theSupplyServiceShouldReturnTrue() {
        assertThat(validationResult).isTrue();
    }

    @Then("the supply service should return false")
    public void theSupplyServiceShouldReturnFalse() {
        assertThat(validationResult).isFalse();
    }

    @Then("I should receive a notification message {string}")
    public void iShouldReceiveANotificationMessage(String expectedMessage) {
        assertThat(notificationMessage).isEqualTo(expectedMessage);
    }

    @Then("I should receive an error message {string}")
    public void iShouldReceiveAnErrorMessage(String expectedMessage) {
        assertThat(notificationMessage).isEqualTo(expectedMessage);
    }
}
