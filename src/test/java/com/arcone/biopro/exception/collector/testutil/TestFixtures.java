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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Test fixtures providing pre-configured test data objects.
 * Contains commonly used test scenarios and data sets.
 */
public class TestFixtures {

    // Common test timestamps
    public static final OffsetDateTime BASE_TIME = OffsetDateTime.parse("2025-08-05T10:00:00Z");
    public static final OffsetDateTime ONE_HOUR_AGO = BASE_TIME.minusHours(1);
    public static final OffsetDateTime ONE_DAY_AGO = BASE_TIME.minusDays(1);
    public static final OffsetDateTime ONE_WEEK_AGO = BASE_TIME.minusWeeks(1);

    // Common test identifiers
    public static final String TEST_TRANSACTION_ID = "TEST-TXN-001";
    public static final String TEST_CUSTOMER_ID = "TEST-CUST-001";
    public static final String TEST_LOCATION_CODE = "TEST-LOC-001";
    public static final String TEST_EXTERNAL_ID = "TEST-EXT-001";
    public static final String TEST_USER = "test-user";

    // Exception Fixtures
    public static class Exceptions {

        public static InterfaceException newOrderException() {
            return InterfaceException.builder()
                    .id(1L)
                    .transactionId(TEST_TRANSACTION_ID)
                    .interfaceType(InterfaceType.ORDER)
                    .exceptionReason("Order validation failed: Invalid customer ID")
                    .operation("CREATE_ORDER")
                    .externalId(TEST_EXTERNAL_ID)
                    .status(ExceptionStatus.NEW)
                    .severity(ExceptionSeverity.HIGH)
                    .category(ExceptionCategory.VALIDATION)
                    .retryable(true)
                    .customerId(TEST_CUSTOMER_ID)
                    .locationCode(TEST_LOCATION_CODE)
                    .timestamp(BASE_TIME)
                    .processedAt(BASE_TIME.plusMinutes(1))
                    .retryCount(0)
                    .build();
        }

        public static InterfaceException acknowledgedCollectionException() {
            return InterfaceException.builder()
                    .id(2L)
                    .transactionId("TEST-TXN-002")
                    .interfaceType(InterfaceType.COLLECTION)
                    .exceptionReason("Collection processing error: Donor not found")
                    .operation("CREATE_COLLECTION")
                    .externalId("TEST-EXT-002")
                    .status(ExceptionStatus.ACKNOWLEDGED)
                    .severity(ExceptionSeverity.CRITICAL)
                    .category(ExceptionCategory.BUSINESS_RULE)
                    .retryable(false)
                    .customerId("TEST-CUST-002")
                    .locationCode("TEST-LOC-002")
                    .timestamp(ONE_HOUR_AGO)
                    .processedAt(ONE_HOUR_AGO.plusMinutes(1))
                    .acknowledgedAt(ONE_HOUR_AGO.plusMinutes(30))
                    .acknowledgedBy(TEST_USER)
                    .retryCount(0)
                    .build();
        }

        public static InterfaceException resolvedDistributionException() {
            return InterfaceException.builder()
                    .id(3L)
                    .transactionId("TEST-TXN-003")
                    .interfaceType(InterfaceType.DISTRIBUTION)
                    .exceptionReason("Distribution failed: Network timeout")
                    .operation("CREATE_DISTRIBUTION")
                    .externalId("TEST-EXT-003")
                    .status(ExceptionStatus.RESOLVED)
                    .severity(ExceptionSeverity.MEDIUM)
                    .category(ExceptionCategory.NETWORK_ERROR)
                    .retryable(true)
                    .customerId("TEST-CUST-003")
                    .locationCode("TEST-LOC-003")
                    .timestamp(ONE_DAY_AGO)
                    .processedAt(ONE_DAY_AGO.plusMinutes(1))
                    .acknowledgedAt(ONE_DAY_AGO.plusMinutes(30))
                    .acknowledgedBy(TEST_USER)
                    .resolvedAt(ONE_DAY_AGO.plusHours(2))
                    .resolvedBy("system")
                    .retryCount(2)
                    .lastRetryAt(ONE_DAY_AGO.plusHours(1))
                    .build();
        }

