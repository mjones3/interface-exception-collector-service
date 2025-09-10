package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.api.graphql.service.MutationEventPublisher;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for mutation completion subscriptions.
 * Tests the end-to-end flow of mutation events through GraphQL subscriptions.
 */
@SpringBootTest
class MutationSubscriptionIntegrationTest {

    @Autowired
    private ExceptionSubscriptionResolver subscriptionResolver;

    @Autowired
    private MutationEventPublisher mutationEventPublisher;

    @Autowired
    private GraphQLSecurityService securityService;

    private Authentication operationsAuth;
    private Authentication viewerAuth;

    @BeforeEach
    void setUp() {
        operationsAuth = new UsernamePasswordAuthenticationToken(
                "operations-user", "password",
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));

        viewerAuth = new UsernamePasswordAuthenticationToken(
                "viewer-user", "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
    }

    @Test
    void mutationCompletedSubscription_WithOperationsRole_ShouldReceiveEvents() throws InterruptedException {
        // Given
        CountDownLatch eventReceived = new CountDownLatch(1);
        InterfaceException testException = createTestException("TXN-SUB-001");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe to mutation completion events
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> subscription = subscriptionResolver
                .mutationCompleted(null, null, operationsAuth);

        subscription.subscribe(event -> {
            assertThat(event).isNotNull();
            assertThat(event.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RETRY);
            assertThat(event.getTransactionId()).isEqualTo("TXN-SUB-001");
            assertThat(event.isSuccess()).isTrue();
            assertThat(event.getPerformedBy()).isEqualTo("operations-user");
            eventReceived.countDown();
        });

        // Publish a test mutation completion event
        mutationEventPublisher.publishRetryMutationCompleted(testException, testRetryAttempt, true, "operations-user");

        // Then: Event should be received within 2 seconds (latency requirement)
        boolean received = eventReceived.await(2, TimeUnit.SECONDS);
        assertThat(received).isTrue();
    }

    @Test
    void mutationCompletedSubscription_WithViewerRole_ShouldNotReceiveEvents() throws InterruptedException {
        // Given
        CountDownLatch eventReceived = new CountDownLatch(1);
        InterfaceException testException = createTestException("TXN-SUB-002");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe to mutation completion events as viewer
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> subscription = subscriptionResolver
                .mutationCompleted(null, null, viewerAuth);

        subscription.subscribe(event -> {
            // This should not be called for viewer users
            eventReceived.countDown();
        });

        // Publish a test mutation completion event
        mutationEventPublisher.publishRetryMutationCompleted(testException, testRetryAttempt, true, "operations-user");

        // Then: Event should NOT be received (viewer users cannot see mutation events)
        boolean received = eventReceived.await(1, TimeUnit.SECONDS);
        assertThat(received).isFalse();
    }

    @Test
    void mutationCompletedSubscription_WithMutationTypeFilter_ShouldFilterCorrectly() throws InterruptedException {
        // Given
        CountDownLatch retryEventReceived = new CountDownLatch(1);
        CountDownLatch acknowledgeEventReceived = new CountDownLatch(1);
        InterfaceException testException = createTestException("TXN-SUB-003");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe to only RETRY mutation events
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> retrySubscription = subscriptionResolver
                .mutationCompleted("RETRY", null, operationsAuth);

        retrySubscription.subscribe(event -> {
            assertThat(event.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RETRY);
            retryEventReceived.countDown();
        });

        // Subscribe to only ACKNOWLEDGE mutation events
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> acknowledgeSubscription = subscriptionResolver
                .mutationCompleted("ACKNOWLEDGE", null, operationsAuth);

        acknowledgeSubscription.subscribe(event -> {
            assertThat(event.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.ACKNOWLEDGE);
            acknowledgeEventReceived.countDown();
        });

        // Publish a retry event
        mutationEventPublisher.publishRetryMutationCompleted(testException, testRetryAttempt, true, "operations-user");

        // Publish an acknowledge event
        mutationEventPublisher.publishAcknowledgeMutationCompleted(testException, "operations-user");

        // Then: Only the retry subscription should receive the retry event
        boolean retryReceived = retryEventReceived.await(2, TimeUnit.SECONDS);
        assertThat(retryReceived).isTrue();

        // And only the acknowledge subscription should receive the acknowledge event
        boolean acknowledgeReceived = acknowledgeEventReceived.await(2, TimeUnit.SECONDS);
        assertThat(acknowledgeReceived).isTrue();
    }

