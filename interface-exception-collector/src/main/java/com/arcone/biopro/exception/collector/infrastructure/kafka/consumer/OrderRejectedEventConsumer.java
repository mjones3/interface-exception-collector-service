package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for OrderRejected events.
 * Processes incoming order rejection events and creates interface exceptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRejectedEventConsumer {

    private final ExceptionProcessingService exceptionProcessingService;

    /**
     * Consumes OrderRejected events from Kafka and processes them to create interface exceptions.
     * This triggers the GraphQL subscription events when exceptions are created.
     */
    @KafkaListener(
        topics = "OrderRejected",
        groupId = "interface-exception-collector",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderRejectedEvent(
            @Payload OrderRejectedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        String transactionId = event.getPayload().getTransactionId();
        String externalId = event.getPayload().getExternalId();

        log.info("Received OrderRejected event from Kafka - topic: {}, partition: {}, offset: {}, transactionId: {}, externalId: {}",
                topic, partition, offset, transactionId, externalId);

        try {
            // Process the event to create an interface exception
            InterfaceException exception = exceptionProcessingService.processOrderRejectedEvent(event);

            log.info("Successfully processed OrderRejected event - created exception with ID: {}, transactionId: {}, externalId: {}",
                    exception.getId(), exception.getTransactionId(), exception.getExternalId());

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process OrderRejected event - transactionId: {}, externalId: {}", 
                    transactionId, externalId, e);
            
            // Don't acknowledge on failure - this will cause the message to be retried
            // depending on your Kafka configuration
            throw new RuntimeException("Failed to process OrderRejected event", e);
        }
    }
}