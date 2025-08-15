package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing order exception events (OrderRejected and
 * OrderCancelled).
 * Implements requirements US-001 for order exception capture with error
 * handling and retry logic.
 * Provides exponential backoff retry mechanism and dead letter queue handling
 * as per US-018.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExceptionConsumer {

    private final ExceptionProcessingService exceptionProcessingService;
    private final ObjectMapper objectMapper;

    /**
     * Consumes OrderRejected events from the OrderRejected Kafka topic.
     * Implements requirement US-001 for capturing order rejection events within
     * 100ms.
     *
     * @param event          the OrderRejected event payload
     * @param partition      the Kafka partition number
     * @param offset         the message offset
     * @param acknowledgment manual acknowledgment for message processing
     */
    @KafkaListener(topics = KafkaTopics.ORDER_REJECTED, groupId = "interface-exception-collector", containerFactory = "kafkaListenerContainerFactory")
    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 30000), dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, include = {
            Exception.class })
    public void handleOrderRejectedEvent(
            @Payload Object eventPayload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.debug("Received OrderRejected event from partition: {}, offset: {}", partition, offset);

            // Additional null checks for better error handling
            if (eventPayload == null) {
                log.error("Received null OrderRejected event from partition: {}, offset: {}", partition, offset);
                acknowledgment.acknowledge();
                return;
            }

            // Convert the payload to OrderRejectedEvent
            OrderRejectedEvent event;
            try {
                if (eventPayload instanceof OrderRejectedEvent) {
                    event = (OrderRejectedEvent) eventPayload;
                } else {
                    // Convert from LinkedHashMap or other format to OrderRejectedEvent
                    event = objectMapper.convertValue(eventPayload, OrderRejectedEvent.class);
                }
            } catch (Exception conversionError) {
                log.error("Failed to convert event payload to OrderRejectedEvent: {}", conversionError.getMessage(),
                        conversionError);
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing OrderRejected event for transaction: {} from partition: {}, offset: {}",
                    event.getPayload() != null ? event.getPayload().getTransactionId() : "null", partition, offset);

            // Validate event payload
            if (event.getPayload() == null || event.getPayload().getTransactionId() == null) {
                log.error("Invalid OrderRejected event payload: missing required fields");
                acknowledgment.acknowledge();
                return;
            }

            // Process the exception event
            InterfaceException processedException = exceptionProcessingService.processOrderRejectedEvent(event);

            log.info(
                    "Successfully processed OrderRejected event. Created/updated exception with ID: {} for transaction: {}",
                    processedException.getId(), processedException.getTransactionId());

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing OrderRejected event from partition: {}, offset: {}. Error: {}",
                    partition, offset, e.getMessage(), e);

            // Don't acknowledge - let retry mechanism handle it
            throw new RuntimeException("Failed to process OrderRejected event", e);
        }
    }

    /**
     * Consumes OrderCancelled events from the OrderCancelled Kafka topic.
     * Implements requirement US-001 for capturing order cancellation events within
     * 100ms.
     *
     * @param event          the OrderCancelled event payload
     * @param partition      the Kafka partition number
     * @param offset         the message offset
     * @param acknowledgment manual acknowledgment for message processing
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "interface-exception-collector", containerFactory = "kafkaListenerContainerFactory")
    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 30000), dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, include = {
            Exception.class })
    public void handleOrderCancelledEvent(
            @Payload Object eventPayload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            // Convert the payload to OrderCancelledEvent
            OrderCancelledEvent event;
            try {
                if (eventPayload instanceof OrderCancelledEvent) {
                    event = (OrderCancelledEvent) eventPayload;
                } else {
                    // Convert from LinkedHashMap or other format to OrderCancelledEvent
                    event = objectMapper.convertValue(eventPayload, OrderCancelledEvent.class);
                }
            } catch (Exception conversionError) {
                log.error("Failed to convert event payload to OrderCancelledEvent: {}", conversionError.getMessage(),
                        conversionError);
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing OrderCancelled event for transaction: {} from partition: {}, offset: {}",
                    event.getPayload().getTransactionId(), partition, offset);

            // Validate event payload
            if (event.getPayload() == null || event.getPayload().getTransactionId() == null) {
                log.error("Invalid OrderCancelled event payload: missing required fields");
                acknowledgment.acknowledge();
                return;
            }

            // Process the exception event
            InterfaceException processedException = exceptionProcessingService.processOrderCancelledEvent(event);

            log.info(
                    "Successfully processed OrderCancelled event. Created/updated exception with ID: {} for transaction: {}",
                    processedException.getId(), processedException.getTransactionId());

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing OrderCancelled event from partition: {}, offset: {}. Error: {}",
                    partition, offset, e.getMessage(), e);

            // Don't acknowledge - let retry mechanism handle it
            throw new RuntimeException("Failed to process OrderCancelled event", e);
        }
    }
}