    @Test
    void mutationCompletedSubscription_WithTransactionIdFilter_ShouldFilterCorrectly() throws InterruptedException {
        // Given
        CountDownLatch specificEventReceived = new CountDownLatch(1);
        CountDownLatch otherEventReceived = new CountDownLatch(1);
        InterfaceException specificException = createTestException("TXN-SPECIFIC");
        InterfaceException otherException = createTestException("TXN-OTHER");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe to events for specific transaction ID
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> specificSubscription = subscriptionResolver
                .mutationCompleted(null, "TXN-SPECIFIC", operationsAuth);

        specificSubscription.subscribe(event -> {
            assertThat(event.getTransactionId()).isEqualTo("TXN-SPECIFIC");
            specificEventReceived.countDown();
        });

        // Subscribe to all events (no filter)
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> allSubscription = subscriptionResolver
                .mutationCompleted(null, null, operationsAuth);

        allSubscription.subscribe(event -> {
            if ("TXN-OTHER".equals(event.getTransactionId())) {
                otherEventReceived.countDown();
            }
        });

        // Publish events for both transactions
        mutationEventPublisher.publishRetryMutationCompleted(specificException, testRetryAttempt, true, "operations-user");
        mutationEventPublisher.publishRetryMutationCompleted(otherException, testRetryAttempt, true, "operations-user");

        // Then: Specific subscription should only receive its event
        boolean specificReceived = specificEventReceived.await(2, TimeUnit.SECONDS);
        assertThat(specificReceived).isTrue();

        // And all subscription should receive the other event
        boolean otherReceived = otherEventReceived.await(2, TimeUnit.SECONDS);
        assertThat(otherReceived).isTrue();
    }

    @Test
    void mutationCompletedSubscription_ShouldMeetLatencyRequirement() {
        // Given
        InterfaceException testException = createTestException("TXN-LATENCY");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe and measure latency
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> subscription = subscriptionResolver
                .mutationCompleted(null, null, operationsAuth);

        StepVerifier.create(subscription.take(1))
                .then(() -> {
                    // Publish event after subscription is active
                    mutationEventPublisher.publishRetryMutationCompleted(testException, testRetryAttempt, true, "operations-user");
                })
                .assertNext(event -> {
                    assertThat(event.getTransactionId()).isEqualTo("TXN-LATENCY");
                    assertThat(event.getMutationType()).isEqualTo(ExceptionSubscriptionResolver.MutationType.RETRY);
                })
                .expectComplete()
                .verify(Duration.ofSeconds(2)); // Should complete within 2 seconds (latency requirement)
    }

    @Test
    void mutationCompletedSubscription_WithMultipleEventTypes_ShouldReceiveAll() throws InterruptedException {
        // Given
        CountDownLatch eventsReceived = new CountDownLatch(4); // Expecting 4 different mutation types
        InterfaceException testException = createTestException("TXN-MULTI");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(1);

        // When: Subscribe to all mutation events
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> subscription = subscriptionResolver
                .mutationCompleted(null, null, operationsAuth);

        subscription.subscribe(event -> {
            assertThat(event.getTransactionId()).isEqualTo("TXN-MULTI");
            eventsReceived.countDown();
        });

        // Publish different types of mutation events
        mutationEventPublisher.publishRetryMutationCompleted(testException, testRetryAttempt, true, "operations-user");
        mutationEventPublisher.publishAcknowledgeMutationCompleted(testException, "operations-user");
        mutationEventPublisher.publishResolveMutationCompleted(testException, "operations-user");
        mutationEventPublisher.publishCancelRetryMutationCompleted(testException, testRetryAttempt, "operations-user", "Test cancel");

        // Then: All events should be received within 2 seconds
        boolean allReceived = eventsReceived.await(2, TimeUnit.SECONDS);
        assertThat(allReceived).isTrue();
    }

    @Test
    void mutationCompletedSubscription_WithBulkOperations_ShouldReceiveBulkEvents() throws InterruptedException {
        // Given
        CountDownLatch bulkEventReceived = new CountDownLatch(1);

        // When: Subscribe to mutation events
        Flux<ExceptionSubscriptionResolver.MutationCompletionEvent> subscription = subscriptionResolver
                .mutationCompleted(null, null, operationsAuth);

        subscription.subscribe(event -> {
            if ("BULK_OPERATION".equals(event.getTransactionId())) {
                assertThat(event.getMessage()).contains("Bulk RETRY completed");
                bulkEventReceived.countDown();
            }
        });

        // Publish bulk mutation event
        mutationEventPublisher.publishBulkMutationCompleted(
                MutationEventPublisher.MutationType.RETRY, 10, 8, 2, "operations-user");

        // Then: Bulk event should be received
        boolean received = bulkEventReceived.await(2, TimeUnit.SECONDS);
        assertThat(received).isTrue();
    }

    private InterfaceException createTestException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setId(1L);
        exception.setTransactionId(transactionId);
        exception.setInterfaceType(InterfaceType.ORDER_COLLECTION);
        exception.setExceptionReason("Test exception for subscription");
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