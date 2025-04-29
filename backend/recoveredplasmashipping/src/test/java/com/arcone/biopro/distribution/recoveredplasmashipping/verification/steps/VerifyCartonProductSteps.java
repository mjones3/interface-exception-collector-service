package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public class VerifyCartonProductSteps {
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    CreateShipmentController createShipmentController;

    @Given("I verify a(n) {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void iVerifyAProductWithTheUnitNumberProductCodeAndProductType(String productQuality, String unitNumber, String productCode, String productType) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        createShipmentController.verifyCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }
    @Given("I verify a product with the unit number {string}, product code {string} and volume {string}.")
    public void iVerifyAProductWithTheUnitNumberProductCodeAndVolume(String unitNumber, String productCode, String volume) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        createShipmentController.verifyCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @And("I should receive a {string} static message response {string}.")
    public void iShouldReceiveAStaticMessageResponse(String messageType, String message) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        String nextLink = String.format("/recovered-plasma/%s/carton-details?step=0&reset=true&resetMessage=%s", cartonId, message);
        var linksResponse = sharedContext.getLinksResponse().get("next");

        Assert.assertEquals(linksResponse, nextLink);
    }
}
