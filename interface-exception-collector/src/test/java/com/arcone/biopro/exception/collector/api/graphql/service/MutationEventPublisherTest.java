package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MutationEventPublisher.
 * Tests the publishing of mutation completion events to GraphQL subscriptions.
 */
@ExtendWith(MockitoExtension.class)
class MutationEventPublisherTest {

    @Mock
    private ExceptionSubscriptionResolver subscriptionResolver;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DashboardSubscriptionService dashboardSubscriptionService;

    private MutationEventPublisher mutationEventPublisher;

    @BeforeEach
    void setUp() {
        mutationEventPublisher = new MutationEventPublisher(
                subscriptionResolver,
                applicationEventPublisher,
                dashboardSubscriptionService
        );
    }

    @Test
    void publishRetryMutationCompleted_WithSuccessfulRetry_ShouldPublishEvents() {
        // Given
        InterfaceException exception = createTestException("TXN-001");
        RetryAttempt retryAttempt = createTestRetryAttempt(1);
        String initiatedBy = "test-user";

        // When
        mutationEventPublisher.publishRetryMutationCompleted(exception, retryAttempt, true, initiatedBy);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RETRY);
        assertThat(capturedEvent.getTransactionId()).isEqualTo("TXN-001");
        assertThat(capturedEvent.isSuccess()).isTrue();
        assertThat(capturedEvent.getPerformedBy()).isEqualTo("test-user");
        assertThat(capturedEvent.getMessage()).contains("completed successfully");

