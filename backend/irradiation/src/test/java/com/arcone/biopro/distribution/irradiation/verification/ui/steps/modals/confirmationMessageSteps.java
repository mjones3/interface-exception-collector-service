package com.arcone.biopro.distribution.irradiation.verification.ui.steps.modals;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.AcknowledgeModalPage;
import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.ConfirmationModalPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class confirmationMessageSteps {

    @Autowired
    private ConfirmationModalPage confirmationModalPage;

    @Autowired
    private AcknowledgeModalPage acknowledgeModalPage;

    @Then("I see the acknowledge pop-up with message {string}")
    public void iSeeTheAcknowledgePopUpWithMessage(String message) {
        confirmationModalPage.waitForConfirmationModal();
        if (!confirmationModalPage.getConfirmationMessage().getText().contains(message)) {
            fail("Incorrect expected message. Expected message: " + message + ". Actual message: " + confirmationModalPage.getConfirmationMessage().getText());
        }
        confirmationModalPage.closeConfirmationMessage();
    }

    // this step validates the message without closing the modal window
    @Then("I see the modal with message {string}")
    public void iSeeTheModalWithMessage(String message) {
        confirmationModalPage.waitForConfirmationModal();
        if (!confirmationModalPage.getConfirmationMessage().getText().contains(message)) {
            fail("Incorrect expected message. Expected message: " + message + ". Actual message: " + confirmationModalPage.getConfirmationMessage().getText());
        }
    }

    // this step validates the message without closing the modal window in a modal that contains a list of details.
    @Then("I see the modal with message {string} and additional details")
    public void iSeeTheModalWithMessageAndDetails(String message) {
        confirmationModalPage.waitForConfirmationModal();
        if (!confirmationModalPage.getContentHeaderMessage().getText().contains(message)) {
            fail("Incorrect expected message. Expected message: " + message + ". Actual message: " + confirmationModalPage.getConfirmationMessage().getText());
        }
    }


    @Then("I see the following acknowledge message")
    public void iSeeTheFollowingAcknowledgeMessage(DataTable dataTable) {
        confirmationModalPage.waitForConfirmationModal();
        Map<String, String> acknowledgeInfoTable = dataTable.asMap(String.class, String.class);
        String expectedTitle = acknowledgeInfoTable.get("Title");
        String expectedDescription = acknowledgeInfoTable.get("Description");

        if (!acknowledgeModalPage.getAcknowledgeTitle().getText().contains(expectedTitle)) {
            fail("Incorrect title. Expected: " + expectedTitle + ". Actual: " + acknowledgeModalPage.getAcknowledgeTitle().getText());
        }

        if (!acknowledgeModalPage.getAcknowledgeDescription().getText().contains(expectedDescription)) {
            fail("Incorrect description. Expected: " + expectedDescription + ". Actual: " + acknowledgeModalPage.getAcknowledgeDescription().getText());
        }

        if (acknowledgeInfoTable.containsKey("Subtitle")) {
            String expectedSubtitle = acknowledgeInfoTable.get("Subtitle");
            if (!acknowledgeModalPage.getAcknowledgeSubtitle().getText().contains(expectedSubtitle)) {
                fail("Incorrect subtitle. Expected: " + expectedSubtitle + ". Actual: " + acknowledgeModalPage.getAcknowledgeSubtitle().getText());
            }
        }
        if (acknowledgeInfoTable.containsKey("Reasons")) {
            String expectedReasonsFromDataTable = acknowledgeInfoTable.get("Reasons");
            List<String> expectedReasonsList = List.of(expectedReasonsFromDataTable.split(",\\s*"));
            List<String> actualReasons = acknowledgeModalPage.getAcknowledgeDetails();
            assertEquals(expectedReasonsList.size(), actualReasons.size(), "The number of reasons is not equal to the expected number of reasons. Expected: " + expectedReasonsList.size() + ". Actual: " + actualReasons.size());

            for (String expectedReason : expectedReasonsList) {
                if (!actualReasons.contains(expectedReason)) {
                    fail("Incorrect reasons. Expected: " + expectedReasonsList + ". Actual: " + actualReasons);
                }
            }
        }
    }
}
