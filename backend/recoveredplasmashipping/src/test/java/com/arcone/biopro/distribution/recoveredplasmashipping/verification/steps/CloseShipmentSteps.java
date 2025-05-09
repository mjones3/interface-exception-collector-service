package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.FilterShipmentsController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class CloseShipmentSteps {

    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private CartonTestingController cartonTestingController;

    @Autowired
    private FilterShipmentsController filterShipmentsController;

    @Value("${default.employee.id}")
    private String employeeId;

    private String shipmentId;


    @Given("I have a shipment created with the Customer Code as {string} , Product Type as {string}, Carton Tare Weight as {string}, Shipment Date as {string}, Transportation Reference Number as {string} and Location Code as {string}.")
    public void iHaveAShipmentCreatedWithTheCustomerCodeAsProductTypeAsCartonTareWeightAsShipmentDateAsTransportationReferenceNumberAsAndLocationCodeAs(String customerCode, String productType, String cartonTare
        , String shipmentDate, String transportationRefNumber, String locationCode) {
        createShipmentController.createShipment(customerCode, productType, Float.parseFloat(cartonTare), testUtils.parseDataKeyword(shipmentDate), transportationRefNumber, locationCode);

        Assertions.assertNotNull(sharedContext.getShipmentCreateResponse());

        this.shipmentId = sharedContext.getShipmentCreateResponse().get("id").toString();
    }


    @And("I have a closed carton with the unit numbers as {string} and product codes as {string}.")
    public void iHaveAClosedCartonWithTheUnitNumbersAsAndProductCodesAs(String unitNumber, String productCode) {
        cartonTestingController.createCarton(this.shipmentId);
        Assertions.assertNotNull(sharedContext.getCreateCartonResponseList());

        var cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();

        var unitNumberList = testUtils.getCommaSeparatedList(unitNumber);
        var productCodeList = testUtils.getCommaSeparatedList(productCode);
        Assert.assertEquals(unitNumberList.length, productCodeList.length);

        for (int i = 0; i < unitNumberList.length; i++) { // pack all
            cartonTestingController.packCartonProduct(cartonId, unitNumberList[i], productCodeList[i], sharedContext.getLocationCode());
            Assert.assertFalse(sharedContext.getPackedProductsList().isEmpty());
        }

        for (int i = 0; i < unitNumberList.length; i++) { // verify all
            cartonTestingController.verifyCartonProduct(cartonId, unitNumberList[i], productCodeList[i], sharedContext.getLocationCode());
            Assert.assertFalse(sharedContext.getVerifiedProductsList().isEmpty());
        }

        cartonTestingController.closeCarton(cartonId,employeeId, sharedContext.getLocationCode());

        Assertions.assertNotNull(sharedContext.getLastCloseCartonResponse());
    }

    @When("I request to close the shipment with ship date as {string}")
    public void iRequestToCloseTheShipmentWithShipDateAs(String shipDate) {
        createShipmentController.closeShipment(sharedContext.getShipmentCreateResponse().get("id").toString()
           ,employeeId,sharedContext.getLocationCode() , testUtils.parseDataKeyword(shipDate));

        Assertions.assertNotNull(sharedContext.getLastShipmentCloseResponse());

    }

    @And("The shipment status should be {string}")
    public void theShipmentStatusShouldBe(String shipmentStatus) {
        var id = this.shipmentId != null ? this.shipmentId : sharedContext.getShipmentCreateResponse().get("id").toString();
        filterShipmentsController.findShipmentByIdAndLocation(id, sharedContext.getLocationCode());
        Assertions.assertEquals(shipmentStatus, sharedContext.getFindShipmentApiResponse().get("status").toString());
    }

    @Then("I should receive a API {string} error message response {string}.")
    public void iShouldReceiveAAPIErrorMessageResponse(String classification, String message) {
        if(sharedContext.getApiErrorResponse() != null){
            Assertions.assertNotNull(sharedContext.getApiErrorResponse());
            var extensions = (Map) sharedContext.getApiErrorResponse().get("extensions");
            Assertions.assertNotNull(extensions);
            Assertions.assertTrue(sharedContext.getApiErrorResponse().get("message").toString().contains(message));
            Assertions.assertEquals(classification,extensions.get("classification").toString());
        }else{
            var notification = sharedContext.getApiListMessageResponse().stream().filter(x -> x.get("type").equals(classification.toUpperCase())).findAny().orElse(null);
            assertNotNull(notification, "Failed to find the notification.");
            assertEquals(message, notification.get("message"), "Failed to find the message.");

        }
    }
}
