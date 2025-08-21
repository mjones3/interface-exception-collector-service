package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.infrastructure.config.DatabaseRetryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DatabaseRetryConfig.
 * Tests database retry configuration and exponential backoff behavior.
 */
class DatabaseRetryConfigTest {

    private final DatabaseRetryConfig config = new DatabaseRetryConfig();

    @Test
    void databaseRetryTemplate_ShouldRetryOnTransientDataAccessException() {
        // Given
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(() -> retryTemplate.execute((RetryCallback<Void, TransientDataAccessException>) context -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 3) {
                throw new TransientDataAccessException("Transient error") {
                };
            }
            return null;
        })).isInstanceOf(TransientDataAccessException.class);

        assertThat(attemptCount.get()).isEqualTo(5); // Should retry 5 times (max attempts)
    }

    @Test
    void databaseRetryTemplate_ShouldRetryOnSQLException() {
        // Given
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(() -> retryTemplate.execute((RetryCallback<Void, SQLException>) context -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 3) {
                throw new SQLException("Database connection failed");
            }
            return null;
        })).isInstanceOf(SQLException.class);

        assertThat(attemptCount.get()).isEqualTo(5); // Should retry 5 times (max attempts)
    }

    @Test
    void databaseRetryTemplate_ShouldRetryOnDataAccessResourceFailureException() {
        // Given
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(
                () -> retryTemplate.execute((RetryCallback<Void, DataAccessResourceFailureException>) context -> {
                    attemptCount.incrementAndGet();
                    if (attemptCount.get() < 3) {
                        throw new DataAccessResourceFailureException("Cannot get JDBC connection");
                    }
                    return null;
                })).isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(attemptCount.get()).isEqualTo(5); // Should retry 5 times (max attempts)
    }

    @Test
    void databaseRetryTemplate_ShouldSucceedAfterRetries() throws Exception {
        // Given
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When
        String result = retryTemplate.execute((RetryCallback<String, SQLException>) context -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 3) {
                throw new SQLException("Temporary failure");
            }
            return "Success";
        });

        // Then
        assertThat(result).isEqualTo("Success");
        assertThat(attemptCount.get()).isEqualTo(3); // Should succeed on 3rd attempt
    }

    @Test
    void databaseRetryTemplate_ShouldNotRetryOnNonRetryableException() {
        // Given
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(() -> retryTemplate.execute((RetryCallback<Void, IllegalArgumentException>) context -> {
            attemptCount.incrementAndGet();
            throw new IllegalArgumentException("Non-retryable error");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(attemptCount.get()).isEqualTo(1); // Should not retry
    }
}