package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.verification.support.DatabaseService;
import com.arcone.biopro.distribution.order.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class OrderSteps {

    private String externalId;

    private JSONObject partnerOrder;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private DatabaseService databaseService;

    @Value("${kafka.waiting.time}")
    private long kafkaWaitingTime;
    @Autowired
    private ObjectMapper objectMapper;

    @Given("I cleaned up from the database the orders with external ID {string}.")
    public void cleanUpOrders(String externalId) {
        var externalIdParam = externalId.replace(",", "','");
        var childQuery = String.format("DELETE FROM bld_order_item WHERE order_id in ( SELECT id from bld_order WHERE external_id in ('%s'))", externalIdParam);
        databaseService.executeSql(childQuery).block();
        var query = String.format("DELETE FROM bld_order WHERE external_id in ('%s')", externalIdParam);
        databaseService.executeSql(query).block();
    }

    @Given("I have received an order inbound request with externalId {string} and content {string}.")
    public void postOrderReceivedEvent(String externalId , String jsonFileName) throws Exception {
        this.externalId = externalId;
        var jsonContent = testUtils.getResource(jsonFileName);
        var eventPayload = objectMapper.readValue(jsonContent, OrderReceivedEventDTO.class);
        partnerOrder = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", partnerOrder);
        Assert.assertNotNull(this.externalId);
        Assert.assertNotNull(partnerOrder);
        var event = kafkaHelper.sendPartnerOrderReceivedEvent(eventPayload.payload().id().toString(),eventPayload).block();
        Assert.assertNotNull(event);
    }

    @When("The system process the order request.")
    public void waitForProcess() throws InterruptedException {
        Thread.sleep(kafkaWaitingTime);
    }

    @Then("A biopro Order will be available in the Distribution local data store.")
    public void checkOrderExists() {
        var query = String.format("SELECT count(*) FROM bld_order WHERE external_id = '%s'", this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(1L, records.get("count"));
    }

    @Then("A biopro Order will not be available in the Distribution local data store.")
    public void checkOrderDoesNotExist() {
        var query = String.format("SELECT count(*) FROM bld_order WHERE external_id = '%s'", this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(0L,records.get("count"));
    }

    @Then("The duplicated biopro Order will not be available in the Distribution local data store.")
    public void checkDuplicatedOrderDoesNotExist() {
        var query = String.format("SELECT count(*) FROM bld_order WHERE external_id = '%s'", this.externalId);
        var data = databaseService.fetchData(query);
        var records = data.first().block();
        Assert.assertEquals(1L,records.get("count"));
    }

}
