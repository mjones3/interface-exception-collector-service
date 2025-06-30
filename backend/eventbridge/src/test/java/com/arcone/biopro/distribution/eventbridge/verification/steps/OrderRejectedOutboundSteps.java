package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.verification.context.OrderRejectedOutboundContext;
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
public class OrderRejectedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private OrderRejectedOutboundContext orderRejectedOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Order Rejected event is triggered.")
    public void createOrderRejectedEvent() throws Exception {

        String orderRejectedPayload = """
            {
              "eventId": "cf04b122-3b2a-4082-9e64-3f24dde2673d",
              "occurredOn": "2025-06-27T18:32:41.425553Z",
              "payload": {
                "externalId": "114117922233592",
                "rejectedReason": "Order already exists",
                "operation": "CREATE_ORDER",
                "transactionId": "8134cea6-a256-438b-a737-50dfc6cdfba1"
              },
              "eventType": "OrderRejected",
              "eventVersion": "1.0"
            }
            """;

        JSONObject orderRejectedJson = new JSONObject(orderRejectedPayload);
        JSONObject payloadJson = orderRejectedJson.getJSONObject("payload");
        orderRejectedOutboundContext.setOrderRejected(orderRejectedJson);
        orderRejectedOutboundContext.setExternalId(payloadJson.getString("externalId"));
        orderRejectedOutboundContext.setRejectedReason(payloadJson.getString("rejectedReason"));
        orderRejectedOutboundContext.setOperation(payloadJson.getString("operation"));

        log.info("JSON PAYLOAD :{}", orderRejectedOutboundContext.getOrderRejected());
        Assert.assertNotNull(orderRejectedOutboundContext.getOrderRejected());

        var event = kafkaHelper.sendEvent(orderRejectedJson.getString("eventId"), objectMapper.readTree(orderRejectedPayload), Topics.ORDER_REJECTED).block();
        Assert.assertNotNull(event);
    }

    @When("The Order Rejected event is received.")
    public void theOrderRejectedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = orderRejectedOutboundContext.getLatchOrderRejected().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @Then("The Order Rejected outbound event is produced.")
    public void theOrderRejectedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = orderRejectedOutboundContext.getLatchOrderRejectedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Order Rejected outbound event is posted in the outbound events topic.")
    public void theOrderRejectedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/order-rejected-outbound.json").getInputStream();

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(orderRejectedOutboundContext.getOrderRejectedOutboundPayload()));

        log.debug("Schema Errors {}", errors.toString());
        Assert.assertTrue(errors.isEmpty());

        JSONObject outboundPayloadJson = new JSONObject(orderRejectedOutboundContext.getOrderRejectedOutbound().getString("payload"));
        Assert.assertEquals(orderRejectedOutboundContext.getExternalId(), outboundPayloadJson.getString("externalId"));
        Assert.assertEquals(orderRejectedOutboundContext.getRejectedReason(), outboundPayloadJson.getString("rejectedReason"));
        Assert.assertEquals(orderRejectedOutboundContext.getOperation(), outboundPayloadJson.getString("operation"));
    }
}