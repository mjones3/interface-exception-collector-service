package com.arcone.biopro.distribution.irradiation.verification.ui.steps.modals;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.ConfirmationModalPage;
import io.cucumber.java.en.And;
import lombok.RequiredArgsConstructor;

import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor
public class confirmationMessageSteps {

    private final ConfirmationModalPage confirmationModalPage;

    @And("I see the confirmation message with title {string} and message {string}")
    public void iSeeTheConfirmationMessageWithTitleAndMessage(String title, String message) {
        confirmationModalPage.waitForConfirmationModal();
        if (!confirmationModalPage.getConfirmationTitleText().contains(title)) {
            fail("Incorrect expected title. Expected title: " + title + ". Actual title: " + confirmationModalPage.getConfirmationTitleText());
        }
        if (!confirmationModalPage.getConfirmationMessageText().contains(message)) {
            fail("Incorrect expected message. Expected message: " + message + ". Actual message: " + confirmationModalPage.getConfirmationMessageText());
        }

    }

    @And("I confirm the confirmation message")
    public void iConfirmTheConfirmationMessage() {
        confirmationModalPage.waitForConfirmationModal();
        confirmationModalPage.clickConfirmButton();
    }
}
