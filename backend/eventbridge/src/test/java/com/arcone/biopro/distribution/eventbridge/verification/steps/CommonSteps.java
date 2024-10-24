package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.verification.pages.SharedActions;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CommonSteps {

    @Autowired
    private SharedActions sharedActions;

    @Then("I should see a {string} message: {string}.")
    public void iShouldSeeAMessage(String header, String message) {
        log.info("I should see a message: {}", message);
        sharedActions.verifyMessage(header, message);
    }

}
