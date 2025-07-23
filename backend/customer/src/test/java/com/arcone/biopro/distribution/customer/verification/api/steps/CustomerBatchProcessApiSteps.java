package com.arcone.biopro.distribution.customer.verification.api.steps;

import com.arcone.biopro.distribution.customer.application.event.EventMessage;
import com.arcone.biopro.distribution.customer.verification.support.KafkaTestHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@RequiredArgsConstructor
public class CustomerBatchProcessApiSteps {

    private final ObjectMapper objectMapper;

    private final KafkaTestHelper kafkaTestHelper;

    private JsonNode batchRequestPayload;

    private boolean hasValidData = true;

    @Given("I receive a batch update request {string} json")
    public void iReceiveBatchUpdateRequest(String payloadFile, DataTable dataTable) throws IOException {
        List<Map<String, String>> rows = dataTable.asMaps();
        String fileName = rows.get(0).get("payload");

        ClassPathResource resource = new ClassPathResource("json/" + fileName);
        batchRequestPayload = objectMapper.readTree(resource.getInputStream());

        log.info("Loaded batch request payload: {}", batchRequestPayload);
    }

    @Given("The batch request has the customer with {string}")
    public void theBatchRequestHasTheDonorWith(String customerId) {
        String targetCustomerId = customerId;
        log.info("Target Customer ID set to: {}", targetCustomerId);
    }

    @When("The request has a valid data to be updated")
    public void theRequestHasValidDataToBeUpdated() {
        hasValidData = true;
        log.info("Request marked as having valid data");
    }

    @Then("The batch request should be processed")
    public void theBatchRequestShouldBeProcessed() throws Exception {
        // Create event wrapper using EventMessage
        EventMessage<JsonNode> eventMessage = new EventMessage<>(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            "CustomerDataReceived",
            "1.0",
            batchRequestPayload
        );

        String eventJson = objectMapper.writeValueAsString(eventMessage);
        System.out.println("Sending event to CustomerDataReceived topic: " + eventJson);
        kafkaTestHelper.sendMessage("CustomerDataReceived", eventJson);

        log.info("Event sent to CustomerDataReceived topic");
    }

    @Then("An event should be published with {string}")
    public void anEventShouldBePublishedWith(String status, DataTable dataTable) throws Exception {
        List<Map<String, String>> expectedResults = dataTable.asMaps();
        String expectedCustomerId = expectedResults.get(0).get("External ID");

        // Wait for events to be published
        Thread.sleep(5000);

        log.info("Looking for event with key (External ID): {}", expectedCustomerId);

        // Consume message by key (External ID) from CustomerProcessed topic
        String eventMessage = kafkaTestHelper.consumeMessageByKey("CustomerProcessed", expectedCustomerId, 10, TimeUnit.SECONDS);

        assertNotNull(eventMessage, "No event received for customer: " + expectedCustomerId);
        log.info("Received event for customer {}: {}", expectedCustomerId, eventMessage);

        // Parse the event wrapper and extract payload
        JsonNode eventWrapper = objectMapper.readTree(eventMessage);
        JsonNode payloadNode = eventWrapper.get("payload");
        assertNotNull(payloadNode, "payload is missing in event wrapper");

        // Validate the payload
        for (Map<String, String> expected : expectedResults) {
            JsonNode statusObj = payloadNode.get("status");
            assertNotNull(statusObj, "status object is missing in payload");

            assertEquals(expected.get("External ID"), statusObj.get("customerId").asText());
            assertEquals(expected.get("status"), statusObj.get("status").asText());
        }

        log.info("Success event verified for customer: {}", expectedCustomerId);
    }
}
