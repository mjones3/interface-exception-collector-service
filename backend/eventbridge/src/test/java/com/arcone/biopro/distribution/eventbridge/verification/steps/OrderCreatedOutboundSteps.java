package com.arcone.biopro.distribution.eventbridge.verification.steps;

import com.arcone.biopro.distribution.eventbridge.verification.context.OrderCreatedOutboundContext;
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
public class OrderCreatedOutboundSteps {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private OrderCreatedOutboundContext orderCreatedOutboundContext;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time:3}")
    private Integer kafkaWait;

    @Given("The Order Created event is triggered.")
    public void createOrderCreatedEvent() throws Exception {
        
        String orderCreatedPayload = """
            {
                "eventId": "d1509fc0-0d1d-4835-a2fc-c5d066aebabb",
                "occurredOn": "2025-06-27T13:35:05.195666Z",
                "orderNumber": 2,
                "externalId": "114117922233599",
                "orderStatus": "OPEN",
                "locationCode": "123456789",
                "createDate": "2025-06-27T10:35:05Z",
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
                "transactionId": "bb9809e3-089a-4768-bd2a-13dfa78c642d",
                "orderItems": [
                    {
                        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                        "bloodType": "ABN",
                        "quantity": 1,
                        "comments": "Comments"
                    }
                ]
            }
            """;

        JSONObject orderCreatedJson = new JSONObject(orderCreatedPayload);
        orderCreatedOutboundContext.setOrderCreated(orderCreatedJson);
        orderCreatedOutboundContext.setOrderNumber(String.valueOf(orderCreatedJson.getInt("orderNumber")));
        orderCreatedOutboundContext.setExternalId(orderCreatedJson.getString("externalId"));
        orderCreatedOutboundContext.setOrderStatus(orderCreatedJson.getString("orderStatus"));

        log.info("JSON PAYLOAD :{}", orderCreatedOutboundContext.getOrderCreated());
        Assert.assertNotNull(orderCreatedOutboundContext.getOrderCreated());
        
        var event = kafkaHelper.sendEvent(orderCreatedJson.getString("eventId"), objectMapper.readTree(orderCreatedPayload), Topics.ORDER_CREATED).block();
        Assert.assertNotNull(event);
    }

    @When("The Order Created event is received.")
    public void theOrderCreatedEventIsReceived() throws InterruptedException {
        boolean messageConsumed = orderCreatedOutboundContext.getLatchOrderCreated().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @Then("The Order Created outbound event is produced.")
    public void theOrderCreatedOutboundEventIsProduced() throws InterruptedException {
        boolean messageConsumed = orderCreatedOutboundContext.getLatchOrderCreatedOutbound().await(kafkaWait, TimeUnit.SECONDS);
        Assert.assertTrue(messageConsumed);
    }

    @And("The Order Created outbound event is posted in the outbound events topic.")
    public void theOrderCreatedOutboundEventIsPostedInTheOutboundEventsTopic() throws Exception {
        
        var fileInputStream = new ClassPathResource("schema/order-created-outbound.json").getInputStream();

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(fileInputStream);

        Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(orderCreatedOutboundContext.getOrderCreatedOutboundPayload()));

        log.debug("Schema Errors {}", errors.toString());
        Assert.assertTrue(errors.isEmpty());
        
        JSONObject payloadJson = new JSONObject(orderCreatedOutboundContext.getOrderCreatedOutbound().getString("payload"));
        Assert.assertEquals(orderCreatedOutboundContext.getOrderNumber(), String.valueOf(payloadJson.getInt("orderNumber")));
        Assert.assertEquals(orderCreatedOutboundContext.getExternalId(), payloadJson.getString("externalId"));
    }
}