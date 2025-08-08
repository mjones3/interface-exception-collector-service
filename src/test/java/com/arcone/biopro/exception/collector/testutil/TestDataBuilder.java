package com.arcone.biopro.exception.collector.testutil;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.CriticalExceptionAlertEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionResolvedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionRetryCompletedEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fluent builder for creating test data objects.
 * Provides convenient methods for creating test entities and events with
 * sensible defaults.
 */
public class TestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);
    private static final AtomicLong TRANSACTION_COUNTER = new AtomicLong(1);

    // InterfaceException Builder
    public static class InterfaceExceptionBuilder {
        private Long id;
        private String transactionId;
        private InterfaceType interfaceType = InterfaceType.ORDER;
        private String exceptionReason = "Test exception reason";
        private String operation = "CREATE_ORDER";
        private String externalId;
        private ExceptionStatus status = ExceptionStatus.NEW;
        private ExceptionSeverity severity = ExceptionSeverity.MEDIUM;
        private ExceptionCategory category = ExceptionCategory.VALIDATION;
        private Boolean retryable = true;
        private String customerId = "TEST-CUST-001";
        private String locationCode = "TEST-LOC-001";
        private OffsetDateTime timestamp = OffsetDateTime.now();
        private OffsetDateTime processedAt = OffsetDateTime.now();
        private OffsetDateTime acknowledgedAt;
        private String acknowledgedBy;
        private OffsetDateTime resolvedAt;
        private String resolvedBy;
        private Integer retryCount = 0;
        private OffsetDateTime lastRetryAt;

        public InterfaceExceptionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public InterfaceExceptionBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public InterfaceExceptionBuilder interfaceType(InterfaceType interfaceType) {
            this.interfaceType = interfaceType;
            return this;
        }

        public InterfaceExceptionBuilder exceptionReason(String exceptionReason) {
            this.exceptionReason = exceptionReason;
            return this;
        }

        public InterfaceExceptionBuilder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public InterfaceExceptionBuilder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public InterfaceExceptionBuilder status(ExceptionStatus status) {
            this.status = status;
            return this;
        }

        public InterfaceExceptionBuilder severity(ExceptionSeverity severity) {
            this.severity = severity;
            return this;
        }

        public InterfaceExceptionBuilder category(ExceptionCategory category) {
            this.category = category;
            return this;
        }

        public InterfaceExceptionBuilder retryable(Boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public InterfaceExceptionBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public InterfaceExceptionBuilder locationCode(String locationCode) {
            this.locationCode = locationCode;
            return this;
        }

        public InterfaceExceptionBuilder timestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public InterfaceExceptionBuilder processedAt(OffsetDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public InterfaceExceptionBuilder acknowledgedAt(OffsetDateTime acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public InterfaceExceptionBuilder acknowledgedBy(String acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public InterfaceExceptionBuilder resolvedAt(OffsetDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public InterfaceExceptionBuilder resolvedBy(String resolvedBy) {
            this.resolvedBy = resolvedBy;
            return this;
        }

        public InterfaceExceptionBuilder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public InterfaceExceptionBuilder lastRetryAt(OffsetDateTime lastRetryAt) {
            this.lastRetryAt = lastRetryAt;
            return this;
        }

        public InterfaceExceptionBuilder acknowledged() {
            this.status = ExceptionStatus.ACKNOWLEDGED;
            this.acknowledgedAt = OffsetDateTime.now();
            this.acknowledgedBy = "test-user";
            return this;
        }

        public InterfaceExceptionBuilder resolved() {
            this.status = ExceptionStatus.RESOLVED;
            this.resolvedAt = OffsetDateTime.now();
            this.resolvedBy = "test-user";
            return this;
        }

        public InterfaceExceptionBuilder withRetries(int count) {
            this.retryCount = count;
            this.lastRetryAt = OffsetDateTime.now();
            return this;
        }

        public InterfaceException build() {
            if (transactionId == null) {
                transactionId = "TEST-TXN-" + TRANSACTION_COUNTER.getAndIncrement();
            }
            if (externalId == null) {
                externalId = "EXT-" + transactionId;
            }

            return InterfaceException.builder()
                    .id(id)
                    .transactionId(transactionId)
                    .interfaceType(interfaceType)
                    .exceptionReason(exceptionReason)
                    .operation(operation)
                    .externalId(externalId)
                    .status(status)
                    .severity(severity)
                    .category(category)
                    .retryable(retryable)
                    .customerId(customerId)
                    .locationCode(locationCode)
                    .timestamp(timestamp)
                    .processedAt(processedAt)
                    .acknowledgedAt(acknowledgedAt)
                    .acknowledgedBy(acknowledgedBy)
                    .resolvedAt(resolvedAt)
                    .resolvedBy(resolvedBy)
                    .retryCount(retryCount)
                    .lastRetryAt(lastRetryAt)
                    .build();
        }
    }

    // RetryAttempt Builder
    public static class RetryAttemptBuilder {
        private Long id;
        private Long exceptionId;
        private Integer attemptNumber = 1;
        private RetryStatus status = RetryStatus.PENDING;
        private String initiatedBy = "test-user";
        private OffsetDateTime initiatedAt = OffsetDateTime.now();
        private OffsetDateTime completedAt;
        private Boolean resultSuccess;
        private String resultMessage;
        private Integer resultResponseCode;
        private Map<String, Object> resultErrorDetails;

        public RetryAttemptBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RetryAttemptBuilder exceptionId(Long exceptionId) {
            this.exceptionId = exceptionId;
            return this;
        }

        public RetryAttemptBuilder attemptNumber(Integer attemptNumber) {
            this.attemptNumber = attemptNumber;
            return this;
        }

        public RetryAttemptBuilder status(RetryStatus status) {
            this.status = status;
            return this;
        }

        public RetryAttemptBuilder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

        public RetryAttemptBuilder initiatedAt(OffsetDateTime initiatedAt) {
            this.initiatedAt = initiatedAt;
            return this;
        }

        public RetryAttemptBuilder completedAt(OffsetDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public RetryAttemptBuilder resultSuccess(Boolean resultSuccess) {
            this.resultSuccess = resultSuccess;
            return this;
        }

        public RetryAttemptBuilder resultMessage(String resultMessage) {
            this.resultMessage = resultMessage;
            return this;
        }

        public RetryAttemptBuilder resultResponseCode(Integer resultResponseCode) {
            this.resultResponseCode = resultResponseCode;
            return this;
        }

        public RetryAttemptBuilder resultErrorDetails(Map<String, Object> resultErrorDetails) {
            this.resultErrorDetails = resultErrorDetails;
            return this;
        }

        public RetryAttemptBuilder successful() {
            this.status = RetryStatus.SUCCESS;
            this.completedAt = OffsetDateTime.now();
            this.resultSuccess = true;
            this.resultMessage = "Retry successful";
            this.resultResponseCode = 200;
            return this;
        }

        public RetryAttemptBuilder failed() {
            this.status = RetryStatus.FAILED;
            this.completedAt = OffsetDateTime.now();
            this.resultSuccess = false;
            this.resultMessage = "Retry failed";
            this.resultResponseCode = 500;
            return this;
        }

        public RetryAttempt build() {
            return RetryAttempt.builder()
                    .id(id)
                    .exceptionId(exceptionId)
                    .attemptNumber(attemptNumber)
                    .status(status)
                    .initiatedBy(initiatedBy)
                    .initiatedAt(initiatedAt)
                    .completedAt(completedAt)
                    .resultSuccess(resultSuccess)
                    .resultMessage(resultMessage)
                    .resultResponseCode(resultResponseCode)
                    .resultErrorDetails(resultErrorDetails)
                    .build();
        }
    }

    // Event Builders
    public static class OrderRejectedEventBuilder {
        private String eventId = "test-event-" + ID_COUNTER.getAndIncrement();
        private String eventType = "OrderRejected";
        private String eventVersion = "1.0";
        private OffsetDateTime occurredOn = OffsetDateTime.now();
        private String source = "order-service";
        private String correlationId = "test-correlation-" + ID_COUNTER.get();
        private String transactionId = "test-txn-" + TRANSACTION_COUNTER.getAndIncrement();
        private String externalId;
        private String operation = "CREATE_ORDER";
        private String rejectedReason = "Test rejection reason";
        private String customerId = "TEST-CUST-001";
        private String locationCode = "TEST-LOC-001";
        private List<Map<String, Object>> orderItems;

        public OrderRejectedEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public OrderRejectedEventBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public OrderRejectedEventBuilder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public OrderRejectedEventBuilder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public OrderRejectedEventBuilder rejectedReason(String rejectedReason) {
            this.rejectedReason = rejectedReason;
            return this;
        }

        public OrderRejectedEventBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderRejectedEventBuilder locationCode(String locationCode) {
            this.locationCode = locationCode;
            return this;
        }

        public OrderRejectedEventBuilder orderItems(List<Map<String, Object>> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public OrderRejectedEventBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public OrderRejectedEvent build() {
            if (externalId == null) {
                externalId = "EXT-" + transactionId;
            }

            return OrderRejectedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .eventVersion(eventVersion)
                    .occurredOn(occurredOn)
                    .source(source)
                    .correlationId(correlationId)
                    .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                            .transactionId(transactionId)
                            .externalId(externalId)
                            .operation(operation)
                            .rejectedReason(rejectedReason)
                            .customerId(customerId)
                            .locationCode(locationCode)
                            .orderItems(orderItems)
                            .build())
                    .build();
        }
    }

    public static class ValidationErrorEventBuilder {
        private String eventId = "test-validation-event-" + ID_COUNTER.getAndIncrement();
        private String eventType = "ValidationError";
        private String eventVersion = "1.0";
        private OffsetDateTime occurredOn = OffsetDateTime.now();
        private String source = "order-service";
        private String correlationId = "test-validation-correlation-" + ID_COUNTER.get();
        private String transactionId = "test-validation-txn-" + TRANSACTION_COUNTER.getAndIncrement();
        private String interfaceType = "ORDER";
        private List<ValidationErrorEvent.ValidationError> validationErrors;

        public ValidationErrorEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public ValidationErrorEventBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public ValidationErrorEventBuilder interfaceType(String interfaceType) {
            this.interfaceType = interfaceType;
            return this;
        }

        public ValidationErrorEventBuilder validationErrors(
                List<ValidationErrorEvent.ValidationError> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ValidationErrorEventBuilder withSingleError(String field, String message, String rejectedValue) {
            this.validationErrors = List.of(
                    ValidationErrorEvent.ValidationError.builder()
                            .field(field)
                            .message(message)
                            .rejectedValue(rejectedValue)
                            .build());
            return this;
        }

        public ValidationErrorEvent build() {
            if (validationErrors == null) {
                validationErrors = List.of(
                        ValidationErrorEvent.ValidationError.builder()
                                .field("testField")
                                .message("Test validation error")
                                .rejectedValue("invalid-value")
                                .build());
            }

            return ValidationErrorEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .eventVersion(eventVersion)
                    .occurredOn(occurredOn)
                    .source(source)
                    .correlationId(correlationId)
                    .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                            .transactionId(transactionId)
                            .interfaceType(interfaceType)
                            .validationErrors(validationErrors)
                            .build())
                    .build();
        }
    }

    // Static factory methods
    public static InterfaceExceptionBuilder anInterfaceException() {
        return new InterfaceExceptionBuilder();
    }

    public static RetryAttemptBuilder aRetryAttempt() {
        return new RetryAttemptBuilder();
    }

    public static OrderRejectedEventBuilder anOrderRejectedEvent() {
        return new OrderRejectedEventBuilder();
    }

    public static ValidationErrorEventBuilder aValidationErrorEvent() {
        return new ValidationErrorEventBuilder();
    }

    // Convenience methods for common scenarios
    public static InterfaceException createOrderException() {
        return anInterfaceException()
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Order validation failed")
                .operation("CREATE_ORDER")
                .build();
    }

    public static InterfaceException createCollectionException() {
        return anInterfaceException()
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Collection processing error")
                .operation("CREATE_COLLECTION")
                .build();
    }

    public static InterfaceException createDistributionException() {
        return anInterfaceException()
                .interfaceType(InterfaceType.DISTRIBUTION)
                .exceptionReason("Distribution failed")
                .operation("CREATE_DISTRIBUTION")
                .build();
    }

    public static InterfaceException createCriticalException() {
        return anInterfaceException()
                .severity(ExceptionSeverity.CRITICAL)
                .exceptionReason("System error occurred")
                .category(ExceptionCategory.SYSTEM_ERROR)
                .build();
    }

    public static InterfaceException createAcknowledgedException() {
        return anInterfaceException()
                .acknowledged()
                .build();
    }

    public static InterfaceException createResolvedException() {
        return anInterfaceException()
                .resolved()
                .build();
    }

    public static RetryAttempt createSuccessfulRetryAttempt(Long exceptionId) {
        return aRetryAttempt()
                .exceptionId(exceptionId)
                .successful()
                .build();
    }

    public static RetryAttempt createFailedRetryAttempt(Long exceptionId) {
        return aRetryAttempt()
                .exceptionId(exceptionId)
                .failed()
                .build();
    }

    public static OrderRejectedEvent createOrderRejectedEvent() {
        return anOrderRejectedEvent().build();
    }

    public static OrderRejectedEvent createOrderRejectedEvent(String transactionId, String reason) {
        return anOrderRejectedEvent()
                .transactionId(transactionId)
                .rejectedReason(reason)
                .build();
    }

    public static ValidationErrorEvent createValidationErrorEvent() {
        return aValidationErrorEvent().build();
    }

    public static ValidationErrorEvent createValidationErrorEvent(String transactionId, String field, String message) {
        return aValidationErrorEvent()
                .transactionId(transactionId)
                .withSingleError(field, message, "invalid-value")
                .build();
    }

    // Batch creation methods
    public static List<InterfaceException> createMultipleExceptions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> anInterfaceException()
                        .transactionId("BATCH-TXN-" + i)
                        .exceptionReason("Batch exception " + i)
                        .customerId("BATCH-CUST-" + (i % 10))
                        .build())
                .toList();
    }

    public static List<InterfaceException> createExceptionsWithDifferentTypes(int countPerType) {
        List<InterfaceException> exceptions = new java.util.ArrayList<>();

        for (InterfaceType type : InterfaceType.values()) {
            for (int i = 0; i < countPerType; i++) {
                exceptions.add(anInterfaceException()
                        .interfaceType(type)
                        .transactionId(type.name() + "-TXN-" + i)
                        .exceptionReason(type.name() + " exception " + i)
                        .build());
            }
        }

        return exceptions;
    }

    public static List<InterfaceException> createExceptionsWithDifferentSeverities(int countPerSeverity) {
        List<InterfaceException> exceptions = new java.util.ArrayList<>();

        for (ExceptionSeverity severity : ExceptionSeverity.values()) {
            for (int i = 0; i < countPerSeverity; i++) {
                exceptions.add(anInterfaceException()
                        .severity(severity)
                        .transactionId(severity.name() + "-TXN-" + i)
                        .exceptionReason(severity.name() + " severity exception " + i)
                        .build());
            }
        }

        return exceptions;
    }
}