        // Verify exception update event
        ArgumentCaptor<ExceptionSubscriptionResolver.ExceptionUpdateEvent> exceptionCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.ExceptionUpdateEvent.class);
        verify(subscriptionResolver).publishExceptionUpdate(exceptionCaptor.capture());

        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = exceptionCaptor.getValue();
        assertThat(exceptionEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.ExceptionEventType.RETRY_COMPLETED);

        // Verify retry status event
        ArgumentCaptor<ExceptionSubscriptionResolver.RetryStatusEvent> retryCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.RetryStatusEvent.class);
        verify(subscriptionResolver).publishRetryStatusUpdate(retryCaptor.capture());

        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = retryCaptor.getValue();
        assertThat(retryEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.RetryEventType.COMPLETED);
        assertThat(retryEvent.getTransactionId()).isEqualTo("TXN-001");

        // Verify application event and dashboard update
        verify(applicationEventPublisher).publishEvent(any(SubscriptionEventBridge.RetryCompletedEvent.class));
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    @Test
    void publishRetryMutationCompleted_WithFailedRetry_ShouldPublishFailureEvents() {
        // Given
        InterfaceException exception = createTestException("TXN-002");
        RetryAttempt retryAttempt = createTestRetryAttempt(2);
        String initiatedBy = "test-user";

        // When
        mutationEventPublisher.publishRetryMutationCompleted(exception, retryAttempt, false, initiatedBy);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.isSuccess()).isFalse();
        assertThat(capturedEvent.getMessage()).contains("failed");

        // Verify retry status shows failed
        ArgumentCaptor<ExceptionSubscriptionResolver.RetryStatusEvent> retryCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.RetryStatusEvent.class);
        verify(subscriptionResolver).publishRetryStatusUpdate(retryCaptor.capture());

        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = retryCaptor.getValue();
        assertThat(retryEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.RetryEventType.FAILED);
    }

    @Test
    void publishAcknowledgeMutationCompleted_ShouldPublishAcknowledgeEvents() {
        // Given
        InterfaceException exception = createTestException("TXN-003");
        String acknowledgedBy = "ops-user";

        // When
        mutationEventPublisher.publishAcknowledgeMutationCompleted(exception, acknowledgedBy);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.ACKNOWLEDGE);
        assertThat(capturedEvent.getTransactionId()).isEqualTo("TXN-003");
        assertThat(capturedEvent.isSuccess()).isTrue();
        assertThat(capturedEvent.getPerformedBy()).isEqualTo("ops-user");
        assertThat(capturedEvent.getMessage()).contains("acknowledged successfully");

        // Verify exception update event
        ArgumentCaptor<ExceptionSubscriptionResolver.ExceptionUpdateEvent> exceptionCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.ExceptionUpdateEvent.class);
        verify(subscriptionResolver).publishExceptionUpdate(exceptionCaptor.capture());

        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = exceptionCaptor.getValue();
        assertThat(exceptionEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.ExceptionEventType.ACKNOWLEDGED);

        // Verify application event and dashboard update
        verify(applicationEventPublisher).publishEvent(any(SubscriptionEventBridge.ExceptionAcknowledgedEvent.class));
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    @Test
    void publishResolveMutationCompleted_ShouldPublishResolveEvents() {
        // Given
        InterfaceException exception = createTestException("TXN-004");
        String resolvedBy = "admin-user";

        // When
        mutationEventPublisher.publishResolveMutationCompleted(exception, resolvedBy);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RESOLVE);
        assertThat(capturedEvent.getTransactionId()).isEqualTo("TXN-004");
        assertThat(capturedEvent.isSuccess()).isTrue();
        assertThat(capturedEvent.getPerformedBy()).isEqualTo("admin-user");
        assertThat(capturedEvent.getMessage()).contains("resolved successfully");

        // Verify exception update event
        ArgumentCaptor<ExceptionSubscriptionResolver.ExceptionUpdateEvent> exceptionCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.ExceptionUpdateEvent.class);
        verify(subscriptionResolver).publishExceptionUpdate(exceptionCaptor.capture());

        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = exceptionCaptor.getValue();
        assertThat(exceptionEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.ExceptionEventType.RESOLVED);

        // Verify application event and dashboard update
        verify(applicationEventPublisher).publishEvent(any(SubscriptionEventBridge.ExceptionResolvedEvent.class));
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    @Test
    void publishCancelRetryMutationCompleted_ShouldPublishCancelEvents() {
        // Given
        InterfaceException exception = createTestException("TXN-005");
        RetryAttempt retryAttempt = createTestRetryAttempt(1);
        String cancelledBy = "ops-user";
        String reason = "No longer needed";

        // When
        mutationEventPublisher.publishCancelRetryMutationCompleted(exception, retryAttempt, cancelledBy, reason);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.CANCEL_RETRY);
        assertThat(capturedEvent.getTransactionId()).isEqualTo("TXN-005");
        assertThat(capturedEvent.isSuccess()).isTrue();
        assertThat(capturedEvent.getPerformedBy()).isEqualTo("ops-user");
        assertThat(capturedEvent.getMessage()).contains("cancelled: No longer needed");

        // Verify exception update event
        ArgumentCaptor<ExceptionSubscriptionResolver.ExceptionUpdateEvent> exceptionCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.ExceptionUpdateEvent.class);
        verify(subscriptionResolver).publishExceptionUpdate(exceptionCaptor.capture());

        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = exceptionCaptor.getValue();
        assertThat(exceptionEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.ExceptionEventType.CANCELLED);

        // Verify retry status event
        ArgumentCaptor<ExceptionSubscriptionResolver.RetryStatusEvent> retryCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.RetryStatusEvent.class);
        verify(subscriptionResolver).publishRetryStatusUpdate(retryCaptor.capture());

        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = retryCaptor.getValue();
        assertThat(retryEvent.getEventType()).isEqualTo(ExceptionSubscriptionResolver.RetryEventType.CANCELLED);

        // Verify application event and dashboard update
        verify(applicationEventPublisher).publishEvent(any(SubscriptionEventBridge.RetryCancelledEvent.class));
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    @Test
    void publishBulkMutationCompleted_ShouldPublishBulkEvents() {
        // Given
        MutationEventPublisher.MutationType mutationType = MutationEventPublisher.MutationType.RETRY;
        int totalCount = 10;
        int successCount = 8;
        int failureCount = 2;
        String initiatedBy = "bulk-user";

        // When
        mutationEventPublisher.publishBulkMutationCompleted(mutationType, totalCount, successCount, failureCount, initiatedBy);

        // Then
        ArgumentCaptor<ExceptionSubscriptionResolver.MutationCompletionEvent> mutationCaptor = 
                ArgumentCaptor.forClass(ExceptionSubscriptionResolver.MutationCompletionEvent.class);
        verify(subscriptionResolver).publishMutationCompletion(mutationCaptor.capture());

        ExceptionSubscriptionResolver.MutationCompletionEvent capturedEvent = mutationCaptor.getValue();
        assertThat(capturedEvent.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RETRY);
        assertThat(capturedEvent.getTransactionId()).isEqualTo("BULK_OPERATION");
        assertThat(capturedEvent.isSuccess()).isTrue(); // Success because successCount > 0
        assertThat(capturedEvent.getPerformedBy()).isEqualTo("bulk-user");
        assertThat(capturedEvent.getMessage()).contains("Bulk RETRY completed: 10 total, 8 success, 2 failures");

        // Verify dashboard update
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    @Test
    void publishRetryMutationCompleted_WithException_ShouldNotThrow() {
        // Given
        InterfaceException exception = createTestException("TXN-006");
        RetryAttempt retryAttempt = createTestRetryAttempt(1);
        String initiatedBy = "test-user";

        // Mock subscription resolver to throw exception
        doThrow(new RuntimeException("Subscription failed")).when(subscriptionResolver)
                .publishMutationCompletion(any());

        // When/Then - should not throw exception
        mutationEventPublisher.publishRetryMutationCompleted(exception, retryAttempt, true, initiatedBy);

        // Verify that other methods were still called despite the exception
        verify(subscriptionResolver).publishExceptionUpdate(any());
        verify(subscriptionResolver).publishRetryStatusUpdate(any());
    }

    @Test
    void publishEvents_ShouldEnsureLatencyRequirement() {
        // Given
        InterfaceException exception = createTestException("TXN-007");
        RetryAttempt retryAttempt = createTestRetryAttempt(1);
        String initiatedBy = "test-user";

        // When
        long startTime = System.currentTimeMillis();
        mutationEventPublisher.publishRetryMutationCompleted(exception, retryAttempt, true, initiatedBy);
        long endTime = System.currentTimeMillis();

        // Then - should complete within 2 seconds (latency requirement)
        long executionTime = endTime - startTime;
        assertThat(executionTime).isLessThan(2000); // 2 seconds

        // Verify all events were published
        verify(subscriptionResolver).publishMutationCompletion(any());
        verify(subscriptionResolver).publishExceptionUpdate(any());
        verify(subscriptionResolver).publishRetryStatusUpdate(any());
        verify(applicationEventPublisher).publishEvent(any());
        verify(dashboardSubscriptionService).triggerUpdate();
    }

    private InterfaceException createTestException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setId(1L);
        exception.setTransactionId(transactionId);
        exception.setInterfaceType(InterfaceType.ORDER_COLLECTION);
        exception.setExceptionReason("Test exception");
        exception.setOperation("TEST_OPERATION");
        exception.setStatus(ExceptionStatus.NEW);
        exception.setSeverity(ExceptionSeverity.MEDIUM);
        exception.setTimestamp(OffsetDateTime.now());
        exception.setRetryable(true);
        exception.setRetryCount(0);
        exception.setMaxRetries(3);
        return exception;
    }

    private RetryAttempt createTestRetryAttempt(int attemptNumber) {
        RetryAttempt retryAttempt = new RetryAttempt();
        retryAttempt.setId(1L);
        retryAttempt.setAttemptNumber(attemptNumber);
        retryAttempt.setInitiatedBy("test-user");
        retryAttempt.setInitiatedAt(OffsetDateTime.now());
        return retryAttempt;
    }
}