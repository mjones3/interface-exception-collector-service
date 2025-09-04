package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.net.ConnectException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Configuration for comprehensive error handling and retry mechanisms
 * for RSocket connections. Provides fallback strategies and resilience patterns.
 */
@Configuration
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class RSocketErrorHandlingConfiguration {

    private final RSocketProperties rSocketProperties;

    /**
     * Configures retry template for RSocket connection operations.
     * Implements exponential backoff and specific exception handling.
     */
    @Bean
    public RetryTemplate rSocketRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(ConnectException.class, true);
        retryableExceptions.put(TimeoutException.class, true);
        retryableExceptions.put(RuntimeException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
            rSocketProperties.getMockServer().getRetry().getMaxAttempts(),
            retryableExceptions
        );
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Configure backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(rSocketProperties.getMockServer().getRetry().getWaitDuration().toMillis());
        backOffPolicy.setMultiplier(rSocketProperties.getMockServer().getRetry().getExponentialBackoffMultiplier());
        backOffPolicy.setMaxInterval(Duration.ofSeconds(30).toMillis()); // Maximum 30 seconds between retries
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Add retry listener for logging and metrics
        retryTemplate.registerListener(new RSocketRetryListener());
        
        return retryTemplate;
    }

    /**
     * Retry listener for RSocket operations to provide comprehensive logging
     * and metrics collection during retry attempts.
     */
    public static class RSocketRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            log.debug("Starting RSocket retry operation, context: {}", context);
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            if (throwable != null) {
                log.warn("RSocket retry operation failed after {} attempts, final error: {}", 
                        context.getRetryCount(), throwable.getMessage());
            } else {
                if (context.getRetryCount() > 0) {
                    log.info("RSocket retry operation succeeded after {} attempts", context.getRetryCount());
                } else {
                    log.debug("RSocket operation succeeded on first attempt");
                }
            }
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.warn("RSocket retry attempt {} failed: {}", context.getRetryCount(), throwable.getMessage());
            
            // Add context information for debugging
            context.setAttribute("lastError", throwable.getMessage());
            context.setAttribute("errorType", throwable.getClass().getSimpleName());
        }
    }

    /**
     * Configuration for RSocket error classification and handling strategies.
     */
    @Bean
    public RSocketErrorClassifier rSocketErrorClassifier() {
        return new RSocketErrorClassifier();
    }

    /**
     * Classifies RSocket errors and determines appropriate handling strategies.
     */
    public static class RSocketErrorClassifier {

        /**
         * Determines if an error is retryable based on its type and characteristics.
         */
        public boolean isRetryable(Throwable error) {
            if (error == null) {
                return false;
            }

            // Connection-related errors are retryable
            if (isConnectionError(error)) {
                return true;
            }

            // Timeout errors are retryable
            if (error instanceof TimeoutException) {
                return true;
            }

            // Certain runtime exceptions are retryable
            if (error instanceof RuntimeException) {
                String message = error.getMessage();
                if (message != null) {
                    return message.contains("Connection refused") ||
                           message.contains("Connection reset") ||
                           message.contains("Connection closed") ||
                           message.contains("Network is unreachable") ||
                           message.contains("Host is unreachable");
                }
            }

            return false;
        }

        /**
         * Determines if an error should trigger circuit breaker opening.
         */
        public boolean shouldOpenCircuitBreaker(Throwable error) {
            if (error == null) {
                return false;
            }

            // Persistent connection errors should open circuit breaker
            if (isConnectionError(error)) {
                return true;
            }

            // Repeated timeouts should open circuit breaker
            if (error instanceof TimeoutException) {
                return true;
            }

            // Service unavailable errors should open circuit breaker
            if (error instanceof RuntimeException) {
                String message = error.getMessage();
                if (message != null) {
                    return message.contains("Service unavailable") ||
                           message.contains("Server error") ||
                           message.contains("Internal server error");
                }
            }

            return false;
        }

        /**
         * Determines if an error should trigger fallback mode.
         */
        public boolean shouldEnableFallback(Throwable error) {
            // All circuit breaker triggering errors should also enable fallback
            return shouldOpenCircuitBreaker(error);
        }

        /**
         * Gets the error category for metrics and logging.
         */
        public String getErrorCategory(Throwable error) {
            if (error == null) {
                return "UNKNOWN";
            }

            if (isConnectionError(error)) {
                return "CONNECTION_ERROR";
            }

            if (error instanceof TimeoutException) {
                return "TIMEOUT";
            }

            if (error instanceof IllegalArgumentException) {
                return "CONFIGURATION_ERROR";
            }

            if (error instanceof RuntimeException) {
                String message = error.getMessage();
                if (message != null) {
                    if (message.contains("Circuit breaker")) {
                        return "CIRCUIT_BREAKER";
                    }
                    if (message.contains("Service unavailable")) {
                        return "SERVICE_UNAVAILABLE";
                    }
                }
            }

            return "GENERAL_ERROR";
        }

        /**
         * Gets a user-friendly error message for the given error.
         */
        public String getFriendlyErrorMessage(Throwable error) {
            if (error == null) {
                return "Unknown error occurred";
            }

            String category = getErrorCategory(error);
            
            switch (category) {
                case "CONNECTION_ERROR":
                    return "Unable to connect to mock RSocket server. Please check if the server is running and accessible.";
                case "TIMEOUT":
                    return "Request to mock RSocket server timed out. The server may be overloaded or unresponsive.";
                case "CONFIGURATION_ERROR":
                    return "RSocket configuration error: " + error.getMessage();
                case "CIRCUIT_BREAKER":
                    return "Mock RSocket server is temporarily unavailable due to repeated failures. Please try again later.";
                case "SERVICE_UNAVAILABLE":
                    return "Mock RSocket server is currently unavailable. Please check server status.";
                default:
                    return "An error occurred while communicating with mock RSocket server: " + error.getMessage();
            }
        }

        /**
         * Checks if an error is related to connection issues.
         */
        private boolean isConnectionError(Throwable error) {
            return error instanceof ConnectException ||
                   error instanceof java.nio.channels.ClosedChannelException ||
                   (error.getClass().getName().contains("ConnectionErrorException")) ||
                   (error.getMessage() != null && 
                    (error.getMessage().contains("Connection refused") ||
                     error.getMessage().contains("Connection reset") ||
                     error.getMessage().contains("Connection closed")));
        }
    }

    /**
     * Configuration for fallback strategies when RSocket operations fail.
     */
    @Bean
    public RSocketFallbackStrategy rSocketFallbackStrategy() {
        return new RSocketFallbackStrategy();
    }

    /**
     * Implements fallback strategies for RSocket operation failures.
     */
    public static class RSocketFallbackStrategy {

        /**
         * Determines the appropriate fallback action based on error type.
         */
        public FallbackAction determineFallbackAction(Throwable error, int failureCount) {
            if (error == null) {
                return FallbackAction.CONTINUE_NORMAL;
            }

            // For configuration errors, fail fast
            if (error instanceof IllegalArgumentException) {
                return FallbackAction.FAIL_FAST;
            }

            // For connection errors, enable graceful degradation
            if (isConnectionError(error)) {
                if (failureCount >= 3) {
                    return FallbackAction.ENABLE_FALLBACK_MODE;
                } else {
                    return FallbackAction.RETRY_WITH_BACKOFF;
                }
            }

            // For timeouts, retry with longer timeout
            if (error instanceof TimeoutException) {
                if (failureCount >= 2) {
                    return FallbackAction.ENABLE_FALLBACK_MODE;
                } else {
                    return FallbackAction.RETRY_WITH_EXTENDED_TIMEOUT;
                }
            }

            // Default fallback for other errors
            return failureCount >= 5 ? FallbackAction.ENABLE_FALLBACK_MODE : FallbackAction.RETRY_WITH_BACKOFF;
        }

        /**
         * Gets the recommended retry delay based on error type and failure count.
         */
        public Duration getRetryDelay(Throwable error, int failureCount) {
            if (error instanceof TimeoutException) {
                // Longer delays for timeout errors
                return Duration.ofSeconds(Math.min(10, 2 * failureCount));
            }
            
            if (isConnectionError(error)) {
                // Exponential backoff for connection errors
                return Duration.ofSeconds(Math.min(30, (long) Math.pow(2, failureCount)));
            }
            
            // Default exponential backoff
            return Duration.ofSeconds(Math.min(15, failureCount * 2));
        }

        private boolean isConnectionError(Throwable error) {
            return error instanceof ConnectException ||
                   error instanceof java.nio.channels.ClosedChannelException ||
                   (error.getMessage() != null && 
                    (error.getMessage().contains("Connection refused") ||
                     error.getMessage().contains("Connection reset")));
        }
    }

    /**
     * Enumeration of possible fallback actions.
     */
    public enum FallbackAction {
        CONTINUE_NORMAL,
        RETRY_WITH_BACKOFF,
        RETRY_WITH_EXTENDED_TIMEOUT,
        ENABLE_FALLBACK_MODE,
        FAIL_FAST
    }
}