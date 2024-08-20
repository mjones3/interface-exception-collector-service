package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import com.arcone.biopro.distribution.order.verification.pages.order.HomePage;
import com.arcone.biopro.distribution.order.verification.pages.order.OrderDetailsPage;
import com.arcone.biopro.distribution.order.verification.pages.order.SearchOrderPage;
import com.arcone.biopro.distribution.order.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.order.verification.support.DatabaseService;
import com.arcone.biopro.distribution.order.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Random;

@Slf4j
public class OrderSteps {

    //    Order details
    private String externalId;
    private String locationCode;
    private String priority;
    private String status;
    private Integer orderId;
    private String orderComments;
    private String shippingCustomerCode;
    private String shippingCustomerName;
    private String shippingMethod;
    private String billCustomerCode;
    private String billCustomerName;
    private String productFamily;
    private String bloodType;
    private Integer quantity;
    private String productComments;

    private OrderController orderController = new OrderController();
    private JSONObject partnerOrder;
    private boolean isLoggedIn = false;

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SearchOrderPage searchOrderPage;

    @Autowired
    private HomePage homePage;

    @Autowired
    private OrderDetailsPage orderDetailsPage;

    @Value("${kafka.waiting.time}")
    private long kafkaWaitingTime;

    private void createOrderInboundRequest(String jsonContent, OrderReceivedEventDTO eventPayload) throws JSONException {
        partnerOrder = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", partnerOrder);
        Assert.assertNotNull(this.externalId);
        Assert.assertNotNull(partnerOrder);
        var event = kafkaHelper.sendPartnerOrderReceivedEvent(eventPayload.payload().id().toString(), eventPayload).block();
        Assert.assertNotNull(event);
    }


    @Given("I have received an order inbound request with externalId {string} and content {string}.")
    public void postOrderReceivedEvent(String externalId, String jsonFileName) throws Exception {
        this.externalId = externalId;
        var jsonContent = testUtils.getResource(jsonFileName);
        var newDesiredShippingDate = LocalDate.now().plusDays(
            new Random().nextInt(10) + 1
        ).toString();
        jsonContent = jsonContent.replace("DESIRED_DATE", newDesiredShippingDate);
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        createOrderInboundRequest(jsonContent, eventPayload);
    }

    @Given("I have received an order inbound request with externalId {string}, content {string}, and desired shipping date {string}.")
    public void postOrderReceivedEventPast(String externalId, String jsonFileName, String date) throws Exception {
        this.externalId = externalId;
        var jsonContent = testUtils.getResource(jsonFileName);
        jsonContent = jsonContent.replace("DESIRED_DATE", date);
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        createOrderInboundRequest(jsonContent, eventPayload);
    }


    @When("The system process the order request.")
    public void waitForProcess() throws InterruptedException {
        Thread.sleep(kafkaWaitingTime);
    }

