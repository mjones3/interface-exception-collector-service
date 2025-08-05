package com.arcone.biopro.distribution.shipping.verification.steps.packinglist;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentRequestDetailsResponseType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLQueryMapper.printPackingListQuery;
import static com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLQueryMapper.printShippingLabelQuery;

@SpringBootTest
@Slf4j
public class PrintPackingListSteps {

    private ShipmentRequestDetailsResponseType shipmentDetails;

    @Autowired
    public ShipmentTestingController shipmentController;

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Value("${kafka.waiting.time}")
    private long kafkaWaitingTime;

    @Value("${selenium.headless.execution}")
    private boolean headless;


    @Given("The shipment details are Order Number {string}, Location Code {string}, Customer ID {string}, Customer Name {string}, Department {string}, Address Line 1 {string}, Address Line 2 {string}, Unit Number {string}, Product Code {string}, Product Family {string}, Blood Type {string}, Expiration {string}, Quantity {int}.")
    public void setShipmentDetails(String orderNumber, String locationCode, String customerID, String customerName, String department, String addressLine1, String addressLine2, String unitNumber, String productCode, String productFamily, String bloodType, String expiration, int quantity) {
        this.shipmentDetails = shipmentController.buildShipmentRequestDetailsResponseType(Long.parseLong(orderNumber), locationCode, customerID, customerName, department, addressLine1, addressLine2, TestUtils.removeUnitNumberScanDigits(unitNumber), TestUtils.removeProductCodeScanDigits(productCode), productFamily, bloodType, expiration, quantity);
        context.setOrderNumber(Long.parseLong(orderNumber));
        Assert.assertNotNull(this.shipmentDetails);
    }

    @And("I received a shipment fulfillment request with above details.")
    public void receiveShipmentFulfillmentRequest() throws Exception {
        shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", context.getOrderNumber());
    }

    @And("I have filled the shipment with the unit number {string} and product code {string}.")
    public void fillShipmentStep(String unitNumber, String productCode) throws Exception {
        context.setShipmentId(shipmentTestingController.getShipmentId(context.getOrderNumber()));
        shipmentController.fillShipment(context.getShipmentId(), TestUtils.removeUnitNumberScanDigits(unitNumber), TestUtils.removeProductCodeScanDigits(productCode), "SATISFACTORY", false);
    }

    @And("I have filled the shipment with the unit number {string} and product code {string} for order {string}.")
    public void fillShipmentForOrder(String unitNumber, String productCode, String orderNumber) throws Exception {
        context.setOrderNumber(Long.valueOf(orderNumber));
        context.setShipmentId(shipmentTestingController.getShipmentId(context.getOrderNumber()));
        shipmentController.fillShipment(context.getShipmentId(), TestUtils.removeUnitNumberScanDigits(unitNumber), TestUtils.removeProductCodeScanDigits(productCode), "SATISFACTORY", false);
    }

