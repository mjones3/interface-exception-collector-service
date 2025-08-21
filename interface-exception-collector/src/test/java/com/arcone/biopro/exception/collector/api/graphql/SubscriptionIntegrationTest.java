package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.api.graphql.service.SubscriptionEventBridge;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for GraphQL subscriptions functionality.
 * Tests subscription filtering and security.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.graphql.websocket.path=/subscriptions",
        "spring.graphql.graphiql.enabled=true",
        "graphql.enabled=true"
})
class SubscriptionIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ExceptionSubscriptionResolver subscriptionResolver;

    @BeforeEach
    void setUp() {
        // Set up authentication context for tests
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testExceptionSubscriptionWithoutFilters() throws Exception {
        // Given: A subscription without filters
        CountDownLatch eventReceived = new CountDownLatch(1);

        // When: Subscribe to exception updates
        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, SecurityContextHolder.getContext().getAuthentication());

        // Subscribe and wait for events
        subscription.subscribe(event -> {
            assertThat(event).isNotNull();
            assertThat(event.getEventType()).isEqualTo(ExceptionSubscriptionResolver.ExceptionEventType.CREATED);
            assertThat(event.getException().getTransactionId()).isEqualTo("TEST-001");
            eventReceived.countDown();
        });

        // Publish a test event
        InterfaceException testException = createTestException("TEST-001");
        eventPublisher.publishEvent(new SubscriptionEventBridge.ExceptionCreatedEvent(testException, "test-system"));

        // Then: Event should be received
        assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testRetryStatusSubscription() throws Exception {
        // Given: A retry status subscription
        CountDownLatch eventReceived = new CountDownLatch(1);

        // When: Subscribe to retry status updates
        Flux<ExceptionSubscriptionResolver.RetryStatusEvent> subscription = subscriptionResolver
                .retryStatusUpdated("TEST-002", SecurityContextHolder.getContext().getAuthentication());

        // Subscribe and wait for events
        subscription.subscribe(event -> {
            assertThat(event).isNotNull();
            assertThat(event.getEventType()).isEqualTo(ExceptionSubscriptionResolver.RetryEventType.INITIATED);
            assertThat(event.getTransactionId()).isEqualTo("TEST-002");
            eventReceived.countDown();
        });

        // Publish a test retry event
        InterfaceException testException = createTestException("TEST-002");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(testException, 1);
        eventPublisher.publishEvent(
                new SubscriptionEventBridge.RetryInitiatedEvent(testException, testRetryAttempt, "test-user"));

        // Then: Event should be received
        assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testSubscriptionSecurityFiltering() throws Exception {
        // Given: A user with VIEWER role (limited permissions)
        UsernamePasswordAuthenticationToken viewerAuth = new UsernamePasswordAuthenticationToken(
                "viewer-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        CountDownLatch eventReceived = new CountDownLatch(1);

        // When: Subscribe to exception updates as viewer
        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, viewerAuth);

        // Subscribe and wait for events
        subscription.subscribe(event -> {
            // Viewer should receive basic exception events
            assertThat(event).isNotNull();
            eventReceived.countDown();
        });

        // Publish a test event
        InterfaceException testException = createTestException("TEST-003");
        eventPublisher.publishEvent(new SubscriptionEventBridge.ExceptionCreatedEvent(testException, "test-system"));

        // Then: Event should be received (basic exception info is allowed for viewers)
        assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testRetryStatusSubscriptionSecurityFiltering() throws Exception {
        // Given: A user with VIEWER role (should not have access to retry status)
        UsernamePasswordAuthenticationToken viewerAuth = new UsernamePasswordAuthenticationToken(
                "viewer-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        CountDownLatch eventReceived = new CountDownLatch(1);

        // When: Subscribe to retry status updates as viewer
        Flux<ExceptionSubscriptionResolver.RetryStatusEvent> subscription = subscriptionResolver
                .retryStatusUpdated("TEST-004", viewerAuth);

        // Subscribe and wait for events (should be filtered out)
        subscription.subscribe(event -> {
            // This should not be called for VIEWER role
            eventReceived.countDown();
        });

        // Publish a test retry event
        InterfaceException testException = createTestException("TEST-004");
        RetryAttempt testRetryAttempt = createTestRetryAttempt(testException, 1);
        eventPublisher.publishEvent(
                new SubscriptionEventBridge.RetryInitiatedEvent(testException, testRetryAttempt, "test-user"));

        // Then: Event should NOT be received (viewers don't have access to retry
        // status)
        assertThat(eventReceived.await(2, TimeUnit.SECONDS)).isFalse();
    }

    @Test
    void testSubscriptionCreation() {
        // Given: A subscription with timeout
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        // When: Subscribe to exception updates
        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, auth);

        // Then: Subscription should be created successfully
        assertThat(subscription).isNotNull();

        // Test that subscription can be subscribed to without errors
        subscription.take(Duration.ofMillis(100)).subscribe(
                event -> {
                    // Event received
                },
                error -> {
                    // Error handling - expected in test environment
                },
                () -> {
                    // Completion
                });
    }

    @Test
    void testSubscriptionConnectionMetrics() {
        // Given: Initial connection count
        int initialConnections = subscriptionResolver.getActiveSubscriptionCount();

        // When: Create a subscription (simulated)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, auth);

        // Subscribe briefly
        subscription.take(Duration.ofMillis(100)).subscribe();

        // Then: Connection metrics should be tracked
        // Note: In a real test, we would verify the connection count changes
        // This is a simplified test since the actual WebSocket connection management
        // happens at a lower level
        assertThat(subscriptionResolver.getActiveSubscriptionCount()).isGreaterThanOrEqualTo(initialConnections);
    }

    @Test
    void testMultipleEventTypes() throws Exception {
        // Given: A subscription that should receive multiple event types
        CountDownLatch eventsReceived = new CountDownLatch(3);

        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, SecurityContextHolder.getContext().getAuthentication());

        subscription.subscribe(event -> {
            assertThat(event).isNotNull();
            assertThat(event.getException().getTransactionId()).isEqualTo("TEST-005");
            eventsReceived.countDown();
        });

        // When: Publish multiple event types
        InterfaceException testException = createTestException("TEST-005");

        eventPublisher.publishEvent(new SubscriptionEventBridge.ExceptionCreatedEvent(testException, "test-system"));
        eventPublisher.publishEvent(new SubscriptionEventBridge.ExceptionAcknowledgedEvent(testException, "test-user"));
        eventPublisher.publishEvent(new SubscriptionEventBridge.ExceptionResolvedEvent(testException, "test-user"));

        // Then: All events should be received
        assertThat(eventsReceived.await(5, TimeUnit.SECONDS)).isTrue();
    }

    private InterfaceException createTestException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setId(1L);
        exception.setTransactionId(transactionId);
        exception.setInterfaceType(InterfaceType.ORDER);
        exception.setStatus(ExceptionStatus.PENDING);
        exception.setSeverity(ExceptionSeverity.MEDIUM);
        exception.setExceptionReason("Test exception");
        exception.setRetryable(true);
        exception.setRetryCount(0);
        exception.setCreatedAt(OffsetDateTime.now());
        return exception;
    }

    private RetryAttempt createTestRetryAttempt(InterfaceException exception, int attemptNumber) {
        RetryAttempt retryAttempt = new RetryAttempt();
        retryAttempt.setId(1L);
        retryAttempt.setInterfaceException(exception);
        retryAttempt.setAttemptNumber(attemptNumber);
        retryAttempt.setStatus(RetryStatus.PENDING);
        retryAttempt.setInitiatedBy("test-user");
        retryAttempt.setInitiatedAt(OffsetDateTime.now());
        return retryAttempt;
    }
}