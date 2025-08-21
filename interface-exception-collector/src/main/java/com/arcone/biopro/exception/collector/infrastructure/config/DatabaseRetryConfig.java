package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.HashMap;
import java.util.Map;

/**
 * Database retry configuration with exponential backoff.
 * Implements retry logic for database connection failures as per requirement
 * US-019.
 */
@Configuration
@EnableRetry
@Slf4j
public class DatabaseRetryConfig {

    /**
     * Retry template for database operations with exponential backoff.
     * Configured to retry on transient database errors with increasing delays.
     *
     * @return configured RetryTemplate for database operations
     */
    @Bean
    @ConditionalOnProperty(name = "app.database.retry.enabled", havingValue = "true", matchIfMissing = true)
    public RetryTemplate databaseRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L); // Start with 1 second
        backOffPolicy.setMultiplier(2.0); // Double the delay each time
        backOffPolicy.setMaxInterval(30000L); // Maximum 30 seconds between retries
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configure retry policy for specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(TransientDataAccessException.class, true);
        retryableExceptions.put(SQLTransientException.class, true);
        retryableExceptions.put(SQLException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessResourceFailureException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(5, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Add retry listener for logging
        retryTemplate.registerListener(new DatabaseRetryListener());

        log.info("Configured database retry template with exponential backoff (max 5 attempts)");
        return retryTemplate;
    }

    /**
     * Retry listener for database operations to provide detailed logging.
     */
    private static class DatabaseRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            log.debug("Starting database operation with retry support");
            return true;
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                Throwable throwable) {
            log.warn("Database operation failed (attempt {}/{}): {}",
                    context.getRetryCount(),
                    ((SimpleRetryPolicy) context.getAttribute("policy")).getMaxAttempts(),
                    throwable.getMessage());
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
                Throwable throwable) {
            if (throwable == null) {
                if (context.getRetryCount() > 0) {
                    log.info("Database operation succeeded after {} retries", context.getRetryCount());
                }
            } else {
                log.error("Database operation failed after {} attempts: {}",
                        context.getRetryCount(), throwable.getMessage());
            }
        }
    }
}