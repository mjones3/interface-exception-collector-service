package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
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
 * Integration tests for OrderExceptionConsumer using EmbeddedKafka.
 * Tests the complete flow from Kafka message consumption to database
 * persistence.
 * Implements requirements US-001 and US-018 for order exception processing and
 * error handling.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = { KafkaTopics.ORDER_REJECTED,
                KafkaTopics.ORDER_CANCELLED }, brokerProperties = {
                                "listeners=PLAINTEXT://localhost:9092",
                                "port=9092"
                })
@TestPropertySource(properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.group-id=test-interface-exception-collector",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class OrderExceptionConsumerIntegrationTest {

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
        void shouldProcessOrderRejectedEventSuccessfully() {
                // Given
                String transactionId = UUID.randomUUID().toString();
                OrderRejectedEvent event = OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .externalId("ORDER-12345")
                                                .operation("CREATE_ORDER")
                                                .rejectedReason("Order already exists")
                                                .customerId("CUST001")
                                                .locationCode("LOC001")
                                                .orderItems(List.of())
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.ORDER_REJECTED, transactionId, event);

                // Then
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> savedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        System.out.println("Looking for exception with transactionId: " + transactionId);
                        System.out.println("Found exception: " + savedException.isPresent());
                        if (savedException.isPresent()) {
                                System.out.println("Exception details: " + savedException.get());
                        }

                        // Check if any exceptions exist at all
                        long totalExceptions = exceptionRepository.count();
                        System.out.println("Total exceptions in database: " + totalExceptions);

                        assertThat(savedException).isPresent();

                        InterfaceException exception = savedException.get();
                        assertThat(exception.getTransactionId()).isEqualTo(transactionId);
                        assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
                        assertThat(exception.getExceptionReason()).isEqualTo("Order already exists");
                        assertThat(exception.getOperation()).isEqualTo("CREATE_ORDER");
                        assertThat(exception.getExternalId()).isEqualTo("ORDER-12345");
                        assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
                        assertThat(exception.getCustomerId()).isEqualTo("CUST001");
                        assertThat(exception.getLocationCode()).isEqualTo("LOC001");
                        assertThat(exception.getRetryCount()).isEqualTo(0);
                        assertThat(exception.getRetryable()).isTrue();
                });
        }

        @Test
        @Transactional
        void shouldProcessOrderCancelledEventSuccessfully() {
                // Given
                String transactionId = UUID.randomUUID().toString();
                OrderCancelledEvent event = OrderCancelledEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderCancelledEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(OrderCancelledEvent.OrderCancelledPayload.builder()
                                                .transactionId(transactionId)
                                                .externalId("ORDER-67890")
                                                .cancelReason("Customer requested cancellation")
                                                .cancelledBy("CUSTOMER")
                                                .customerId("CUST002")
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, transactionId, event);

                // Then
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> savedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        assertThat(savedException).isPresent();

                        InterfaceException exception = savedException.get();
                        assertThat(exception.getTransactionId()).isEqualTo(transactionId);
                        assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
                        assertThat(exception.getExceptionReason()).isEqualTo("Customer requested cancellation");
                        assertThat(exception.getOperation()).isEqualTo("CANCEL_ORDER");
                        assertThat(exception.getExternalId()).isEqualTo("ORDER-67890");
                        assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
                        assertThat(exception.getCustomerId()).isEqualTo("CUST002");
                        assertThat(exception.getRetryCount()).isEqualTo(0);
                });
        }

        @Test
        @Transactional
        void shouldUpdateExistingExceptionForDuplicateTransactionId() {
                // Given - Create initial exception
                String transactionId = UUID.randomUUID().toString();
                OrderRejectedEvent firstEvent = OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .externalId("ORDER-11111")
                                                .operation("CREATE_ORDER")
                                                .rejectedReason("Initial rejection reason")
                                                .customerId("CUST001")
                                                .locationCode("LOC001")
                                                .build())
                                .build();

                kafkaTemplate.send(KafkaTopics.ORDER_REJECTED, transactionId, firstEvent);

                // Wait for first event to be processed
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                        assertThat(exception).isPresent();
                        assertThat(exception.get().getExceptionReason()).isEqualTo("Initial rejection reason");
                });

                // When - Send duplicate event with different reason
                OrderRejectedEvent duplicateEvent = OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                                                .transactionId(transactionId)
                                                .externalId("ORDER-11111")
                                                .operation("CREATE_ORDER")
                                                .rejectedReason("Updated rejection reason")
                                                .customerId("CUST001")
                                                .locationCode("LOC001")
                                                .build())
                                .build();

                kafkaTemplate.send(KafkaTopics.ORDER_REJECTED, transactionId, duplicateEvent);

                // Then - Should update existing exception, not create new one
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                        List<InterfaceException> exceptions = exceptionRepository.findAll();
                        long matchingExceptions = exceptions.stream()
                                        .filter(ex -> ex.getTransactionId().equals(transactionId))
                                        .count();

                        assertThat(matchingExceptions).isEqualTo(1);

                        Optional<InterfaceException> updatedException = exceptionRepository
                                        .findByTransactionId(transactionId);
                        assertThat(updatedException).isPresent();
                        assertThat(updatedException.get().getExceptionReason()).isEqualTo("Updated rejection reason");
                });
        }

        @Test
        @Transactional
        void shouldHandleInvalidEventPayloadGracefully() {
                // Given - Event with null payload
                String transactionId = UUID.randomUUID().toString();
                OrderRejectedEvent invalidEvent = OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(null)
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.ORDER_REJECTED, transactionId, invalidEvent);

                // Then - Should not create exception in database
                await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
                        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                        assertThat(exception).isEmpty();
                });
        }

        @Test
        @Transactional
        void shouldHandleEventWithMissingTransactionIdGracefully() {
                // Given - Event with null transaction ID
                OrderRejectedEvent invalidEvent = OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType("OrderRejectedEvent")
                                .eventVersion("1.0")
                                .occurredOn(OffsetDateTime.now())
                                .source("order-service")
                                .correlationId(UUID.randomUUID().toString())
                                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                                                .transactionId(null)
                                                .externalId("ORDER-99999")
                                                .operation("CREATE_ORDER")
                                                .rejectedReason("Some reason")
                                                .customerId("CUST001")
                                                .build())
                                .build();

                // When
                kafkaTemplate.send(KafkaTopics.ORDER_REJECTED, "test-key", invalidEvent);

                // Then - Should not create exception in database
                await().during(5, TimeUnit.SECONDS).untilAsserted(() -> {
                        List<InterfaceException> exceptions = exceptionRepository.findAll();
                        assertThat(exceptions).isEmpty();
                });
        }
}