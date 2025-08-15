package com.arcone.biopro.exception.collector.domain.mapper;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.OrderItem;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.*;
import com.arcone.biopro.exception.collector.domain.event.outbound.*;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between Kafka events and domain entities.
 * Handles mapping logic for inbound and outbound event transformations.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    // ========== Inbound Event Mappings ==========

    /**
     * Maps OrderRejectedEvent to InterfaceException entity.
     */
    @Mapping(target = "transactionId", source = "payload.transactionId")
    @Mapping(target = "interfaceType", constant = "ORDER")
    @Mapping(target = "exceptionReason", source = "payload.rejectedReason")
    @Mapping(target = "operation", source = "payload.operation", qualifiedByName = "mapOrderOperationToString")
    @Mapping(target = "externalId", source = "payload.externalId")
    @Mapping(target = "customerId", source = "payload.customerId")
    @Mapping(target = "locationCode", source = "payload.locationCode")
    @Mapping(target = "timestamp", source = "occurredOn")
    @Mapping(target = "category", source = "payload.rejectedReason", qualifiedByName = "mapRejectionReasonToCategory")
    @Mapping(target = "severity", source = "payload.rejectedReason", qualifiedByName = "mapRejectionReasonToSeverity")
    @Mapping(target = "orderItems", source = "payload.orderItems", qualifiedByName = "mapOrderItems")
    InterfaceException toInterfaceException(OrderRejectedEvent event);

    /**
     * Maps OrderCancelledEvent to InterfaceException entity.
     */
    @Mapping(target = "transactionId", source = "payload.transactionId")
    @Mapping(target = "interfaceType", constant = "ORDER")
    @Mapping(target = "exceptionReason", source = "payload.cancelReason")
    @Mapping(target = "operation", constant = "CANCEL_ORDER")
    @Mapping(target = "externalId", source = "payload.externalId")
    @Mapping(target = "customerId", source = "payload.customerId")
    @Mapping(target = "timestamp", source = "occurredOn")
    @Mapping(target = "category", constant = "BUSINESS_RULE")
    @Mapping(target = "severity", constant = "MEDIUM")
    InterfaceException toInterfaceException(OrderCancelledEvent event);

    /**
     * Maps CollectionRejectedEvent to InterfaceException entity.
     */
    @Mapping(target = "transactionId", source = "payload.transactionId")
    @Mapping(target = "interfaceType", constant = "COLLECTION")
    @Mapping(target = "exceptionReason", source = "payload.rejectedReason")
    @Mapping(target = "operation", source = "payload.operation")
    @Mapping(target = "externalId", source = "payload.collectionId")
    @Mapping(target = "customerId", source = "payload.donorId")
    @Mapping(target = "locationCode", source = "payload.locationCode")
    @Mapping(target = "timestamp", source = "occurredOn")
    @Mapping(target = "category", source = "payload.rejectedReason", qualifiedByName = "mapRejectionReasonToCategory")
    @Mapping(target = "severity", source = "payload.rejectedReason", qualifiedByName = "mapRejectionReasonToSeverity")
    InterfaceException toInterfaceException(CollectionRejectedEvent event);

    /**
     * Maps DistributionFailedEvent to InterfaceException entity.
     */
    @Mapping(target = "transactionId", source = "payload.transactionId")
    @Mapping(target = "interfaceType", constant = "DISTRIBUTION")
    @Mapping(target = "exceptionReason", source = "payload.failureReason")
    @Mapping(target = "operation", source = "payload.operation")
    @Mapping(target = "externalId", source = "payload.distributionId")
    @Mapping(target = "customerId", source = "payload.customerId")
    @Mapping(target = "locationCode", source = "payload.destinationLocation")
    @Mapping(target = "timestamp", source = "occurredOn")
    @Mapping(target = "category", source = "payload.failureReason", qualifiedByName = "mapFailureReasonToCategory")
    @Mapping(target = "severity", source = "payload.failureReason", qualifiedByName = "mapFailureReasonToSeverity")
    InterfaceException toInterfaceException(DistributionFailedEvent event);

    /**
     * Maps ValidationErrorEvent to InterfaceException entity.
     */
    @Mapping(target = "transactionId", source = "payload.transactionId")
    @Mapping(target = "interfaceType", source = "payload.interfaceType", qualifiedByName = "mapStringToInterfaceType")
    @Mapping(target = "exceptionReason", source = "payload.validationErrors", qualifiedByName = "mapValidationErrorsToReason")
    @Mapping(target = "operation", constant = "VALIDATION")
    @Mapping(target = "timestamp", source = "occurredOn")
    @Mapping(target = "category", constant = "VALIDATION")
    @Mapping(target = "severity", constant = "MEDIUM")
    InterfaceException toInterfaceException(ValidationErrorEvent event);

    // ========== Outbound Event Mappings ==========

    /**
     * Maps InterfaceException to ExceptionCapturedEvent.
     */
    default ExceptionCapturedEvent toExceptionCapturedEvent(InterfaceException exception, String correlationId,
            String causationId) {
        ExceptionCapturedEvent.ExceptionCapturedPayload payload = ExceptionCapturedEvent.ExceptionCapturedPayload
                .builder()
                .exceptionId(exception.getId())
                .transactionId(exception.getTransactionId())
                .interfaceType(exception.getInterfaceType().toString())
                .severity(exception.getSeverity().toString())
                .category(exception.getCategory().toString())
                .exceptionReason(exception.getExceptionReason())
                .customerId(exception.getCustomerId())
                .retryable(exception.getRetryable())
                .build();

        return ExceptionCapturedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ExceptionCaptured")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId(correlationId)
                .causationId(causationId)
                .payload(payload)
                .build();
    }

    /**
     * Maps RetryAttempt to ExceptionRetryCompletedEvent.
     */
    default ExceptionRetryCompletedEvent toExceptionRetryCompletedEvent(RetryAttempt retryAttempt, String correlationId,
            String causationId) {
        ExceptionRetryCompletedEvent.RetryResult retryResult = ExceptionRetryCompletedEvent.RetryResult.builder()
                .success(retryAttempt.getResultSuccess())
                .message(retryAttempt.getResultMessage())
                .responseCode(retryAttempt.getResultResponseCode())
                .errorDetails(retryAttempt.getResultErrorDetails())
                .build();

        ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload payload = ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload
                .builder()
                .exceptionId(retryAttempt.getInterfaceException().getId())
                .transactionId(retryAttempt.getInterfaceException().getTransactionId())
                .attemptNumber(retryAttempt.getAttemptNumber())
                .retryStatus(retryAttempt.getStatus().toString())
                .retryResult(retryResult)
                .initiatedBy(retryAttempt.getInitiatedBy())
                .completedAt(retryAttempt.getCompletedAt())
                .build();

        return ExceptionRetryCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ExceptionRetryCompleted")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId(correlationId)
                .causationId(causationId)
                .payload(payload)
                .build();
    }

    /**
     * Maps InterfaceException to ExceptionResolvedEvent.
     */
    default ExceptionResolvedEvent toExceptionResolvedEvent(InterfaceException exception, String resolutionMethod,
            String resolutionNotes, String correlationId, String causationId) {
        ExceptionResolvedEvent.ExceptionResolvedPayload payload = ExceptionResolvedEvent.ExceptionResolvedPayload
                .builder()
                .exceptionId(exception.getId())
                .transactionId(exception.getTransactionId())
                .resolutionMethod(resolutionMethod)
                .resolvedBy(exception.getResolvedBy())
                .resolvedAt(exception.getResolvedAt())
                .totalRetryAttempts(exception.getRetryCount())
                .resolutionNotes(resolutionNotes)
                .build();

        return ExceptionResolvedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ExceptionResolved")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId(correlationId)
                .causationId(causationId)
                .payload(payload)
                .build();
    }

    /**
     * Maps InterfaceException to CriticalExceptionAlertEvent.
     */
    default CriticalExceptionAlertEvent toCriticalExceptionAlertEvent(
            InterfaceException exception,
            String alertLevel,
            String alertReason,
            String escalationTeam,
            Boolean requiresImmediateAction,
            String estimatedImpact,
            Integer customersAffected,
            String correlationId,
            String causationId) {
        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = CriticalExceptionAlertEvent.CriticalExceptionAlertPayload
                .builder()
                .exceptionId(exception.getId())
                .transactionId(exception.getTransactionId())
                .alertLevel(alertLevel)
                .alertReason(alertReason)
                .interfaceType(exception.getInterfaceType().toString())
                .exceptionReason(exception.getExceptionReason())
                .customerId(exception.getCustomerId())
                .escalationTeam(escalationTeam)
                .requiresImmediateAction(requiresImmediateAction)
                .estimatedImpact(estimatedImpact)
                .customersAffected(customersAffected)
                .build();

        return CriticalExceptionAlertEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CriticalExceptionAlert")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId(correlationId)
                .causationId(causationId)
                .payload(payload)
                .build();
    }

    // ========== Named Mapping Methods ==========

    @Named("mapRejectionReasonToCategory")
    default ExceptionCategory mapRejectionReasonToCategory(String rejectedReason) {
        if (rejectedReason == null) {
            return ExceptionCategory.SYSTEM_ERROR;
        }

        String reason = rejectedReason.toLowerCase();
        if (reason.contains("already exists") || reason.contains("duplicate") || reason.contains("business rule")) {
            return ExceptionCategory.BUSINESS_RULE;
        } else if (reason.contains("validation") || reason.contains("invalid") || reason.contains("format")) {
            return ExceptionCategory.VALIDATION;
        } else if (reason.contains("timeout") || reason.contains("timed out")) {
            return ExceptionCategory.TIMEOUT;
        } else if (reason.contains("network") || reason.contains("connection")) {
            return ExceptionCategory.NETWORK_ERROR;
        } else if (reason.contains("authentication") || reason.contains("unauthorized")) {
            return ExceptionCategory.AUTHENTICATION;
        } else if (reason.contains("authorization") || reason.contains("forbidden")) {
            return ExceptionCategory.AUTHORIZATION;
        } else if (reason.contains("external service") || reason.contains("service unavailable")) {
            return ExceptionCategory.EXTERNAL_SERVICE;
        } else {
            return ExceptionCategory.SYSTEM_ERROR;
        }
    }

    @Named("mapRejectionReasonToSeverity")
    default ExceptionSeverity mapRejectionReasonToSeverity(String rejectedReason) {
        if (rejectedReason == null) {
            return ExceptionSeverity.MEDIUM;
        }

        String reason = rejectedReason.toLowerCase();
        if (reason.contains("critical") || reason.contains("system error") || reason.contains("data corruption")) {
            return ExceptionSeverity.CRITICAL;
        } else if (reason.contains("timeout") || reason.contains("network") || reason.contains("external service")) {
            return ExceptionSeverity.HIGH;
        } else if (reason.contains("validation") || reason.contains("business rule")) {
            return ExceptionSeverity.MEDIUM;
        } else {
            return ExceptionSeverity.LOW;
        }
    }

    @Named("mapFailureReasonToCategory")
    default ExceptionCategory mapFailureReasonToCategory(String failureReason) {
        return mapRejectionReasonToCategory(failureReason);
    }

    @Named("mapFailureReasonToSeverity")
    default ExceptionSeverity mapFailureReasonToSeverity(String failureReason) {
        return mapRejectionReasonToSeverity(failureReason);
    }

    @Named("mapStringToInterfaceType")
    default InterfaceType mapStringToInterfaceType(String interfaceType) {
        if (interfaceType == null) {
            return InterfaceType.ORDER;
        }

        try {
            return InterfaceType.valueOf(interfaceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InterfaceType.ORDER;
        }
    }

    @Named("mapValidationErrorsToReason")
    default String mapValidationErrorsToReason(java.util.List<ValidationErrorEvent.ValidationError> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return "Validation failed";
        }

        return validationErrors.stream()
                .map(error -> {
                    String field = error.getField() != null ? error.getField() + ": " : "";
                    return field + error.getMessage();
                })
                .collect(Collectors.joining("; "));
    }

    @Named("mapOrderOperationToString")
    default String mapOrderOperationToString(OrderRejectedEvent.OrderOperation operation) {
        return operation != null ? operation.name() : "CREATE_ORDER";
    }

    @Named("mapOrderItems")
    default List<OrderItem> mapOrderItems(List<OrderRejectedEvent.OrderItem> eventOrderItems) {
        if (eventOrderItems == null || eventOrderItems.isEmpty()) {
            return new ArrayList<>();
        }

        return eventOrderItems.stream()
                .map(eventItem -> OrderItem.builder()
                        .bloodType(eventItem.getBloodType())
                        .productFamily(eventItem.getProductFamily())
                        .quantity(eventItem.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}