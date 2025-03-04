package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.arcone.biopro.distribution.eventbridge.verification.context.InventoryUpdatedOutboundContext;
import com.arcone.biopro.distribution.eventbridge.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.eventbridge.verification.support.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InventoryUpdatedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private InventoryUpdatedOutboundContext inventoryUpdatedOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Inventory Updated event is triggered.")
    public void createInventoryUpdatedEvent() throws Exception {

        inventoryUpdatedOutboundContext.setUnitNumber("W035625205983");
        inventoryUpdatedOutboundContext.setProductCode("E067800");
        inventoryUpdatedOutboundContext.setUpdateType("CREATED");

        var JSON = TestUtil.resource("inventory-updated-event-automation.json")
            .replace("{unit-number}", inventoryUpdatedOutboundContext.getUnitNumber())
            .replace("{product-code}", inventoryUpdatedOutboundContext.getProductCode())
            .replace("{update-type}", inventoryUpdatedOutboundContext.getUpdateType());

        inventoryUpdatedOutboundContext.setInventoryUpdated(new JSONObject(JSON));
        log.info("JSON PAYLOAD :{}", inventoryUpdatedOutboundContext.getInventoryUpdated());
        Assert.assertNotNull(inventoryUpdatedOutboundContext.getInventoryUpdated());
        var event = kafkaHelper.sendEvent(inventoryUpdatedOutboundContext.getInventoryUpdated().getString("eventId"), objectMapper.readTree(JSON), Topics.INVENTORY_UPDATED).block();
        Assert.assertNotNull(event);
    }

    @When("The Inventory Updated event is received.")
    public void theInventoryUpdatedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = inventoryUpdatedOutboundContext.getLatchInventoryUpdated().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);

    }

    @Then("The Inventory Updated outbound event is produced.")
    public void theInventoryUpdatedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = inventoryUpdatedOutboundContext.getLatchInventoryUpdatedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Inventory Updated outbound event is posted in the outbound events topic.")
    public void theInventoryUpdatedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/inventory-updated-outbound.json").getInputStream();

        JsonSchema jsonSchema  = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V7 )
            .getSchema( fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(inventoryUpdatedOutboundContext.getInventoryUpdatedOutboundPayload()));

        log.debug("Schema Errors {}",errors.toString());
        Assert.assertTrue(errors.isEmpty());
        Assert.assertEquals(inventoryUpdatedOutboundContext.getUnitNumber(),new JSONObject(inventoryUpdatedOutboundContext.getInventoryUpdatedOutbound().getString("payload")).getString("unitNumber"));
        Assert.assertEquals(inventoryUpdatedOutboundContext.getProductCode(),new JSONObject(inventoryUpdatedOutboundContext.getInventoryUpdatedOutbound().getString("payload")).getString("productCode"));
    }
}
