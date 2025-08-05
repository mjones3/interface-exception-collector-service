package com.arcone.biopro.distribution.customer.adapter.in.messaging;

import com.arcone.biopro.distribution.customer.application.dto.CustomerBatchRequestDto;
import com.arcone.biopro.distribution.customer.domain.service.CustomerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerDataListener {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "CustomerDataReceived")
    public void handleCustomerData(String message) {
        log.info("Received message on CustomerDataReceived topic: {}", message);
        try {
            // Parse the event wrapper
            JsonNode eventNode = objectMapper.readTree(message);
            log.debug("Parsed event node: {}", eventNode);
            JsonNode payloadNode = eventNode.get("payload");

            if (payloadNode == null) {
                log.error("No payload found in event message: {}", message);
                return;
            }
            log.debug("Extracted payload: {}", payloadNode);

            // Extract the actual batch request from payload
            CustomerBatchRequestDto batchRequest = objectMapper.treeToValue(payloadNode, CustomerBatchRequestDto.class);

            log.info("Processing batch: {}", batchRequest.getBatchId());

            customerService.processBatch(batchRequest.getBatchId(), batchRequest.getCustomers())
                .doOnSuccess(result -> log.info("Batch processing completed: {}", batchRequest.getBatchId()))
                .doOnError(error -> log.error("Error processing batch: {}", error.getMessage()))
                .subscribe();

        } catch (Exception e) {
            log.error("Error parsing batch request: {}", e.getMessage());
        }
    }
}
