package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.controllers.ImportProductsController;
import com.arcone.biopro.distribution.receiving.verification.pages.EnterShippingInformationPage;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Map;

public class ImportProductSteps {

    @Autowired
    private ImportProductsController importProductsController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EnterShippingInformationPage enterShippingInformationPage;

    private Map apiResponse;

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
            Assert.assertEquals(attributeValue,data.get(attribute).toString());
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
    public void iShouldBeAbleToFillTheFollowingFields(String shouldShouldNot , String fields) {
        String[] shippingInformationFields = testUtils.getCommaSeparatedList(fields);
        var visible = "should".equals(shouldShouldNot);
        for (String attribute : shippingInformationFields) {
            if(!attribute.isBlank()){
                enterShippingInformationPage.setRandomFormValue(attribute,visible);
            }
        }
    }
}
