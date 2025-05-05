package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ManageCartonPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class VerifyCartonProductSteps {
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private ManageCartonPage manageCartonPage;
    @Autowired
    private CartonTestingController cartonTestingController;

    @Given("I verify a(n) {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void iVerifyAProductWithTheUnitNumberProductCodeAndProductType(String productQuality, String unitNumber, String productCode, String productType) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        cartonTestingController.verifyCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @Given("I verify a(n) {string} product with the unit number {string}, product code {string} and product type {string} into the carton sequence {int}.")
    public void iVerifyAProductWithTheUnitNumberProductCodeAndProductTypeIntoCarton(String productQuality, String unitNumber, String productCode, String productType, int cartonSequence) {
        String cartonId = sharedContext.getCreateCartonResponseList().get(cartonSequence - 1).get("id").toString();
        cartonTestingController.verifyCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @Given("I verify a product with the unit number {string}, product code {string} and volume {string}.")
    public void iVerifyAProductWithTheUnitNumberProductCodeAndVolume(String unitNumber, String productCode, String volume) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        cartonTestingController.verifyCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @And("I should receive a {string} static message response {string}.")
    public void iShouldReceiveAStaticMessageResponse(String messageType, String message) {
        String cartonId = sharedContext.getLastCartonResponse().get("id").toString();
        String nextLink = String.format("/recovered-plasma/%s/carton-details?step=0&reset=true&resetMessage=%s", cartonId, message);
        var linksResponse = sharedContext.getLinksResponse().get("next");

        Assert.assertEquals(linksResponse, nextLink);
    }

    @Then("The verify products option should be {string}")
    public void theVerifyProductsOptionShouldBe(String enabledDisabled) {
        Assert.assertTrue(manageCartonPage.verifyNextButtonIs(enabledDisabled));
    }

    @And("I choose the Next option to start verify products process.")
    public void iChooseTheNextOptionToStartVerifyProductsProcess() {
        manageCartonPage.clickNext();
        manageCartonPage.waitForVerifyTab();
    }

    @And("The close carton option should be {string}.")
    public void theCloseCartonOptionShouldBe(String enabledDisabled) {
        Assert.assertTrue(manageCartonPage.verifyCloseButtonIs(enabledDisabled));
    }

    @And("I choose to close the carton.")
    public void closeCarton() {
        manageCartonPage.closeCarton();
    }

    @And("I have verified product with the unit number {string}, product code {string} and product type {string}.")
    public void iHaveVerifiedProductWithTheUnitNumberProductCodeAndProductType(String unitNumber, String productCode, String productType) {
        var cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();

        var unitNumberList = testUtils.getCommaSeparatedList(unitNumber);
        var productCodeList = testUtils.getCommaSeparatedList(productCode);
        Assert.assertEquals(unitNumberList.length, productCodeList.length);

        for (int i = 0; i < unitNumberList.length; i++) { // pack all
            cartonTestingController.packCartonProduct(cartonId, unitNumberList[i], productCodeList[i], sharedContext.getLocationCode());
        }
        for (int i = 0; i < unitNumberList.length; i++) { // verify all
            cartonTestingController.verifyCartonProduct(cartonId, unitNumberList[i], productCodeList[i], sharedContext.getLocationCode());
        }
    }
}
