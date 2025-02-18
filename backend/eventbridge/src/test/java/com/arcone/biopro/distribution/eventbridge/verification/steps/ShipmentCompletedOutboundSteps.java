package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.arcone.biopro.distribution.eventbridge.verification.context.ShipmentCompletedOutboundContext;
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

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShipmentCompletedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private ShipmentCompletedOutboundContext shipmentCompletedOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The shipment completed event is triggered.")
    public void createShipmentCompletedEvent() throws Exception {

        shipmentCompletedOutboundContext.setShipmentId(new Random().nextLong());
        shipmentCompletedOutboundContext.setOrderNumber(new Random().nextLong());
        shipmentCompletedOutboundContext.setExternalId("EXTERNAL"+shipmentCompletedOutboundContext.getShipmentId()+shipmentCompletedOutboundContext.getOrderNumber());

        var JSON = TestUtil.resource("shipment-completed-event-automation.json")
            .replace("\"{order-number}\"", shipmentCompletedOutboundContext.getOrderNumber().toString())
            .replace("\"{shipment-id}\"", shipmentCompletedOutboundContext.getShipmentId().toString())
            .replace("{external-id}", shipmentCompletedOutboundContext.getExternalId());

        shipmentCompletedOutboundContext.setShipmentCompleted(new JSONObject(JSON));
        log.info("JSON PAYLOAD :{}", shipmentCompletedOutboundContext.getShipmentCompleted());
        Assert.assertNotNull(shipmentCompletedOutboundContext.getShipmentCompleted());
        var event = kafkaHelper.sendEvent(shipmentCompletedOutboundContext.getShipmentCompleted().getString("eventId"), objectMapper.readTree(JSON), Topics.SHIPMENT_COMPLETED).block();
        Assert.assertNotNull(event);
    }

    @When("The shipment completed event is received.")
    public void theShipmentCompletedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = shipmentCompletedOutboundContext.getLatchShipmentCompleted().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);

    }

    @Then("The shipment completed outbound event is produced")
    public void theShipmentCompletedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = shipmentCompletedOutboundContext.getLatchShipmentCompletedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The shipment completed outbound event is posted in the outbound events topic.")
    public void theShipmentCompletedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/shipment-completed-outbound.json").getInputStream();

        JsonSchema jsonSchema  = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V7 )
            .getSchema( fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(shipmentCompletedOutboundContext.getShipmentCompletedOutboundPayload()));

        log.debug("Schema Errors {}",errors.toString());
        Assert.assertTrue(errors.isEmpty());

        Assert.assertEquals(shipmentCompletedOutboundContext.getShipmentId().longValue(),shipmentCompletedOutboundContext.getShipmentCompletedOutbound().getLong("shipmentNumber"));
        Assert.assertEquals(shipmentCompletedOutboundContext.getExternalId(),shipmentCompletedOutboundContext.getShipmentCompletedOutbound().getString("externalOrderId"));
    }
}
