package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import com.arcone.biopro.distribution.order.verification.pages.order.HomePage;
import com.arcone.biopro.distribution.order.verification.pages.order.OrderDetailsPage;
import com.arcone.biopro.distribution.order.verification.pages.order.SearchOrderPage;
import com.arcone.biopro.distribution.order.verification.support.*;
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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.util.AssertionErrors.assertEquals;

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
    private String[] productFamilies;
    private String[] bloodTypes;
    private String[] quantityList;
    private String[] commentsList;


    private OrderController orderController = new OrderController();
    private JSONObject partnerOrder;
    private boolean isLoggedIn = false;
    private JSONObject orderShipment;

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

    private Object[] response;

    private void createOrderInboundRequest(String jsonContent, OrderReceivedEventDTO eventPayload) throws JSONException {
        partnerOrder = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", partnerOrder);
        Assert.assertNotNull(this.externalId);
        Assert.assertNotNull(partnerOrder);
        var event = kafkaHelper.sendEvent(eventPayload.payload().id().toString(), eventPayload, Topics.ORDER_RECEIVED).block();
        Assert.assertNotNull(event);
    }

    @When("I want to list orders for location {string}")
    public void searchOrders(String locationCode) {
        response = apiHelper.graphQlRequestObjectList(GraphQLQueryMapper.listOrders(locationCode), "searchOrders");
    }

    @Then("I should have orders listed in the following order {string}.")
    public void iShouldHaveOrdersListedInTheFollowingOrder(String order) {
        var responseIds = Arrays.stream(response)
            .map(r ->
            {
                if (r instanceof LinkedHashMap) {
                    return ((LinkedHashMap<?, ?>) r).get("orderNumber").toString();
                } else {
                    throw new IllegalArgumentException("Unexpected response type: " + r.getClass().getName());
                }
            })
            .collect(Collectors.joining(","));

        Assert.assertEquals(order, responseIds);
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
    @Given("I have a Biopro Order with id {string}, externalId {string}, Location Code {string}, Priority {string} and Status {string}.")
    public void createBioproOrder(String id, String externalId, String locationCode, String priority, String status) {
        this.orderId = Integer.valueOf(id);
        this.externalId = externalId;
        this.locationCode = locationCode;
        this.priority = priority;
        this.status = status;
        var query = DatabaseQueries.insertBioProOrder(orderId, externalId, locationCode, orderController.getPriorityValue(priority), priority, status);
        databaseService.executeSql(query).block();
    }

    @Given("I have this/these BioPro Order(s).")
    public void createBioproOrder(DataTable table) {
        var headers = table.row(0);
        for(var i=1;i<table.height();i++) {
            var row = table.row(i);
            this.orderId = Integer.parseInt(row.get(headers.indexOf("Order Id")));
            this.externalId = row.get(headers.indexOf("External ID"));
            this.locationCode = row.get(headers.indexOf("Location Code"));
            this.priority = row.get(headers.indexOf("Priority"));
            this.status = row.get(headers.indexOf("Status"));
            var query = DatabaseQueries.insertBioProOrder(orderId, externalId, locationCode, orderController.getPriorityValue(priority), priority, status, row.get(headers.indexOf("Desired Shipment Date")));
            databaseService.executeSql(query).block();
        }

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

    @And("I have {int} order items with product families {string}, blood types {string}, quantities {string}, and order item comments {string}.")
    public void createMultipleOrderItem(Integer itemQuantity, String productFamily, String bloodType, String quantities, String comments) {
        this.productFamilies = testUtils.getCommaSeparatedList(productFamily);
        this.bloodTypes = testUtils.getCommaSeparatedList(bloodType);
        this.quantityList = testUtils.getCommaSeparatedList(quantities);
        this.commentsList = testUtils.getCommaSeparatedList(comments);
        for (int i = 0; i < itemQuantity; i++) {
            var query = DatabaseQueries.insertBioProOrderItem(this.externalId, productFamilies[i], bloodTypes[i], Integer.parseInt(quantityList[i]), commentsList[i]);
            databaseService.executeSql(query).block();
        }
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

        if (status.equals("IN_PROGRESS")) {
            var queryOrderShipment = DatabaseQueries.insertBioProOrderShipment(externalId);
            databaseService.executeSql(queryOrderShipment).block();
        }
    }

    @Given("I have more than {int} Biopro Orders.")
    public void createMultipleBioproOrders(int quantity) {
        for (int i = 0; i <= quantity; i++) {
            var priority = orderController.getRandomPriority();
            var externalId = "EXT" + i;
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
        searchOrderPage.validateOrderDetails(this.externalId, OrderController.OrderStatusMap.valueOf(this.status).getDescription(), this.priority);
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

    @Then("I should not see the biopro order in the list of orders.")
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
        Assert.assertEquals(this.orderId.toString(), shipmentDetails.get("orderNumber"));
        Assert.assertEquals(this.shippingCustomerCode, shipmentDetails.get("shippingCustomerCode"));
        Assert.assertEquals(this.shippingCustomerName, shipmentDetails.get("customerName"));
    }

    @And("I should see a message {string} indicating There are no suggested short-dated products.")
    public void matchNoShortDateProductsMessage(String message) {
        Assert.assertEquals(message, this.orderDetailsPage.getNoShortDateMessageContent());
    }

    @Then("I can see the pick list details.")
    public void checkPickListDetails() {
        orderDetailsPage.verifyPickListHeaderDetails(this.orderId.toString(), this.shippingCustomerCode, this.shippingCustomerName, this.orderComments);
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
        this.externalId = externalId;
        var jsonContent = testUtils.getResource("shipment-created-event-automation.json");
        jsonContent = jsonContent.replace("{order-number}", this.orderId.toString());
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
        this.orderId = Integer.valueOf(
            Objects.requireNonNull(
                databaseService.fetchData(DatabaseQueries.getOrderId(this.externalId)).first().block()).get("id").toString());
        var jsonContent = testUtils.getResource("shipment-completed-event.json");
        jsonContent = jsonContent.replace("{order-number}", this.orderId.toString());
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
            searchOrderPage.searchOrder(this.orderId.toString());
        } else if (value.equalsIgnoreCase("externalId")) {
            searchOrderPage.searchOrder(this.externalId);
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
        if(valueOption.equalsIgnoreCase("apply")){
            searchOrderPage.iChooseApplyOption();
        } else {
            searchOrderPage.iChooseResetOption();
        }
    }

    @Then("I should see {int} orders in the search results.")
    public void iShouldSeeOrdersInTheSearchResults(int quantity)  {
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
        setValueForField( 1 + "/" + 1 + "/" + Year.now().getValue() + 1, fieldName);
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
}
