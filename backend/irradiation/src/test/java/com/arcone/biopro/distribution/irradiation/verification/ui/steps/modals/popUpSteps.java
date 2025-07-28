package com.arcone.biopro.distribution.irradiation.verification.ui.steps.modals;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.PopUpPage;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class popUpSteps {

    @Autowired
    private PopUpPage popUpPage;

    @Then("I see the {string} message {string}")
    public void iSeeTheMessage(String messageType, String messageSubstring) {
        assertTrue(popUpPage.waitForSpecificPopUpMessage(messageType, messageSubstring, 10), "Couldn't find the message: " + messageType + "-> " + messageSubstring);
    }
}
