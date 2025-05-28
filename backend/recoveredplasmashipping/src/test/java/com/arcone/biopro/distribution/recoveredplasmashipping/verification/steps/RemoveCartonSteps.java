package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RemoveCartonSteps {

    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private CartonTestingController cartonTestingController;
    @Autowired
    ShipmentDetailsPage shipmentDetailsPage;
    @Autowired
    TestUtils  testUtils;

    @Given("I request to remove the carton sequence {string} from the shipment.")
    public void iRequestToRemoveTheCartonSequenceFromTheShipment(String cartonSequence) {
        log.debug("Removing carton sequence: {}", cartonSequence);
        int cartonId = Integer.parseInt(sharedContext.getCreateCartonResponseList().get(Integer.parseInt(cartonSequence) - 1).get("id").toString());
        Assert.assertTrue(cartonId > 0);

        cartonTestingController.removeCarton(cartonId);
    }

    @And("The remove option should be available for the carton number prefix {string} and sequence number {string} and status {string}.")
    public void theRemoveOptionShouldBeAvailableForTheCartonNumberPrefixAndSequenceNumberAndStatus(String prefix, String sequenceNumber, String status) {
        shipmentDetailsPage.checkRemoveCartonOptionIsAvailable(prefix, sequenceNumber, status);
    }

    @When("I choose to remove the carton number prefix {string} and sequence number {string} and status {string}.")
    public void iChooseToRemoveTheCartonNumberPrefixAndSequenceNumberAndStatus(String prefix, String sequenceNumber, String status) {
        shipmentDetailsPage.removeCarton(prefix, sequenceNumber, status);
    }

    @When("I confirm to remove the carton.")
    public void iConfirmToRemoveTheCarton() {
        shipmentDetailsPage.confirmRemoveCarton();
    }

    @When("I request to remove the products {string} with product codes {string} from the carton sequence {string}.")
    public void iRequestToRemoveTheProductsWithProductCodesFromTheCartonSequence(String unitNumbers, String productCodes, String cartonSequence) {
        String[] unitNumberArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodeArray = testUtils.getCommaSeparatedList(productCodes);
        int cartonId = Integer.parseInt(sharedContext.getCreateCartonResponseList().get(Integer.parseInt(cartonSequence) - 1).get("id").toString());

        // Find carton and get the packed products
        var carton = (Map) cartonTestingController.findCartonById(cartonId).get("data");
        var packedProducts = (List<Map>) carton.get("packedProducts");

        // Get the list of product's IDs to be removed based on the UnitNumbers provided
        List<Integer> cartonProductToRemoveIds = new ArrayList<>();
        for (int i = 0; i < unitNumberArray.length; i++) {
            for (Map packedProduct : packedProducts) {
                if (packedProduct.get("unitNumber").equals(unitNumberArray[i]) && packedProduct.get("productCode").equals(productCodeArray[i])) {
                    cartonProductToRemoveIds.add(Integer.parseInt(packedProduct.get("id").toString()));
                }
            }
        }

        // Remove the products from the carton
        cartonTestingController.removeCartonItem(cartonId, cartonProductToRemoveIds);
        log.debug("Removing products from carton sequence: {}", cartonSequence);
    }
}
