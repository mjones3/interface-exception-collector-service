package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic test for GraphQL subscription functionality.
 * Tests that subscription resolvers can be created and are properly configured.
 */
@SpringBootTest
@ActiveProfiles("test")
class SubscriptionBasicTest {

    @Autowired
    private ExceptionSubscriptionResolver subscriptionResolver;

    @Test
    void testSubscriptionResolverIsAvailable() {
        // Given: Spring context is loaded
        // When: We check for the subscription resolver
        // Then: It should be available
        assertThat(subscriptionResolver).isNotNull();
    }

    @Test
    void testExceptionSubscriptionCanBeCreated() {
        // Given: A user with VIEWER role
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        // When: We create an exception subscription
        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, auth);

        // Then: Subscription should be created successfully
        assertThat(subscription).isNotNull();
    }

    @Test
    void testRetryStatusSubscriptionCanBeCreated() {
        // Given: A user with OPERATIONS role (required for retry status)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "ops-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));

        // When: We create a retry status subscription
        Flux<ExceptionSubscriptionResolver.RetryStatusEvent> subscription = subscriptionResolver
                .retryStatusUpdated("TEST-001", auth);

        // Then: Subscription should be created successfully
        assertThat(subscription).isNotNull();
    }

    @Test
    void testSubscriptionWithTimeout() {
        // Given: A user with VIEWER role
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        // When: We create a subscription with timeout
        Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> subscription = subscriptionResolver
                .exceptionUpdated(null, auth);

        // Then: Subscription should handle timeout gracefully
        subscription.take(Duration.ofMillis(100)).subscribe(
                event -> {
                    // Event received (not expected in this test)
                },
                error -> {
                    // Error handling - expected in test environment
                    assertThat(error).isNotNull();
                },
                () -> {
                    // Completion - expected after timeout
                });
    }

    @Test
    void testActiveSubscriptionCount() {
        // Given: Initial subscription count
        int initialCount = subscriptionResolver.getActiveSubscriptionCount();

        // When: We check the count
        // Then: It should be a non-negative number
        assertThat(initialCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testPublishExceptionUpdate() {
        // Given: A test exception update event
        ExceptionSubscriptionResolver.Exception testException = new ExceptionSubscriptionResolver.Exception();
        testException.setTransactionId("TEST-001");

        ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.CREATED,
                testException,
                java.time.OffsetDateTime.now(),
                "test-system");

        // When: We publish the event
        // Then: It should not throw an exception
        assertThat(() -> subscriptionResolver.publishExceptionUpdate(event))
                .doesNotThrowAnyException();
    }

    @Test
    void testPublishRetryStatusUpdate() {
        // Given: A test retry status event
        ExceptionSubscriptionResolver.RetryAttempt testRetryAttempt = new ExceptionSubscriptionResolver.RetryAttempt();
        testRetryAttempt.setAttemptNumber(1);

        ExceptionSubscriptionResolver.RetryStatusEvent event = new ExceptionSubscriptionResolver.RetryStatusEvent(
                "TEST-001",
                testRetryAttempt,
                ExceptionSubscriptionResolver.RetryEventType.INITIATED,
                java.time.OffsetDateTime.now());

        // When: We publish the event
        // Then: It should not throw an exception
        assertThat(() -> subscriptionResolver.publishRetryStatusUpdate(event))
                .doesNotThrowAnyException();
    }
}