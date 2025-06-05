package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.arcone.biopro.distribution.eventbridge.verification.context.RecoveredPlasmaShipmentClosedOutboundContext;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RecoveredPlasmaShipmentClosedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private RecoveredPlasmaShipmentClosedOutboundContext outboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Recovered Plasma shipment closed event is triggered.")
    public void createShipmentCompletedEvent() throws Exception {

        outboundContext.setShipmentNumber(RandomStringUtils.random(10, true, true).toUpperCase());

        var JSON = TestUtil.resource("rps-shipment-closed-event-automation.json")
            .replace("{shipmentNumber}", outboundContext.getShipmentNumber());

        outboundContext.setShipmentClosed(new JSONObject(JSON));
        log.debug("JSON PAYLOAD :{}", outboundContext.getShipmentClosed());
        Assert.assertNotNull(outboundContext.getShipmentClosed());
        var event = kafkaHelper.sendEvent(outboundContext.getShipmentClosed().getString("eventId"), objectMapper.readTree(JSON), Topics.RPS_SHIPMENT_CLOSED).block();
        Assert.assertNotNull(event);
    }

    @When("The Recovered Plasma shipment closed event is received.")
    public void theShipmentCompletedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = outboundContext.getLatchShipmentClosed().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);

    }

    @Then("The Recovered Plasma shipment closed outbound event is produced.")
    public void theShipmentCompletedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = outboundContext.getLatchShipmentClosedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Recovered Plasma shipment closed outbound event is posted in the outbound events topic.")
    public void theShipmentCompletedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/recovered-plasma-shipment-closed-outbound.json").getInputStream();

        JsonSchema jsonSchema  = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V7 )
            .getSchema( fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(outboundContext.getOutboundPayload()));

        log.debug("Schema Errors {}",errors.toString());
        Assert.assertTrue(errors.isEmpty());

        Assert.assertEquals(outboundContext.getShipmentNumber(),outboundContext.getShipmentClosedOutbound().getJSONObject("payload").getString("shipmentNumber"));
    }
}
