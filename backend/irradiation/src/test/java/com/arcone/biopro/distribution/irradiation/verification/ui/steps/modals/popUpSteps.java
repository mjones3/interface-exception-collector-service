package com.arcone.biopro.distribution.irradiation.verification.ui.steps.modals;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.PopUpPage;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class popUpSteps {

    @Autowired
    private PopUpPage popUpPage;

    @Then("I see the {string} message {string}")
    public void iSeeTheMessage(String messageType, String messageSubstring) {
        popUpPage.waitForPopUp();
        assertEquals("Incorrect expected message type. Expected type: " + messageType + " Actual type: " + popUpPage.getPopUpTitle().getText(), messageType.toLowerCase(), popUpPage.getPopUpTitle().getText().toLowerCase());
        if (!popUpPage.getPopUpMessage().getText().contains(messageSubstring)) {
            fail("Incorrect expected message substring. Expected substring: " + messageSubstring + ". Actual message: " + popUpPage.getPopUpMessage().getText());
        }
        popUpPage.closePopUp();
    }
}
