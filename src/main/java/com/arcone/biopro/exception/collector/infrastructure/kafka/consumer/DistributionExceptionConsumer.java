package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
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
 * Kafka consumer for processing distribution exception events
 * (DistributionFailed).
 * Implements requirements US-003 for distribution exception capture with error
 * handling and retry logic.
 * Provides exponential backoff retry mechanism and dead letter queue handling
 * as per US-018.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributionExceptionConsumer {

    private final ExceptionProcessingService exceptionProcessingService;

    /**
     * Consumes DistributionFailed events from the DistributionFailed Kafka topic.
     * Implements requirement US-003 for capturing distribution failure events
     * within 100ms.
     *
     * @param event          the DistributionFailed event payload
     * @param partition      the Kafka partition number
     * @param offset         the message offset
     * @param acknowledgment manual acknowledgment for message processing
     */
    @KafkaListener(topics = KafkaTopics.DISTRIBUTION_FAILED, groupId = "interface-exception-collector", containerFactory = "kafkaListenerContainerFactory")
    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 30000), dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, include = {
            Exception.class })
    public void handleDistributionFailedEvent(
            @Payload DistributionFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Processing DistributionFailed event for transaction: {} from partition: {}, offset: {}",
                    event.getPayload().getTransactionId(), partition, offset);

            // Validate event payload
            if (event.getPayload() == null || event.getPayload().getTransactionId() == null) {
                log.error("Invalid DistributionFailed event payload: missing required fields");
                acknowledgment.acknowledge();
                return;
            }

            // Process the exception event
            InterfaceException processedException = exceptionProcessingService.processDistributionFailedEvent(event);

            log.info(
                    "Successfully processed DistributionFailed event. Created/updated exception with ID: {} for transaction: {}",
                    processedException.getId(), processedException.getTransactionId());

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing DistributionFailed event for transaction: {}. Error: {}",
                    event.getPayload() != null ? event.getPayload().getTransactionId() : "unknown",
                    e.getMessage(), e);

            // Don't acknowledge - let retry mechanism handle it
            throw new RuntimeException("Failed to process DistributionFailed event", e);
        }
    }
}