package com.arcone.biopro.distribution.shipping.verification.steps;

import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
public class CommonSteps {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    SharedContext context;

    @Then("I should see a {string} message: {string}.deprecated")
    public void iShouldSeeAMessageAndClose(String header, String message) throws InterruptedException {
        log.info("I should see a message: {}", message);
        sharedActions.verifyMessage(header, message);
        sharedActions.closeAcknowledgment();
    }

    @Then("I should see a {string} message: {string}.")
    public void iShouldSeeAMessage(String header, String message) throws InterruptedException {
        log.info("I should see a message: {}", message);
        sharedActions.verifyMessage(header, message);
        sharedActions.closeAcknowledgment();
    }

    @When("I confirm the acknowledgment message.")
    public void iConfirmTheAcknowledgmentMessage() {
        sharedActions.confirmAcknowledgment();
    }

    @When("I close the acknowledgment message.")
    public void iCloseTheAcknowledgmentMessage() {
        sharedActions.closeAcknowledgment();
    }

    @Then("I should receive a {string} message response {string}.")
    public void iShouldReceiveAMessage(String messageType, String message) {
        // Step to verify the notifications response from the last API call. This is a common step that can be reused in other scenarios.
        // It's important to set the context variable to the response of the last API call so that the notifications can be verified.

        var notification = context.getApiMessageResponse().stream().filter(x -> x.get("notificationType").equals(messageType)).findAny().orElse(null);
        assertNotNull(notification, "Failed to find the notification.");
        assertEquals(message, notification.get("message"), "Failed to find the message.");
        log.debug("Notification found: {}", notification);
    }

    @And("I should receive the product ineligible type {string} with message {string}")
    public void iShouldReceiveTheProductIneligibleTypeWithMessage(String errorType, String errorMessage) {
        log.debug("Result Response {}", context.getApiMessageResultResponse());

        var inventoryNotificationResponse = context.getApiMessageResultResponse().stream().filter(x -> x.containsKey("inventoryNotificationsDTO")).findAny().orElse(null);
        assertNotNull(inventoryNotificationResponse, "Failed to find the notification.");

        var inventoryNotificationsDTO = ((ArrayList<Map>)inventoryNotificationResponse.get("inventoryNotificationsDTO")).getFirst();
        assertNotNull(inventoryNotificationsDTO, "Failed to find the notification.");


        assertEquals(errorMessage, inventoryNotificationsDTO.get("errorMessage"), "Failed to find the message.");
        assertEquals(errorType, inventoryNotificationsDTO.get("errorName"), "Failed to find the error name.");

    }
}
