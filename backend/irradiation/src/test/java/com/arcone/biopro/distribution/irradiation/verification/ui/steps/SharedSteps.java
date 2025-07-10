package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.HomePage;
import com.arcone.biopro.testing.frontend.core.ButtonStatus;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SharedSteps {

    @Autowired
    private HomePage homePage;

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

}