    @Then("A biopro Order will be available in the Distribution local data store.")
    public void checkOrderExists() {
        var query = DatabaseQueries.countOrdersByExternalId(this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(1L, records.get("count"));
    }

    @Then("A biopro Order will not be available in the Distribution local data store.")
    public void checkOrderDoesNotExist() {
        var query = DatabaseQueries.countOrdersByExternalId(this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(0L, records.get("count"));
    }

    @Then("The duplicated biopro Order will not be available in the Distribution local data store.")
    public void checkDuplicatedOrderDoesNotExist() {
        var query = DatabaseQueries.countOrdersByExternalId(this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(1L, records.get("count"));
    }

    @Given("I have a Biopro Order with externalId {string}, Location Code {string}, Priority {string} and Status {string}.")
    public void createBioproOrder(String externalId, String locationCode, String priority, String status) {
        this.externalId = externalId;
        this.locationCode = locationCode;
        this.priority = priority;
        this.status = status;
        var query = DatabaseQueries.insertBioProOrder(externalId, locationCode, orderController.getPriorityValue(priority), priority, status);
        databaseService.executeSql(query).block();
    }

    @And("I have an order item with product family {string}, blood type {string}, quantity {int}, and order item comments {string}.")
    public void createOrderItem(String productFamily, String bloodType, Integer quantity, String comments) {
        this.productFamily = productFamily;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.productComments = comments;
        var query = DatabaseQueries.insertBioProOrderItem(this.externalId, productFamily, bloodType, quantity, comments);
        databaseService.executeSql(query).block();
    }

    @Given("I have a Biopro Order with externalId {string}, Location Code {string}, Priority {string}, Status {string}, shipment type {string}, delivery type {string}, shipping method {string}, product category {string}, desired ship date {string}, shipping customer code and name as {string} and {string}, billing customer code and name as {string} and {string}, and comments {string}.")
    public void createBioproOrderWithDetails(String externalId, String locationCode, String priority, String status, String shipmentType, String deliveryType, String shippingMethod, String productCategory, String desiredShipDate, String shippingCustomerCode, String shippingCustomerName, String billingCustomerCode, String billingCustomerName, String comments) {
        this.externalId = externalId;
        this.locationCode = locationCode;
        this.priority = priority;
        this.status = status;
        this.orderComments = comments;
        this.shippingCustomerCode = shippingCustomerCode;
        this.shippingCustomerName = shippingCustomerName;
        this.shippingMethod = shippingMethod;
        this.billCustomerCode = billingCustomerCode;
        this.billCustomerName = billingCustomerName;
        var query = DatabaseQueries.insertBioProOrderWithDetails(externalId, locationCode, orderController.getPriorityValue(priority), priority, status, shipmentType, shippingMethod, productCategory, desiredShipDate, shippingCustomerCode, shippingCustomerName, billingCustomerCode, billingCustomerName, comments);
        databaseService.executeSql(query).block();
    }

    @Given("I have more than {int} Biopro Orders.")
    public void createMultipleBioproOrders(int quantity) {
        for (int i = 0; i <= quantity; i++) {
            var priority = orderController.getRandomPriority();
            var externalId = "EXT_" + i;
            var query = DatabaseQueries.insertBioProOrder(externalId, "MDL_HUB_1", priority.getValue(), priority.getKey(), "OPEN");
            databaseService.executeSql(query).block();
        }
    }

    @And("I am logged in the location {string}.")
    public void loginAtLocation(String locationCode) throws InterruptedException {
        homePage.goTo(locationCode);
        isLoggedIn = true;
    }

    @When("I choose search orders.")
    public void navigateSearchOrdersPage() throws InterruptedException {
        if (!isLoggedIn) {
            homePage.goTo();
        }
        searchOrderPage.goTo();
    }

    @And("I have setup the order priority {string} color configuration as {string}.")
    public void setupOrderPriorityColor(String priority, String color) {
        var colorHex = orderController.getColorHex(color);
        var query = DatabaseQueries.updatePriorityColor(priority, colorHex);
        databaseService.executeSql(query).block();
    }

    @Then("I should see the order details.")
    public void checkOrderDetails() throws InterruptedException {
        searchOrderPage.validateOrderDetails(this.externalId, this.status, this.priority);
    }

    @And("I should see the priority colored as {string}")
    public void checkPriorityColor(String color) {
        var priorityElement = searchOrderPage.getPriorityElement(this.externalId, this.priority);

        var actualColor = priorityElement.getCssValue("background-color");

        var expectedColorHex = orderController.getColorHex(color);
        var expectedColorRGB = testUtils.convertHexToRGBA(expectedColorHex);
        log.info("Expected color: {}", expectedColorRGB);
        log.info("Actual color: {}", actualColor);
        Assert.assertEquals(expectedColorRGB, actualColor);
    }

    @And("I should see an option to see the order details.")
    public void checkOrderDetailsOption() {
        searchOrderPage.verifyOrderDetailsOption(this.externalId);
    }

    @Then("I should not see the the biopro order in the list of orders.")
    public void checkOrderNotExists() {
        searchOrderPage.verifyOrderNotExists(this.externalId);
    }

    @Then("I should see the list of orders based on priority and status.")
    public void checkOrderList() {
        searchOrderPage.verifyPriorityOrderList();
    }

    @And("I should not see more than {int} orders in the list.")
    public void checkOrderListSize(int expectedSize) {
        var actualSize = searchOrderPage.getOrderPriorityList().size();
        Assert.assertTrue(actualSize <= expectedSize);
    }

    @When("I navigate to the order details page.")
    public void navigateToOrderDetails() {
        this.orderId = Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(this.externalId)).first().block().get("id").toString());
        orderDetailsPage.goToOrderDetails(orderId);
    }

    @And("I can see the order details card filled with the order details.")
    public void checkOrderDetailsCard() {
        orderDetailsPage.verifyOrderDetailsCard(this.externalId, this.orderId, this.priority, this.status, this.orderComments);
    }

    @And("I can see the shipping information card filled with the shipping information.")
    public void checkShippingInformationCard() {
        orderDetailsPage.verifyShippingInformationCard(this.shippingCustomerCode, this.shippingCustomerName, this.shippingMethod);
    }

    @And("I can see the billing information card filled with the billing information.")
    public void checkBillingInformationCard() {
        orderDetailsPage.verifyBillingInformationCard(this.billCustomerCode, this.billCustomerName);
    }

    @And("I can see the Product Details section filled with the product details.")
    public void checkProductDetailsSection() {
        var productFamilyDescription = this.productFamily.replace("_", " ");
        orderDetailsPage.verifyProductDetailsSection(productFamilyDescription, this.bloodType, this.quantity, this.productComments);
    }

    @When("I choose to generate the Pick List.")
    public void whenIChooseViewPickList() {
        orderDetailsPage.openViewPickListModal();
    }

    @Then("I am able to view the correct Order Details.")
    public void matchOrderDetails() {
        var shipmentDetails = this.orderDetailsPage.getShipmentDetailsTableContent();
        Assert.assertNotNull(shipmentDetails);
        Assert.assertEquals(this.orderId.toString(), shipmentDetails.get("orderNumber"));
        Assert.assertEquals(this.shippingCustomerCode, shipmentDetails.get("shippingCustomerCode"));
        Assert.assertEquals(this.shippingCustomerName, shipmentDetails.get("customerName"));
    }

    @And("I am able to view the correct Shipment Details for the {string} product.")
    public void matchProductDetails(String familyDescription) {
        var productDetails = this.orderDetailsPage.getProductDetailsTableContent();
//        log.info("productDetails {}", this.shipmentDetailType.getItems());
//        log.info("Map Details {}", productDetails);
//        Assert.assertNotNull(productDetails);
//        if (this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()) {
//            this.shipmentDetailType.getItems().forEach(item -> {
//                var mapKey = item.getQuantity() + ":" + familyDescription + ":" + item.getBloodType();
//                log.info("comparing key {}", mapKey);
//                Assert.assertNotNull(productDetails.get(mapKey));
//            });
//        }
    }

    @And("I am able to view the correct Shipment Details with short date products for the {string} family.")
    public void matchProductDetailsWithShortDate(String familyDescription) {
        var productDetails = this.orderDetailsPage.getProductDetailsTableContent();
        var shortDateDetails = this.orderDetailsPage.getShortDateProductDetailsTableContent();

//        log.info("productDetails {}", this.shipmentDetailType.getItems());
//        log.info("Map Details {}", productDetails);
//        log.info("Short Date Map Details {}", shortDateDetails);
//        Assert.assertNotNull(productDetails);
//        if (this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()) {
//            this.shipmentDetailType.getItems().forEach(item -> {
//                var mapKey = item.getQuantity() + ":" + familyDescription + ":" + item.getBloodType();
//                log.info("comparing key {}", mapKey);
//                Assert.assertNotNull(productDetails.get(mapKey));
//                if (item.getShortDateProducts() != null && !item.getShortDateProducts().isEmpty()) {
//                    item.getShortDateProducts().forEach(shortDateItem -> {
//                        var mapShortDateKey = shortDateItem.getUnitNumber() + ":" + shortDateItem.getProductCode() + ":" + item.getBloodType();
//                        log.info("Comparing Short Date key {}", mapShortDateKey);
//                        Assert.assertNotNull(shortDateDetails.get(mapShortDateKey));
//                    });
//                }
//            });
//        }
    }

    @And("I should see a message {string} indicating There are no suggested short-dated products.")
    public void matchNoShortDateProductsMessage(String message) {
        graphql.Assert.assertTrue(message.equals(this.orderDetailsPage.getNoShortDateMessageContent()));
    }
}