        public static InterfaceException criticalSystemException() {
            return InterfaceException.builder()
                    .id(4L)
                    .transactionId("TEST-TXN-004")
                    .interfaceType(InterfaceType.ORDER)
                    .exceptionReason("System error occurred: Database connection failed")
                    .operation("CREATE_ORDER")
                    .externalId("TEST-EXT-004")
                    .status(ExceptionStatus.ESCALATED)
                    .severity(ExceptionSeverity.CRITICAL)
                    .category(ExceptionCategory.SYSTEM_ERROR)
                    .retryable(true)
                    .customerId("TEST-CUST-004")
                    .locationCode("TEST-LOC-004")
                    .timestamp(BASE_TIME.minusMinutes(30))
                    .processedAt(BASE_TIME.minusMinutes(29))
                    .retryCount(3)
                    .lastRetryAt(BASE_TIME.minusMinutes(10))
                    .build();
        }

        public static InterfaceException retriedFailedException() {
            return InterfaceException.builder()
                    .id(5L)
                    .transactionId("TEST-TXN-005")
                    .interfaceType(InterfaceType.COLLECTION)
                    .exceptionReason("Collection validation failed: Invalid donor data")
                    .operation("CREATE_COLLECTION")
                    .externalId("TEST-EXT-005")
                    .status(ExceptionStatus.RETRIED_FAILED)
                    .severity(ExceptionSeverity.HIGH)
                    .category(ExceptionCategory.VALIDATION)
                    .retryable(true)
                    .customerId("TEST-CUST-005")
                    .locationCode("TEST-LOC-005")
                    .timestamp(ONE_HOUR_AGO)
                    .processedAt(ONE_HOUR_AGO.plusMinutes(1))
                    .acknowledgedAt(ONE_HOUR_AGO.plusMinutes(15))
                    .acknowledgedBy(TEST_USER)
                    .retryCount(1)
                    .lastRetryAt(ONE_HOUR_AGO.plusMinutes(30))
                    .build();
        }

        public static List<InterfaceException> multipleExceptionsForCustomer() {
            return List.of(
                    newOrderException(),
                    InterfaceException.builder()
                            .id(6L)
                            .transactionId("TEST-TXN-006")
                            .interfaceType(InterfaceType.DISTRIBUTION)
                            .exceptionReason("Distribution timeout for same customer")
                            .operation("CREATE_DISTRIBUTION")
                            .externalId("TEST-EXT-006")
                            .status(ExceptionStatus.NEW)
                            .severity(ExceptionSeverity.MEDIUM)
                            .category(ExceptionCategory.NETWORK_ERROR)
                            .retryable(true)
                            .customerId(TEST_CUSTOMER_ID) // Same customer as first exception
                            .locationCode("TEST-LOC-006")
                            .timestamp(BASE_TIME.minusMinutes(15))
                            .processedAt(BASE_TIME.minusMinutes(14))
                            .retryCount(0)
                            .build());
        }

        public static List<InterfaceException> exceptionsWithDifferentSeverities() {
            return List.of(
                    TestDataBuilder.anInterfaceException()
                            .transactionId("LOW-SEVERITY-001")
                            .severity(ExceptionSeverity.LOW)
                            .exceptionReason("Low severity exception")
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("MEDIUM-SEVERITY-001")
                            .severity(ExceptionSeverity.MEDIUM)
                            .exceptionReason("Medium severity exception")
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("HIGH-SEVERITY-001")
                            .severity(ExceptionSeverity.HIGH)
                            .exceptionReason("High severity exception")
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("CRITICAL-SEVERITY-001")
                            .severity(ExceptionSeverity.CRITICAL)
                            .exceptionReason("Critical severity exception")
                            .build());
        }

        public static List<InterfaceException> exceptionsWithDifferentStatuses() {
            return List.of(
                    TestDataBuilder.anInterfaceException()
                            .transactionId("NEW-STATUS-001")
                            .status(ExceptionStatus.NEW)
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("ACKNOWLEDGED-STATUS-001")
                            .status(ExceptionStatus.ACKNOWLEDGED)
                            .acknowledgedAt(BASE_TIME)
                            .acknowledgedBy(TEST_USER)
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("RESOLVED-STATUS-001")
                            .status(ExceptionStatus.RESOLVED)
                            .resolvedAt(BASE_TIME)
                            .resolvedBy(TEST_USER)
                            .build(),
                    TestDataBuilder.anInterfaceException()
                            .transactionId("CLOSED-STATUS-001")
                            .status(ExceptionStatus.CLOSED)
                            .resolvedAt(BASE_TIME.minusHours(1))
                            .resolvedBy(TEST_USER)
                            .build());
        }
    }

