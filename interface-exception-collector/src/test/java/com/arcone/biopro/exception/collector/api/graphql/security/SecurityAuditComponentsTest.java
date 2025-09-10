package com.arcone.biopro.exception.collector.api.graphql.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Simple test to verify that the security audit components compile and work correctly.
 * This test focuses only on the components created for task 13.
 * 
 * Requirements: 5.3, 5.5
 */
class SecurityAuditComponentsTest {

    @Test
    void mutationRateLimiter_ShouldWorkCorrectly() {
        // Given
        MutationRateLimiter rateLimiter = new MutationRateLimiter(2, 5);

        // When & Then - Should allow requests within limits
        rateLimiter.checkRateLimit("test-user", "RETRY");
        rateLimiter.checkRateLimit("test-user", "RETRY");

        // Should throw exception when limit exceeded
        assertThatThrownBy(() -> rateLimiter.checkRateLimit("test-user", "RETRY"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    void operationTracker_ShouldTrackOperations() {
        // Given
        OperationTracker tracker = new OperationTracker();

        // When
        String trackingId = tracker.recordOperationStart("RETRY", "test-user", "TXN-123");
        tracker.recordOperationComplete(trackingId, "RETRY", "test-user", true, 100L);

        // Then
        OperationTracker.OperationStatsSummary stats = tracker.getOperationStats("RETRY");
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalCount()).isEqualTo(1);
        assertThat(stats.getSuccessCount()).isEqualTo(1);
        assertThat(stats.getSuccessRate()).isEqualTo(1.0);
    }

    @Test
    void rateLimitExceededException_ShouldContainCorrectInformation() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";
        int currentCount = 5;
        int maxAllowed = 3;
        long resetTime = System.currentTimeMillis() + 60000;

        // When
        RateLimitExceededException exception = new RateLimitExceededException(
                userId, operationType, currentCount, maxAllowed, resetTime);

        // Then
        assertThat(exception.getUserId()).isEqualTo(userId);
        assertThat(exception.getOperationType()).isEqualTo(operationType);
        assertThat(exception.getCurrentCount()).isEqualTo(currentCount);
        assertThat(exception.getMaxAllowed()).isEqualTo(maxAllowed);
        assertThat(exception.getResetTimeMs()).isEqualTo(resetTime);
        assertThat(exception.getMessage()).contains("Rate limit exceeded");
        assertThat(exception.getMessage()).contains(userId);
        assertThat(exception.getMessage()).contains(operationType);
    }

    @Test
    void queryNotAllowedException_ShouldWorkCorrectly() {
        // Given
        String operationType = "DANGEROUS_QUERY";
        String reason = "Query contains sensitive operations";

        // When
        QueryNotAllowedException exception = new QueryNotAllowedException(operationType, reason);

        // Then
        assertThat(exception.getOperationType()).isEqualTo(operationType);
        assertThat(exception.getReason()).isEqualTo(reason);
        assertThat(exception.getMessage()).contains(operationType);
        assertThat(exception.getMessage()).contains(reason);
    }

    @Test
    void rateLimitStatus_ShouldProvideCorrectInformation() {
        // Given
        MutationRateLimiter rateLimiter = new MutationRateLimiter(5, 20);

        // When - Make some requests
        rateLimiter.checkRateLimit("test-user", "RETRY");
        rateLimiter.checkRateLimit("test-user", "RETRY");
        rateLimiter.checkRateLimit("test-user", "RETRY");

        MutationRateLimiter.RateLimitStatus status = rateLimiter.getRateLimitStatus("test-user", "RETRY");

        // Then
        assertThat(status.getCurrentMinuteCount()).isEqualTo(3);
        assertThat(status.getMaxMinuteCount()).isEqualTo(5);
        assertThat(status.getCurrentHourCount()).isEqualTo(3);
        assertThat(status.getMaxHourCount()).isEqualTo(20);
        assertThat(status.isMinuteLimitExceeded()).isFalse();
        assertThat(status.isHourLimitExceeded()).isFalse();
        assertThat(status.getMinuteResetTime()).isGreaterThan(System.currentTimeMillis());
        assertThat(status.getHourResetTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void operationTracker_GlobalStats_ShouldAggregateCorrectly() {
        // Given
        OperationTracker tracker = new OperationTracker();

        // When - Record multiple operations
        String id1 = tracker.recordOperationStart("RETRY", "user1", "TXN-1");
        String id2 = tracker.recordOperationStart("ACKNOWLEDGE", "user2", "TXN-2");
        String id3 = tracker.recordOperationStart("RESOLVE", "user1", "TXN-3");

        tracker.recordOperationComplete(id1, "RETRY", "user1", true, 100L);
        tracker.recordOperationComplete(id2, "ACKNOWLEDGE", "user2", false, 50L);
        tracker.recordOperationComplete(id3, "RESOLVE", "user1", true, 200L);

        // Then
        OperationTracker.GlobalStatsSummary globalStats = tracker.getGlobalStats();
        assertThat(globalStats.getTotalOperations()).isEqualTo(3);
        assertThat(globalStats.getTotalSuccessfulOperations()).isEqualTo(2);
        assertThat(globalStats.getTotalFailedOperations()).isEqualTo(1);
        assertThat(globalStats.getUniqueOperationTypes()).isEqualTo(3);
        assertThat(globalStats.getUniqueUsers()).isEqualTo(2);
        assertThat(globalStats.getGlobalSuccessRate()).isEqualTo(2.0 / 3.0);
    }

    @Test
    void operationTracker_UserStats_ShouldTrackPerUser() {
        // Given
        OperationTracker tracker = new OperationTracker();

        // When - Record operations for specific user
        String id1 = tracker.recordOperationStart("RETRY", "test-user", "TXN-1");
        String id2 = tracker.recordOperationStart("ACKNOWLEDGE", "test-user", "TXN-2");

        tracker.recordOperationComplete(id1, "RETRY", "test-user", true, 100L);
        tracker.recordOperationComplete(id2, "ACKNOWLEDGE", "test-user", false, 50L);

        // Then
        OperationTracker.UserOperationStatsSummary userStats = tracker.getUserStats("test-user");
        assertThat(userStats).isNotNull();
        assertThat(userStats.getTotalOperations()).isEqualTo(2);
        assertThat(userStats.getSuccessfulOperations()).isEqualTo(1);
        assertThat(userStats.getFailedOperations()).isEqualTo(1);
        assertThat(userStats.getSuccessRate()).isEqualTo(0.5);
        assertThat(userStats.getOperationCounts().get("RETRY").get()).isEqualTo(1);
        assertThat(userStats.getOperationCounts().get("ACKNOWLEDGE").get()).isEqualTo(1);
    }
}