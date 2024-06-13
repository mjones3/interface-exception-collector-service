package com.arcone.biopro.distribution.shippingservice.verification.steps.packinglist;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shippingservice.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shippingservice.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shippingservice.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.types.ShipmentRequestDetailsResponseType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static com.arcone.biopro.distribution.shippingservice.verification.support.GraphQLQueryMapper.printPackingListQuery;

@SpringBootTest
@Slf4j
public class PrintPackingListSteps {

    private ShipmentRequestDetailsResponseType shipmentDetails;

    @Autowired
    public ShipmentTestingController shipmentController;

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private ApiHelper apiHelper;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Given("The shipment details are Order Number {int}, Location Code {int}, Customer ID {int}, Customer Name {string}, Department {string}, Address Line 1 {string}, Address Line 2 {string}, Address Complement {string}, Unit Number {string}, Product Code {string}, Product Family {string}, Blood Type {string}, Expiration {string}, Quantity {int}.")
    public void setShipmentDetails(int orderNumber, int locationCode, int customerID, String customerName, String department, String addressLine1, String addressLine2, String addressComplement, String unitNumber, String productCode, String productFamily, String bloodType, String expiration, int quantity) {
        this.shipmentDetails = shipmentController.buildShipmentRequestDetailsResponseType(orderNumber, locationCode, customerID, customerName, department, addressLine1, addressLine2, addressComplement, unitNumber, productCode, productFamily, bloodType, expiration, quantity);
        Assert.assertNotNull(this.shipmentDetails);
    }

    @And("I have completed a shipment with above details.")
    public void completeShipment() throws Exception {
        // TODO: change this to use the service
        this.shipmentDetails.setStatus("COMPLETED");
        var orderNumber = shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", orderNumber);
    }

    @When("I choose to print the Packing Slip.")
    public void iChooseToPrintThePackingSlip() throws InterruptedException {
        shipmentDetailPage.clickViewPackingSlip();
        // Wait for the print component to load
        Thread.sleep(2000);
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Then("I am able to see the Packing Slip content.")
    public void iAmAbleToSeeThePackingSlipContent() {
        var query = printPackingListQuery();
        var packingList = apiHelper.graphQlRequest(query, "generatePackingListLabel");
        log.info("Packing slip content: {}", packingList);

        // TODO: When implemented, update the assertions with the current shipment information
        Assert.assertNotNull(packingList);
        Assert.assertEquals(999996, packingList.get("orderNumber"));
        Assert.assertEquals("1", packingList.get("shipmentId"));

        // Ship to
        Map<String, Object> shipTo = (Map<String, Object>) packingList.get("shipTo");
        Assert.assertEquals("Tampa", shipTo.get("customerName"));
        Assert.assertEquals("36544 SW 27th St", shipTo.get("addressLine1"));
        Assert.assertEquals("North Miami", shipTo.get("addressLine2"));
        Assert.assertEquals("Miami, FL, 33016", shipTo.get("addressComplement"));

        // Ship from
        Map<String, Object> shipFrom = (Map<String, Object>) packingList.get("shipFrom");
        Assert.assertEquals("IC39", shipFrom.get("bloodCenterCode"));
        Assert.assertEquals("Charlotte Main", shipFrom.get("bloodCenterName"));
        Assert.assertEquals("447 South Blvd, Suite 100", shipFrom.get("bloodCenterAddressLine1"));
        Assert.assertEquals("", shipFrom.get("bloodCenterAddressLine2"));
        Assert.assertEquals("Charlotte, NC, 28209", shipFrom.get("bloodCenterAddressComplement"));

        // Product details
        List<Map<String, Object>> packedItems = (List<Map<String, Object>>) packingList.get("packedItems");
        packedItems.forEach(item -> {
            Assert.assertNotNull(item.get("shipmentItemId"));
            Assert.assertNotNull(item.get("unitNumber"));
            Assert.assertNotNull(item.get("productCode"));
            Assert.assertNotNull(item.get("aboRh"));
            Assert.assertNotNull(item.get("productDescription"));
            Assert.assertNotNull(item.get("productFamily"));
            Assert.assertNotNull(item.get("expirationDate"));
            Assert.assertNotNull(item.get("collectionDate"));
        });
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
        shipmentDetailPage.viewPackingSlipButtonIsNotVisible();
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }
}
