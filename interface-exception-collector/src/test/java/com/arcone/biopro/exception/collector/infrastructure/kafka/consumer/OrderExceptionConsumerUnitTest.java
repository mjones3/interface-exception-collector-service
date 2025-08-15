package com.arcone.biopro.exception.collector.infrastructure.kafka.consumer;

import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderExceptionConsumer.
 * Tests the consumer logic in isolation using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class OrderExceptionConsumerUnitTest {

    @Mock
    private ExceptionProcessingService exceptionProcessingService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private OrderExceptionConsumer orderExceptionConsumer;

    private OrderRejectedEvent validOrderRejectedEvent;
    private InterfaceException mockException;

    @BeforeEach
    void setUp() {
        String transactionId = UUID.randomUUID().toString();

        validOrderRejectedEvent = OrderRejectedEvent.builder()
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

        mockException = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Order already exists")
                .operation("CREATE_ORDER")
                .externalId("ORDER-12345")
                .status(ExceptionStatus.NEW)
                .customerId("CUST001")
                .locationCode("LOC001")
                .retryCount(0)
                .retryable(true)
                .build();
    }

    @Test
    void shouldProcessOrderRejectedEventSuccessfully() {
        // Given
        when(exceptionProcessingService.processOrderRejectedEvent(any(OrderRejectedEvent.class)))
                .thenReturn(mockException);

        // When
        orderExceptionConsumer.handleOrderRejectedEvent(validOrderRejectedEvent, 0, 0L, acknowledgment);

        // Then
        verify(exceptionProcessingService).processOrderRejectedEvent(validOrderRejectedEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleNullPayloadGracefully() {
        // Given
        OrderRejectedEvent eventWithNullPayload = OrderRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderRejectedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(null)
                .build();

        // When
        orderExceptionConsumer.handleOrderRejectedEvent(eventWithNullPayload, 0, 0L, acknowledgment);

        // Then
        verify(exceptionProcessingService, never()).processOrderRejectedEvent(any());
        verify(acknowledgment).acknowledge(); // Should still acknowledge to avoid reprocessing
    }

    @Test
    void shouldHandleNullTransactionIdGracefully() {
        // Given
        OrderRejectedEvent eventWithNullTransactionId = OrderRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderRejectedEvent")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(null)
                        .externalId("ORDER-12345")
                        .operation("CREATE_ORDER")
                        .rejectedReason("Order already exists")
                        .customerId("CUST001")
                        .build())
                .build();

        // When
        orderExceptionConsumer.handleOrderRejectedEvent(eventWithNullTransactionId, 0, 0L, acknowledgment);

        // Then
        verify(exceptionProcessingService, never()).processOrderRejectedEvent(any());
        verify(acknowledgment).acknowledge(); // Should still acknowledge to avoid reprocessing
    }

    @Test
    void shouldNotAcknowledgeWhenProcessingFails() {
        // Given
        when(exceptionProcessingService.processOrderRejectedEvent(any(OrderRejectedEvent.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            orderExceptionConsumer.handleOrderRejectedEvent(validOrderRejectedEvent, 0, 0L, acknowledgment);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(exceptionProcessingService).processOrderRejectedEvent(validOrderRejectedEvent);
        verify(acknowledgment, never()).acknowledge(); // Should not acknowledge on failure
    }
}