package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Kafka consumer for processing collection exception events
 * (CollectionRejected).
 * Implements requirements US-002 for collection exception capture with error
 * handling and retry logic.
 * Provides exponential backoff retry mechanism and dead letter queue handling
 * as per US-018.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CollectionExceptionConsumer {

    private final ExceptionProcessingService exceptionProcessingService;

    /**
     * Consumes CollectionRejected events from the CollectionRejected Kafka topic.
     * Implements requirement US-002 for capturing collection rejection events
     * within 100ms.
     *
     * @param event          the CollectionRejected event payload
     * @param partition      the Kafka partition number
     * @param offset         the message offset
     * @param acknowledgment manual acknowledgment for message processing
     */
    @KafkaListener(topics = KafkaTopics.COLLECTION_REJECTED, groupId = "interface-exception-collector", containerFactory = "kafkaListenerContainerFactory")
    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 30000), dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, include = {
            Exception.class })
    public void handleCollectionRejectedEvent(
            @Payload CollectionRejectedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Processing CollectionRejected event for transaction: {} from partition: {}, offset: {}",
                    event.getPayload().getTransactionId(), partition, offset);

            // Validate event payload
            if (event.getPayload() == null || event.getPayload().getTransactionId() == null) {
                log.error("Invalid CollectionRejected event payload: missing required fields");
                acknowledgment.acknowledge();
                return;
            }

            // Process the exception event
            InterfaceException processedException = exceptionProcessingService.processCollectionRejectedEvent(event);

            log.info(
                    "Successfully processed CollectionRejected event. Created/updated exception with ID: {} for transaction: {}",
                    processedException.getId(), processedException.getTransactionId());

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing CollectionRejected event for transaction: {}. Error: {}",
                    event.getPayload() != null ? event.getPayload().getTransactionId() : "unknown",
                    e.getMessage(), e);

            // Don't acknowledge - let retry mechanism handle it
            throw new RuntimeException("Failed to process CollectionRejected event", e);
        }
    }
}