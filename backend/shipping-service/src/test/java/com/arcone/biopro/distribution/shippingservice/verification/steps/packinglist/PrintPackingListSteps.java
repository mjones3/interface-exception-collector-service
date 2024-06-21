package com.arcone.biopro.distribution.shippingservice.verification.steps.packinglist;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shippingservice.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shippingservice.verification.support.Endpoints;
import com.arcone.biopro.distribution.shippingservice.verification.support.GraphQLMutationMapper;
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
import static com.arcone.biopro.distribution.shippingservice.verification.support.GraphQLQueryMapper.printShippingLabelQuery;

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
    private ShipmentTestingController shipmentTestingController;

    @Autowired
    private ApiHelper apiHelper;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Value("${default.ui.facility}")
    private String facility;

    private long shipmentId;

    private long orderNumber;

    @Given("The shipment details are Order Number {int}, Location Code {int}, Customer ID {int}, Customer Name {string}, Department {string}, Address Line 1 {string}, Address Line 2 {string}, Unit Number {string}, Product Code {string}, Product Family {string}, Blood Type {string}, Expiration {string}, Quantity {int}.")
    public void setShipmentDetails(int orderNumber, int locationCode, int customerID, String customerName, String department, String addressLine1, String addressLine2, String unitNumber, String productCode, String productFamily, String bloodType, String expiration, int quantity) {
        this.shipmentDetails = shipmentController.buildShipmentRequestDetailsResponseType(orderNumber, locationCode, customerID, customerName, department, addressLine1, addressLine2, unitNumber, productCode, productFamily, bloodType, expiration, quantity);
        this.orderNumber = orderNumber;
        Assert.assertNotNull(this.shipmentDetails);
    }

    @And("I received a shipment fulfillment request with above details.")
    public void receiveShipmentFulfillmentRequest() throws Exception {
        shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", this.orderNumber);
    }

    @And("I have filled the shipment with the unit number {string} and product code {string}.")
    public void fillShipment(String unitNumber, String productCode) throws Exception {
        this.shipmentId = shipmentTestingController.getOrderShipmentId(this.orderNumber);
        var shipmentDetails = shipmentController.parseShipmentRequestDetail(
            shipmentTestingController.getShipmentRequestDetails(this.shipmentId));
        Long shipmentItem;
        shipmentItem = shipmentDetails.getItems().get(0).getId();

        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.packItemMutation(shipmentItem , facility
            ,unitNumber , "test-emplyee-id", productCode , "SATISFACTORY" ),"packItem");
        log.info("Shipment item successfully packed: {}", response);

        Assert.assertEquals("200 OK",response.get("ruleCode"));
    }

    @And("I have completed a shipment with above details.")
    public void completeShipment() {

        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.completeShipmentMutation(this.shipmentId , "test-emplyee-id"),"completeShipment") ;
        log.info("Shipment successfully completed: {}", response);
        Assert.assertEquals("200 OK", response.get("ruleCode"));
    }

    @When("I choose to print the Packing Slip.")
    public void iChooseToPrintThePackingSlip() throws InterruptedException {
        shipmentDetailPage.clickViewPackingSlip();
        // Wait for the print component to load
        Thread.sleep(2000);
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }

    @When("I choose to print the Shipping Label.")
    public void iChooseToPrintTheShippingLabel() throws InterruptedException {
        shipmentDetailPage.clickPrintShippingLabel();
        // Wait for the print component to load
        Thread.sleep(2000);
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Then("I am able to see the Packing Slip content.")
    public void iAmAbleToSeeThePackingSlipContent() {
        var query = printPackingListQuery(this.shipmentId);
        var packingList = apiHelper.graphQlRequest(query, "generatePackingListLabel");
        log.info("Packing slip content: {}", packingList);

        // TODO: When implemented, update the assertions with the current shipment information
        Assert.assertNotNull(packingList);
        Assert.assertEquals(Math.toIntExact(this.shipmentDetails.getOrderNumber()), packingList.get("orderNumber"));
        Assert.assertEquals(String.valueOf(this.shipmentId), packingList.get("shipmentId"));

        // Ship to
        Map<String, Object> shipTo = (Map<String, Object>) packingList.get("shipTo");
        Assert.assertEquals(this.shipmentDetails.getShippingCustomerCode().toString(), shipTo.get("customerCode").toString());
        Assert.assertEquals(this.shipmentDetails.getShippingCustomerName(), shipTo.get("customerName"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine1(), shipTo.get("addressLine1"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine2(), shipTo.get("addressLine2"));
        var complement = String.format("%s, %s, %s", this.shipmentDetails.getCustomerAddressCity(), this.shipmentDetails.getCustomerAddressState(), this.shipmentDetails.getCustomerAddressPostalCode());
        Assert.assertEquals(complement, shipTo.get("addressComplement"));

        // Ship from
        Map<String, Object> shipFrom = (Map<String, Object>) packingList.get("shipFrom");
        Assert.assertEquals("IC39", shipFrom.get("bloodCenterCode"));
        Assert.assertEquals("Charlotte Main", shipFrom.get("bloodCenterName"));
        Assert.assertEquals("447 South Blvd, Suite 100", shipFrom.get("bloodCenterAddressLine1"));
        Assert.assertEquals("", shipFrom.get("bloodCenterAddressLine2"));
        Assert.assertEquals("Charlotte, NC, 28209", shipFrom.get("bloodCenterAddressComplement"));

        // Product details
        List<Map<String, Object>> packedItems = (List<Map<String, Object>>) packingList.get("packedItems");
        Assert.assertFalse(packedItems.isEmpty());

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

    @Then("I am able to see the Shipping Label content.")
    public void iAmAbleToSeeTheShippingLabelContent() {
        var query = printShippingLabelQuery(this.shipmentId);
        var shippingLabel = apiHelper.graphQlRequest(query, "generateShippingLabel");
        log.info("Shipping label content: {}", shippingLabel);

        // TODO: When implemented, update the assertions with the current shipment information
        Assert.assertNotNull(shippingLabel);
        Assert.assertEquals(Math.toIntExact(this.shipmentDetails.getOrderNumber()), shippingLabel.get("orderNumber"));
        Assert.assertEquals(Math.toIntExact(this.shipmentId), shippingLabel.get("shipmentId"));
        Assert.assertNotNull(shippingLabel.get("orderIdBase64Barcode"));
        Assert.assertNotNull(shippingLabel.get("shipmentIdBase64Barcode"));

        // Ship to
        Map<String, Object> shipTo = (Map<String, Object>) shippingLabel.get("shipTo");
        Assert.assertEquals(Math.toIntExact(this.shipmentDetails.getShippingCustomerCode()), shipTo.get("customerCode"));
        Assert.assertEquals(this.shipmentDetails.getShippingCustomerName(), shipTo.get("customerName"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine1(), shipTo.get("addressLine1"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine2(), shipTo.get("addressLine2"));
        var complement = String.format("%s, %s, %s", this.shipmentDetails.getCustomerAddressCity(), this.shipmentDetails.getCustomerAddressState(), this.shipmentDetails.getCustomerAddressPostalCode());
        Assert.assertEquals(complement, shipTo.get("addressComplement"));

        // Ship from
        Map<String, Object> shipFrom = (Map<String, Object>) shippingLabel.get("shipFrom");
        Assert.assertEquals("IC39", shipFrom.get("bloodCenterCode"));
        Assert.assertEquals("Charlotte Main", shipFrom.get("bloodCenterName"));
        Assert.assertEquals("447 South Blvd, Suite 100", shipFrom.get("bloodCenterAddressLine1"));
        Assert.assertEquals("", shipFrom.get("bloodCenterAddressLine2"));
        Assert.assertEquals("Charlotte, NC, 28209", shipFrom.get("bloodCenterAddressComplement"));
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
        shipmentDetailPage.ensureViewPackingSlipButtonIsNotVisible();
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Then("I should not be able to print the Shipping Label.")
    public void iShouldNotBeAbleToPrintTheShippingLabel() {
        shipmentDetailPage.ensureViewShippingLabelButtonIsNotVisible();
        screenshotService.attachConditionalScreenshot(saveAllScreenshots);
    }
}
