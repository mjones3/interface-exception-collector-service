package com.arcone.biopro.exception.collector.api.graphql.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for circuit breakers, retry policies, and time limiters
 * for GraphQL retry operations.
 */
@Configuration
@Slf4j
public class CircuitBreakerConfig {

        /**
         * Circuit breaker registry with custom configurations for retry operations.
         */
        @Bean
        public CircuitBreakerRegistry circuitBreakerRegistry() {
                CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

                // Configuration for single retry operations
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig retryServiceConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
                                .custom()
                                .failureRateThreshold(50.0f) // Open circuit if 50% of calls fail
                                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
                                .slidingWindowSize(10) // Consider last 10 calls
                                .minimumNumberOfCalls(5) // Need at least 5 calls to calculate failure rate
                                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                                .slowCallRateThreshold(80.0f) // Consider calls slow if 80% exceed duration threshold
                                .slowCallDurationThreshold(Duration.ofSeconds(5)) // Calls taking more than 5s are slow
                                .build();

                registry.circuitBreaker("retry-service", retryServiceConfig);

                // Configuration for bulk retry operations (more lenient)
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig bulkRetryServiceConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
                                .custom()
                                .failureRateThreshold(60.0f) // Higher threshold for bulk operations
                                .waitDurationInOpenState(Duration.ofSeconds(60)) // Longer wait for bulk operations
                                .slidingWindowSize(5) // Smaller window for bulk operations
                                .minimumNumberOfCalls(3) // Fewer calls needed
                                .permittedNumberOfCallsInHalfOpenState(2)
                                .slowCallRateThreshold(90.0f)
                                .slowCallDurationThreshold(Duration.ofSeconds(30)) // Bulk operations can take longer
                                .build();

                registry.circuitBreaker("bulk-retry-service", bulkRetryServiceConfig);

                // Configuration for cancel retry operations
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig cancelRetryServiceConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
                                .custom()
                                .failureRateThreshold(40.0f) // Lower threshold for cancel operations
                                .waitDurationInOpenState(Duration.ofSeconds(15)) // Shorter wait for cancel operations
                                .slidingWindowSize(10)
                                .minimumNumberOfCalls(5)
                                .permittedNumberOfCallsInHalfOpenState(3)
                                .slowCallRateThreshold(70.0f)
                                .slowCallDurationThreshold(Duration.ofSeconds(2)) // Cancel should be fast
                                .build();

                registry.circuitBreaker("cancel-retry-service", cancelRetryServiceConfig);

                // Configuration for payload retrieval operations
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig payloadRetrievalConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
                                .custom()
                                .failureRateThreshold(60.0f) // Higher threshold for external service calls
                                .waitDurationInOpenState(Duration.ofSeconds(45)) // Wait 45s before trying again
                                .slidingWindowSize(8)
                                .minimumNumberOfCalls(4)
                                .permittedNumberOfCallsInHalfOpenState(2)
                                .slowCallRateThreshold(85.0f)
                                .slowCallDurationThreshold(Duration.ofSeconds(8)) // External calls can be slower
                                .build();

                registry.circuitBreaker("payload-retrieval", payloadRetrievalConfig);

                // Configuration for retry submission operations
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig retrySubmissionConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
                                .custom()
                                .failureRateThreshold(55.0f) // Moderate threshold for retry submissions
                                .waitDurationInOpenState(Duration.ofSeconds(60)) // Longer wait for retry submissions
                                .slidingWindowSize(6)
                                .minimumNumberOfCalls(3)
                                .permittedNumberOfCallsInHalfOpenState(2)
                                .slowCallRateThreshold(80.0f)
                                .slowCallDurationThreshold(Duration.ofSeconds(10)) // Retry submissions can take time
                                .build();

                registry.circuitBreaker("retry-submission", retrySubmissionConfig);

                // Add event listeners for monitoring
                registry.circuitBreaker("retry-service").getEventPublisher()
                                .onStateTransition(event -> log.info(
                                                "Retry service circuit breaker state transition: {} -> {}",
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()));

                registry.circuitBreaker("bulk-retry-service").getEventPublisher()
                                .onStateTransition(event -> log.info(
                                                "Bulk retry service circuit breaker state transition: {} -> {}",
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()));

                registry.circuitBreaker("cancel-retry-service").getEventPublisher()
                                .onStateTransition(event -> log.info(
                                                "Cancel retry service circuit breaker state transition: {} -> {}",
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()));

                registry.circuitBreaker("payload-retrieval").getEventPublisher()
                                .onStateTransition(event -> log.info(
                                                "Payload retrieval circuit breaker state transition: {} -> {}",
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()));

                registry.circuitBreaker("retry-submission").getEventPublisher()
                                .onStateTransition(event -> log.info(
                                                "Retry submission circuit breaker state transition: {} -> {}",
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()));

                return registry;
        }

