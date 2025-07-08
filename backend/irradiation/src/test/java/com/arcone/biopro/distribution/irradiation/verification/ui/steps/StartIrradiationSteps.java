package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.StartIrradiationPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

@AllArgsConstructor
public class StartIrradiationSteps {
    private final StartIrradiationPage startIrradiationPage;

    @And("I verify that the {string} field is {string}")
    public void iVerifyThatTheFieldIs(String fieldName, String expectedStatus) {
        boolean isEnabled = startIrradiationPage.inputFieldIsEnabled(fieldName);

        boolean expectedEnabled;
        if (expectedStatus.equalsIgnoreCase("enabled")) {
            expectedEnabled = true;
        } else if (expectedStatus.equalsIgnoreCase("disabled")) {
            expectedEnabled = false;
        } else {
            throw new IllegalArgumentException("Invalid expected status: " + expectedStatus + ". Use 'enabled' or 'disabled'.");
        }

        if (expectedEnabled) {
            Assert.assertTrue("Expected the '" + fieldName + "' field to be enabled, but it was disabled.", isEnabled);
        } else {
            Assert.assertFalse("Expected the '" + fieldName + "' field to be disabled, but it was enabled.", isEnabled);
        }
    }

    @When("I scan the irradiator id {string}")
    public void iScanTheIrradiatorId(String irradiatorDeviceId) {
        startIrradiationPage.scanIrradiatorDeviceId(irradiatorDeviceId);
    }

    @When("I scan the lot number {string}")
    public void iScanTheLotNumber(String lotnumber) {
        startIrradiationPage.scanLotNumber(lotnumber);
    }

    @When("I scan the unit number {string} in the irradiation page")
    public void iScanTheUnitNumberInTheIrradiationPage(String unitNumber) {
        startIrradiationPage.scanUnitNumber(unitNumber);
    }

}