    // Retry Attempt Fixtures
    public static class RetryAttempts {

        public static RetryAttempt pendingRetryAttempt() {
            return RetryAttempt.builder()
                    .id(1L)
                    .exceptionId(1L)
                    .attemptNumber(1)
                    .status(RetryStatus.PENDING)
                    .initiatedBy(TEST_USER)
                    .initiatedAt(BASE_TIME)
                    .build();
        }

        public static RetryAttempt successfulRetryAttempt() {
            return RetryAttempt.builder()
                    .id(2L)
                    .exceptionId(1L)
                    .attemptNumber(1)
                    .status(RetryStatus.SUCCESS)
                    .initiatedBy(TEST_USER)
                    .initiatedAt(BASE_TIME)
                    .completedAt(BASE_TIME.plusMinutes(5))
                    .resultSuccess(true)
                    .resultMessage("Retry completed successfully")
                    .resultResponseCode(200)
                    .build();
        }

        public static RetryAttempt failedRetryAttempt() {
            return RetryAttempt.builder()
                    .id(3L)
                    .exceptionId(1L)
                    .attemptNumber(2)
                    .status(RetryStatus.FAILED)
                    .initiatedBy(TEST_USER)
                    .initiatedAt(BASE_TIME.plusMinutes(10))
                    .completedAt(BASE_TIME.plusMinutes(15))
                    .resultSuccess(false)
                    .resultMessage("Retry failed: Service unavailable")
                    .resultResponseCode(503)
                    .resultErrorDetails(Map.of(
                            "error", "Service unavailable",
                            "retryAfter", 300))
                    .build();
        }

        public static List<RetryAttempt> multipleRetryAttempts() {
            return List.of(
                    RetryAttempt.builder()
                            .id(4L)
                            .exceptionId(1L)
                            .attemptNumber(1)
                            .status(RetryStatus.FAILED)
                            .initiatedBy(TEST_USER)
                            .initiatedAt(BASE_TIME)
                            .completedAt(BASE_TIME.plusMinutes(2))
                            .resultSuccess(false)
                            .resultMessage("First retry failed")
                            .resultResponseCode(500)
                            .build(),
                    RetryAttempt.builder()
                            .id(5L)
                            .exceptionId(1L)
                            .attemptNumber(2)
                            .status(RetryStatus.FAILED)
                            .initiatedBy(TEST_USER)
                            .initiatedAt(BASE_TIME.plusMinutes(5))
                            .completedAt(BASE_TIME.plusMinutes(7))
                            .resultSuccess(false)
                            .resultMessage("Second retry failed")
                            .resultResponseCode(503)
                            .build(),
                    RetryAttempt.builder()
                            .id(6L)
                            .exceptionId(1L)
                            .attemptNumber(3)
                            .status(RetryStatus.SUCCESS)
                            .initiatedBy(TEST_USER)
                            .initiatedAt(BASE_TIME.plusMinutes(10))
                            .completedAt(BASE_TIME.plusMinutes(12))
                            .resultSuccess(true)
                            .resultMessage("Third retry succeeded")
                            .resultResponseCode(200)
                            .build());
        }
    }

    // Event Fixtures
    public static class Events {

        public static OrderRejectedEvent orderRejectedEvent() {
            return OrderRejectedEvent.builder()
                    .eventId("test-order-rejected-001")
                    .eventType("OrderRejected")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("order-service")
                    .correlationId("test-correlation-001")
                    .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                            .transactionId(TEST_TRANSACTION_ID)
                            .externalId(TEST_EXTERNAL_ID)
                            .operation("CREATE_ORDER")
                            .rejectedReason("Order validation failed: Invalid customer ID")
                            .customerId(TEST_CUSTOMER_ID)
                            .locationCode(TEST_LOCATION_CODE)
                            .orderItems(List.of(
                                    Map.of("itemId", "ITEM-001", "quantity", 5, "price", 29.99),
                                    Map.of("itemId", "ITEM-002", "quantity", 2, "price", 15.50)))
                            .build())
                    .build();
        }

