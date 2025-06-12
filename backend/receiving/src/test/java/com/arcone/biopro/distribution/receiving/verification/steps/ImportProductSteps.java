package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.controllers.ImportProductsController;
import com.arcone.biopro.distribution.receiving.verification.pages.EnterShippingInformationPage;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class ImportProductSteps {

    @Autowired
    private ImportProductsController importProductsController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EnterShippingInformationPage enterShippingInformationPage;

    private Map apiResponse;
    private boolean isTemperatureValid;
    private boolean isTransitTimeValid;

    @Given("I request to enter shipping data for a {string} product category and location code {string}.")
    public void iRequestToEnterShippingDataForAProductCategory(String temperatureCategory , String locationCode) {
        this.apiResponse = importProductsController.enterShippingInformation(temperatureCategory , locationCode);
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
        isTemperatureValid = importProductsController.isTemperatureValid(temperatureCategory, temperatureValue);
    }

    @Then("The system {string} accept the temperature.")
    public void theSystemAcceptTheTemperature(String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(isTemperatureValid);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(isTemperatureValid);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @When("I enter the temperature {string}.")
    public void iEnterTheTemperature(String temperatureValue) throws InterruptedException {
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

    @When("I request to validate the total transit time of Stat date time as {string}, Start Time Zone as {string}, End date time as {string} and End Time Zone as {string}  for the Temperature Category {string}.")
    public void validateTransitTime(String startDateTime, String startTimeZone, String endDateTime, String endTimeZone, String temperatureCategory) {
        isTransitTimeValid = importProductsController.isTotalTransitTimeValid(temperatureCategory, startDateTime, startTimeZone, endDateTime, endTimeZone);
    }

    @Then("The system {string} accept the transit time.")
    public void theSystemShouldAcceptTheTransitTime(String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(isTransitTimeValid);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(isTransitTimeValid);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("I should receive the total transit time as {string}.")
    public void iShouldReceiveTheTotalTransitTimeAs(String totalTransitTime) {
        String actualTotalTransitTime = importProductsController.getTotalTransitTime();
        log.debug("Expecting total transit time to be: {}. Received {}", totalTransitTime, actualTotalTransitTime);
        Assert.assertEquals(totalTransitTime, actualTotalTransitTime);
    }
}
