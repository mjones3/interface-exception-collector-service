package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.controllers.ImportProductsController;
import com.arcone.biopro.distribution.receiving.verification.pages.EnterShippingInformationPage;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ImportProductSteps {

    @Autowired
    private ImportProductsController importProductsController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EnterShippingInformationPage enterShippingInformationPage;

    private Map apiResponse;
    private boolean isValid;

    @Given("I request to enter shipping data for a {string} product category.")
    public void iRequestToEnterShippingDataForAProductCategory(String temperatureCategory) {
        this.apiResponse = importProductsController.enterShippingInformation(temperatureCategory);
        Assert.assertNotNull(apiResponse);
    }


    @Then("I should be able to enter information for the following attributes: {string}.")
    public void iShouldBeAbleToEnterInformationForTheFollowingAttributes(String shippingInformationAttributes) {
        Assert.assertNotNull(apiResponse);
        var data = (Map) apiResponse.get("data");
        String[] shippingAttributes = testUtils.getCommaSeparatedList(shippingInformationAttributes);
        for (int i = 0; i < shippingAttributes.length; i++) {

            var attributeArray = shippingAttributes[i].split(":");
            var attribute = attributeArray[0];
            var attributeValue = attributeArray[1];
            Assert.assertEquals(attributeValue, data.get(attribute).toString());
        }

    }

    @Given("I am at the Enter Shipping Information Page.")
    public void iAmAtTheEnterShippingInformationPage() throws InterruptedException {
        enterShippingInformationPage.navigateToEnterShippingInformation();
    }

    @When("I select to enter information for a {string} product category.")
    public void iSelectToEnterInformationForAProductCategory(String temperatureCategory) {
        enterShippingInformationPage.selectTemperatureCategory(temperatureCategory);

    }

    @Then("I {string} be able to fill the following fields: {string}.")
    public void iShouldBeAbleToFillTheFollowingFields(String shouldShouldNot, String fields) {
        String[] shippingInformationFields = testUtils.getCommaSeparatedList(fields);
        var visible = "should".equals(shouldShouldNot);
        for (String attribute : shippingInformationFields) {
            if (!attribute.isBlank()) {
                enterShippingInformationPage.setRandomFormValue(attribute, visible);
            }
        }
    }

    @Then("The temperature field should be {string}.")
    public void theTemperatureFieldShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            enterShippingInformationPage.waitForTemperatureFieldToBeEnabled();
            Assert.assertTrue(enterShippingInformationPage.isTemperatureFieldEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(enterShippingInformationPage.isTemperatureFieldEnabled());
        } else {
            Assert.fail("The temperature field should be enabled or disabled");
        }
    }

    @When("I enter thermometer ID {string}.")
    public void iEnterThermometerID(String thermometerId) throws InterruptedException {
        enterShippingInformationPage.enterThermometerId(thermometerId);
    }

    @When("I request to validate the temperature of {string} for the Temperature Category {string}.")
    public void iRequestToValidateTheTemperatureOfForTheTemperatureCategory(String temperatureValue, String temperatureCategory) {
        isValid = importProductsController.isTemperatureValid(temperatureCategory, temperatureValue);
    }

    @Then("The system {string} accept the temperature.")
    public void theSystemAcceptTheTemperature(String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(isValid);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(isValid);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @When("I enter the temperature {string}.")
    public void iEnterTheTemperature(String temperatureValue) {
        enterShippingInformationPage.setTemperature(temperatureValue);
    }

    @Then("The continue option should be {string}.")
    public void theContinueOptionShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            enterShippingInformationPage.waitForContinueButtonToBeEnabled();
            Assert.assertTrue(enterShippingInformationPage.isContinueButtonEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(enterShippingInformationPage.isContinueButtonEnabled());
        } else {
            Assert.fail("The continue button should be enabled or disabled");
        }
    }
}
