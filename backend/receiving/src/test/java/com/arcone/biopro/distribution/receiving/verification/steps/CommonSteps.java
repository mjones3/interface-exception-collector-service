package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.pages.SharedActions;
import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
public class CommonSteps {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    SharedContext context;

    @Then("I should see a {string} message: {string}.")
    public void iShouldSeeAMessage(String header, String message) throws InterruptedException {
        log.info("I should see a message: {}", message);
        sharedActions.verifyMessage(header, message);
        sharedActions.closeAcknowledgment();
    }

    @Then("I should receive a {string} message response {string}.")
    public void iShouldReceiveAMessage(String messageType, String message) {
        // Step to verify the notifications response from the last API call. This is a common step that can be reused in other scenarios.
        // It's important to set the context variable to the response of the last API call so that the notifications can be verified.

        var notification = context.getApiMessageResponse().stream().filter(x -> x.get("type").equals(messageType.toUpperCase())).findAny().orElse(null);
        if (notification == null) {
            iShouldReceiveAResponseErrorMessage(messageType, message);
        } else {
            Assert.assertEquals(message, notification.get("message"));
            log.debug("Notification found: {}", notification);
        }
    }

    @Then("I should receive a {string} error message response {string}.")
    public void iShouldReceiveAResponseErrorMessage(String messageType, String message) {
        // Step to verify the notifications response from the last API call. This is a common step that can be reused in other scenarios.
        // It's important to set the context variable to the response of the last API call so that the notifications can be verified.

        var error = context.getApiErrorResponse();
        assertNotNull(error, "Failed to find the notification.");
        log.debug("Error found: {}", error);

        Assert.assertEquals(messageType, error.get("classification"));

        Assertions.assertTrue(context.getApiErrorResponse().get("message").toString().contains(message));


    }


    @And("The user location is {string}.")
    public void theUserLocationIs(String location) {
        context.setLocationCode(location);
    }

    @And("I {string} see a {string} alert: {string}.")
    public void iSeeAMessage(String shouldShouldNot, String header, String message) throws InterruptedException {

        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            sharedActions.verifyAlert(header, message, true);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            sharedActions.verifyAlert(header, message, false);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }
}
