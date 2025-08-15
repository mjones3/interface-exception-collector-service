package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
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
 * Integration tests for CollectionExceptionConsumer using EmbeddedKafka.
 * Tests the complete flow from Kafka message consumption to database
 * persistence.
 * Implements requirements US-002 and US-018 for collection exception processing
 * and error handling.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = { KafkaTopics.COLLECTION_REJECTED }, brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
})
@TestPropertySource(properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.group-id=test-collection-exception-collector",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class CollectionExceptionConsumerIntegrationTest {

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
        void shouldProcessCollectionRejectedEventSuccessfully() {
                // Given
                String transactionId = UUID.randomUUID().toString();
                CollectionRejectedEvent event = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .collectionId("COLL-12345")
                                                .operation("CREATE_COLLECTION")
                                                .rejectedReason("Invalid donor information")
                                                .donorId("DONOR001")
                                                .locationCode("LAB001")
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, transactionId, event);

                // Then
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> savedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        assertThat(savedException).isPresent();

                        InterfaceException exception = savedException.get();
                        assertThat(exception.getTransactionId()).isEqualTo(transactionId);
                        assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
                        assertThat(exception.getExceptionReason()).isEqualTo("Invalid donor information");
                        assertThat(exception.getOperation()).isEqualTo("CREATE_COLLECTION");
                        assertThat(exception.getExternalId()).isEqualTo("COLL-12345");
                        assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
                        assertThat(exception.getCustomerId()).isEqualTo("DONOR001"); // donorId mapped to customerId
                        assertThat(exception.getLocationCode()).isEqualTo("LAB001");
                        assertThat(exception.getRetryCount()).isEqualTo(0);
                        assertThat(exception.getRetryable()).isTrue();
                });
        }

        @Test
        @Transactional
        void shouldProcessModifyCollectionOperationCorrectly() {
                // Given
                String transactionId = UUID.randomUUID().toString();
                CollectionRejectedEvent event = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .collectionId("COLL-67890")
                                                .operation("MODIFY_COLLECTION")
                                                .rejectedReason("Sample already processed")
                                                .donorId("DONOR002")
                                                .locationCode("LAB002")
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, transactionId, event);

                // Then
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> savedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        assertThat(savedException).isPresent();

                        InterfaceException exception = savedException.get();
                        assertThat(exception.getTransactionId()).isEqualTo(transactionId);
                        assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
                        assertThat(exception.getOperation()).isEqualTo("MODIFY_COLLECTION");
                        assertThat(exception.getExceptionReason()).isEqualTo("Sample already processed");
                        assertThat(exception.getCustomerId()).isEqualTo("DONOR002");
                });
        }

        @Test
        @Transactional
        void shouldHandleInvalidCollectionEventPayloadGracefully() {
                // Given - Event with null payload
                String transactionId = UUID.randomUUID().toString();
                CollectionRejectedEvent invalidEvent = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(null)
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, transactionId, invalidEvent);

                // Then - Should not create exception in database
                await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                        assertThat(exception).isEmpty();
                });
        }

        @Test
        @Transactional
        void shouldHandleCollectionEventWithMissingTransactionIdGracefully() {
                // Given - Event with null transaction ID
                CollectionRejectedEvent invalidEvent = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                                                .transactionId(null)
                                                .collectionId("COLL-99999")
                                                .operation("CREATE_COLLECTION")
                                                .rejectedReason("Some reason")
                                                .donorId("DONOR001")
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, "test-key", invalidEvent);

                // Then - Should not create exception in database
                await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId("null");
                        assertThat(exception).isEmpty();
                });
        }

        @Test
        @Transactional
        void shouldUpdateExistingCollectionExceptionForDuplicateTransactionId() {
                // Given - Create initial exception
                String transactionId = UUID.randomUUID().toString();
                CollectionRejectedEvent firstEvent = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .collectionId("COLL-11111")
                                                .operation("CREATE_COLLECTION")
                                                .rejectedReason("Initial rejection reason")
                                                .donorId("DONOR001")
                                                .locationCode("LAB001")
                                                .build())
                                .build();

                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, transactionId, firstEvent);

                // Wait for first event to be processed
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                        assertThat(exception).isPresent();
                        assertThat(exception.get().getExceptionReason()).isEqualTo("Initial rejection reason");
                });

                // When - Send duplicate event with different reason
                CollectionRejectedEvent duplicateEvent = CollectionRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("CollectionRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("collection-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .collectionId("COLL-11111")
                                                .operation("CREATE_COLLECTION")
                                                .rejectedReason("Updated rejection reason")
                                                .donorId("DONOR001")
                                                .locationCode("LAB001")
                                                .build())
                                .build();

                kafkaTemplate.send(KafkaTopics.COLLECTION_REJECTED, transactionId, duplicateEvent);

                // Then - Should update existing exception, not create new one
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> updatedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        assertThat(updatedException).isPresent();
                        assertThat(updatedException.get().getExceptionReason()).isEqualTo("Updated rejection reason");
                });
        }
}