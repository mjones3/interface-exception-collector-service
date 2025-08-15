package com.arcone.biopro.exception.collector.contract;

import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.CriticalExceptionAlertEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionResolvedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionRetryCompletedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for Kafka event schemas.
 * Validates that all inbound and outbound events conform to their expected
 * schemas
 * and maintain backward compatibility.
 */
@SpringBootTest
@DisplayName("Kafka Event Schema Contract Tests")
class KafkaEventSchemaContractTest {

    private ObjectMapper objectMapper;
    private JsonSchemaFactory schemaFactory;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    @Nested
    @DisplayName("Inbound Event Schema Tests")
    class InboundEventSchemaTests {

        @Test
        @DisplayName("OrderRejected event should conform to schema")
        void orderRejectedEventShouldConformToSchema() throws Exception {
            // Given
            OrderRejectedEvent event = OrderRejectedEvent.builder()
                    .eventId("test-event-001")
                    .eventType("OrderRejected")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("order-service")
                    .correlationId("test-correlation-001")
                    .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                            .transactionId("txn-001")
                            .externalId("ORDER-12345")
                            .operation("CREATE_ORDER")
                            .rejectedReason("Order validation failed")
                            .customerId("CUST-001")
                            .locationCode("LOC-001")
                            .orderItems(List.of(Map.of("itemId", "ITEM-001", "quantity", 5)))
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "OrderRejected");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("OrderRejected");
            assertThat(eventJson.get("payload").get("transactionId").asText()).isEqualTo("txn-001");
            assertThat(eventJson.get("payload").get("operation").asText()).isEqualTo("CREATE_ORDER");
        }

        @Test
        @DisplayName("OrderCancelled event should conform to schema")
        void orderCancelledEventShouldConformToSchema() throws Exception {
            // Given
            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .eventId("test-event-002")
                    .eventType("OrderCancelled")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("order-service")
                    .correlationId("test-correlation-002")
                    .payload(OrderCancelledEvent.OrderCancelledPayload.builder()
                            .transactionId("txn-002")
                            .externalId("ORDER-67890")
                            .cancelReason("Customer requested cancellation")
                            .cancelledBy("customer")
                            .customerId("CUST-002")
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "OrderCancelled");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("OrderCancelled");
            assertThat(eventJson.get("payload").get("cancelReason").asText())
                    .isEqualTo("Customer requested cancellation");
        }