    @And("I have completed a shipment with above details.")
    public void completeShipment() {

        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.completeShipmentMutation(context.getShipmentId(), "test-emplyee-id"), "completeShipment");
        log.info("Shipment successfully completed: {}", response);
        Assert.assertEquals("200 OK", response.get("ruleCode"));
    }

    @When("I choose to print the Packing Slip.")
    public void iChooseToPrintThePackingSlip() throws InterruptedException {
        if (!headless) {
            shipmentDetailPage.clickViewPackingSlip();
            // Wait for the print component to load
            Thread.sleep(2000);
        } else {
            log.info("Skipping print packing slip. Test in headless mode.");
        }
    }

    @When("I choose to print the Shipping Label.")
    public void iChooseToPrintTheShippingLabel() throws InterruptedException {
        if (!headless) {
            shipmentDetailPage.clickPrintShippingLabel();
            // Wait for the print component to load
            Thread.sleep(2000);
        } else {
            log.info("Skipping print packing slip. Test in headless mode.");
        }
    }

    @Then("I am able to see the Packing Slip content.")
    public void iAmAbleToSeeThePackingSlipContent() {
        var query = printPackingListQuery(context.getShipmentId());
        var packingList = apiHelper.graphQlRequest(query, "generatePackingListLabel");
        log.info("Packing slip content: {}", packingList);

        // TODO: When implemented, update the assertions with the current shipment information
        Assert.assertNotNull(packingList);
        Assert.assertEquals(Math.toIntExact(this.shipmentDetails.getOrderNumber()), packingList.get("orderNumber"));
        Assert.assertEquals(String.valueOf(context.getShipmentId()), packingList.get("shipmentId"));

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
        Assert.assertEquals("123456789", shipFrom.get("bloodCenterCode"));
        Assert.assertEquals("MDL Hub 1", shipFrom.get("bloodCenterName"));
        Assert.assertEquals("444 Main St.", shipFrom.get("bloodCenterAddressLine1"));
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
        var query = printShippingLabelQuery(context.getShipmentId());
        var shippingLabel = apiHelper.graphQlRequest(query, "generateShippingLabel");
        log.info("Shipping label content: {}", shippingLabel);

        // TODO: When implemented, update the assertions with the current shipment information
        Assert.assertNotNull(shippingLabel);
        Assert.assertEquals(Math.toIntExact(this.shipmentDetails.getOrderNumber()), shippingLabel.get("orderNumber"));
        Assert.assertEquals(Math.toIntExact(context.getShipmentId()), shippingLabel.get("shipmentId"));
        Assert.assertNotNull(shippingLabel.get("orderIdBase64Barcode"));
        Assert.assertNotNull(shippingLabel.get("shipmentIdBase64Barcode"));

        // Ship to
        Map<String, Object> shipTo = (Map<String, Object>) shippingLabel.get("shipTo");
        Assert.assertEquals(this.shipmentDetails.getShippingCustomerCode(), shipTo.get("customerCode"));
        Assert.assertEquals(this.shipmentDetails.getShippingCustomerName(), shipTo.get("customerName"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine1(), shipTo.get("addressLine1"));
        Assert.assertEquals(this.shipmentDetails.getCustomerAddressAddressLine2(), shipTo.get("addressLine2"));
        var complement = String.format("%s, %s, %s", this.shipmentDetails.getCustomerAddressCity(), this.shipmentDetails.getCustomerAddressState(), this.shipmentDetails.getCustomerAddressPostalCode());
        Assert.assertEquals(complement, shipTo.get("addressComplement"));

        // Ship from
        Map<String, Object> shipFrom = (Map<String, Object>) shippingLabel.get("shipFrom");
        Assert.assertEquals("123456789", shipFrom.get("bloodCenterCode"));
        Assert.assertEquals("MDL Hub 1", shipFrom.get("bloodCenterName"));
        Assert.assertEquals("444 Main St.", shipFrom.get("bloodCenterAddressLine1"));
        Assert.assertEquals("", shipFrom.get("bloodCenterAddressLine2"));
        Assert.assertEquals("Charlotte, NC, 28209", shipFrom.get("bloodCenterAddressComplement"));
    }

    @And("I have an open shipment with above details.")
    public void iHaveAnOpenShipmentWithAboveDetails() throws Exception {
        this.shipmentDetails.setStatus("OPEN");
        shipmentController.createShippingRequest(this.shipmentDetails);
        log.info("Order number successfully created: {}", context.getOrderNumber());
    }

    @When("I enter the Shipment Fulfillment Details page.")
    public void iEnterTheShipmentFulfillmentDetailsPage() {
    }

    @Then("I should not be able to print the Packing List.")
    public void iShouldNotBeAbleToPrintThePackingList() {
        shipmentDetailPage.ensureViewPackingSlipButtonIsNotVisible();
    }

    @Then("I should not be able to print the Shipping Label.")
    public void iShouldNotBeAbleToPrintTheShippingLabel() {
        shipmentDetailPage.ensureViewShippingLabelButtonIsNotVisible();
    }

    @And("I {string} see the Billed to information.")
    public void iSeeTheBilledToInformation(String shouldShouldNot) {
        if(shouldShouldNot.equalsIgnoreCase("should")) {
            Assert.assertNotNull(this.shipmentDetails.getBillingCustomerName());
        }else if (shouldShouldNot.equalsIgnoreCase("should not")) {
            Assert.assertNull(this.shipmentDetails.getBillingCustomerName());
        }else {
            Assert.fail("Invalid Option.");
        }
    }

    @Given("The Internal Transfer shipment details are:")
    public void theInternalTransferShipmentDetailsAre(DataTable dataTable) {

        var internalTransferData = dataTable.asMaps();
        var dataMap = internalTransferData.getFirst();

        var units = Arrays.stream(dataMap.get("Unit_Numbers").split(",")).toList();
        var productCodeList = Arrays.stream(dataMap.get("Product_Codes").split(",")).toList();

        context.setUnitNumber(units.getFirst());
        context.setProductCode(productCodeList.getFirst());
        context.setOrderNumber(Long.valueOf(dataMap.get("Order_Number")));

        context.setShipmentId(shipmentTestingController.createPackedShipment(dataMap.get("Order_Number"),dataMap.get("Customer_ID"),dataMap.get("Customer_Name")
            ,dataMap.get("Temp_Category"),dataMap.get("Shipment_Type"),dataMap.get("Label_Status")
            ,Boolean.parseBoolean(dataMap.get("Quarantined_Products"))
            ,units
            ,productCodeList
            ,dataMap.get("Product_Status")
            ,dataMap.get("Product_Family")
            ,dataMap.get("Blood_Type")
            ,Integer.parseInt(dataMap.get("Quantity"))));

        Assert.assertNotNull(context.getShipmentId());
        context.setTotalPacked(units.size());

        this.shipmentDetails = shipmentController.parseShipmentRequestDetail(shipmentController.getShipmentRequestDetails(context.getShipmentId()));
        Assert.assertNotNull(this.shipmentDetails);

    }
}
