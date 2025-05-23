package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RemoveCartonSteps {

    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private CartonTestingController cartonTestingController;
    @Autowired
    ShipmentDetailsPage shipmentDetailsPage;

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

}
