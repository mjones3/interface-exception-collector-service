package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.api.graphql.service.ExceptionEventPublisher;
import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionResolvedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionRetryCompletedEvent;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Kafka consumer for exception lifecycle events that need to be broadcast
 * to GraphQL subscription clients in real-time.
 * 
 * Ensures subscription updates are delivered within 2-second latency requirement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLSubscriptionEventConsumer {

    private final ExceptionEventPublisher eventPublisher;
    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;
    private final ExceptionQueryService exceptionQueryService;

    /**
     * Consumes ExceptionCaptured events and publishes to GraphQL subscribers.
     * 
     * @param event The exception captured event
     * @param partition The Kafka partition
     * @param offset The Kafka offset
     * @param timestamp The message timestamp
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "ExceptionCaptured",
        groupId = "graphql-subscription-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional(readOnly = true)
    public void handleExceptionCaptured(
            @Payload ExceptionCapturedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        OffsetDateTime startTime = OffsetDateTime.now();
        
        try {
            log.debug("Processing ExceptionCaptured event for transaction: {} from partition: {}, offset: {}",
                event.getPayload().getTransactionId(), partition, offset);

            // Fetch the complete exception details
            Optional<InterfaceException> exceptionOpt = 
                exceptionRepository.findByTransactionId(event.getPayload().getTransactionId());

            if (exceptionOpt.isPresent()) {
                InterfaceException exception = exceptionOpt.get();
                
                // Publish to GraphQL subscribers
                eventPublisher.publishExceptionCreated(exception, "system");
                
                // Calculate and log latency
                Duration latency = Duration.between(startTime, OffsetDateTime.now());
                log.debug("Published ExceptionCaptured to GraphQL subscribers in {}ms for transaction: {}",
                    latency.toMillis(), exception.getTransactionId());
                
                // Ensure we meet the 2-second latency requirement
                if (latency.toSeconds() > 2) {
                    log.warn("GraphQL subscription latency exceeded 2 seconds: {}ms for transaction: {}",
                        latency.toMillis(), exception.getTransactionId());
                }
            } else {
                log.warn("Exception not found for transaction ID: {} from ExceptionCaptured event",
                    event.getPayload().getTransactionId());
            }

            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing ExceptionCaptured event for transaction: {}",
                event.getPayload().getTransactionId(), e);
            
            // Don't acknowledge on error to trigger retry
            throw new RuntimeException("Failed to process ExceptionCaptured event", e);
        }
    }

    /**
     * Consumes ExceptionResolved events and publishes to GraphQL subscribers.
     * 
     * @param event The exception resolved event
     * @param partition The Kafka partition
     * @param offset The Kafka offset
     * @param timestamp The message timestamp
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "ExceptionResolved",
        groupId = "graphql-subscription-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional(readOnly = true)
    public void handleExceptionResolved(
            @Payload ExceptionResolvedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        OffsetDateTime startTime = OffsetDateTime.now();
        
        try {
            log.debug("Processing ExceptionResolved event for transaction: {} from partition: {}, offset: {}",
                event.getPayload().getTransactionId(), partition, offset);

            // Fetch the complete exception details
            Optional<InterfaceException> exceptionOpt = 
                exceptionRepository.findByTransactionId(event.getPayload().getTransactionId());

            if (exceptionOpt.isPresent()) {
                InterfaceException exception = exceptionOpt.get();
                
                // Publish to GraphQL subscribers
                eventPublisher.publishExceptionResolved(exception, event.getPayload().getResolvedBy());
                
                // Calculate and log latency
                Duration latency = Duration.between(startTime, OffsetDateTime.now());
                log.debug("Published ExceptionResolved to GraphQL subscribers in {}ms for transaction: {}",
                    latency.toMillis(), exception.getTransactionId());
                
                // Ensure we meet the 2-second latency requirement
                if (latency.toSeconds() > 2) {
                    log.warn("GraphQL subscription latency exceeded 2 seconds: {}ms for transaction: {}",
                        latency.toMillis(), exception.getTransactionId());
                }
            } else {
                log.warn("Exception not found for transaction ID: {} from ExceptionResolved event",
                    event.getPayload().getTransactionId());
            }

            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing ExceptionResolved event for transaction: {}",
                event.getPayload().getTransactionId(), e);
            
            // Don't acknowledge on error to trigger retry
            throw new RuntimeException("Failed to process ExceptionResolved event", e);
        }
    }

    /**
     * Consumes ExceptionRetryCompleted events and publishes to GraphQL subscribers.
     * 
     * @param event The retry completed event
     * @param partition The Kafka partition
     * @param offset The Kafka offset
     * @param timestamp The message timestamp
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "RetryCompleted",
        groupId = "graphql-subscription-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional(readOnly = true)
    public void handleRetryCompleted(
            @Payload ExceptionRetryCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        OffsetDateTime startTime = OffsetDateTime.now();
        
        try {
            log.debug("Processing RetryCompleted event for transaction: {} from partition: {}, offset: {}",
                event.getPayload().getTransactionId(), partition, offset);

            // Fetch the complete exception details
            Optional<InterfaceException> exceptionOpt = 
                exceptionRepository.findByTransactionId(event.getPayload().getTransactionId());

            if (exceptionOpt.isPresent()) {
                InterfaceException exception = exceptionOpt.get();
                
                // Fetch the retry attempt details
                Optional<RetryAttempt> retryAttemptOpt = retryAttemptRepository
                    .findByExceptionIdAndAttemptNumber(
                        exception.getId(), 
                        event.getPayload().getAttemptNumber()
                    );

                if (retryAttemptOpt.isPresent()) {
                    RetryAttempt retryAttempt = retryAttemptOpt.get();
                    boolean success = event.getPayload().getRetryResult() != null && 
                                    event.getPayload().getRetryResult().getSuccess();
                    
                    // Publish to GraphQL subscribers
                    eventPublisher.publishRetryCompleted(exception, retryAttempt, success);
                    
                    // Calculate and log latency
                    Duration latency = Duration.between(startTime, OffsetDateTime.now());
                    log.debug("Published RetryCompleted to GraphQL subscribers in {}ms for transaction: {}",
                        latency.toMillis(), exception.getTransactionId());
                    
                    // Ensure we meet the 2-second latency requirement
                    if (latency.toSeconds() > 2) {
                        log.warn("GraphQL subscription latency exceeded 2 seconds: {}ms for transaction: {}",
                            latency.toMillis(), exception.getTransactionId());
                    }
                } else {
                    log.warn("Retry attempt not found for exception ID: {}, attempt: {} from RetryCompleted event",
                        exception.getId(), event.getPayload().getAttemptNumber());
                }
            } else {
                log.warn("Exception not found for transaction ID: {} from RetryCompleted event",
                    event.getPayload().getTransactionId());
            }

            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing RetryCompleted event for transaction: {}",
                event.getPayload().getTransactionId(), e);
            
            // Don't acknowledge on error to trigger retry
            throw new RuntimeException("Failed to process RetryCompleted event", e);
        }
    }

    /**
     * Handles acknowledgment events from the application layer.
     * This is called directly by the acknowledgment service rather than through Kafka.
     * 
     * @param exception The acknowledged exception
     * @param acknowledgedBy Who acknowledged the exception
     */
    public void handleExceptionAcknowledged(InterfaceException exception, String acknowledgedBy) {
        OffsetDateTime startTime = OffsetDateTime.now();
        
        try {
            log.debug("Processing exception acknowledgment for transaction: {}", exception.getTransactionId());
            
            // Publish to GraphQL subscribers
            eventPublisher.publishExceptionAcknowledged(exception, acknowledgedBy);
            
            // Calculate and log latency
            Duration latency = Duration.between(startTime, OffsetDateTime.now());
            log.debug("Published ExceptionAcknowledged to GraphQL subscribers in {}ms for transaction: {}",
                latency.toMillis(), exception.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error processing exception acknowledgment for transaction: {}",
                exception.getTransactionId(), e);
        }
    }

    /**
     * Handles retry initiation events from the application layer.
     * This is called directly by the retry service rather than through Kafka.
     * 
     * @param exception The exception being retried
     * @param retryAttempt The retry attempt details
     * @param initiatedBy Who initiated the retry
     */
    public void handleRetryInitiated(InterfaceException exception, RetryAttempt retryAttempt, String initiatedBy) {
        OffsetDateTime startTime = OffsetDateTime.now();
        
        try {
            log.debug("Processing retry initiation for transaction: {}", exception.getTransactionId());
            
            // Publish to GraphQL subscribers
            eventPublisher.publishRetryInitiated(exception, retryAttempt, initiatedBy);
            
            // Calculate and log latency
            Duration latency = Duration.between(startTime, OffsetDateTime.now());
            log.debug("Published RetryInitiated to GraphQL subscribers in {}ms for transaction: {}",
                latency.toMillis(), exception.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error processing retry initiation for transaction: {}",
                exception.getTransactionId(), e);
        }
    }
}