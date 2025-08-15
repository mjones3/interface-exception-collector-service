package com.arcone.biopro.exception.collector.infrastructure.kafka.publisher;

import com.arcone.biopro.exception.collector.domain.event.constants.EventTypes;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionResolvedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ExceptionEventPublisher exceptionEventPublisher;

    @BeforeEach
    void setUp() {
        exceptionEventPublisher = new ExceptionEventPublisher(kafkaTemplate);
    }

    @Test
    void publishExceptionCaptured_ShouldPublishEventWithCorrectPayload() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String interfaceType = "ORDER";
        String severity = "MEDIUM";
        String category = "BUSINESS_RULE";
        String exceptionReason = "Order already exists";
        String customerId = "CUST001";
        Boolean retryable = true;
        String correlationId = "corr-123";
        String causationId = "cause-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.EXCEPTION_CAPTURED, 0), 0, 0, 0, 0,
                0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.EXCEPTION_CAPTURED, transactionId,
                null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.EXCEPTION_CAPTURED), eq(transactionId),
                any(ExceptionCapturedEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = exceptionEventPublisher.publishExceptionCaptured(
                exceptionId, transactionId, interfaceType, severity, category, exceptionReason,
                customerId, retryable, correlationId, causationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<ExceptionCapturedEvent> eventCaptor = ArgumentCaptor.forClass(ExceptionCapturedEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.EXCEPTION_CAPTURED), eq(transactionId), eventCaptor.capture());

        ExceptionCapturedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventTypes.EXCEPTION_CAPTURED);
        assertThat(capturedEvent.getEventVersion()).isEqualTo("1.0");
        assertThat(capturedEvent.getSource()).isEqualTo("exception-collector-service");
        assertThat(capturedEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(capturedEvent.getCausationId()).isEqualTo(causationId);
        assertThat(capturedEvent.getEventId()).isNotNull();
        assertThat(capturedEvent.getOccurredOn()).isNotNull();

        ExceptionCapturedEvent.ExceptionCapturedPayload payload = capturedEvent.getPayload();
        assertThat(payload.getExceptionId()).isEqualTo(exceptionId);
        assertThat(payload.getTransactionId()).isEqualTo(transactionId);
        assertThat(payload.getInterfaceType()).isEqualTo(interfaceType);
        assertThat(payload.getSeverity()).isEqualTo(severity);
        assertThat(payload.getCategory()).isEqualTo(category);
        assertThat(payload.getExceptionReason()).isEqualTo(exceptionReason);
        assertThat(payload.getCustomerId()).isEqualTo(customerId);
        assertThat(payload.getRetryable()).isEqualTo(retryable);
    }

    @Test
    void publishExceptionResolved_ShouldPublishEventWithCorrectPayload() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String resolutionMethod = "RETRY_SUCCESS";
        String resolvedBy = "system";
        OffsetDateTime resolvedAt = OffsetDateTime.now();
        Integer totalRetryAttempts = 2;
        String resolutionNotes = "Resolved after retry";
        String correlationId = "corr-123";
        String causationId = "cause-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.EXCEPTION_RESOLVED, 0), 0, 0, 0, 0,
                0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.EXCEPTION_RESOLVED, transactionId,
                null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.EXCEPTION_RESOLVED), eq(transactionId),
                any(ExceptionResolvedEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = exceptionEventPublisher.publishExceptionResolved(
                exceptionId, transactionId, resolutionMethod, resolvedBy, resolvedAt,
                totalRetryAttempts, resolutionNotes, correlationId, causationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<ExceptionResolvedEvent> eventCaptor = ArgumentCaptor.forClass(ExceptionResolvedEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.EXCEPTION_RESOLVED), eq(transactionId), eventCaptor.capture());

        ExceptionResolvedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventTypes.EXCEPTION_RESOLVED);
        assertThat(capturedEvent.getEventVersion()).isEqualTo("1.0");
        assertThat(capturedEvent.getSource()).isEqualTo("exception-collector-service");
        assertThat(capturedEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(capturedEvent.getCausationId()).isEqualTo(causationId);
        assertThat(capturedEvent.getEventId()).isNotNull();
        assertThat(capturedEvent.getOccurredOn()).isNotNull();

        ExceptionResolvedEvent.ExceptionResolvedPayload payload = capturedEvent.getPayload();
        assertThat(payload.getExceptionId()).isEqualTo(exceptionId);
        assertThat(payload.getTransactionId()).isEqualTo(transactionId);
        assertThat(payload.getResolutionMethod()).isEqualTo(resolutionMethod);
        assertThat(payload.getResolvedBy()).isEqualTo(resolvedBy);
        assertThat(payload.getResolvedAt()).isEqualTo(resolvedAt);
        assertThat(payload.getTotalRetryAttempts()).isEqualTo(totalRetryAttempts);
        assertThat(payload.getResolutionNotes()).isEqualTo(resolutionNotes);
    }

    @Test
    void publishExceptionCaptured_WithNullCustomerId_ShouldHandleGracefully() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String interfaceType = "ORDER";
        String severity = "MEDIUM";
        String category = "BUSINESS_RULE";
        String exceptionReason = "Order already exists";
        String customerId = null; // null customer ID
        Boolean retryable = true;
        String correlationId = "corr-123";
        String causationId = "cause-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.EXCEPTION_CAPTURED, 0), 0, 0, 0, 0,
                0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.EXCEPTION_CAPTURED, transactionId,
                null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.EXCEPTION_CAPTURED), eq(transactionId),
                any(ExceptionCapturedEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = exceptionEventPublisher.publishExceptionCaptured(
                exceptionId, transactionId, interfaceType, severity, category, exceptionReason,
                customerId, retryable, correlationId, causationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<ExceptionCapturedEvent> eventCaptor = ArgumentCaptor.forClass(ExceptionCapturedEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.EXCEPTION_CAPTURED), eq(transactionId), eventCaptor.capture());

        ExceptionCapturedEvent capturedEvent = eventCaptor.getValue();
        ExceptionCapturedEvent.ExceptionCapturedPayload payload = capturedEvent.getPayload();
        assertThat(payload.getCustomerId()).isNull();
    }
}