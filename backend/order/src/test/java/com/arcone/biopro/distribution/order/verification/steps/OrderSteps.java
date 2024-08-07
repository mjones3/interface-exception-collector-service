package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.order.HomePage;
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

@Slf4j
public class OrderSteps {

    private String externalId;
    private OrderController orderController = new OrderController();
    private JSONObject partnerOrder;
    private boolean isLoggedIn = false;

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

    @Given("I cleaned up from the database the orders with external ID {string}.")
    public void cleanUpOrders(String externalId) {
        var externalIdParam = testUtils.formatSqlCommaSeparatedInParamList(externalId);
        var childQuery = DatabaseQueries.deleteOrderItemsByExternalId(externalIdParam);
        databaseService.executeSql(childQuery).block();
        var query = DatabaseQueries.deleteOrdersByExternalId(externalIdParam);
        databaseService.executeSql(query).block();
    }

    @And("I cleaned up from the database the orders with external ID starting with {string}.")
    public void cleanUpOrdersStartingWith(String externalIdPrefix) {
        var childQuery = DatabaseQueries.deleteOrderItemsByExternalIdStartingWith(externalIdPrefix);
        databaseService.executeSql(childQuery).block();
        var query = DatabaseQueries.deleteOrdersByExternalIdStartingWith(externalIdPrefix);
        databaseService.executeSql(query).block();
    }

    @Given("I have received an order inbound request with externalId {string} and content {string}.")
    public void postOrderReceivedEvent(String externalId, String jsonFileName) throws Exception {
        this.externalId = externalId;
        var jsonContent = testUtils.getResource(jsonFileName);
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
    public void createBioproOrder(String externalId, String locationCode, String deliveryType, String status) throws Exception {
        this.externalId = externalId;
        var query = DatabaseQueries.insertBioProOrder(externalId, locationCode, orderController.getPriorityValue(deliveryType), deliveryType, status);
        databaseService.executeSql(query).block();
    }

    @Given("I have more than {int} Biopro Orders.")
    public void createMultipleBioproOrders(int quantity) throws Exception {
        for (int i = 0; i <= quantity; i++) {
            var priority = orderController.getRandomPriority();
            var externalId = "EXT_" + i;
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
}
