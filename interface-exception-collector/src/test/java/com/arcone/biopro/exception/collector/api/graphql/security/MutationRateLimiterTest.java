package com.arcone.biopro.exception.collector.api.graphql.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for MutationRateLimiter.
 * Tests rate limiting functionality for mutation operations.
 * 
 * Requirements: 5.3, 5.5
 */
@ExtendWith(MockitoExtension.class)
class MutationRateLimiterTest {

    private MutationRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Initialize with low limits for testing
        rateLimiter = new MutationRateLimiter(3, 10); // 3 per minute, 10 per hour
    }

    @Test
    void checkRateLimit_WithinLimits_ShouldPass() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When & Then - Should not throw exception
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
    }

    @Test
    void checkRateLimit_ExceedsMinuteLimit_ShouldThrowException() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When - Exceed minute limit
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);

        // Then - Fourth request should fail
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(userId, operationType))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded")
                .hasMessageContaining("test-user")
                .hasMessageContaining("RETRY");
    }

    @Test
    void checkRateLimit_DifferentUsers_ShouldHaveSeparateLimits() {
        // Given
        String user1 = "user1";
        String user2 = "user2";
        String operationType = "RETRY";

        // When - Each user uses their full limit
        rateLimiter.checkRateLimit(user1, operationType);
        rateLimiter.checkRateLimit(user1, operationType);
        rateLimiter.checkRateLimit(user1, operationType);

        rateLimiter.checkRateLimit(user2, operationType);
        rateLimiter.checkRateLimit(user2, operationType);
        rateLimiter.checkRateLimit(user2, operationType);

        // Then - Both should be at their limit
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(user1, operationType))
                .isInstanceOf(RateLimitExceededException.class);

        assertThatThrownBy(() -> rateLimiter.checkRateLimit(user2, operationType))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void checkRateLimit_DifferentOperations_ShouldHaveSeparateLimits() {
        // Given
        String userId = "test-user";
        String retryOp = "RETRY";
        String acknowledgeOp = "ACKNOWLEDGE";

        // When - Use full limit for retry operations
        rateLimiter.checkRateLimit(userId, retryOp);
        rateLimiter.checkRateLimit(userId, retryOp);
        rateLimiter.checkRateLimit(userId, retryOp);

        // Then - Should still be able to perform acknowledge operations
        rateLimiter.checkRateLimit(userId, acknowledgeOp);
        rateLimiter.checkRateLimit(userId, acknowledgeOp);
        rateLimiter.checkRateLimit(userId, acknowledgeOp);

        // But retry should be limited
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(userId, retryOp))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void checkRateLimit_WithNullValues_ShouldNotThrow() {
        // When & Then - Should handle null values gracefully
        rateLimiter.checkRateLimit(null, "RETRY");
        rateLimiter.checkRateLimit("user", null);
        rateLimiter.checkRateLimit(null, null);
    }

    @Test
    void getRateLimitStatus_ShouldReturnCorrectStatus() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When - Make some requests
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);

        MutationRateLimiter.RateLimitStatus status = rateLimiter.getRateLimitStatus(userId, operationType);

        // Then
        assertThat(status.getCurrentMinuteCount()).isEqualTo(2);
        assertThat(status.getMaxMinuteCount()).isEqualTo(3);
        assertThat(status.getCurrentHourCount()).isEqualTo(2);
        assertThat(status.getMaxHourCount()).isEqualTo(10);
        assertThat(status.isMinuteLimitExceeded()).isFalse();
        assertThat(status.isHourLimitExceeded()).isFalse();
    }

    @Test
    void getRateLimitStatus_WithNullValues_ShouldReturnEmptyStatus() {
        // When
        MutationRateLimiter.RateLimitStatus status1 = rateLimiter.getRateLimitStatus(null, "RETRY");
        MutationRateLimiter.RateLimitStatus status2 = rateLimiter.getRateLimitStatus("user", null);

        // Then
        assertThat(status1.getCurrentMinuteCount()).isEqualTo(0);
        assertThat(status1.getCurrentHourCount()).isEqualTo(0);
        assertThat(status2.getCurrentMinuteCount()).isEqualTo(0);
        assertThat(status2.getCurrentHourCount()).isEqualTo(0);
    }

    @Test
    void clearAll_ShouldResetAllLimits() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When - Use up the limit
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);

        // Clear all limits
        rateLimiter.clearAll();

        // Then - Should be able to make requests again
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
    }

    @Test
    void rateLimitException_ShouldContainCorrectInformation() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When - Exceed the limit
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);
        rateLimiter.checkRateLimit(userId, operationType);

        // Then
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(userId, operationType))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(exception -> {
                    RateLimitExceededException rle = (RateLimitExceededException) exception;
                    assertThat(rle.getUserId()).isEqualTo(userId);
                    assertThat(rle.getOperationType()).isEqualTo(operationType);
                    assertThat(rle.getCurrentCount()).isEqualTo(3);
                    assertThat(rle.getMaxAllowed()).isEqualTo(3);
                    assertThat(rle.getResetTimeMs()).isGreaterThan(System.currentTimeMillis());
                });
    }
}