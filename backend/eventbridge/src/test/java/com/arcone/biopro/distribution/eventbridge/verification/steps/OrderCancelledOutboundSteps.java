package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.verification.context.OrderCancelledOutboundContext;
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
public class OrderCancelledOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private OrderCancelledOutboundContext orderCancelledOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Order Cancelled event is triggered.")
    public void createOrderCancelledEvent() throws Exception {

        String orderCancelledPayload = """
            {
              "eventId": "9bc2826a-b9be-453b-957b-4b5cd9a87e67",
              "occurredOn": "2025-06-27T14:53:29.172603Z",
              "payload": {
                "orderNumber": 8,
                "externalId": "114117922233597",
                "orderStatus": "CANCELLED",
                "locationCode": "123456789",
                "createDate": "2025-06-27T11:52:38Z",
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
                "cancelEmployeeId": "A1235",
                "cancelDate": "2025-06-27T14:53:29.131995Z",
                "cancelReason": "Customer no longer need",
                "transactionId": "838affdd-1513-4b13-b8f4-317772ff8b14"
              },
              "eventType": "OrderCancelled",
              "eventVersion": "1.0"
            }
            """;

        JSONObject orderCancelledJson = new JSONObject(orderCancelledPayload);
        JSONObject payloadJson = orderCancelledJson.getJSONObject("payload");
        orderCancelledOutboundContext.setOrderCancelled(orderCancelledJson);
        orderCancelledOutboundContext.setOrderNumber(String.valueOf(payloadJson.getInt("orderNumber")));
        orderCancelledOutboundContext.setExternalId(payloadJson.getString("externalId"));
        orderCancelledOutboundContext.setOrderStatus(payloadJson.getString("orderStatus"));

        log.info("JSON PAYLOAD :{}", orderCancelledOutboundContext.getOrderCancelled());
        Assert.assertNotNull(orderCancelledOutboundContext.getOrderCancelled());

        var event = kafkaHelper.sendEvent(orderCancelledJson.getString("eventId"), objectMapper.readTree(orderCancelledPayload), Topics.ORDER_CANCELLED).block();
        Assert.assertNotNull(event);
    }

    @When("The Order Cancelled event is received.")
    public void theOrderCancelledEventIsReceived() throws InterruptedException {
        boolean messageConsumed = orderCancelledOutboundContext.getLatchOrderCancelled().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @Then("The Order Cancelled outbound event is produced.")
    public void theOrderCancelledOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = orderCancelledOutboundContext.getLatchOrderCancelledOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Order Cancelled outbound event is posted in the outbound events topic.")
    public void theOrderCancelledOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {

        var fileInputStream = new ClassPathResource("schema/order-cancelled-outbound.json").getInputStream();

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(orderCancelledOutboundContext.getOrderCancelledOutboundPayload()));

        log.debug("Schema Errors {}", errors.toString());
        Assert.assertTrue(errors.isEmpty());

        JSONObject outboundPayloadJson = new JSONObject(orderCancelledOutboundContext.getOrderCancelledOutbound().getString("payload"));
        Assert.assertEquals(orderCancelledOutboundContext.getOrderNumber(), String.valueOf(outboundPayloadJson.getInt("orderNumber")));
        Assert.assertEquals(orderCancelledOutboundContext.getExternalId(), outboundPayloadJson.getString("externalId"));
    }
}