        public static OrderCancelledEvent orderCancelledEvent() {
            return OrderCancelledEvent.builder()
                    .eventId("test-order-cancelled-001")
                    .eventType("OrderCancelled")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("order-service")
                    .correlationId("test-correlation-002")
                    .payload(OrderCancelledEvent.OrderCancelledPayload.builder()
                            .transactionId("TEST-TXN-002")
                            .externalId("TEST-EXT-002")
                            .cancelReason("Customer requested cancellation")
                            .cancelledBy("customer")
                            .customerId("TEST-CUST-002")
                            .build())
                    .build();
        }

        public static CollectionRejectedEvent collectionRejectedEvent() {
            return CollectionRejectedEvent.builder()
                    .eventId("test-collection-rejected-001")
                    .eventType("CollectionRejected")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("collection-service")
                    .correlationId("test-correlation-003")
                    .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                            .transactionId("TEST-TXN-003")
                            .collectionId("COLL-001")
                            .operation("CREATE_COLLECTION")
                            .rejectedReason("Invalid donor information")
                            .donorId("DONOR-001")
                            .locationCode("TEST-LOC-003")
                            .build())
                    .build();
        }

        public static DistributionFailedEvent distributionFailedEvent() {
            return DistributionFailedEvent.builder()
                    .eventId("test-distribution-failed-001")
                    .eventType("DistributionFailed")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("distribution-service")
                    .correlationId("test-correlation-004")
                    .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                            .transactionId("TEST-TXN-004")
                            .distributionId("DIST-001")
                            .operation("CREATE_DISTRIBUTION")
                            .failureReason("Destination location not found")
                            .customerId("TEST-CUST-004")
                            .destinationLocation("DEST-001")
                            .build())
                    .build();
        }

        public static ValidationErrorEvent validationErrorEvent() {
            return ValidationErrorEvent.builder()
                    .eventId("test-validation-error-001")
                    .eventType("ValidationError")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("order-service")
                    .correlationId("test-correlation-005")
                    .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                            .transactionId("TEST-TXN-005")
                            .interfaceType("ORDER")
                            .validationErrors(List.of(
                                    ValidationErrorEvent.ValidationError.builder()
                                            .field("customerId")
                                            .message("Customer ID is required")
                                            .rejectedValue("")
                                            .build(),
                                    ValidationErrorEvent.ValidationError.builder()
                                            .field("orderDate")
                                            .message("Order date is invalid")
                                            .rejectedValue("invalid-date")
                                            .build(),
                                    ValidationErrorEvent.ValidationError.builder()
                                            .field("items")
                                            .message("At least one item is required")
                                            .rejectedValue("[]")
                                            .build()))
                            .build())
                    .build();
        }

        public static ValidationErrorEvent singleFieldValidationErrorEvent() {
            return ValidationErrorEvent.builder()
                    .eventId("test-single-validation-error-001")
                    .eventType("ValidationError")
                    .eventVersion("1.0")
                    .occurredOn(BASE_TIME)
                    .source("collection-service")
                    .correlationId("test-correlation-006")
                    .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                            .transactionId("TEST-TXN-006")
                            .interfaceType("COLLECTION")
                            .validationErrors(List.of(
                                    ValidationErrorEvent.ValidationError.builder()
                                            .field("donorId")
                                            .message("Donor ID format is invalid")
                                            .rejectedValue("INVALID-DONOR-ID")
                                            .build()))
                            .build())
                    .build();
        }
    }

    // API Response Fixtures
    public static class ApiResponses {

        public static String exceptionListResponse() {
            return """
                    {
                        "content": [
                            {
                                "id": 1,
                                "transactionId": "TEST-TXN-001",
                                "interfaceType": "ORDER",
                                "exceptionReason": "Order validation failed",
                                "operation": "CREATE_ORDER",
                                "status": "NEW",
                                "severity": "HIGH",
                                "category": "VALIDATION",
                                "retryable": true,
                                "customerId": "TEST-CUST-001",
                                "timestamp": "2025-08-05T10:00:00Z"
                            }
                        ],
                        "page": 0,
                        "size": 20,
                        "totalElements": 1,
                        "totalPages": 1,
                        "first": true,
                        "last": true,
                        "numberOfElements": 1,
                        "empty": false
                    }
                    """;
        }

        public static String exceptionDetailResponse() {
            return """
                    {
                        "id": 1,
                        "transactionId": "TEST-TXN-001",
                        "interfaceType": "ORDER",
                        "exceptionReason": "Order validation failed: Invalid customer ID",
                        "operation": "CREATE_ORDER",
                        "externalId": "TEST-EXT-001",
                        "status": "NEW",
                        "severity": "HIGH",
                        "category": "VALIDATION",
                        "retryable": true,
                        "customerId": "TEST-CUST-001",
                        "locationCode": "TEST-LOC-001",
                        "timestamp": "2025-08-05T10:00:00Z",
                        "processedAt": "2025-08-05T10:01:00Z",
                        "retryCount": 0,
                        "retryHistory": [],
                        "relatedExceptions": [],
                        "originalPayload": null
                    }
                    """;
        }

        public static String exceptionSummaryResponse() {
            return """
                    {
                        "totalExceptions": 100,
                        "byInterfaceType": {
                            "ORDER": 50,
                            "COLLECTION": 30,
                            "DISTRIBUTION": 20
                        },
                        "bySeverity": {
                            "LOW": 20,
                            "MEDIUM": 60,
                            "HIGH": 15,
                            "CRITICAL": 5
                        },
                        "byStatus": {
                            "NEW": 40,
                            "ACKNOWLEDGED": 30,
                            "RESOLVED": 25,
                            "CLOSED": 5
                        },
                        "trends": [
                            {
                                "date": "2025-08-05",
                                "count": 25
                            },
                            {
                                "date": "2025-08-04",
                                "count": 30
                            }
                        ]
                    }
                    """;
        }

        public static String acknowledgeResponse() {
            return """
                    {
                        "status": "ACKNOWLEDGED",
                        "acknowledgedAt": "2025-08-05T10:30:00Z",
                        "acknowledgedBy": "test-user",
                        "message": "Exception acknowledged successfully"
                    }
                    """;
        }

        public static String retryResponse() {
            return """
                    {
                        "retryId": "retry-001",
                        "status": "ACCEPTED",
                        "message": "Retry operation initiated",
                        "estimatedCompletionTime": "2025-08-05T10:35:00Z"
                    }
                    """;
        }
    }

    // External Service Mock Responses
    public static class ExternalServiceResponses {

        public static String orderPayloadResponse() {
            return """
                    {
                        "orderId": "ORDER-12345",
                        "customerId": "TEST-CUST-001",
                        "orderDate": "2025-08-05T09:00:00Z",
                        "items": [
                            {
                                "itemId": "ITEM-001",
                                "quantity": 5,
                                "price": 29.99
                            },
                            {
                                "itemId": "ITEM-002",
                                "quantity": 2,
                                "price": 15.50
                            }
                        ],
                        "totalAmount": 180.95,
                        "status": "PENDING"
                    }
                    """;
        }

        public static String collectionPayloadResponse() {
            return """
                    {
                        "collectionId": "COLL-12345",
                        "donorId": "DONOR-001",
                        "collectionDate": "2025-08-05T08:00:00Z",
                        "locationCode": "TEST-LOC-002",
                        "samples": [
                            {
                                "sampleId": "SAMPLE-001",
                                "type": "BLOOD",
                                "volume": 450
                            }
                        ],
                        "status": "COLLECTED"
                    }
                    """;
        }

        public static String distributionPayloadResponse() {
            return """
                    {
                        "distributionId": "DIST-12345",
                        "customerId": "TEST-CUST-003",
                        "destinationLocation": "DEST-001",
                        "distributionDate": "2025-08-05T11:00:00Z",
                        "items": [
                            {
                                "itemId": "ITEM-003",
                                "quantity": 10
                            }
                        ],
                        "status": "IN_TRANSIT"
                    }
                    """;
        }

        public static String serviceUnavailableResponse() {
            return """
                    {
                        "error": "Service temporarily unavailable",
                        "code": "SERVICE_UNAVAILABLE",
                        "retryAfter": 300
                    }
                    """;
        }

        public static String successfulRetryResponse() {
            return """
                    {
                        "success": true,
                        "message": "Operation completed successfully",
                        "transactionId": "TEST-TXN-001",
                        "processedAt": "2025-08-05T10:35:00Z"
                    }
                    """;
        }

        public static String failedRetryResponse() {
            return """
                    {
                        "success": false,
                        "message": "Operation failed: Invalid data",
                        "error": {
                            "code": "VALIDATION_ERROR",
                            "details": "Customer ID is invalid"
                        }
                    }
                    """;
        }
    }
}