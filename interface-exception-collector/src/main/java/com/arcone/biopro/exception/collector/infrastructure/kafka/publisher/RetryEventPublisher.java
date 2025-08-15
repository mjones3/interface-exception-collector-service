package com.arcone.biopro.exception.collector.infrastructure.kafka.publisher;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.domain.event.constants.EventTypes;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionRetryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher for retry-related events including ExceptionRetryCompleted.
 * Handles event correlation ID tracking and causation chains for retry
 * operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryEventPublisher {

        private static final String SERVICE_SOURCE = "exception-collector-service";
        private static final String EVENT_VERSION = "1.0";

        private final KafkaTemplate<String, Object> kafkaTemplate;

        /**
         * Publishes an ExceptionRetryCompleted event when a retry operation completes.
         * Maintains correlation ID from the original exception event for traceability.
         *
         * @param exceptionId   the ID of the exception that was retried
         * @param transactionId the transaction ID from the original event
         * @param attemptNumber the retry attempt number
         * @param retryStatus   the status of the retry (SUCCESS or FAILED)
         * @param retryResult   the result message from the retry operation
         * @param initiatedBy   who initiated the retry
         * @param completedAt   when the retry was completed
         * @return CompletableFuture for async processing result
         */
        public CompletableFuture<SendResult<String, Object>> publishRetryCompleted(
                        Long exceptionId,
                        String transactionId,
                        Integer attemptNumber,
                        RetryStatus retryStatus,
                        String retryResult,
                        String initiatedBy,
                        OffsetDateTime completedAt) {

                ExceptionRetryCompletedEvent.RetryResult retryResultObj = ExceptionRetryCompletedEvent.RetryResult
                                .builder()
                                .success(retryStatus == RetryStatus.SUCCESS)
                                .message(retryResult)
                                .build();

                ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload payload = ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload
                                .builder()
                                .exceptionId(exceptionId)
                                .transactionId(transactionId)
                                .attemptNumber(attemptNumber)
                                .retryStatus(retryStatus.name())
                                .retryResult(retryResultObj)
                                .initiatedBy(initiatedBy)
                                .completedAt(completedAt)
                                .build();

                ExceptionRetryCompletedEvent event = ExceptionRetryCompletedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(EventTypes.EXCEPTION_RETRY_COMPLETED)
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(SERVICE_SOURCE)
                                .correlationId(transactionId) // Use transaction ID as correlation ID
                                .causationId(UUID.randomUUID().toString()) // Generate new causation ID
                                .payload(payload)
                                .build();

                log.info("Publishing ExceptionRetryCompleted event for exception ID: {}, transaction ID: {}, attempt: {}, status: {}",
                                exceptionId, transactionId, attemptNumber, retryStatus);

                return kafkaTemplate.send(KafkaTopics.EXCEPTION_RETRY_COMPLETED, transactionId, event)
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error(
                                                                "Failed to publish ExceptionRetryCompleted event for exception ID: {}, transaction ID: {}, attempt: {}. Error: {}",
                                                                exceptionId, transactionId, attemptNumber,
                                                                throwable.getMessage(), throwable);
                                        } else {
                                                log.debug(
                                                                "Successfully published ExceptionRetryCompleted event for exception ID: {} to topic: {}, partition: {}, offset: {}",
                                                                exceptionId, result.getRecordMetadata().topic(),
                                                                result.getRecordMetadata().partition(),
                                                                result.getRecordMetadata().offset());
                                        }
                                });
        }

        /**
         * Publishes a retry initiated event (optional - for tracking retry initiation).
         *
         * @param exception     the exception being retried
         * @param attemptNumber the retry attempt number
         * @param initiatedBy   who initiated the retry
         * @param reason        the reason for the retry
         * @return CompletableFuture for async processing result
         */
        public CompletableFuture<SendResult<String, Object>> publishRetryInitiated(
                        InterfaceException exception,
                        Integer attemptNumber,
                        String initiatedBy,
                        String reason) {

                // Create a simple retry initiated payload
                var payload = new RetryInitiatedPayload(
                                exception.getId(),
                                exception.getTransactionId(),
                                attemptNumber,
                                initiatedBy,
                                reason,
                                OffsetDateTime.now());

                // Create a generic event structure for retry initiated
                var event = new RetryInitiatedEvent(
                                UUID.randomUUID().toString(),
                                "ExceptionRetryInitiated",
                                EVENT_VERSION,
                                OffsetDateTime.now(),
                                SERVICE_SOURCE,
                                exception.getTransactionId(),
                                UUID.randomUUID().toString(),
                                payload);

                log.info("Publishing ExceptionRetryInitiated event for exception ID: {}, transaction ID: {}, attempt: {}",
                                exception.getId(), exception.getTransactionId(), attemptNumber);

                return kafkaTemplate.send("exception-retry-initiated", exception.getTransactionId(), event)
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error(
                                                                "Failed to publish ExceptionRetryInitiated event for exception ID: {}, transaction ID: {}. Error: {}",
                                                                exception.getId(), exception.getTransactionId(),
                                                                throwable.getMessage(), throwable);
                                        } else {
                                                log.debug(
                                                                "Successfully published ExceptionRetryInitiated event for exception ID: {} to topic: {}",
                                                                exception.getId(), result.getRecordMetadata().topic());
                                        }
                                });
        }

        // Simple record classes for retry initiated event
        public record RetryInitiatedPayload(
                        Long exceptionId,
                        String transactionId,
                        Integer attemptNumber,
                        String initiatedBy,
                        String reason,
                        OffsetDateTime initiatedAt) {
        }

        public record RetryInitiatedEvent(
                        String eventId,
                        String eventType,
                        String eventVersion,
                        OffsetDateTime occurredOn,
                        String source,
                        String correlationId,
                        String causationId,
                        RetryInitiatedPayload payload) {
        }
}