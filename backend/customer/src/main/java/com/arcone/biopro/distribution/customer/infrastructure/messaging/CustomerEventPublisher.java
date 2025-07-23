package com.arcone.biopro.distribution.customer.infrastructure.messaging;

import com.arcone.biopro.distribution.customer.application.event.CustomerProcessedEvent;
import com.arcone.biopro.distribution.customer.application.event.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.customer-processed.name:CustomerProcessed}")
    private String customerProcessedTopic;

    public void publishCustomerCompleted(String batchId, String customerId) {
        CustomerProcessedEvent payload = CustomerProcessedEvent.builder()
            .batchId(batchId)
            .status(CustomerProcessedEvent.CustomerStatus.builder()
                .customerId(customerId)
                .status("COMPLETED")
                .processedAt(ZonedDateTime.now())
                .build())
            .build();

        EventMessage<CustomerProcessedEvent> eventMessage = new EventMessage<>(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            "CustomerProcessed",
            "1.0",
            payload
        );

        kafkaTemplate.send(customerProcessedTopic, customerId, eventMessage)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Customer completed event sent for customerId: {}", customerId);
                } else {
                    log.error("Failed to send customer completed event for customerId: {}", customerId, ex);
                }
            });
    }

    public void publishCustomerFailed(String batchId, String customerId, String errorMessage) {
        CustomerProcessedEvent payload = CustomerProcessedEvent.builder()
            .batchId(batchId)
            .status(CustomerProcessedEvent.CustomerStatus.builder()
                .customerId(customerId)
                .status("FAILED")
                .error(errorMessage)
                .processedAt(ZonedDateTime.now())
                .build())
            .build();

        EventMessage<CustomerProcessedEvent> eventMessage = new EventMessage<>(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            "CustomerProcessed",
            "1.0",
            payload
        );

        kafkaTemplate.send(customerProcessedTopic, customerId, eventMessage)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Customer failed event sent for customerId: {}", customerId);
                } else {
                    log.error("Failed to send customer failed event for customerId: {}", customerId, ex);
                }
            });
    }

}
