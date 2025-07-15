package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.StartIrradiationPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;

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
            assertTrue("Expected the '" + fieldName + "' field to be enabled, but it was disabled.", isEnabled);
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

    @Then("I verify the product {string} is displayed for selection")
    public void iVerifyTheProductIsDisplayedForSelection(String productCode) {
        assertTrue(startIrradiationPage.productIsDisplayedForSelection(productCode));
    }

    @When("I select the product {string}")
    public void iSelectTheProduct(String productCode) {
        startIrradiationPage.selectProductForIrradiation(productCode);
    }

    @Then("I verify that the unit number {string} with product {string} was added to the batch")
    public void iVerifyThatTheUnitNumberWithProductWasAddedToTheBatch(String unitNumber, String productCode) {
        boolean isAddedToBatch = startIrradiationPage.unitNumberCardExists(unitNumber,productCode);
        Assert.assertTrue(String.format("A card for the unit number '%s' and product code '%s' was not found in the irradiation batch.", unitNumber, productCode), isAddedToBatch);
    }
}
