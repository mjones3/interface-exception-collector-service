package com.arcone.biopro.exception.collector.infrastructure.kafka.publisher;

import com.arcone.biopro.exception.collector.domain.event.constants.EventTypes;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.outbound.CriticalExceptionAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher for critical exception alert events.
 * Handles urgent alerts that require immediate attention as per requirement
 * US-015.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertPublisher {

    private static final String SERVICE_SOURCE = "exception-collector-service";
    private static final String EVENT_VERSION = "1.0";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a CriticalExceptionAlert event for exceptions requiring immediate
     * attention.
     * Maintains correlation ID from the original exception event for traceability.
     *
     * @param exceptionId             the ID of the exception triggering the alert
     * @param transactionId           the transaction ID from the original event
     * @param alertLevel              the level of the alert (CRITICAL, EMERGENCY)
     * @param alertReason             the reason for the alert (CRITICAL_SEVERITY,
     *                                MULTIPLE_RETRIES_FAILED, etc.)
     * @param interfaceType           the interface type where the exception
     *                                occurred
     * @param exceptionReason         the reason for the original exception
     * @param customerId              the customer ID if available
     * @param escalationTeam          the team to escalate to
     * @param requiresImmediateAction whether immediate action is required
     * @param estimatedImpact         the estimated impact level
     * @param customersAffected       number of customers affected
     * @param correlationId           the correlation ID from the original event
     * @param causationId             the causation ID for event chain tracking
     * @return CompletableFuture for async processing result
     */
    public CompletableFuture<SendResult<String, Object>> publishCriticalAlert(
            Long exceptionId,
            String transactionId,
            String alertLevel,
            String alertReason,
            String interfaceType,
            String exceptionReason,
            String customerId,
            String escalationTeam,
            Boolean requiresImmediateAction,
            String estimatedImpact,
            Integer customersAffected,
            String correlationId,
            String causationId) {

        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = CriticalExceptionAlertEvent.CriticalExceptionAlertPayload
                .builder()
                .exceptionId(exceptionId)
                .transactionId(transactionId)
                .alertLevel(alertLevel)
                .alertReason(alertReason)
                .interfaceType(interfaceType)
                .exceptionReason(exceptionReason)
                .customerId(customerId)
                .escalationTeam(escalationTeam)
                .requiresImmediateAction(requiresImmediateAction)
                .estimatedImpact(estimatedImpact)
                .customersAffected(customersAffected)
                .build();

        CriticalExceptionAlertEvent event = CriticalExceptionAlertEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventTypes.CRITICAL_EXCEPTION_ALERT)
                .eventVersion(EVENT_VERSION)
                .occurredOn(OffsetDateTime.now())
                .source(SERVICE_SOURCE)
                .correlationId(correlationId)
                .causationId(causationId)
                .payload(payload)
                .build();

        log.warn(
                "Publishing CriticalExceptionAlert event for exception ID: {}, transaction ID: {}, alert level: {}, reason: {}, correlation ID: {}",
                exceptionId, transactionId, alertLevel, alertReason, correlationId);

        return kafkaTemplate.send(KafkaTopics.CRITICAL_EXCEPTION_ALERT, transactionId, event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error(
                                "Failed to publish CriticalExceptionAlert event for exception ID: {}, transaction ID: {}. Error: {}",
                                exceptionId, transactionId, throwable.getMessage(), throwable);
                    } else {
                        log.info(
                                "Successfully published CriticalExceptionAlert event for exception ID: {} to topic: {}, partition: {}, offset: {}",
                                exceptionId, result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Convenience method for publishing critical alerts with default escalation
     * settings.
     * Automatically determines escalation team and immediate action requirements
     * based on alert level.
     *
     * @param exceptionId     the ID of the exception triggering the alert
     * @param transactionId   the transaction ID from the original event
     * @param alertLevel      the level of the alert
     * @param alertReason     the reason for the alert
     * @param interfaceType   the interface type where the exception occurred
     * @param exceptionReason the reason for the original exception
     * @param customerId      the customer ID if available
     * @param estimatedImpact the estimated impact level
     * @param correlationId   the correlation ID from the original event
     * @return CompletableFuture for async processing result
     */
    public CompletableFuture<SendResult<String, Object>> publishCriticalAlert(
            Long exceptionId,
            String transactionId,
            String alertLevel,
            String alertReason,
            String interfaceType,
            String exceptionReason,
            String customerId,
            String estimatedImpact,
            String correlationId) {

        // Determine escalation team and immediate action based on alert level
        String escalationTeam = "EMERGENCY".equals(alertLevel) ? "MANAGEMENT" : "OPERATIONS";
        Boolean requiresImmediateAction = "CRITICAL".equals(alertLevel) || "EMERGENCY".equals(alertLevel);

        // Generate causation ID for this alert event
        String causationId = UUID.randomUUID().toString();

        return publishCriticalAlert(
                exceptionId,
                transactionId,
                alertLevel,
                alertReason,
                interfaceType,
                exceptionReason,
                customerId,
                escalationTeam,
                requiresImmediateAction,
                estimatedImpact,
                null, // customersAffected - to be calculated by calling service
                correlationId,
                causationId);
    }
}