        /**
         * Retry registry with custom configurations for retry operations.
         */
        @Bean
        public RetryRegistry retryRegistry() {
                RetryRegistry registry = RetryRegistry.ofDefaults();

                // Configuration for retry service operations
                RetryConfig retryServiceConfig = RetryConfig.custom()
                                .maxAttempts(3) // Retry up to 3 times
                                .waitDuration(Duration.ofSeconds(1)) // Wait 1 second between retries
                                .retryOnException(throwable -> {
                                        // Retry on specific exceptions
                                        return throwable instanceof RuntimeException &&
                                                        !(throwable instanceof IllegalArgumentException);
                                })
                                .build();

                registry.retry("retry-service", retryServiceConfig);

                // Configuration for payload retrieval retry
                RetryConfig payloadRetrievalRetryConfig = RetryConfig.custom()
                                .maxAttempts(2) // Fewer retries for external services
                                .waitDuration(Duration.ofSeconds(2)) // Longer wait for external services
                                .retryOnException(throwable -> {
                                        return throwable instanceof RuntimeException &&
                                                        !(throwable instanceof IllegalArgumentException);
                                })
                                .build();

                registry.retry("payload-retrieval", payloadRetrievalRetryConfig);

                // Configuration for retry submission retry
                RetryConfig retrySubmissionRetryConfig = RetryConfig.custom()
                                .maxAttempts(2) // Fewer retries for retry submissions
                                .waitDuration(Duration.ofSeconds(3)) // Longer wait for retry submissions
                                .retryOnException(throwable -> {
                                        return throwable instanceof RuntimeException &&
                                                        !(throwable instanceof IllegalArgumentException);
                                })
                                .build();

                registry.retry("retry-submission", retrySubmissionRetryConfig);

                // Add event listeners for monitoring
                registry.retry("retry-service").getEventPublisher()
                                .onRetry(event -> log.warn("Retry attempt {} for retry service, last exception: {}",
                                                event.getNumberOfRetryAttempts(),
                                                event.getLastThrowable().getMessage()));

                registry.retry("payload-retrieval").getEventPublisher()
                                .onRetry(event -> log.warn("Retry attempt {} for payload retrieval, last exception: {}",
                                                event.getNumberOfRetryAttempts(),
                                                event.getLastThrowable().getMessage()));

                registry.retry("retry-submission").getEventPublisher()
                                .onRetry(event -> log.warn("Retry attempt {} for retry submission, last exception: {}",
                                                event.getNumberOfRetryAttempts(),
                                                event.getLastThrowable().getMessage()));

                return registry;
        }

        /**
         * Time limiter registry with custom configurations for retry operations.
         */
        @Bean
        public TimeLimiterRegistry timeLimiterRegistry() {
                TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();

                // Configuration for single retry operations
                TimeLimiterConfig retryServiceConfig = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(10)) // 10 second timeout for single retries
                                .cancelRunningFuture(true)
                                .build();

                registry.timeLimiter("retry-service", retryServiceConfig);

                // Configuration for bulk retry operations
                TimeLimiterConfig bulkRetryServiceConfig = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofMinutes(5)) // 5 minute timeout for bulk operations
                                .cancelRunningFuture(true)
                                .build();

                registry.timeLimiter("bulk-retry-service", bulkRetryServiceConfig);

                // Configuration for cancel retry operations
                TimeLimiterConfig cancelRetryServiceConfig = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(5)) // 5 second timeout for cancel operations
                                .cancelRunningFuture(true)
                                .build();

                registry.timeLimiter("cancel-retry-service", cancelRetryServiceConfig);

                // Configuration for payload retrieval operations
                TimeLimiterConfig payloadRetrievalConfig = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(15)) // 15 second timeout for external service calls
                                .cancelRunningFuture(true)
                                .build();

                registry.timeLimiter("payload-retrieval", payloadRetrievalConfig);

                // Configuration for retry submission operations
                TimeLimiterConfig retrySubmissionConfig = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(20)) // 20 second timeout for retry submissions
                                .cancelRunningFuture(true)
                                .build();

                registry.timeLimiter("retry-submission", retrySubmissionConfig);

                // Add event listeners for monitoring
                registry.timeLimiter("retry-service").getEventPublisher()
                                .onTimeout(event -> log.warn("Retry service operation timed out"));

                registry.timeLimiter("bulk-retry-service").getEventPublisher()
                                .onTimeout(event -> log.warn("Bulk retry service operation timed out"));

                registry.timeLimiter("cancel-retry-service").getEventPublisher()
                                .onTimeout(event -> log.warn("Cancel retry service operation timed out"));

                registry.timeLimiter("payload-retrieval").getEventPublisher()
                                .onTimeout(event -> log.warn("Payload retrieval operation timed out"));

                registry.timeLimiter("retry-submission").getEventPublisher()
                                .onTimeout(event -> log.warn("Retry submission operation timed out"));

                return registry;
        }
}