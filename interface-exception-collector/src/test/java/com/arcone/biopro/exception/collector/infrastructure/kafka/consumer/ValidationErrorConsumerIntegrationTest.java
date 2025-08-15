package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for ValidationErrorConsumer using EmbeddedKafka.
 * Tests the complete flow from Kafka message consumption to database
 * persistence.
 * Implements requirements US-004 and US-018 for validation error processing and
 * error handling.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = { KafkaTopics.VALIDATION_ERROR }, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9095",
        "port=9095"
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-validation-error-collector",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class ValidationErrorConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void shouldProcessValidationErrorEventSuccessfully() {
        // Given
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent event = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("ORDER")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("customerId")
                                        .rejectedValue("INVALID_ID")
                                        .message("Customer ID format is invalid")
                                        .errorCode("VALIDATION_001")
                                        .build(),
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("orderDate")
                                        .rejectedValue("2023-13-45")
                                        .message("Invalid date format")
                                        .errorCode("VALIDATION_002")
                                        .build()))
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> savedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(savedException).isPresent();

            InterfaceException exception = savedException.get();
            assertThat(exception.getTransactionId()).isEqualTo(transactionId);
            assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
            assertThat(exception.getExceptionReason()).contains("Field 'customerId': Customer ID format is invalid");
            assertThat(exception.getExceptionReason()).contains("Field 'orderDate': Invalid date format");
            assertThat(exception.getOperation()).isEqualTo("VALIDATION");
            assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(exception.getCategory()).isEqualTo(ExceptionCategory.VALIDATION);
            assertThat(exception.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
            assertThat(exception.getRetryCount()).isEqualTo(0);
            assertThat(exception.getRetryable()).isTrue();
        });
    }

    @Test
    @Transactional
    void shouldProcessCollectionValidationErrorEventSuccessfully() {
        // Given
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent event = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("collection-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("COLLECTION")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("donorId")
                                        .rejectedValue("")
                                        .message("Donor ID is required")
                                        .errorCode("VALIDATION_003")
                                        .build()))
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> savedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(savedException).isPresent();

            InterfaceException exception = savedException.get();
            assertThat(exception.getTransactionId()).isEqualTo(transactionId);
            assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
            assertThat(exception.getExceptionReason()).isEqualTo("Field 'donorId': Donor ID is required");
            assertThat(exception.getOperation()).isEqualTo("VALIDATION");
            assertThat(exception.getCategory()).isEqualTo(ExceptionCategory.VALIDATION);
            assertThat(exception.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
        });
    }

    @Test
    @Transactional
    void shouldProcessDistributionValidationErrorEventSuccessfully() {
        // Given
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent event = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("DISTRIBUTION")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("destinationLocation")
                                        .rejectedValue("INVALID_LOC")
                                        .message("Destination location does not exist")
                                        .errorCode("VALIDATION_004")
                                        .build()))
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> savedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(savedException).isPresent();

            InterfaceException exception = savedException.get();
            assertThat(exception.getTransactionId()).isEqualTo(transactionId);
            assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.DISTRIBUTION);
            assertThat(exception.getExceptionReason())
                    .isEqualTo("Field 'destinationLocation': Destination location does not exist");
            assertThat(exception.getOperation()).isEqualTo("VALIDATION");
            assertThat(exception.getCategory()).isEqualTo(ExceptionCategory.VALIDATION);
            assertThat(exception.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
        });
    }

    @Test
    @Transactional
    void shouldHandleInvalidValidationErrorEventPayloadGracefully() {
        // Given - Event with null payload
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent invalidEvent = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(null)
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, invalidEvent);

        // Then - Should not create exception in database
        await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
            assertThat(exception).isEmpty();
        });
    }

    @Test
    @Transactional
    void shouldHandleValidationErrorEventWithMissingTransactionIdGracefully() {
        // Given - Event with null transaction ID
        ValidationErrorEvent invalidEvent = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(null)
                        .interfaceType("ORDER")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("testField")
                                        .message("Test error")
                                        .build()))
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, "test-key", invalidEvent);

        // Then - Should not create exception in database
        await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId("null");
            assertThat(exception).isEmpty();
        });
    }

    @Test
    @Transactional
    void shouldHandleValidationErrorEventWithEmptyValidationErrorsGracefully() {
        // Given - Event with empty validation errors list
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent invalidEvent = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("ORDER")
                        .validationErrors(List.of())
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, invalidEvent);

        // Then - Should not create exception in database
        await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
            assertThat(exception).isEmpty();
        });
    }

    @Test
    @Transactional
    void shouldUpdateExistingValidationExceptionForDuplicateTransactionId() {
        // Given - Create initial exception
        String transactionId = UUID.randomUUID().toString();
        ValidationErrorEvent firstEvent = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("ORDER")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("field1")
                                        .message("Initial error message")
                                        .build()))
                        .build())
                .build();

        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, firstEvent);

        // Wait for first event to be processed
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
            assertThat(exception).isPresent();
            assertThat(exception.get().getExceptionReason()).contains("Initial error message");
        });

        // When - Send duplicate event with different validation errors
        ValidationErrorEvent duplicateEvent = ValidationErrorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ValidationErrorEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType("ORDER")
                        .validationErrors(List.of(
                                ValidationErrorEvent.ValidationError.builder()
                                        .field("field2")
                                        .message("Updated error message")
                                        .build()))
                        .build())
                .build();

        kafkaTemplate.send(KafkaTopics.VALIDATION_ERROR, transactionId, duplicateEvent);

        // Then - Should update existing exception, not create new one
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> updatedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(updatedException).isPresent();
            assertThat(updatedException.get().getExceptionReason()).contains("Updated error message");
        });
    }
}