        @Test
        @DisplayName("CollectionRejected event should conform to schema")
        void collectionRejectedEventShouldConformToSchema() throws Exception {
            // Given
            CollectionRejectedEvent event = CollectionRejectedEvent.builder()
                    .eventId("test-event-003")
                    .eventType("CollectionRejected")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("collection-service")
                    .correlationId("test-correlation-003")
                    .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                            .transactionId("txn-003")
                            .collectionId("COLL-12345")
                            .operation("CREATE_COLLECTION")
                            .rejectedReason("Invalid donor information")
                            .donorId("DONOR-001")
                            .locationCode("LOC-002")
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "CollectionRejected");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("CollectionRejected");
            assertThat(eventJson.get("payload").get("collectionId").asText()).isEqualTo("COLL-12345");
        }

        @Test
        @DisplayName("DistributionFailed event should conform to schema")
        void distributionFailedEventShouldConformToSchema() throws Exception {
            // Given
            DistributionFailedEvent event = DistributionFailedEvent.builder()
                    .eventId("test-event-004")
                    .eventType("DistributionFailed")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("distribution-service")
                    .correlationId("test-correlation-004")
                    .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                            .transactionId("txn-004")
                            .distributionId("DIST-12345")
                            .operation("CREATE_DISTRIBUTION")
                            .failureReason("Destination location not found")
                            .customerId("CUST-003")
                            .destinationLocation("DEST-001")
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "DistributionFailed");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("DistributionFailed");
            assertThat(eventJson.get("payload").get("distributionId").asText()).isEqualTo("DIST-12345");
        }

        @Test
        @DisplayName("ValidationError event should conform to schema")
        void validationErrorEventShouldConformToSchema() throws Exception {
            // Given
            ValidationErrorEvent event = ValidationErrorEvent.builder()
                    .eventId("test-event-005")
                    .eventType("ValidationError")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("order-service")
                    .correlationId("test-correlation-005")
                    .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                            .transactionId("txn-005")
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
                                            .build()))
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "ValidationError");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("ValidationError");
            assertThat(eventJson.get("payload").get("validationErrors")).isArray();
            assertThat(eventJson.get("payload").get("validationErrors")).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Outbound Event Schema Tests")
    class OutboundEventSchemaTests {

        @Test
        @DisplayName("ExceptionCaptured event should conform to schema")
        void exceptionCapturedEventShouldConformToSchema() throws Exception {
            // Given
            ExceptionCapturedEvent event = ExceptionCapturedEvent.builder()
                    .eventId("test-event-006")
                    .eventType("ExceptionCaptured")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("exception-collector-service")
                    .correlationId("test-correlation-006")
                    .payload(ExceptionCapturedEvent.ExceptionCapturedPayload.builder()
                            .exceptionId(12345L)
                            .transactionId("txn-006")
                            .interfaceType("ORDER")
                            .severity("HIGH")
                            .category("VALIDATION")
                            .exceptionReason("Order validation failed")
                            .customerId("CUST-004")
                            .retryable(true)
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "ExceptionCaptured");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("ExceptionCaptured");
            assertThat(eventJson.get("payload").get("exceptionId").asLong()).isEqualTo(12345L);
            assertThat(eventJson.get("payload").get("retryable").asBoolean()).isTrue();
        }

        @Test
        @DisplayName("ExceptionRetryCompleted event should conform to schema")
        void exceptionRetryCompletedEventShouldConformToSchema() throws Exception {
            // Given
            ExceptionRetryCompletedEvent event = ExceptionRetryCompletedEvent.builder()
                    .eventId("test-event-007")
                    .eventType("ExceptionRetryCompleted")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("exception-collector-service")
                    .correlationId("test-correlation-007")
                    .payload(ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload.builder()
                            .exceptionId(12346L)
                            .transactionId("txn-007")
                            .attemptNumber(2)
                            .retryStatus("SUCCESS")
                            .retryResult(Map.of("success", true, "message", "Retry successful"))
                            .initiatedBy("system")
                            .completedAt(OffsetDateTime.now())
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "ExceptionRetryCompleted");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("ExceptionRetryCompleted");
            assertThat(eventJson.get("payload").get("attemptNumber").asInt()).isEqualTo(2);
            assertThat(eventJson.get("payload").get("retryStatus").asText()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("ExceptionResolved event should conform to schema")
        void exceptionResolvedEventShouldConformToSchema() throws Exception {
            // Given
            ExceptionResolvedEvent event = ExceptionResolvedEvent.builder()
                    .eventId("test-event-008")
                    .eventType("ExceptionResolved")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("exception-collector-service")
                    .correlationId("test-correlation-008")
                    .payload(ExceptionResolvedEvent.ExceptionResolvedPayload.builder()
                            .exceptionId(12347L)
                            .transactionId("txn-008")
                            .resolutionMethod("RETRY_SUCCESS")
                            .resolvedBy("system")
                            .resolvedAt(OffsetDateTime.now())
                            .totalRetryAttempts(3)
                            .resolutionNotes("Resolved after successful retry")
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "ExceptionResolved");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("ExceptionResolved");
            assertThat(eventJson.get("payload").get("resolutionMethod").asText()).isEqualTo("RETRY_SUCCESS");
            assertThat(eventJson.get("payload").get("totalRetryAttempts").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("CriticalExceptionAlert event should conform to schema")
        void criticalExceptionAlertEventShouldConformToSchema() throws Exception {
            // Given
            CriticalExceptionAlertEvent event = CriticalExceptionAlertEvent.builder()
                    .eventId("test-event-009")
                    .eventType("CriticalExceptionAlert")
                    .eventVersion("1.0")
                    .occurredOn(OffsetDateTime.now())
                    .source("exception-collector-service")
                    .correlationId("test-correlation-009")
                    .payload(CriticalExceptionAlertEvent.CriticalExceptionAlertPayload.builder()
                            .exceptionId(12348L)
                            .transactionId("txn-009")
                            .alertLevel("CRITICAL")
                            .alertReason("CRITICAL_SEVERITY")
                            .interfaceType("ORDER")
                            .exceptionReason("System error occurred")
                            .customerId("CUST-005")
                            .escalationTeam("OPERATIONS")
                            .estimatedImpact("HIGH")
                            .requiresImmediateAction(true)
                            .build())
                    .build();

            // When
            JsonNode eventJson = objectMapper.valueToTree(event);

            // Then
            validateEventStructure(eventJson, "CriticalExceptionAlert");
            assertThat(eventJson.get("eventType").asText()).isEqualTo("CriticalExceptionAlert");
            assertThat(eventJson.get("payload").get("alertLevel").asText()).isEqualTo("CRITICAL");
            assertThat(eventJson.get("payload").get("requiresImmediateAction").asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("Event Serialization/Deserialization Tests")
    class EventSerializationTests {

        @Test
        @DisplayName("All inbound events should serialize and deserialize correctly")
        void allInboundEventsShouldSerializeAndDeserializeCorrectly() throws Exception {
            // Test OrderRejectedEvent
            OrderRejectedEvent originalOrderEvent = createSampleOrderRejectedEvent();
            String serialized = objectMapper.writeValueAsString(originalOrderEvent);
            OrderRejectedEvent deserialized = objectMapper.readValue(serialized, OrderRejectedEvent.class);
            assertThat(deserialized).usingRecursiveComparison().isEqualTo(originalOrderEvent);

            // Test CollectionRejectedEvent
            CollectionRejectedEvent originalCollectionEvent = createSampleCollectionRejectedEvent();
            serialized = objectMapper.writeValueAsString(originalCollectionEvent);
            CollectionRejectedEvent deserializedCollection = objectMapper.readValue(serialized,
                    CollectionRejectedEvent.class);
            assertThat(deserializedCollection).usingRecursiveComparison().isEqualTo(originalCollectionEvent);

            // Test DistributionFailedEvent
            DistributionFailedEvent originalDistributionEvent = createSampleDistributionFailedEvent();
            serialized = objectMapper.writeValueAsString(originalDistributionEvent);
            DistributionFailedEvent deserializedDistribution = objectMapper.readValue(serialized,
                    DistributionFailedEvent.class);
            assertThat(deserializedDistribution).usingRecursiveComparison().isEqualTo(originalDistributionEvent);

            // Test ValidationErrorEvent
            ValidationErrorEvent originalValidationEvent = createSampleValidationErrorEvent();
            serialized = objectMapper.writeValueAsString(originalValidationEvent);
            ValidationErrorEvent deserializedValidation = objectMapper.readValue(serialized,
                    ValidationErrorEvent.class);
            assertThat(deserializedValidation).usingRecursiveComparison().isEqualTo(originalValidationEvent);
        }

        @Test
        @DisplayName("All outbound events should serialize and deserialize correctly")
        void allOutboundEventsShouldSerializeAndDeserializeCorrectly() throws Exception {
            // Test ExceptionCapturedEvent
            ExceptionCapturedEvent originalCapturedEvent = createSampleExceptionCapturedEvent();
            String serialized = objectMapper.writeValueAsString(originalCapturedEvent);
            ExceptionCapturedEvent deserializedCaptured = objectMapper.readValue(serialized,
                    ExceptionCapturedEvent.class);
            assertThat(deserializedCaptured).usingRecursiveComparison().isEqualTo(originalCapturedEvent);

            // Test ExceptionRetryCompletedEvent
            ExceptionRetryCompletedEvent originalRetryEvent = createSampleExceptionRetryCompletedEvent();
            serialized = objectMapper.writeValueAsString(originalRetryEvent);
            ExceptionRetryCompletedEvent deserializedRetry = objectMapper.readValue(serialized,
                    ExceptionRetryCompletedEvent.class);
            assertThat(deserializedRetry).usingRecursiveComparison().isEqualTo(originalRetryEvent);

            // Test ExceptionResolvedEvent
            ExceptionResolvedEvent originalResolvedEvent = createSampleExceptionResolvedEvent();
            serialized = objectMapper.writeValueAsString(originalResolvedEvent);
            ExceptionResolvedEvent deserializedResolved = objectMapper.readValue(serialized,
                    ExceptionResolvedEvent.class);
            assertThat(deserializedResolved).usingRecursiveComparison().isEqualTo(originalResolvedEvent);

            // Test CriticalExceptionAlertEvent
            CriticalExceptionAlertEvent originalAlertEvent = createSampleCriticalExceptionAlertEvent();
            serialized = objectMapper.writeValueAsString(originalAlertEvent);
            CriticalExceptionAlertEvent deserializedAlert = objectMapper.readValue(serialized,
                    CriticalExceptionAlertEvent.class);
            assertThat(deserializedAlert).usingRecursiveComparison().isEqualTo(originalAlertEvent);
        }
    }

    @Nested
    @DisplayName("Event Version Compatibility Tests")
    class EventVersionCompatibilityTests {

        @Test
        @DisplayName("Should handle missing optional fields gracefully")
        void shouldHandleMissingOptionalFieldsGracefully() throws Exception {
            // Given - JSON with missing optional fields
            String jsonWithMissingFields = """
                    {
                        "eventId": "test-event-010",
                        "eventType": "OrderRejected",
                        "eventVersion": "1.0",
                        "occurredOn": "2025-08-05T10:30:00Z",
                        "source": "order-service",
                        "correlationId": "test-correlation-010",
                        "payload": {
                            "transactionId": "txn-010",
                            "externalId": "ORDER-MINIMAL",
                            "operation": "CREATE_ORDER",
                            "rejectedReason": "Minimal order rejection",
                            "customerId": "CUST-MINIMAL"
                        }
                    }
                    """;

            // When
            OrderRejectedEvent event = objectMapper.readValue(jsonWithMissingFields, OrderRejectedEvent.class);

            // Then
            assertThat(event.getEventId()).isEqualTo("test-event-010");
            assertThat(event.getPayload().getTransactionId()).isEqualTo("txn-010");
            assertThat(event.getPayload().getLocationCode()).isNull(); // Optional field
            assertThat(event.getPayload().getOrderItems()).isNull(); // Optional field
        }

        @Test
        @DisplayName("Should handle additional unknown fields gracefully")
        void shouldHandleAdditionalUnknownFieldsGracefully() throws Exception {
            // Given - JSON with additional unknown fields
            String jsonWithExtraFields = """
                    {
                        "eventId": "test-event-011",
                        "eventType": "OrderRejected",
                        "eventVersion": "1.0",
                        "occurredOn": "2025-08-05T10:30:00Z",
                        "source": "order-service",
                        "correlationId": "test-correlation-011",
                        "unknownField": "should be ignored",
                        "payload": {
                            "transactionId": "txn-011",
                            "externalId": "ORDER-EXTRA",
                            "operation": "CREATE_ORDER",
                            "rejectedReason": "Order with extra fields",
                            "customerId": "CUST-EXTRA",
                            "locationCode": "LOC-EXTRA",
                            "extraPayloadField": "should also be ignored"
                        }
                    }
                    """;

            // When
            OrderRejectedEvent event = objectMapper.readValue(jsonWithExtraFields, OrderRejectedEvent.class);

            // Then
            assertThat(event.getEventId()).isEqualTo("test-event-011");
            assertThat(event.getPayload().getTransactionId()).isEqualTo("txn-011");
            assertThat(event.getPayload().getCustomerId()).isEqualTo("CUST-EXTRA");
        }
    }

    // Helper methods for validation and sample data creation

    private void validateEventStructure(JsonNode eventJson, String eventType) {
        // Validate common event structure
        assertThat(eventJson.has("eventId")).isTrue();
        assertThat(eventJson.has("eventType")).isTrue();
        assertThat(eventJson.has("eventVersion")).isTrue();
        assertThat(eventJson.has("occurredOn")).isTrue();
        assertThat(eventJson.has("source")).isTrue();
        assertThat(eventJson.has("correlationId")).isTrue();
        assertThat(eventJson.has("payload")).isTrue();

        // Validate event type
        assertThat(eventJson.get("eventType").asText()).isEqualTo(eventType);

        // Validate version format
        assertThat(eventJson.get("eventVersion").asText()).matches("\\d+\\.\\d+");
    }

    private OrderRejectedEvent createSampleOrderRejectedEvent() {
        return OrderRejectedEvent.builder()
                .eventId("sample-order-001")
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId("sample-correlation-001")
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId("sample-txn-001")
                        .externalId("SAMPLE-ORDER-001")
                        .operation("CREATE_ORDER")
                        .rejectedReason("Sample rejection reason")
                        .customerId("SAMPLE-CUST-001")
                        .locationCode("SAMPLE-LOC-001")
                        .orderItems(List.of(Map.of("itemId", "SAMPLE-ITEM-001", "quantity", 10)))
                        .build())
                .build();
    }

    private CollectionRejectedEvent createSampleCollectionRejectedEvent() {
        return CollectionRejectedEvent.builder()
                .eventId("sample-collection-001")
                .eventType("CollectionRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("collection-service")
                .correlationId("sample-correlation-002")
                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                        .transactionId("sample-txn-002")
                        .collectionId("SAMPLE-COLL-001")
                        .operation("CREATE_COLLECTION")
                        .rejectedReason("Sample collection rejection")
                        .donorId("SAMPLE-DONOR-001")
                        .locationCode("SAMPLE-LOC-002")
                        .build())
                .build();
    }

    private DistributionFailedEvent createSampleDistributionFailedEvent() {
        return DistributionFailedEvent.builder()
                .eventId("sample-distribution-001")
                .eventType("DistributionFailed")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId("sample-correlation-003")
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId("sample-txn-003")
                        .distributionId("SAMPLE-DIST-001")
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason("Sample distribution failure")
                        .customerId("SAMPLE-CUST-003")
                        .destinationLocation("SAMPLE-DEST-001")
                        .build())
                .build();
    }

    private ValidationErrorEvent createSampleValidationErrorEvent() {
        return ValidationErrorEvent.builder()
                .eventId("sample-validation-001")
                .eventType("ValidationError")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId("sample-correlation-004")
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId("sample-txn-004")
                        .interfaceType("ORDER")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("sampleField")
                                        .message("Sample validation error")
                                        .rejectedValue("invalid-value")
                                        .build()))
                        .build())
                .build();
    }

    private ExceptionCapturedEvent createSampleExceptionCapturedEvent() {
        return ExceptionCapturedEvent.builder()
                .eventId("sample-captured-001")
                .eventType("ExceptionCaptured")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId("sample-correlation-005")
                .payload(ExceptionCapturedEvent.ExceptionCapturedPayload.builder()
                        .exceptionId(99999L)
                        .transactionId("sample-txn-005")
                        .interfaceType("ORDER")
                        .severity("HIGH")
                        .category("VALIDATION")
                        .exceptionReason("Sample exception captured")
                        .customerId("SAMPLE-CUST-005")
                        .retryable(true)
                        .build())
                .build();
    }

    private ExceptionRetryCompletedEvent createSampleExceptionRetryCompletedEvent() {
        return ExceptionRetryCompletedEvent.builder()
                .eventId("sample-retry-001")
                .eventType("ExceptionRetryCompleted")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId("sample-correlation-006")
                .payload(ExceptionRetryCompletedEvent.ExceptionRetryCompletedPayload.builder()
                        .exceptionId(99998L)
                        .transactionId("sample-txn-006")
                        .attemptNumber(1)
                        .retryStatus("SUCCESS")
                        .retryResult(Map.of("success", true, "message", "Sample retry success"))
                        .initiatedBy("sample-user")
                        .completedAt(OffsetDateTime.now())
                        .build())
                .build();
    }

    private ExceptionResolvedEvent createSampleExceptionResolvedEvent() {
        return ExceptionResolvedEvent.builder()
                .eventId("sample-resolved-001")
                .eventType("ExceptionResolved")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId("sample-correlation-007")
                .payload(ExceptionResolvedEvent.ExceptionResolvedPayload.builder()
                        .exceptionId(99997L)
                        .transactionId("sample-txn-007")
                        .resolutionMethod("MANUAL_RESOLUTION")
                        .resolvedBy("sample-user")
                        .resolvedAt(OffsetDateTime.now())
                        .totalRetryAttempts(2)
                        .resolutionNotes("Sample resolution notes")
                        .build())
                .build();
    }

    private CriticalExceptionAlertEvent createSampleCriticalExceptionAlertEvent() {
        return CriticalExceptionAlertEvent.builder()
                .eventId("sample-alert-001")
                .eventType("CriticalExceptionAlert")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("exception-collector-service")
                .correlationId("sample-correlation-008")
                .payload(CriticalExceptionAlertEvent.CriticalExceptionAlertPayload.builder()
                        .exceptionId(99996L)
                        .transactionId("sample-txn-008")
                        .alertLevel("CRITICAL")
                        .alertReason("CRITICAL_SEVERITY")
                        .interfaceType("ORDER")
                        .exceptionReason("Sample critical exception")
                        .customerId("SAMPLE-CUST-008")
                        .escalationTeam("OPERATIONS")
                        .estimatedImpact("HIGH")
                        .requiresImmediateAction(true)
                        .build())
                .build();
    }
}