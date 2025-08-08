package com.arcone.biopro.exception.collector.infrastructure.kafka.publisher;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.event.constants.EventTypes;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher for exception lifecycle events including ExceptionCaptured and
 * ExceptionResolved.
 * Handles event correlation ID tracking and causation chains as per
 * requirements US-016 and US-017.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionEventPublisher {

        private static final String SERVICE_SOURCE = "exception-collector-service";
        private static final String EVENT_VERSION = "1.0";

        private final KafkaTemplate<String, Object> kafkaTemplate;

        /**
         * Publishes an ExceptionCaptured event when an exception is successfully
         * stored.
         * Maintains correlation ID from the original inbound event for traceability.
         *
         * @param exceptionId     the ID of the captured exception
         * @param transactionId   the transaction ID from the original event
         * @param interfaceType   the interface type (ORDER, COLLECTION, DISTRIBUTION)
         * @param severity        the exception severity
         * @param category        the exception category
         * @param exceptionReason the reason for the exception
         * @param customerId      the customer ID if available
         * @param retryable       whether the exception is retryable
         * @param correlationId   the correlation ID from the original event
         * @param causationId     the causation ID for event chain tracking
         * @return CompletableFuture for async processing result
         */
        public CompletableFuture<SendResult<String, Object>> publishExceptionCaptured(
                        Long exceptionId,
                        String transactionId,
                        String interfaceType,
                        String severity,
                        String category,
                        String exceptionReason,
                        String customerId,
                        Boolean retryable,
                        String correlationId,
                        String causationId) {

                ExceptionCapturedEvent.ExceptionCapturedPayload payload = ExceptionCapturedEvent.ExceptionCapturedPayload
                                .builder()
                                .exceptionId(exceptionId)
                                .transactionId(transactionId)
                                .interfaceType(interfaceType)
                                .severity(severity)
                                .category(category)
                                .exceptionReason(exceptionReason)
                                .customerId(customerId)
                                .retryable(retryable)
                                .build();

                ExceptionCapturedEvent event = ExceptionCapturedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(EventTypes.EXCEPTION_CAPTURED)
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(SERVICE_SOURCE)
                                .correlationId(correlationId)
                                .causationId(causationId)
                                .payload(payload)
                                .build();

                log.info("Publishing ExceptionCaptured event for exception ID: {}, transaction ID: {}, correlation ID: {}",
                                exceptionId, transactionId, correlationId);

                return kafkaTemplate.send(KafkaTopics.EXCEPTION_CAPTURED, transactionId, event)
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error(
                                                                "Failed to publish ExceptionCaptured event for exception ID: {}, transaction ID: {}. Error: {}",
                                                                exceptionId, transactionId, throwable.getMessage(),
                                                                throwable);
                                        } else {
                                                log.debug(
                                                                "Successfully published ExceptionCaptured event for exception ID: {} to topic: {}, partition: {}, offset: {}",
                                                                exceptionId, result.getRecordMetadata().topic(),
                                                                result.getRecordMetadata().partition(),
                                                                result.getRecordMetadata().offset());
                                        }
                                });
        }

        /**
         * Publishes an ExceptionResolved event when an exception reaches resolved
         * status.
         * Maintains correlation ID consistency and includes resolution details.
         *
         * @param exceptionId        the ID of the resolved exception
         * @param transactionId      the transaction ID from the original event
         * @param resolutionMethod   the method used to resolve the exception
         * @param resolvedBy         who resolved the exception
         * @param resolvedAt         when the exception was resolved
         * @param totalRetryAttempts total number of retry attempts made
         * @param resolutionNotes    additional notes about the resolution
         * @param correlationId      the correlation ID from the original event
         * @param causationId        the causation ID for event chain tracking
         * @return CompletableFuture for async processing result
         */
        public CompletableFuture<SendResult<String, Object>> publishExceptionResolved(
                        Long exceptionId,
                        String transactionId,
                        String resolutionMethod,
                        String resolvedBy,
                        OffsetDateTime resolvedAt,
                        Integer totalRetryAttempts,
                        String resolutionNotes,
                        String correlationId,
                        String causationId) {

                ExceptionResolvedEvent.ExceptionResolvedPayload payload = ExceptionResolvedEvent.ExceptionResolvedPayload
                                .builder()
                                .exceptionId(exceptionId)
                                .transactionId(transactionId)
                                .resolutionMethod(resolutionMethod)
                                .resolvedBy(resolvedBy)
                                .resolvedAt(resolvedAt)
                                .totalRetryAttempts(totalRetryAttempts)
                                .resolutionNotes(resolutionNotes)
                                .build();

                ExceptionResolvedEvent event = ExceptionResolvedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(EventTypes.EXCEPTION_RESOLVED)
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(SERVICE_SOURCE)
                                .correlationId(correlationId)
                                .causationId(causationId)
                                .payload(payload)
                                .build();

                log.info(
                                "Publishing ExceptionResolved event for exception ID: {}, transaction ID: {}, resolution method: {}, correlation ID: {}",
                                exceptionId, transactionId, resolutionMethod, correlationId);

                return kafkaTemplate.send(KafkaTopics.EXCEPTION_RESOLVED, transactionId, event)
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error(
                                                                "Failed to publish ExceptionResolved event for exception ID: {}, transaction ID: {}. Error: {}",
                                                                exceptionId, transactionId, throwable.getMessage(),
                                                                throwable);
                                        } else {
                                                log.debug(
                                                                "Successfully published ExceptionResolved event for exception ID: {} to topic: {}, partition: {}, offset: {}",
                                                                exceptionId, result.getRecordMetadata().topic(),
                                                                result.getRecordMetadata().partition(),
                                                                result.getRecordMetadata().offset());
                                        }
                                });
        }

        /**
         * Convenience method to publish ExceptionResolved event from InterfaceException
         * entity.
         * Uses the transaction ID as correlation ID for simplicity.
         *
         * @param exception the resolved exception entity
         * @return CompletableFuture for async processing result
         */
        public CompletableFuture<SendResult<String, Object>> publishExceptionResolved(InterfaceException exception) {
                return publishExceptionResolved(
                                exception.getId(),
                                exception.getTransactionId(),
                                exception.getResolutionMethod() != null ? exception.getResolutionMethod().name() : null,
                                exception.getResolvedBy(),
                                exception.getResolvedAt(),
                                exception.getRetryCount(),
                                exception.getResolutionNotes(),
                                exception.getTransactionId(), // Use transaction ID as correlation ID
                                null // No causation ID for manual resolution
                );
        }
}