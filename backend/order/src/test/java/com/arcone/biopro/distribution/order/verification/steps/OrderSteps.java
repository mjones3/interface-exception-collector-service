package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import com.arcone.biopro.distribution.order.verification.controllers.OrderTestingController;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import com.arcone.biopro.distribution.order.verification.pages.order.HomePage;
import com.arcone.biopro.distribution.order.verification.pages.order.OrderDetailsPage;
import com.arcone.biopro.distribution.order.verification.pages.order.SearchOrderPage;
import com.arcone.biopro.distribution.order.verification.support.ApiHelper;
import com.arcone.biopro.distribution.order.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.order.verification.support.DatabaseService;
import com.arcone.biopro.distribution.order.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.order.verification.support.SharedContext;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import com.arcone.biopro.distribution.order.verification.support.Topics;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLQueryMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class OrderSteps {

    //    Order details
    private String priority;
    private String status;
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
    private String[] productFamilies;
    private String[] bloodTypes;
    private String[] quantityList;
    private String[] commentsList;

    private JSONObject partnerOrder;

    private JSONObject partnerModifyOrder;
    private boolean isLoggedIn = false;
    private JSONObject orderShipment;

    @Autowired
    OrderTestingController orderController;

    @Autowired
    private SharedContext context;

    @Autowired
    private ApiHelper apiHelper;

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

    private PageDTO<JsonNode> response;

    private static final String HAS = "has";
    private static final String HAS_NOT = "has no";


    // Modify request tables
    private DataTable originalOrderTable;
    private DataTable modifiedOrderTable;
    private Map<String, Integer> orderIdMap;

    private static final String ASCENDING = "ascending";
    private static final String DESCENDING = "descending";

    private static final String NULL_VALUE = "NULL_VALUE";
    private static final String CURRENT_DATE = "CURRENT_DATE";
    private static final String CURRENT_DATE_TIME = "CURRENT_DATE_TIME";

    private JsonNode originalOrder;

    @When("I want to list orders for location {string}.")
    public void searchOrders(String locationCode) {
        response = apiHelper.graphQlPageRequest(GraphQLQueryMapper.listOrdersByLocation(locationCode), "searchOrders");
    }

    @Then("I should have orders listed in the following order.")
    public void iShouldHaveOrdersListedInTheFollowingOrder(DataTable table) {
        checkOrdersResponseList(table, response);
    }

    @Given("I have received an order inbound request with externalId {string}, shipping method as {string} and content {string}.")
    public void postOrderReceivedEvent(String externalId, String shippingMethod, String jsonFileName) throws Exception {
        context.setExternalId(externalId);
        var jsonContent = testUtils.getResource(jsonFileName);
        var newDesiredShippingDate = LocalDate.now().plusDays(
            new Random().nextInt(10) + 1
        ).toString();
        jsonContent = jsonContent.replace("\"DESIRED_DATE\"", "\"" + newDesiredShippingDate + "\"")
            .replace("{EXTERNAL_ID}", externalId);
        if (shippingMethod != null && !shippingMethod.isBlank()) {
            jsonContent = jsonContent.replace("{SHIPPING_METHOD}", shippingMethod);
        }
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        orderController.createOrderInboundRequest(jsonContent, eventPayload);
    }

    @Given("I have received an order inbound request with externalId {string} and content {string}.")
    public void postOrderReceivedEvent(String externalId, String jsonFileName) throws Exception {
        this.postOrderReceivedEvent(externalId, null, jsonFileName);
    }

    @Given("I have received an order inbound request with externalId {string}, content {string}, and desired shipping date {string}.")
    public void postOrderReceivedEventPast(String externalId, String jsonFileName, String date) throws Exception {
        context.setExternalId(externalId);
        var dateValue = "";
        if (NULL_VALUE.equals(date)) {
            dateValue = "null";
        } else if (CURRENT_DATE.equals(date)) {
            dateValue = "\"" + LocalDate.now() + "\"";
        } else {
            dateValue = "\"" + date + "\"";
        }

        var jsonContent = testUtils.getResource(jsonFileName);
        jsonContent = jsonContent.replace("\"DESIRED_DATE\"", dateValue)
            .replace("{EXTERNAL_ID}", externalId);
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        orderController.createOrderInboundRequest(jsonContent, eventPayload);
    }


    @When("The system process the order request.")
    public void waitForProcess() throws InterruptedException {
        Thread.sleep(kafkaWaitingTime);
    }

    @Then("A biopro Order will be available in the Distribution local data store.")
    public void checkOrderExists() {
        var query = DatabaseQueries.getOrderId(context.getExternalId());
        var orderIdData = databaseService.fetchData(query);
        var orderId = Integer.valueOf(orderIdData.first().block().get("id").toString());
        orderController.getOrderDetails(orderId);
        var order = context.getOrderDetails();
        Assert.assertNotNull(order.get("transactionId"));
    }

    @Then("A biopro Order {string} be available in the Distribution local data store.")
    public void checkOrderDoesNotExist(String action) {
        var query = DatabaseQueries.countOrdersByExternalId(context.getExternalId());
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        if ("should not".equalsIgnoreCase(action)) {
            Assert.assertEquals(0L, records.get("count"));
        } else if ("should".equalsIgnoreCase(action)) {
            Assert.assertEquals(1L, records.get("count"));
        } else {
            Assert.fail("unexpected action: " + action);
        }
    }

    @Then("The duplicated biopro Order will not be available in the Distribution local data store.")
    public void checkDuplicatedOrderDoesNotExist() {
        var query = DatabaseQueries.countOrdersByExternalId(context.getExternalId());
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(1L, records.get("count"));
    }

    @Given("I have a Biopro Order with externalId {string}, Location Code {string}, Priority {string} and Status {string}.")
    public void createBioproOrder(String externalId, String locationCode, String priority, String status) {
        context.setExternalId(externalId);
        context.setLocationCode(locationCode);
        this.priority = priority;
        this.status = status;
        var query = DatabaseQueries.insertBioProOrder(context.getExternalId(), locationCode, orderController.getPriorityValue(priority.replace('-', '_')), priority.replace('-', '_'), status);
        databaseService.executeSql(query).block();
        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString()));
        context.setOrderNumber(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderNumber(context.getOrderId().toString())).first().block().get("order_number").toString()));
        Assert.assertNotNull(context.getOrderId());
    }

    @Given("I have a Biopro Order with id {string}, externalId {string}, Location Code {string}, Priority {string} and Status {string}.")
    public void createBioproOrder(String id, String externalId, String locationCode, String priority, String status) {
        context.setOrderId(Integer.valueOf(id));
        context.setExternalId(externalId);
        context.setLocationCode(locationCode);
        this.priority = priority;
        this.status = status;
        var query = DatabaseQueries.insertBioProOrder(context.getOrderId(), externalId, locationCode, orderController.getPriorityValue(priority), priority, status);
        databaseService.executeSql(query).block();
    }

    @Given("I have this/these BioPro Order(s).")
    public void createBioproOrder(DataTable table) {
        originalOrderTable = table;
        orderIdMap = new HashMap<>();
        var headers = table.row(0);
        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);
            context.setExternalId(row.get(headers.indexOf("External ID")));
            context.setLocationCode(row.get(headers.indexOf("Location Code")));
            this.priority = row.get(headers.indexOf("Priority"));
            this.status = row.get(headers.indexOf("Status"));
            var desireShipDate = row.get(headers.indexOf("Desired Shipment Date")).equals("NULL_VALUE") ? null : "'" + row.get(headers.indexOf("Desired Shipment Date")) + "'";

            var query = DatabaseQueries.insertBioProOrder(context.getExternalId(), context.getLocationCode(), orderController.getPriorityValue(priority), priority, status, desireShipDate
                , row.get(headers.indexOf("Customer Code")), row.get(headers.indexOf("Ship To Customer Name")), row.get(headers.indexOf("Create Date")));
            databaseService.executeSql(query).block();

            var orderId = Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString());
            orderIdMap.put(context.getExternalId(), orderId);

            // Will keep the last order id
            context.setOrderId(orderId);
        }

    }

    @And("I have an order item with product family {string}, blood type {string}, quantity {int}, and order item comments {string}.")
    public void createOrderItem(String productFamily, String bloodType, Integer quantity, String comments) {
        this.productFamily = productFamily;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.productComments = comments;
        var query = DatabaseQueries.insertBioProOrderItem(context.getExternalId(), productFamily, bloodType, quantity, comments);
        databaseService.executeSql(query).block();
    }

    @And("I have {int} order items with product families {string}, blood types {string}, quantities {string}, and order item comments {string}.")
    public void createMultipleOrderItem(Integer itemQuantity, String productFamily, String bloodType, String quantities, String comments) {
        this.productFamilies = testUtils.getCommaSeparatedList(productFamily);
        this.bloodTypes = testUtils.getCommaSeparatedList(bloodType);
        this.quantityList = testUtils.getCommaSeparatedList(quantities);
        this.commentsList = testUtils.getCommaSeparatedList(comments);
        for (int i = 0; i < itemQuantity; i++) {
            var query = DatabaseQueries.insertBioProOrderItem(context.getExternalId(), productFamilies[i], bloodTypes[i], Integer.parseInt(quantityList[i]), commentsList[i]);
            databaseService.executeSql(query).block();
        }
    }

    @Given("I have a Biopro Order with externalId {string}, Location Code {string}, Priority {string}, Status {string}, shipment type {string}, delivery type {string}, shipping method {string}, product category {string}, desired ship date {string}, shipping customer code and name as {string} and {string}, billing customer code and name as {string} and {string}, and comments {string}.")
    public void createBioproOrderWithDetails(String externalId, String locationCode, String priority, String status, String shipmentType, String deliveryType, String shippingMethod, String productCategory, String desiredShipDate, String shippingCustomerCode, String shippingCustomerName, String billingCustomerCode, String billingCustomerName, String comments) {
        context.setExternalId(externalId);
        context.setLocationCode(locationCode);
        this.priority = priority;
        this.status = status;
        this.orderComments = comments;
        this.shippingCustomerCode = shippingCustomerCode;
        this.shippingCustomerName = shippingCustomerName;
        this.shippingMethod = shippingMethod;
        this.billCustomerCode = billingCustomerCode;
        this.billCustomerName = billingCustomerName;
        var query = DatabaseQueries.insertBioProOrderWithDetails(context.getExternalId(), locationCode, orderController.getPriorityValue(priority), priority, status, shipmentType, shippingMethod, productCategory, desiredShipDate, shippingCustomerCode, shippingCustomerName, billingCustomerCode, billingCustomerName, comments);
        databaseService.executeSql(query).block();

        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString()));
        Assert.assertNotNull(context.getOrderId());

        if (status.equals("IN_PROGRESS")) {
            var queryOrderShipment = DatabaseQueries.insertBioProOrderShipment(context.getOrderId().toString());
            databaseService.executeSql(queryOrderShipment).block();
        }
    }

    @Given("I have more than {int} Biopro Orders.")
    public void createMultipleBioproOrders(int quantity) {
        createMultipleBioproOrders(quantity, "EXT20RECORDS");
    }

    private void createMultipleBioproOrders(int quantity, String externalIdPrefix) {
        for (int i = 0; i <= quantity; i++) {
            var priority = orderController.getRandomPriority();
            var externalId = externalIdPrefix + i;
            var query = DatabaseQueries.insertBioProOrder(externalId, "123456789", priority.getValue(), priority.getKey(), "OPEN");
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
        searchOrderPage.validateOrderDetails(context.getExternalId(), OrderTestingController.OrderStatusMap.valueOf(this.status).getDescription(), this.priority);
    }

    @And("I should see the priority colored as {string}")
    public void checkPriorityColor(String color) {
        var priorityElement = searchOrderPage.getPriorityElement(context.getExternalId(), this.priority);

        var actualColor = priorityElement.getCssValue("background-color");

        var expectedColorHex = orderController.getColorHex(color);
        var expectedColorRGB = testUtils.convertHexToRGBA(expectedColorHex);
        log.info("Expected color: {}", expectedColorRGB);
        log.info("Actual color: {}", actualColor);
        Assert.assertEquals(expectedColorRGB, actualColor);
    }

    @And("I should see an option to see the order details.")
    public void checkOrderDetailsOption() {
        searchOrderPage.verifyOrderDetailsOption(context.getExternalId());
    }

    @Then("I should not see the biopro order in the list of orders.")
    public void checkOrderNotExists() {
        searchOrderPage.verifyOrderNotExists(context.getExternalId());
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
    public void navigateToOrderDetails() throws InterruptedException {
        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString()));
        orderDetailsPage.goToOrderDetails(context.getOrderId());
    }

    @And("I can see the order details card filled with the order details.")
    public void checkOrderDetailsCard() {
        orderDetailsPage.verifyOrderDetailsCard(context.getExternalId(), context.getOrderId(), this.priority, this.status, this.orderComments);
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

    @And("I can see the Product Details section filled with all the product details.")
    public void checkAllProductDetailsSection() {
        for (int i = 0; i < this.productFamilies.length; i++) {
            var productFamilyDescription = this.productFamilies[i].replace("_", " ");
            orderDetailsPage.verifyProductDetailsSection(productFamilyDescription, this.bloodTypes[i], Integer.parseInt(this.quantityList[i]), this.commentsList[i]);
        }
    }

    @And("I can see the number of Available Inventories for each line item.")
    public void checkAvailableInventory() {
        orderDetailsPage.checkAvailableInventory(this.productFamilies, this.bloodTypes, this.quantityList);
    }

    @When("I choose to generate the Pick List.")
    public void whenIChooseViewPickList() {
        orderDetailsPage.openViewPickListModal();
    }

    @Then("I am able to view the correct Order Details.")
    public void matchOrderDetails() {
        var shipmentDetails = this.orderDetailsPage.getShipmentDetailsTableContent();
        Assert.assertNotNull(shipmentDetails);
        Assert.assertEquals(context.getOrderId().toString(), shipmentDetails.get("orderNumber"));
        Assert.assertEquals(this.shippingCustomerCode, shipmentDetails.get("shippingCustomerCode"));
        Assert.assertEquals(this.shippingCustomerName, shipmentDetails.get("customerName"));
    }

    @And("I should see a message {string} indicating There are no suggested short-dated products.")
    public void matchNoShortDateProductsMessage(String message) {
        Assert.assertEquals(message, this.orderDetailsPage.getNoShortDateMessageContent());
    }

    @Then("I can see the pick list details.")
    public void checkPickListDetails() {
        orderDetailsPage.verifyPickListHeaderDetails(context.getOrderId().toString(), this.shippingCustomerCode, this.shippingCustomerName, this.orderComments);
        orderDetailsPage.verifyPickListProductDetails(
            this.productFamilies != null ? this.productFamilies : new String[]{this.productFamily},
            this.bloodTypes != null ? this.bloodTypes : new String[]{this.bloodType},
            this.quantityList != null ? this.quantityList : new String[]{this.quantity.toString()},
            this.commentsList != null ? this.commentsList : new String[]{this.productComments}
        );
    }

    @And("I {string} see the short date product details.")
    public void checkShortDateProductDetails(String should) {
        if (should.equals("CAN")) {
            orderDetailsPage.verifyShortDateProductDetails(true);
        } else if (should.equals("CANNOT")) {
            orderDetailsPage.verifyShortDateProductDetails(false);
        } else {
            Assert.fail("Invalid option for short date product details.");
        }
    }

    @When("I close the pick list.")
    public void closePickList() {
        orderDetailsPage.closePickListModal();
    }


    @Given("I have received a shipment created event.")
    public void postShipmentCreatedEvent() throws Exception {
        context.setOrderNumber(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderNumber(context.getOrderId().toString())).first().block().get("order_number").toString()));
        var jsonContent = testUtils.getResource("shipment-created-event-automation.json");
        jsonContent = jsonContent.replace("{order-number}", context.getOrderNumber().toString());
        var eventPayload = objectMapper.readValue(jsonContent, ShipmentCreatedEventDTO.class);

        createShipmentCreatedRequest(jsonContent, eventPayload);
    }


    @And("I should see the shipment details.")
    public void checkShipmentDetails() throws JSONException {
        orderDetailsPage.verifyShipmentTable(orderShipment);
    }

    @And("I should see an option to navigate to the shipment details page.")
    public void checkShipmentDetailsOption() {
        orderDetailsPage.verifyShipmentDetailsButton();
    }

    @And("The order status is {string}.")
    public void verifyOrderStatus(String orderStatus) {
        orderDetailsPage.verifyOrderStatus(orderStatus);
    }

    @And("I should not see multiple shipments generated.")
    public void verifyMultipleShipments() {
        Assert.assertFalse(orderDetailsPage.verifyHasMultipleShipments());
    }

    @Given("I have received a shipment completed event.")
    public void postShipmentCompletedEvent() throws Exception {
        context.setOrderId(Integer.valueOf(
            Objects.requireNonNull(
                databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block()).get("id").toString()));
        var jsonContent = testUtils.getResource("shipment-completed-event.json");
        jsonContent = jsonContent.replace("{order-number}", context.getOrderId().toString());
        var eventPayload = objectMapper.readValue(jsonContent, ShipmentCompletedEventDTO.class);

        createShipmentCompletedRequest(jsonContent, eventPayload);
    }

    @Then("I can see the pending log of products is updated with {int} product\\(s) out of {int}.")
    public void checkPendingLogNotZero(Integer filledProducts, Integer totalProducts) {
        orderDetailsPage.assertFilledProductIs(filledProducts);
        orderDetailsPage.assertTotalProductIs(totalProducts);
    }

    //    Common methods

    private void createShipmentCreatedRequest(String jsonContent, ShipmentCreatedEventDTO eventPayload) throws JSONException {
        orderShipment = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", orderShipment);
        Assert.assertNotNull(orderShipment);
        var event = kafkaHelper.sendEvent(eventPayload.eventId().toString(), eventPayload, Topics.SHIPMENT_CREATED).block();
        Assert.assertNotNull(event);
    }

    private void createShipmentCompletedRequest(String jsonContent, ShipmentCompletedEventDTO eventPayload) throws JSONException {
        orderShipment = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", orderShipment);
        Assert.assertNotNull(orderShipment);
        var event = kafkaHelper.sendEvent(eventPayload.eventId().toString(), eventPayload, Topics.SHIPMENT_COMPLETED).block();
        Assert.assertNotNull(event);
    }

    @And("I can see the Filled Products section filled with {string} shipped products.")
    public void checkFilledProducts(String shippedProducts) {
        var shippedQuantityList = testUtils.getCommaSeparatedList(shippedProducts);
        for (int i = 0; i < shippedQuantityList.length; i++) {
            orderDetailsPage.verifyFilledProductsSection(this.productFamilies[i].replace("_", " "), this.bloodTypes[i], this.quantityList[i], shippedQuantityList[i]);
        }
    }

    @And("I can see the shipment status as {string}.")
    public void checkShipmentStatus(String status) {
        orderDetailsPage.verifyShipmentStatus(status.toUpperCase());
    }

    @Then("I cannot see the progress status bar.")
    public void checkProgressBarNotExists() {
        orderDetailsPage.verifyProgressBarNotExists();
    }

    @And("I search the order by {string}.")
    public void iSearchTheOrderBy(String value) throws InterruptedException {
        if (value.equalsIgnoreCase("orderId")) {
            searchOrderPage.searchOrder(context.getOrderId().toString());
        } else if (value.equalsIgnoreCase("externalId")) {
            searchOrderPage.searchOrder(context.getExternalId());
        } else {
            searchOrderPage.searchOrder(value);
        }
    }

    @When("I open the search orders filter panel.")
    public void iOpenTheSearchPanel() throws InterruptedException {
        searchOrderPage.openTheSearchPanel();
    }

    @When("I choose {string} option.")
    public void iChooseApplyOption(String valueOption) throws InterruptedException {
        if (valueOption.equalsIgnoreCase("apply")) {
            searchOrderPage.iChooseApplyOption();
        } else {
            searchOrderPage.iChooseResetOption();
        }
    }

    @Then("I should see {int} orders in the search results.")
    public void iShouldSeeOrdersInTheSearchResults(int quantity) {
        Assert.assertEquals(quantity, searchOrderPage.tableRowsCount());
    }

    @Then("I should be redirected to the order details page.")
    public void iShouldBeRedirectedToTheOrderDetailsPage() {
        searchOrderPage.checkIfDetailsPageIsOpened();
    }

    @And("I should see {string} fields as required.")
    public void areFieldsRequired(String valueFields) throws InterruptedException {
        searchOrderPage.checkRequiredFields(valueFields);
    }

    @And("I should see {string} fields.")
    public void allFieldsDisplayed(String valueFields) throws InterruptedException {
        searchOrderPage.checkForFieldsVisibility(valueFields);
    }

    @And("{string} fields are {string}.")
    public void theFieldsHaveTheStatus(String valueFields, String valueStatus) throws InterruptedException {
        Arrays.stream(valueFields.split(",")).map(String::trim).forEach
            (valueField -> {
                try {
                    theFieldHaveTheStatus(valueField, valueStatus);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @And("{string} field is {string}.")
    public void theFieldHaveTheStatus(String valueField, String valueStatus) throws InterruptedException {
        searchOrderPage.checkIfEnabledOrDisabled(valueField, valueStatus.equalsIgnoreCase("enabled"));
    }

    @And("{string} option is {string}.")
    public void theOptionHaveTheStatus(String valueOption, String valueStatus) throws InterruptedException {
        searchOrderPage.checkIfOptionHasStatus(valueOption, valueStatus);
    }


    @And("I select {string} for the {string}.")
    public void iSelectValuesForTheDropdown(String values, String dropdown) {
        searchOrderPage.selectOptionsForDropdownDescription(values, dropdown);
    }


    @And("Items {string} should be selected for {string}.")
    public void itemsShouldBeSelected(String values, String dropdown) {
        searchOrderPage.checkSelectedValuesFromDropdownDescription(values, dropdown);
    }

    private void setValueForField(String value, String fieldName) throws InterruptedException {
        searchOrderPage.setValueForField(value, fieldName);
    }

    @When("I enter the date: {string} for the field {string} and the date: {string}  for the field {string}.")
    public void iEnterTheDateForTheFieldAndTheDateForTheField(String fromDate, String initialDateField, String toDate, String finalDateField) throws InterruptedException {
        setValueForField(fromDate, initialDateField);
        setValueForField(toDate, finalDateField);
    }


    @When("I enter a future date for the field {string}.")
    public void iEnterFutureDateForTheField(String fieldName) throws InterruptedException {
        setValueForField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "create date from");
        setValueForField(1 + "/" + 1 + "/" + Year.now().getValue() + 1, fieldName);
    }

    @When("I enter a past date: {string} for the field {string}.")
    public void iEnterPastDateForTheField(String value, String fieldName) throws InterruptedException {
        setValueForField(value, fieldName);
        setValueForField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "create date to");
    }


    @Then("The filter information should be empty.")
    public void theFilterInformationShouldBeEmpty() {
        searchOrderPage.theOrderNumberFieldShouldHaveEmptyValue();
    }

    @Then("I should see a validation message: {string}.")
    public void iShouldSeeAValidationMessage(String message) {
        searchOrderPage.iShouldSeeAValidationMessage(message);
    }


    @And("I should see {string} as the number of used filters for the search.")
    public void iShouldSeeAsTheNumberOfUsedFiltersForTheSearch(String value) {

        searchOrderPage.checkNumberOfUsedFiltersForTheSearch(value);


    }

    @And("I should not see {string}.")
    public void iShouldNotSee(String externalOrderIds) {
        Arrays.stream(externalOrderIds.split(",")).map(String::trim).forEach
            (externalOrderId -> {
                searchOrderPage.verifyOrderNotExists(externalOrderId);
            });
    }

    @And("I should see {string} orders in the search results.")
    public void iShouldSeeOrdersInTheSearchResults(String externalOrderIds) {
        Arrays.stream(externalOrderIds.split(",")).map(String::trim).forEach
            (externalOrderId -> {
                searchOrderPage.verifyOrderExists(externalOrderId);
            });
    }

    @And("I can see the Temperature Category as {string}.")
    public void iCanSeeTheTemperatureCategoryAs(String category) {
        orderDetailsPage.verifyTemperatureCategory(category);
    }

    @And("I select the current date as the {string} range")
    public void iSelectTheCurrentDateAsThe(String fieldRangeName) throws InterruptedException {
        setValueForField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), fieldRangeName + " from");
        setValueForField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), fieldRangeName + " to");
    }

    @And("I select the {string} as the {string} range")
    public void iSelectTheDateAsThe(String date, String fieldRangeName) throws InterruptedException {
        setValueForField(date, fieldRangeName + " from");
        setValueForField(date, fieldRangeName + " to");
    }

    @And("I have another Biopro Order with the externalId equals to order number of the previous order.")
    public void iHaveAnotherBioproOrderWithTheExternalIdEqualsToOrderNumberOfThePreviousOrder() {
        var query = DatabaseQueries.insertBioProOrder(context.getOrderNumber().toString(), context.getLocationCode(), orderController.getPriorityValue(priority), priority.replace('-', '_'), status);
        databaseService.executeSql(query).block();
    }

    @Given("I have an order with external ID {string} partially fulfilled with a shipment {string}.")
    public void iHaveAnOrderPartiallyFulfilledWithAShipment(String externalId, String shipmentStatus) {
        iHaveAOrderPartiallyFulfilledWithAShipment("IN_PROGRESS", externalId, shipmentStatus);
    }

    @Given("I have a {string} order with external ID {string} partially fulfilled with a shipment {string}.")
    public void iHaveAOrderPartiallyFulfilledWithAShipment(String orderStatus, String externalId, String shipmentStatus) {
        context.setExternalId(externalId);
        context.setOrderStatus(orderStatus);
        var priority = orderController.getRandomPriority();

        // Order
        var createOrderQuery = DatabaseQueries.insertBioProOrder(context.getExternalId(), context.getLocationCode(), priority.getValue(), priority.getKey(), orderStatus);
        databaseService.executeSql(createOrderQuery).block();

        // Order Item
        var createOrderItemQuery = DatabaseQueries.insertBioProOrderItem(context.getExternalId(), "PLASMA_TRANSFUSABLE", "A", 10, "Comments");
        databaseService.executeSql(createOrderItemQuery).block();

        // Order Shipment
        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString()));
        var createShipmentQuery = DatabaseQueries.insertBioProOrderShipment(context.getOrderId().toString(), shipmentStatus);
        databaseService.executeSql(createShipmentQuery).block();
    }

    @When("I request to complete the order.")
    public void iRequestToCompleteTheOrder() throws InterruptedException {
        Map completeOrderRequest = orderController.completeOrder(context.getOrderId(), context.isBackOrderConfig());
        try {
            Map orderStatus = (Map) completeOrderRequest.get("data");
            context.setOrderStatus(orderStatus.get("status").toString());
            Thread.sleep(kafkaWaitingTime);
        } catch (NullPointerException e) {
            log.error("Order complete request failed: {}", e.getMessage());
        }
    }

    @Then("The order status should be {string}.")
    public void theOrderStatusShouldBe(String orderStatus) {
        Assert.assertEquals(orderStatus, context.getOrderStatus());
    }

    @Given("I have an order with external ID {string} and status {string}.")
    public void iHaveAnOrderWithStatus(String externalId, String orderStatus) {
        context.setExternalId(externalId);
        context.setOrderStatus(orderStatus);
        var priority = orderController.getRandomPriority();

        // Order
        var createOrderQuery = DatabaseQueries.insertBioProOrder(context.getExternalId(), context.getLocationCode(), priority.getValue(), priority.getKey(), orderStatus);
        databaseService.executeSql(createOrderQuery).block();
        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(context.getExternalId())).first().block().get("id").toString()));

        // Order Item
        var createOrderItemQuery = DatabaseQueries.insertBioProOrderItem(context.getExternalId(), "PLASMA_TRANSFUSABLE", "A", 10, "Comments");
        databaseService.executeSql(createOrderItemQuery).block();
    }

    @And("I choose to complete the order.")
    public void iChooseToCompleteTheOrder() {
        orderDetailsPage.completeOrder();
    }

    @Then("I should be prompted to confirm to complete the order.")
    public void iShouldBePromptedToConfirmToCompleteTheOrder() {
        Assert.assertTrue(orderDetailsPage.verifyCompleteOrderConfirmation());
    }

    @When("I confirm to complete the order with the reason {string}.")
    public void iConfirmToCompleteTheOrderWithTheReason(String comment) {
        orderDetailsPage.confirmCompleteOrder(comment);
    }

    @And("I define the backorder creation option as {string}.")
    public void iDefineTheBackorderCreationOptionAs(String createBackOrderOption) {
        boolean createBkOrderOption = createBackOrderOption.equalsIgnoreCase("true");
        orderDetailsPage.defineBackOrderOption(createBkOrderOption);
    }

    @When("I request the order details.")
    public void iRequestTheOrderDetails() {
        orderController.getOrderDetails(context.getOrderId());
    }

    @Then("I {string} have an option to create a back order.")
    public void iShouldHaveTheBackOrderConfigurationAs(String backOrderConfig) {
        if (backOrderConfig.equalsIgnoreCase("should")) {
            Assert.assertEquals(true, context.getOrderDetails().get("backOrderCreationActive"));
        } else if (backOrderConfig.equalsIgnoreCase("should not")) {
            Assert.assertEquals(false, context.getOrderDetails().get("backOrderCreationActive"));
        } else {
            Assert.fail("Invalid option for back order configuration.");
        }
    }

    @And("I {string} have {int} remaining products as part of the back order created.")
    public void iHaveRemainingProductsAsPartOfTheBackOrderCreated(String choice, Integer quantity) throws InterruptedException {
        Thread.sleep(kafkaWaitingTime);
        orderController.listOrdersByExternalId();
        originalOrder = context.getOrdersPage().content().stream().filter(order -> order.get("orderStatus").asText().equals("COMPLETED")).findFirst().orElse(null);
        Assert.assertNotNull(originalOrder);

        if (choice.equalsIgnoreCase("should")) { // Back order configured
            Assert.assertEquals(2, context.getOrdersPage().content().size());

            var backOrder = context.getOrdersPage().content().stream().filter(order -> order.get("orderStatus").asText().equals("OPEN")).findFirst().orElse(null);
            Assert.assertNotNull(backOrder);

            // Get by id backOrder
            orderController.getOrderDetails(Integer.valueOf(backOrder.get("orderId").toString()));
            var backOrderDetails = context.getOrderDetails();

            // Check remaining quantity
            Assert.assertEquals(quantity, Integer.valueOf(backOrderDetails.get("totalProducts").toString()));

        } else if (choice.equalsIgnoreCase("should not")) { // Back order not configured
            Assert.assertEquals(1, context.getOrdersPage().content().size());
        } else {
            Assert.fail("Invalid option for back order configuration.");
        }
    }

    @When("I search for orders by {string}.")
    public void iSearchForOrdersByExternalID(String key) {
        if (key.equalsIgnoreCase("externalId")) {
            orderController.listOrdersByExternalId();
        } else if (key.equalsIgnoreCase("orderId")) {
            orderController.listOrdersByOrderId();
        } else {
            Assert.fail("Invalid search key.");
        }
    }

    @When("I search for orders by {string} from {string} to {string}.")
    public void searchForOrdersByCreateDate(String searchKey, String createDateFrom, String createDateTo) {
        if (searchKey.equalsIgnoreCase("createDate")) {
            orderController.searchOrdersByCreateDate(context.getLocationCode(), createDateFrom, createDateTo);
        } else {
            Assert.fail("Invalid search key.");
        }
    }

    @Then("I should receive the search results containing {string} order(s).")
    public void iShouldReceiveTheSearchResultsContainingOrder(String expectedQuantity) {
        var ordersPage = context.getOrdersPage();
        Assert.assertEquals(Integer.parseInt(expectedQuantity), ordersPage.content().size());
    }

    @Then("I should receive the search results containing {string} order(s) with status(es) {string}.")
    public void iShouldReceiveTheSearchResultsContainingOrderAndStatuses(String expectedQuantity, String statuses) {
        var ordersPage = context.getOrdersPage();
        Assert.assertEquals(Integer.parseInt(expectedQuantity), ordersPage.content().size());
        var statusesList = testUtils.getCommaSeparatedList(statuses);
        Arrays.stream(statusesList).toList().forEach(status -> {
            Assert.assertTrue(ordersPage.content().stream().anyMatch(order -> order.get("orderStatus").asText().equals(status)));
        });
    }

    @Given("I have an order with external ID {string}, status {string} and backorder flag {string}.")
    public void iHaveAnOrderWithExternalIDStatusAndBackorderFlag(String externalId, String status, String backOrderFlag) {
        context.setExternalId(externalId);
        context.setOrderStatus(status);

        var priority = orderController.getRandomPriority();
        var query = DatabaseQueries.insertBioProOrder(externalId, context.getLocationCode(), priority.getValue(), priority.getKey(), status, Boolean.parseBoolean(backOrderFlag));
        databaseService.executeSql(query).block();

        context.setOrderId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(externalId)).first().block().get("id").toString()));

        createOrderItem("PLASMA_TRANSFUSABLE", "A", 10, "Comments");
    }

    @And("I have received a cancel order request with externalId {string}, cancel date {string} and content {string}.")
    public void iHaveReceivedACancelOrderRequestWithExternalIdAndContent(String externalId, String cancelDate, String jsonFileName) throws Exception {
        orderController.cancelOrder(externalId, cancelDate, jsonFileName);
    }

    @When("The system processes the cancel order request.")
    public void theSystemProcessesTheCancelOrderRequest() throws InterruptedException {
        Thread.sleep(kafkaWaitingTime);
    }

    @Then("The Biopro Order must have status {string}.")
    public void theBioproOrderMustHaveStatus(String expectedStatus) {
        orderController.getOrderDetails(context.getOrderId());
        Assert.assertEquals(expectedStatus, context.getOrderDetails().get("status"));
    }

    @And("I {string} be able to receive the cancel details.")
    public void iBeAbleToReceiveTheCancelDetails(String isShould) {
        orderController.getOrderDetails(context.getOrderId());

        if (isShould.equalsIgnoreCase("should")) {
            Assert.assertNotNull(context.getOrderDetails().get("cancelEmployeeId"));
            Assert.assertNotNull(context.getOrderDetails().get("cancelReason"));
            Assert.assertNotNull(context.getOrderDetails().get("cancelDate"));
            Assert.assertNotNull(context.getOrderDetails().get("transactionId"));
        } else if (isShould.equalsIgnoreCase("should not")) {
            Assert.assertNull(context.getOrderDetails().get("cancelEmployeeId"));
            Assert.assertNull(context.getOrderDetails().get("cancelReason"));
            Assert.assertNull(context.getOrderDetails().get("cancelDate"));
            Assert.assertNull(context.getOrderDetails().get("transactionId"));
        } else {
            Assert.fail("Invalid option for cancel details.");
        }
    }

    @Given("I have {int} Biopro Order\\(s).")
    public void iHaveBioproOrderS(int totalRecords) {
        this.createMultipleBioproOrders(totalRecords - 1, "EXTDIS220");
    }

    @When("I request to list the Orders.")
    public void iRequestToListTheOrders() {
        orderController.listOrdersByPage(null);
    }

    @Then("I should receive a minimum of {int} order\\(s) splitted in {int} page\\(s).")
    public void iShouldReceiveOrderSSplittedInPageS(int totalRecords, int totalPages) {
        var page = context.getOrdersPage();
        Assert.assertTrue(page.totalRecords() >= totalRecords);
        Assert.assertEquals(totalPages, page.totalPages());
    }

    @And("I confirm that the page {int} {string} {int} orders.")
    public void iConfirmThatThePageOrders(int page, String hasHasNot, int totalElements) {
        var pageIndex = page - 1;
        orderController.listOrdersByPage(pageIndex);
        var currentPage = context.getOrdersPage();
        Assert.assertEquals(pageIndex, currentPage.pageNumber());
        if (HAS.equals(hasHasNot)) {
            Assertions.assertFalse(currentPage.content().isEmpty());
            Assert.assertEquals(totalElements, currentPage.content().size());
        } else if (HAS_NOT.equals(hasHasNot)) {
            Assertions.assertTrue(currentPage.content().isEmpty());
            Assert.assertEquals(totalElements, 0);
        } else {
            Assert.fail("Invalid Option of has / has not");
        }
    }
    @And("I confirm that the page {int} {string} a minimum of {int} orders.")
    public void iConfirmThatThePageHasOrders(int page, String hasHasNot, int totalElements) {
        var pageIndex = page - 1;
        orderController.listOrdersByPage(pageIndex);
        var currentPage = context.getOrdersPage();
        Assert.assertEquals(pageIndex, currentPage.pageNumber());
        if (HAS.equals(hasHasNot)) {
            Assertions.assertFalse(currentPage.content().isEmpty());
            Assert.assertTrue(currentPage.content().size() >= totalElements);
        } else if (HAS_NOT.equals(hasHasNot)) {
            Assertions.assertTrue(currentPage.content().isEmpty());
            Assert.assertEquals(0, totalElements);
        } else {
            Assert.fail("Invalid Option of has / has not");
        }
    }

    @And("I confirm that the page {int} {string} previous page and {string} next page.")
    public void iConfirmThatThePagePreviousPageAndNextPage(int page, String hasHasNotPreviousPage, String hasHasNotNextPage) {
        var pageIndex = page - 1;
        orderController.listOrdersByPage(pageIndex);
        var currentPage = context.getOrdersPage();
        Assert.assertEquals(pageIndex, currentPage.pageNumber());

        if (HAS.equals(hasHasNotPreviousPage)) {
            Assert.assertTrue(currentPage.hasPrevious());
        } else if (HAS_NOT.equals(hasHasNotPreviousPage)) {
            Assert.assertFalse(currentPage.hasPrevious());
        } else {
            Assert.fail("Invalid Option of has / has not");
        }

        if (HAS.equals(hasHasNotNextPage)) {
            Assert.assertTrue(currentPage.hasNext());
        } else if (HAS_NOT.equals(hasHasNotNextPage)) {
            Assert.assertFalse(currentPage.hasNext());
        } else {
            Assert.fail("Invalid Option of has / has not");
        }

    }

    @When("I request to list the Orders at page {int}.")
    public void iRequestToListTheOrdersAtPage(int page) {
        orderController.listOrdersByPage(page);
        Assertions.assertNotNull(context.getOrdersPage());
    }


    @Given("I have orders with the following details.")
    public void iHaveOrdersWithTheFollowingDetails(DataTable table) {
        originalOrderTable = table;
        orderIdMap = new HashMap<>();

        // Headers -> | External ID | Status | Location Code | Delivery Type | Shipping Method | Product Category | Product Family | Blood Type | Quantity | Back Order |
        var headers = table.row(0);

        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);

            var externalId = row.get(headers.indexOf("External ID"));
            var priority = orderController.getRandomPriority();

            // Order
            var query = DatabaseQueries.insertBioProOrder(
                externalId,
                row.get(headers.indexOf("Location Code")),
                priority.getValue(),
                row.get(headers.indexOf("Delivery Type")),
                Boolean.parseBoolean(row.get(headers.indexOf("Back Order")))
                    ? "COMPLETED"
                    : row.get(headers.indexOf("Status")),
                row.get(headers.indexOf("Product Category")),
                false);
            databaseService.executeSql(query).block();

            var orderId = Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(externalId)).first().block().get("id").toString());
            orderIdMap.put(externalId, orderId);

            // Order Item
            var productFamilyList = testUtils.getCommaSeparatedList(row.get(headers.indexOf("Product Family")));
            var bloodTypeList = testUtils.getCommaSeparatedList(row.get(headers.indexOf("Blood Type")));
            var quantityList = testUtils.getCommaSeparatedList(row.get(headers.indexOf("Quantity")));
            for (var j = 0; j < productFamilyList.length; j++) {
                var queryItem = DatabaseQueries.insertBioProOrderItem(
                    externalId,
                    productFamilyList[j],
                    bloodTypeList[j],
                    Integer.parseInt(quantityList[j]),
                    "Comments");
                databaseService.executeSql(queryItem).block();
            }

            // Back Order
            if (Boolean.parseBoolean(row.get(headers.indexOf("Back Order")))) {
                var queryBack = DatabaseQueries.insertBioProOrder(
                    externalId,
                    row.get(headers.indexOf("Location Code")),
                    priority.getValue(),
                    row.get(headers.indexOf("Delivery Type")),
                    row.get(headers.indexOf("Status")),
                    row.get(headers.indexOf("Product Category")),
                    true);
                databaseService.executeSql(queryBack).block();
                var backOrderId = Integer.valueOf(databaseService.fetchData(DatabaseQueries.getOrderId(externalId)).first().block().get("id").toString());
                orderIdMap.replace(externalId, backOrderId);

                // Order Item
                for (var j = 0; j < productFamilyList.length; j++) {
                    var queryItem = DatabaseQueries.insertBioProOrderItem(
                        externalId,
                        productFamilyList[j],
                        bloodTypeList[j],
                        Integer.parseInt(quantityList[j]),
                        "Comments");
                    databaseService.executeSql(queryItem).block();
                }
            }

        }
    }

    @And("I have received modify order requests with the following details externalId.")
    public void iHaveReceivedModifyOrderRequestsWithTheFollowingDetailsExternalId(DataTable table) throws Exception {
        modifiedOrderTable = table;

        // Headers -> | Modify External ID | Modify Date | Location Code | Delivery Type | Shipping Method | Product Category | Product Family | Blood Type | Quantity | Modify Reason | Modify Employee Code |
        var headers = table.row(0);

        for (var i = 1; i < table.height(); i++) {

            var productFamilyList = testUtils.getCommaSeparatedList(table.row(i).get(headers.indexOf("Product Family")));
            var bloodTypeList = testUtils.getCommaSeparatedList(table.row(i).get(headers.indexOf("Blood Type")));
            var quantityList = testUtils.getCommaSeparatedList(table.row(i).get(headers.indexOf("Quantity")));

            orderController.modifyOrderRequest(
                table.row(i).get(headers.indexOf("Modify External ID")),
                table.row(i).get(headers.indexOf("Location Code")),
                table.row(i).get(headers.indexOf("Modify Employee Code")),
                table.row(i).get(headers.indexOf("Delivery Type")),
                table.row(i).get(headers.indexOf("Shipping Method")),
                table.row(i).get(headers.indexOf("Product Category")),
                table.row(i).get(headers.indexOf("Modify Reason")),
                table.row(i).get(headers.indexOf("Modify Date")),
                productFamilyList,
                bloodTypeList,
                quantityList,
                table.row(i).get(headers.indexOf("Transaction Id")),
                "modify-order-valid-request.json",
                "modify-order-item.json"
            );
        }
    }

    @When("The system processes the modify order requests.")
    public void theSystemProcessesTheModifyOrderRequests() {
        try {
            Thread.sleep(kafkaWaitingTime);
        } catch (InterruptedException e) {
            log.error("Error waiting for Kafka to process the modify order requests: {}", e.getMessage());
        }
    }

    @Then("The Modify order request should be processed as.")
    public void theModifyOrderRequestShouldBeProcessedAs(DataTable table) {
        // Headers -> | Modify External ID | Location Code | Should be Found? | Should be Updated? |
        var headers = table.row(0);

        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);
            var externalId = row.get(headers.indexOf("Modify External ID"));
            var locationCode = row.get(headers.indexOf("Location Code"));
            var shouldBeFound = row.get(headers.indexOf("Should be Found?"));
            var shouldBeUpdated = row.get(headers.indexOf("Should be Updated?"));
            var expectedTransactionId = row.get(headers.indexOf("Expected Transaction Id"));

            var orderDetails = orderController.getOrderDetailsMap(orderIdMap.get(externalId));
            if (shouldBeFound.equalsIgnoreCase("yes")) {
                Assert.assertNotNull(orderDetails);
                List<Map> orderItems = (List<Map>) orderDetails.get("orderItems");
                if (shouldBeUpdated.equalsIgnoreCase("yes")) {
                    // Validate order data
                    Assert.assertEquals(orderDetails.get("locationCode"), locationCode);
                    Assert.assertEquals(orderDetails.get("priority"), modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Delivery Type")));
                    Assert.assertEquals(orderDetails.get("productCategory"), modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Product Category")));
                    Assert.assertEquals(orderDetails.get("modifyReason"), modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Modify Reason")));
                    Assert.assertEquals(orderDetails.get("transactionId"), expectedTransactionId);
                    // Validate order items data
                    var productFamilyList = testUtils.getCommaSeparatedList(modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Product Family")));
                    Arrays.sort(productFamilyList);
                    var bloodTypeList = testUtils.getCommaSeparatedList(modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Blood Type")));
                    Arrays.sort(bloodTypeList);
                    var quantityList = testUtils.getCommaSeparatedList(modifiedOrderTable.row(i).get(modifiedOrderTable.row(0).indexOf("Quantity")), Integer::parseInt, Integer[]::new);
                    Arrays.sort(quantityList);
                    for (var j = 0; j < orderItems.size(); j++) {
                        // IMPORTANT: when comparing by a property, make sure to sort the comparison list by the property before asserting
                        var productFamily = testUtils.sortListOfMapByProperty(orderItems, "productFamily", String.class).get(j).get("productFamily");
                        Assert.assertEquals(productFamilyList[j], productFamily);
                        var bloodType = testUtils.sortListOfMapByProperty(orderItems, "bloodType", String.class).get(j).get("bloodType");
                        Assert.assertEquals(bloodTypeList[j], bloodType);
                        var quantity = testUtils.sortListOfMapByProperty(orderItems, "quantity", Integer.class).get(j).get("quantity");
                        Assert.assertEquals(quantityList[j], quantity);
                    }
                } else if (shouldBeUpdated.equalsIgnoreCase("no")) {
                    // Validate order data
                    Assert.assertEquals(orderDetails.get("locationCode"), originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Location Code")));
                    Assert.assertEquals(orderDetails.get("priority"), originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Delivery Type")));
                    Assert.assertEquals(orderDetails.get("productCategory"), originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Product Category")));
                    // Validate order items data
                    var productFamilyList = testUtils.getCommaSeparatedList(originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Product Family")));
                    Arrays.sort(productFamilyList);
                    var bloodTypeList = testUtils.getCommaSeparatedList(originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Blood Type")));
                    Arrays.sort(bloodTypeList);
                    var quantityList = testUtils.getCommaSeparatedList(originalOrderTable.row(i).get(originalOrderTable.row(0).indexOf("Quantity")), Integer::parseInt, Integer[]::new);
                    Arrays.sort(quantityList);
                    for (var j = 0; j < orderItems.size(); j++) {
                        // IMPORTANT: when comparing by a property, make sure to sort the comparison list by the property before asserting
                        var productFamily = testUtils.sortListOfMapByProperty(orderItems, "productFamily", String.class).get(j).get("productFamily");
                        Assert.assertEquals(productFamilyList[j], productFamily);
                        var bloodType = testUtils.sortListOfMapByProperty(orderItems, "bloodType", String.class).get(j).get("bloodType");
                        Assert.assertEquals(bloodTypeList[j], bloodType);
                        var quantity = testUtils.sortListOfMapByProperty(orderItems, "quantity", Integer.class).get(j).get("quantity");
                        Assert.assertEquals(quantityList[j], quantity);
                    }
                } else {
                    Assert.fail("Invalid option for should be updated.");
                }
            } else if (shouldBeFound.equalsIgnoreCase("no")) {
                Assert.assertNull(orderDetails);
            } else {
                Assert.fail("Invalid option for should be found.");
            }
        }
    }

    @When("I request to list the Orders sorted by {string} in {string} order.")
    public void iRequestToListTheOrdersSortedByInOrder(String property, String sortingOrder) {

        var order = "";
        if (ASCENDING.equals(sortingOrder)) {
            order = "ASC";
        } else if (DESCENDING.equals(sortingOrder)) {
            order = "DESC";
        } else {
            Assert.fail("Invalid Sorting Order");
        }

        orderController.sortOrdersByPage(0, property, order);
        Assertions.assertNotNull(context.getOrdersPage());
    }

    @Then("I should receive the orders listed in the following order.")
    public void iShouldReceiveTheOrdersListedInTheFollowingOrder(DataTable table) {
        checkOrdersResponseList(table, context.getOrdersPage());
    }

    private void checkOrdersResponseList(DataTable table, PageDTO<JsonNode> response) {
        var headers = table.row(0);

        var responseIds = response.content().stream()
            .map(r -> r.get("externalId").asText())
            .collect(Collectors.joining(","));

        var expectedIds = new ArrayList<String>();
        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);
            expectedIds.add(row.get(headers.indexOf("External ID")));
        }

        log.debug("responseIds {}", responseIds);
        log.debug("expectedIds {}", String.join(",", expectedIds));

        Assert.assertEquals(String.join(",", expectedIds), responseIds);
    }

    @And("The sorting indicator should be at {string} property in {string} order.")
    public void theSortingIndicatorShouldBeAtPropertyInOrder(String property, String sortingOrder) {

        Assert.assertEquals(property, context.getOrdersPage().querySort().orderByList().getFirst().property());

        if (ASCENDING.equals(sortingOrder)) {
            Assert.assertEquals("ASC", context.getOrdersPage().querySort().orderByList().getFirst().direction());
        } else if (DESCENDING.equals(sortingOrder)) {
            Assert.assertEquals("DESC", context.getOrdersPage().querySort().orderByList().getFirst().direction());
        } else {
            Assert.fail("Invalid Sorting Order");
        }
    }

    @Then("I should receive the orders listed by {string} in {string} order.")
    public void iShouldReceiveTheOrdersListedByInOrder(String property, String sortingOrder) {

        var expectedIds = new ArrayList<Integer>();
        for (var i = 1; i < originalOrderTable.height(); i++) {
            var row = originalOrderTable.row(i);
            expectedIds.add(orderIdMap.get(row.get(originalOrderTable.row(0).indexOf("External ID"))));
        }

        Assert.assertEquals(property, context.getOrdersPage().querySort().orderByList().getFirst().property());

        var responseIds = context.getOrdersPage().content().stream()
            .map(r -> r.get("orderNumber").asText())
            .collect(Collectors.joining(","));

        if (ASCENDING.equals(sortingOrder)) {
            Assert.assertEquals("ASC", context.getOrdersPage().querySort().orderByList().getFirst().direction());
            Collections.sort(expectedIds);
        } else if (DESCENDING.equals(sortingOrder)) {
            Assert.assertEquals("DESC", context.getOrdersPage().querySort().orderByList().getFirst().direction());
            Collections.reverse(expectedIds);
        } else {
            Assert.fail("Invalid Sorting Order");
        }

        Assert.assertEquals(expectedIds.stream().map(String::valueOf).collect(Collectors.joining(",")), responseIds);
    }

    @When("I search for orders by {string} with the value {string}.")
    public void iSearchForOrdersByWithTheValue(String searchKey, String searchValue) {
        if (searchKey.equalsIgnoreCase("orderId")) {
            context.setOrderId(Integer.valueOf(searchValue));
            orderController.listOrdersByOrderId();
        } else if (searchKey.equalsIgnoreCase("externalId")) {
            context.setExternalId(searchValue);
            orderController.listOrdersByExternalId();
        } else {
            Assert.fail("Invalid Search Key");
        }
    }

    @Given("I have received an order inbound request with externalId {string}, content {string}, and create date {string}.")
    public void iHaveReceivedAnOrderInboundRequestWithExternalIdContentAndCreateDate(String externalId, String jsonFileName, String date) throws Exception {

        context.setExternalId(externalId);
        var desireShipDate = "\"" + LocalDate.now().plusDays(2) + "\"";
        var dateValue = "";
        if (NULL_VALUE.equals(date)) {
            dateValue = "null";
        } else if (CURRENT_DATE_TIME.equals(date)) {
            dateValue = "\"" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + "\"";
        } else {
            dateValue = "\"" + date + "\"";
        }

        var jsonContent = testUtils.getResource(jsonFileName);
        jsonContent = jsonContent.replace("\"CREATE_DATE\"", dateValue)
            .replace("\"DESIRED_DATE\"", desireShipDate)
            .replace("{EXTERNAL_ID}", externalId);
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        orderController.createOrderInboundRequest(jsonContent, eventPayload);
    }

    @And("I {string} have the back order created with the same desired shipping date as the original order.")
    public void iHaveTheBackOrderCreatedWithTheSameDesiredShippingDateAsTheOriginalOrder(String option) {
        if (option.equalsIgnoreCase("should")){
            Assert.assertEquals(context.getOrderDetails().get("desiredShippingDate").toString(), originalOrder.get("desireShipDate").toString().replace("\"", ""));
        }
    }
}
