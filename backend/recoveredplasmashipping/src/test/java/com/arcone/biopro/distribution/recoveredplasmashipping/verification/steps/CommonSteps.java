package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class CommonSteps {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    SharedContext context;

    @Then("I should see a {string} message: {string}.")
    public void iShouldSeeAMessage(String header, String message) throws InterruptedException {
        log.info("I should see a message: {}", message);
        sharedActions.verifyMessage(header, message);
    }

    @Then("I should receive a {string} message response {string}.")
    public void iShouldReceiveAMessage(String messageType, String message) {
        // Step to verify the notifications response from the last API call. This is a common step that can be reused in other scenarios.
        // It's important to set the context variable to the response of the last API call so that the notifications can be verified.

        var notification = context.getApiListMessageResponse().stream().filter(x -> x.get("type").equals(messageType.toUpperCase())).findAny().orElse(null);
        assertNotNull(notification, "Failed to find the notification.");
        assertEquals(message, notification.get("message"), "Failed to find the message.");
        log.debug("Notification found: {}", notification);
    }

    @Then("I should receive a {string} error message response {string}.")
    public void iShouldReceiveAResponseErrorMessage(String messageType, String message) {
        // Step to verify the notifications response from the last API call. This is a common step that can be reused in other scenarios.
        // It's important to set the context variable to the response of the last API call so that the notifications can be verified.

        var error = context.getApiErrorResponse();
        assertNotNull(error, "Failed to find the notification.");
        log.debug("Error found: {}", error);

        assertEquals(messageType, error.get("classification"), "Failed to find the message.");

        assertEquals(message, error.get("message"), "Failed to find the message.");


    }

    @And("The user location is {string}.")
    public void theUserLocationIs(String location) {
        context.setLocationCode(location);
    }

    @When("I close the acknowledgment message.")
    public void iCloseTheAcknowledgmentMessage() {
        sharedActions.closeAcknowledgment();
    }

}
