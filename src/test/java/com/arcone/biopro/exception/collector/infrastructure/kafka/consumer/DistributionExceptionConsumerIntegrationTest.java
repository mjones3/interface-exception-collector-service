package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for DistributionExceptionConsumer using EmbeddedKafka.
 * Tests the complete flow from Kafka message consumption to database
 * persistence.
 * Implements requirements US-003 and US-018 for distribution exception
 * processing and error handling.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = { KafkaTopics.DISTRIBUTION_FAILED }, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9094",
        "port=9094"
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-distribution-exception-collector",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class DistributionExceptionConsumerIntegrationTest {

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
    void shouldProcessDistributionFailedEventSuccessfully() {
        // Given
        String transactionId = UUID.randomUUID().toString();
        DistributionFailedEvent event = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(transactionId)
                        .distributionId("DIST-12345")
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason("Destination location unavailable")
                        .customerId("CUST001")
                        .destinationLocation("DEST001")
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, transactionId, event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> savedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(savedException).isPresent();

            InterfaceException exception = savedException.get();
            assertThat(exception.getTransactionId()).isEqualTo(transactionId);
            assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.DISTRIBUTION);
            assertThat(exception.getExceptionReason()).isEqualTo("Destination location unavailable");
            assertThat(exception.getOperation()).isEqualTo("CREATE_DISTRIBUTION");
            assertThat(exception.getExternalId()).isEqualTo("DIST-12345");
            assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(exception.getCustomerId()).isEqualTo("CUST001");
            assertThat(exception.getLocationCode()).isEqualTo("DEST001"); // destinationLocation mapped to locationCode
            assertThat(exception.getRetryCount()).isEqualTo(0);
            assertThat(exception.getRetryable()).isTrue();
        });
    }

    @Test
    @Transactional
    void shouldProcessModifyDistributionOperationCorrectly() {
        // Given
        String transactionId = UUID.randomUUID().toString();
        DistributionFailedEvent event = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(transactionId)
                        .distributionId("DIST-67890")
                        .operation("MODIFY_DISTRIBUTION")
                        .failureReason("Inventory insufficient")
                        .customerId("CUST002")
                        .destinationLocation("DEST002")
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, transactionId, event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> savedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(savedException).isPresent();

            InterfaceException exception = savedException.get();
            assertThat(exception.getTransactionId()).isEqualTo(transactionId);
            assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.DISTRIBUTION);
            assertThat(exception.getOperation()).isEqualTo("MODIFY_DISTRIBUTION");
            assertThat(exception.getExceptionReason()).isEqualTo("Inventory insufficient");
            assertThat(exception.getCustomerId()).isEqualTo("CUST002");
            assertThat(exception.getLocationCode()).isEqualTo("DEST002");
        });
    }

    @Test
    @Transactional
    void shouldHandleInvalidDistributionEventPayloadGracefully() {
        // Given - Event with null payload
        String transactionId = UUID.randomUUID().toString();
        DistributionFailedEvent invalidEvent = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(null)
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, transactionId, invalidEvent);

        // Then - Should not create exception in database
        await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
            assertThat(exception).isEmpty();
        });
    }

    @Test
    @Transactional
    void shouldHandleDistributionEventWithMissingTransactionIdGracefully() {
        // Given - Event with null transaction ID
        DistributionFailedEvent invalidEvent = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(null)
                        .distributionId("DIST-99999")
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason("Some reason")
                        .customerId("CUST001")
                        .build())
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, "test-key", invalidEvent);

        // Then - Should not create exception in database
        await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId("null");
            assertThat(exception).isEmpty();
        });
    }

    @Test
    @Transactional
    void shouldUpdateExistingDistributionExceptionForDuplicateTransactionId() {
        // Given - Create initial exception
        String transactionId = UUID.randomUUID().toString();
        DistributionFailedEvent firstEvent = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(transactionId)
                        .distributionId("DIST-11111")
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason("Initial failure reason")
                        .customerId("CUST001")
                        .destinationLocation("DEST001")
                        .build())
                .build();

        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, transactionId, firstEvent);

        // Wait for first event to be processed
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
            assertThat(exception).isPresent();
            assertThat(exception.get().getExceptionReason()).isEqualTo("Initial failure reason");
        });

        // When - Send duplicate event with different reason
        DistributionFailedEvent duplicateEvent = DistributionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DistributionFailedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("distribution-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(transactionId)
                        .distributionId("DIST-11111")
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason("Updated failure reason")
                        .customerId("CUST001")
                        .destinationLocation("DEST001")
                        .build())
                .build();

        kafkaTemplate.send(KafkaTopics.DISTRIBUTION_FAILED, transactionId, duplicateEvent);

        // Then - Should update existing exception, not create new one
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InterfaceException> updatedException = exceptionRepository.findByTransactionId(transactionId);
            assertThat(updatedException).isPresent();
            assertThat(updatedException.get().getExceptionReason()).isEqualTo("Updated failure reason");
        });
    }
}