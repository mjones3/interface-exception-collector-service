package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.CloseIrradiationPage;
import com.arcone.biopro.distribution.irradiation.verification.ui.pages.HomePage;
import com.arcone.biopro.distribution.irradiation.verification.ui.pages.IrradiationPage;
import com.arcone.biopro.distribution.irradiation.verification.ui.pages.StartIrradiationPage;
import com.arcone.biopro.testing.frontend.core.ButtonStatus;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@AllArgsConstructor
public class SharedSteps {

    private HomePage homePage;
    private final StartIrradiationPage startIrradiationPage;
    private final CloseIrradiationPage closeIrradiationPage;
    
    /**
     * Returns the appropriate irradiation page based on the page name.
     * 
     * @param pageName The name of the page ("Start Irradiation" or "Close Irradiation")
     * @return The corresponding irradiation page object
     */
    private IrradiationPage getIrradiationPage(String pageName) {
        return switch (pageName) {
            case "Start Irradiation" -> startIrradiationPage;
            case "Close Irradiation" -> closeIrradiationPage;
            default -> throw new IllegalArgumentException("Unknown page: " + pageName);
        };
    }


    @When("I select the location {string}")
    public void iSelectLocation(String location) {
        homePage.selectLocation(location);
    }

    @And("I choose to {string}")
    public void iClickOnTheButton(String buttonLabel) {
        homePage.clickOnButton(buttonLabel);
    }

    @Then("I verify that I am {string} to {string}")
    public void iVerifyThatEnableOrDisabled(String expectedStatus, String buttonLabel) {
        // expectedStatus: {"Able", "Unable"}
        ButtonStatus expectedStatusEnum;
        // Convert the expected status to an enum
        Map<String, String> buttonMap = Map.of("Able",ButtonStatus.Enabled.toString(), "Unable", ButtonStatus.Disabled.toString());
        try {
            expectedStatusEnum = ButtonStatus.valueOf(buttonMap.get(expectedStatus));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid button status: " + expectedStatus);
        }
        // Get the actual status of the button
        ButtonStatus actualStatus = homePage.getButtonStatus(buttonLabel);
        // Compare the actual status with the expected status
        assertEquals(expectedStatusEnum, actualStatus, "The button status did not match the expected status: " + expectedStatus);
    }

    @And("On the {string} page, I verify that the {string} field is {string}")
    public void iVerifyThatTheFieldIs(String page, String fieldName, String expectedStatus) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        boolean isEnabled = irradiationPage.inputFieldIsEnabled(fieldName);

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

    @When("On the {string} page, I scan the irradiator id {string}")
    public void iScanTheIrradiatorId(String page, String irradiatorDeviceId) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        irradiationPage.scanIrradiatorDeviceId(irradiatorDeviceId);
    }

    @When("On the {string} page, I scan the unit number {string}")
    public void iScanTheUnitNumberInTheIrradiationPage(String page, String unitNumber) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        irradiationPage.scanUnitNumber(unitNumber);
    }

    @Then("On the {string} page, I verify that the unit number {string} with product {string} was added to the batch")
    public void iVerifyThatTheUnitNumberWithProductWasAddedToTheBatch(String page, String unitNumber, String product) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        boolean isAddedToBatch = irradiationPage.unitNumberCardExists(unitNumber,product);
        Assert.assertTrue(String.format("A card for the unit number '%s' and product code '%s' was not found in the irradiation batch.", unitNumber, product), isAddedToBatch);
    }

    @Then("On the {string} page, I verify that the unit number {string} with product {string} was not added to the batch")
    public void iVerifyThatTheUnitNumberWithProductWasNotAddedToTheBatch(String page, String unitNumber, String product) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        int unitNumberCards = irradiationPage.unitNumberProductCardCount(unitNumber,product);
        Assert.assertEquals("Unit number cards amount does not match.", 0,unitNumberCards );
    }

    @Then("On the {string} page, I verify that the card for unit number {string} and product {string} shows as {string}")
    public void iVerifyThatTheProductShowsAs(String page, String unitNumber, String product, String status) {
        IrradiationPage irradiationPage = getIrradiationPage(page);
        Assertions.assertTrue(irradiationPage.isProductInStatus(unitNumber, product, status));
    }
}
