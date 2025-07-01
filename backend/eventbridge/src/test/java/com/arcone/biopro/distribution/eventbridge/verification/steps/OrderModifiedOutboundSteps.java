package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.verification.context.OrderModifiedOutboundContext;
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
public class OrderModifiedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private OrderModifiedOutboundContext orderModifiedOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Order Modified event is triggered.")
    public void createOrderModifiedEvent() throws Exception {

        String orderModifiedPayload = """
            {
              "eventId": "e7930442-93f4-4d78-94e9-79e70a155d7b",
              "occurredOn": "2025-06-27T15:44:41.874216Z",
              "payload": {
                "orderNumber": 13,
                "externalId": "114117922233595",
                "orderStatus": "OPEN",
                "locationCode": "123456789",
                "createDate": "2025-06-27T12:43:42Z",
                "createEmployeeCode": "A1235",
                "shipmentType": "CUSTOMER",
                "priority": "SCHEDULED",
                "shippingMethod": "FEDEX",
                "productCategory": "REFRIGERATED",
                "desiredShippingDate": "2026-04-25",
                "shippingCustomerCode": "A1235",
                "billingCustomerCode": "A1235",
                "comments": "string",
                "willPickUp": true,
                "willPickUpPhoneNumber": "12333333",
                "totalShipped": 0,
                "totalRemaining": 1,
                "totalProducts": 1,
                "orderItems": [
                  {
                    "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                    "bloodType": "ABN",
                    "quantity": 1,
                    "quantityShipped": 0,
                    "quantityRemaining": 1,
                    "comments": "Comments"
                  }
                ],
                "modifyEmployeeId": "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "modifyDate": "2025-06-27T15:44:41.834725Z",
                "modifyReason": "Missing Product details.",
                "transactionId": "f02386e0-0e23-4575-9f23-716879abfa83"
              },
              "eventType": "OrderModified",
              "eventVersion": "1.0"
            }
            """;

        JSONObject orderModifiedJson = new JSONObject(orderModifiedPayload);
        JSONObject payloadJson = orderModifiedJson.getJSONObject("payload");
        orderModifiedOutboundContext.setOrderModified(orderModifiedJson);
        orderModifiedOutboundContext.setOrderNumber(String.valueOf(payloadJson.getInt("orderNumber")));
        orderModifiedOutboundContext.setExternalId(payloadJson.getString("externalId"));
        orderModifiedOutboundContext.setOrderStatus(payloadJson.getString("orderStatus"));

        log.info("JSON PAYLOAD :{}", orderModifiedOutboundContext.getOrderModified());
        Assert.assertNotNull(orderModifiedOutboundContext.getOrderModified());

        var event = kafkaHelper.sendEvent(orderModifiedJson.getString("eventId"), objectMapper.readTree(orderModifiedPayload), Topics.ORDER_MODIFIED).block();
        Assert.assertNotNull(event);
    }

    @When("The Order Modified event is received.")
    public void theOrderModifiedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = orderModifiedOutboundContext.getLatchOrderModified().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @Then("The Order Modified outbound event is produced.")
    public void theOrderModifiedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = orderModifiedOutboundContext.getLatchOrderModifiedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Order Modified outbound event is posted in the outbound events topic.")
    public void theOrderModifiedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/order-modified-outbound.json").getInputStream();

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(orderModifiedOutboundContext.getOrderModifiedOutboundPayload()));

        log.debug("Schema Errors {}", errors.toString());
        Assert.assertTrue(errors.isEmpty());

        JSONObject outboundPayloadJson = new JSONObject(orderModifiedOutboundContext.getOrderModifiedOutbound().getString("payload"));
        Assert.assertEquals(orderModifiedOutboundContext.getOrderNumber(), String.valueOf(outboundPayloadJson.getInt("orderNumber")));
        Assert.assertEquals(orderModifiedOutboundContext.getExternalId(), outboundPayloadJson.getString("externalId"));
    }
}