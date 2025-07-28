package com.arcone.biopro.distribution.customer.infrastructure.messaging;

import com.arcone.biopro.distribution.customer.application.event.CustomerProcessedEvent;
import com.arcone.biopro.distribution.customer.application.event.EventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CustomerEventPublisher customerEventPublisher;

    @Captor
    private ArgumentCaptor<EventMessage<CustomerProcessedEvent>> eventCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    private final String TOPIC_NAME = "test-customer-processed";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customerEventPublisher, "customerProcessedTopic", TOPIC_NAME);

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
    }

    @Test
    void publishCustomerCompleted_SendsCorrectEvent() {
        // Given
        String batchId = "BATCH001";
        String customerId = "EXT123";

        // When
        customerEventPublisher.publishCustomerCompleted(batchId, customerId);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(TOPIC_NAME, topicCaptor.getValue());
        assertEquals(customerId, keyCaptor.getValue());

        EventMessage<CustomerProcessedEvent> eventMessage = eventCaptor.getValue();
        assertNotNull(eventMessage.eventId());
        assertNotNull(eventMessage.occurredOn());
        assertEquals("CustomerProcessed", eventMessage.eventType());
        assertEquals("1.0", eventMessage.eventVersion());

        CustomerProcessedEvent event = eventMessage.payload();
        assertEquals(batchId, event.getBatchId());
        assertEquals(customerId, event.getStatus().getCustomerId());
        assertEquals("COMPLETED", event.getStatus().getStatus());
        assertNotNull(event.getStatus().getProcessedAt());
    }

    @Test
    void publishCustomerFailed_SendsCorrectEvent() {
        // Given
        String batchId = "BATCH001";
        String customerId = "EXT123";
        String errorMessage = "Test error message";

        // When
        customerEventPublisher.publishCustomerFailed(batchId, customerId, errorMessage);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(TOPIC_NAME, topicCaptor.getValue());
        assertEquals(customerId, keyCaptor.getValue());

        EventMessage<CustomerProcessedEvent> eventMessage = eventCaptor.getValue();
        assertNotNull(eventMessage.eventId());
        assertNotNull(eventMessage.occurredOn());
        assertEquals("CustomerProcessed", eventMessage.eventType());
        assertEquals("1.0", eventMessage.eventVersion());

        CustomerProcessedEvent event = eventMessage.payload();
        assertEquals(batchId, event.getBatchId());
        assertEquals(customerId, event.getStatus().getCustomerId());
        assertEquals("FAILED", event.getStatus().getStatus());
        assertEquals(errorMessage, event.getStatus().getError());
        assertNotNull(event.getStatus().getProcessedAt());
    }

    // Helper method to fix the compilation error with 'any()' method
    private static <T> T any() {
        return (T) org.mockito.ArgumentMatchers.any();
    }
}
