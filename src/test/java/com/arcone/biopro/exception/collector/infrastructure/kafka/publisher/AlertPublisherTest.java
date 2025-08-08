package com.arcone.biopro.exception.collector.infrastructure.kafka.publisher;

import com.arcone.biopro.exception.collector.domain.event.constants.EventTypes;
import com.arcone.biopro.exception.collector.domain.event.constants.KafkaTopics;
import com.arcone.biopro.exception.collector.domain.event.outbound.CriticalExceptionAlertEvent;
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

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private AlertPublisher alertPublisher;

    @BeforeEach
    void setUp() {
        alertPublisher = new AlertPublisher(kafkaTemplate);
    }

    @Test
    void publishCriticalAlert_ShouldPublishEventWithCorrectPayload() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String alertLevel = "CRITICAL";
        String alertReason = "CRITICAL_SEVERITY";
        String interfaceType = "ORDER";
        String exceptionReason = "Critical system error";
        String customerId = "CUST001";
        String escalationTeam = "OPERATIONS";
        Boolean requiresImmediateAction = true;
        String estimatedImpact = "HIGH";
        Integer customersAffected = 5;
        String correlationId = "corr-123";
        String causationId = "cause-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.CRITICAL_EXCEPTION_ALERT, 0), 0, 0,
                0, 0, 0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.CRITICAL_EXCEPTION_ALERT,
                transactionId, null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId),
                any(CriticalExceptionAlertEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = alertPublisher.publishCriticalAlert(
                exceptionId, transactionId, alertLevel, alertReason, interfaceType, exceptionReason,
                customerId, escalationTeam, requiresImmediateAction, estimatedImpact,
                customersAffected, correlationId, causationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<CriticalExceptionAlertEvent> eventCaptor = ArgumentCaptor
                .forClass(CriticalExceptionAlertEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId), eventCaptor.capture());

        CriticalExceptionAlertEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventTypes.CRITICAL_EXCEPTION_ALERT);
        assertThat(capturedEvent.getEventVersion()).isEqualTo("1.0");
        assertThat(capturedEvent.getSource()).isEqualTo("exception-collector-service");
        assertThat(capturedEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(capturedEvent.getCausationId()).isEqualTo(causationId);
        assertThat(capturedEvent.getEventId()).isNotNull();
        assertThat(capturedEvent.getOccurredOn()).isNotNull();

        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = capturedEvent.getPayload();
        assertThat(payload.getExceptionId()).isEqualTo(exceptionId);
        assertThat(payload.getTransactionId()).isEqualTo(transactionId);
        assertThat(payload.getAlertLevel()).isEqualTo(alertLevel);
        assertThat(payload.getAlertReason()).isEqualTo(alertReason);
        assertThat(payload.getInterfaceType()).isEqualTo(interfaceType);
        assertThat(payload.getExceptionReason()).isEqualTo(exceptionReason);
        assertThat(payload.getCustomerId()).isEqualTo(customerId);
        assertThat(payload.getEscalationTeam()).isEqualTo(escalationTeam);
        assertThat(payload.getRequiresImmediateAction()).isEqualTo(requiresImmediateAction);
        assertThat(payload.getEstimatedImpact()).isEqualTo(estimatedImpact);
        assertThat(payload.getCustomersAffected()).isEqualTo(customersAffected);
    }

    @Test
    void publishCriticalAlert_ConvenienceMethod_ShouldSetDefaultsForCriticalLevel() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String alertLevel = "CRITICAL";
        String alertReason = "CRITICAL_SEVERITY";
        String interfaceType = "ORDER";
        String exceptionReason = "Critical system error";
        String customerId = "CUST001";
        String estimatedImpact = "HIGH";
        String correlationId = "corr-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.CRITICAL_EXCEPTION_ALERT, 0), 0, 0,
                0, 0, 0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.CRITICAL_EXCEPTION_ALERT,
                transactionId, null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId),
                any(CriticalExceptionAlertEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = alertPublisher.publishCriticalAlert(
                exceptionId, transactionId, alertLevel, alertReason, interfaceType,
                exceptionReason, customerId, estimatedImpact, correlationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<CriticalExceptionAlertEvent> eventCaptor = ArgumentCaptor
                .forClass(CriticalExceptionAlertEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId), eventCaptor.capture());

        CriticalExceptionAlertEvent capturedEvent = eventCaptor.getValue();
        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = capturedEvent.getPayload();

        // Should set default escalation team for CRITICAL level
        assertThat(payload.getEscalationTeam()).isEqualTo("OPERATIONS");
        // Should set requiresImmediateAction to true for CRITICAL level
        assertThat(payload.getRequiresImmediateAction()).isTrue();
        // Should set customersAffected to null (to be calculated by calling service)
        assertThat(payload.getCustomersAffected()).isNull();
    }

    @Test
    void publishCriticalAlert_ConvenienceMethod_ShouldSetDefaultsForEmergencyLevel() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String alertLevel = "EMERGENCY";
        String alertReason = "SYSTEM_ERROR";
        String interfaceType = "ORDER";
        String exceptionReason = "System down";
        String customerId = "CUST001";
        String estimatedImpact = "SEVERE";
        String correlationId = "corr-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.CRITICAL_EXCEPTION_ALERT, 0), 0, 0,
                0, 0, 0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.CRITICAL_EXCEPTION_ALERT,
                transactionId, null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId),
                any(CriticalExceptionAlertEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = alertPublisher.publishCriticalAlert(
                exceptionId, transactionId, alertLevel, alertReason, interfaceType,
                exceptionReason, customerId, estimatedImpact, correlationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<CriticalExceptionAlertEvent> eventCaptor = ArgumentCaptor
                .forClass(CriticalExceptionAlertEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId), eventCaptor.capture());

        CriticalExceptionAlertEvent capturedEvent = eventCaptor.getValue();
        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = capturedEvent.getPayload();

        // Should set escalation team to MANAGEMENT for EMERGENCY level
        assertThat(payload.getEscalationTeam()).isEqualTo("MANAGEMENT");
        // Should set requiresImmediateAction to true for EMERGENCY level
        assertThat(payload.getRequiresImmediateAction()).isTrue();
    }

    @Test
    void publishCriticalAlert_WithNullCustomerId_ShouldHandleGracefully() {
        // Given
        Long exceptionId = 12345L;
        String transactionId = "txn-123";
        String alertLevel = "CRITICAL";
        String alertReason = "CRITICAL_SEVERITY";
        String interfaceType = "ORDER";
        String exceptionReason = "Critical system error";
        String customerId = null; // null customer ID
        String estimatedImpact = "HIGH";
        String correlationId = "corr-123";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.CRITICAL_EXCEPTION_ALERT, 0), 0, 0,
                0, 0, 0);
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.CRITICAL_EXCEPTION_ALERT,
                transactionId, null);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId),
                any(CriticalExceptionAlertEvent.class)))
                .thenReturn(future);

        // When
        CompletableFuture<SendResult<String, Object>> result = alertPublisher.publishCriticalAlert(
                exceptionId, transactionId, alertLevel, alertReason, interfaceType,
                exceptionReason, customerId, estimatedImpact, correlationId);

        // Then
        assertThat(result).isCompleted();

        ArgumentCaptor<CriticalExceptionAlertEvent> eventCaptor = ArgumentCaptor
                .forClass(CriticalExceptionAlertEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.CRITICAL_EXCEPTION_ALERT), eq(transactionId), eventCaptor.capture());

        CriticalExceptionAlertEvent capturedEvent = eventCaptor.getValue();
        CriticalExceptionAlertEvent.CriticalExceptionAlertPayload payload = capturedEvent.getPayload();
        assertThat(payload.getCustomerId()).isNull();
    }
}