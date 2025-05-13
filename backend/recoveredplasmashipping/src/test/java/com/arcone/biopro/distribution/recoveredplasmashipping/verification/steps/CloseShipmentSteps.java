package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.FilterShipmentsController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private ShipmentDetailsPage shipmentDetailsPage;


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
        Assertions.assertEquals(shipmentStatus, sharedContext.getShipmentCreateResponse().get("status").toString());
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

    @Then("The close shipment option should be {string}.")
    public void theCloseShipmentOptionShouldBe(String enabledDisabled) {
        if (enabledDisabled.equalsIgnoreCase("enabled")) {
            Assert.assertTrue(shipmentDetailsPage.isCloseShipmentButtonEnabled());
        } else if (enabledDisabled.equalsIgnoreCase("disabled")) {
            Assert.assertFalse(shipmentDetailsPage.isCloseShipmentButtonEnabled());
        } else {
            Assert.fail("Wrong option for button enabledDisabled");
        }
    }

    @When("I choose to close the shipment.")
    public void iChooseToCloseTheShipment() {
        shipmentDetailsPage.clickCloseShipment();
    }


    @And("I should have the shipment date as {string}.")
    public void iShouldHaveTheShipmentDateAs(String shipmentDate){
        if (shipmentDate.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            shipmentDate = tomorrow.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        Assertions.assertEquals(shipmentDetailsPage.getShipmentConfirmationDate(),shipmentDate);
    }

    @When("I confirm to close the shipment.")
    public void iConfirmToCloseTheShipment() {
        shipmentDetailsPage.clickConfirmCloseShipment();
    }

    @And("The shipment status should be updated to {string}")
    public void theShipmentStatusShouldBeUpdatedTo(String shipmentStatus) {
        Assertions.assertEquals(shipmentDetailsPage.getShipmentStatus(),shipmentStatus);
    }

    @And("I have a closed carton with the unit numbers as {string} and product codes as {string} and product types {string} which become unacceptable.")
    public void iHaveAClosedCartonWithTheUnitNumbersAsAndProductCodesAsWhichBecomeUnacceptable(String unitNumbers, String productCodes , String productTypes) {

        cartonTestingController.createCarton(this.shipmentId);
        Assertions.assertNotNull(sharedContext.getCreateCartonResponseList());

        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        String[] unitNumbersArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodesArray = testUtils.getCommaSeparatedList(productCodes);
        String[] productTypesArray = testUtils.getCommaSeparatedList(productTypes);

        for (int i = 0; i < unitNumbersArray.length; i++) {
            cartonTestingController.insertVerifiedProduct(cartonId, unitNumbersArray[i], productCodesArray[i], productTypesArray[i]);
        }
        cartonTestingController.updateCartonStatus(cartonId,"CLOSED");
    }
}
