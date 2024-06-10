package com.arcone.biopro.distribution.shippingservice.verification.steps.packinglist;

import com.arcone.biopro.distribution.shippingservice.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.types.ShipmentRequestDetailsResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class PrintPackingListSteps {

    private ShipmentRequestDetailsResponseType shipmentDetails;

    @Autowired
    public ShipmentTestingController shipmentController;

    @Given("The shipment details are Order Number {int}, Location Code {int}, Customer ID {int}, Customer Name {string}, Department {string}, Address Line 1 {string}, Address Line 2 {string}, Address Complement {string}, Unit Number {string}, Product Code {string}, Product Family {string}, Blood Type {string}, Expiration {string}, Quantity {int}.")
    public void setShipmentDetails(int orderNumber,int locationCode, int customerID, String customerName, String department, String addressLine1, String addressLine2, String addressComplement, String unitNumber, String productCode, String productFamily, String bloodType, String expiration, int quantity) {
        this.shipmentDetails = shipmentController.buildShipmentRequestDetailsResponseType(orderNumber, locationCode, customerID, customerName, department, addressLine1, addressLine2, addressComplement, unitNumber, productCode, productFamily, bloodType, expiration, quantity);
        Assert.assertNotNull(this.shipmentDetails);
    }

    @And("I have completed a shipment with above details.")
    public void completeShipment() throws Exception {
        this.shipmentDetails.setStatus("COMPLETED");
        var orderNumber = shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", orderNumber);
    }

    @When("I choose to print the Packing Slip.")
    public void iChooseToPrintThePackingSlip() {
    }

    @Then("I am able to see the Packing Slip content.")
    public void iAmAbleToSeeThePackingSlipContent() {
    }

    @And("I am able to Print or generate a PDF")
    public void iAmAbleToPrintOrGenerateAPDF() {
    }

    @And("I have an open shipment with above details.")
    public void iHaveAnOpenShipmentWithAboveDetails() throws Exception {
        this.shipmentDetails.setStatus("OPEN");
        var orderNumber = shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", orderNumber);
    }

    @When("I enter the Shipment Fulfillment Details page.")
    public void iEnterTheShipmentFulfillmentDetailsPage() {
    }

    @Then("I should not be able to print the Packing List.")
    public void iShouldNotBeAbleToPrintThePackingList() {
    }
}
