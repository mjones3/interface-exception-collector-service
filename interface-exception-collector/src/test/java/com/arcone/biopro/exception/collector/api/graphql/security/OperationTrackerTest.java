package com.arcone.biopro.exception.collector.api.graphql.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OperationTracker.
 * Tests basic operation tracking functionality for mutation operations.
 * 
 * Requirements: 5.3, 5.5
 */
@ExtendWith(MockitoExtension.class)
class OperationTrackerTest {

    private OperationTracker operationTracker;

    @BeforeEach
    void setUp() {
        operationTracker = new OperationTracker();
    }

    @Test
    void recordOperationStart_ShouldGenerateTrackingId() {
        // When
        String trackingId = operationTracker.recordOperationStart("RETRY", "test-user", "TXN-123");

        // Then
        assertThat(trackingId).isNotNull();
        assertThat(trackingId).startsWith("RETRY_testuser_");
        assertThat(trackingId).contains("testuser"); // Special characters removed
    }

    @Test
    void recordOperationComplete_ShouldUpdateStatistics() {
        // Given
        String trackingId = operationTracker.recordOperationStart("RETRY", "test-user", "TXN-123");

        // When
        operationTracker.recordOperationComplete(trackingId, "RETRY", "test-user", true, 150L);

        // Then
        OperationTracker.OperationStatsSummary stats = operationTracker.getOperationStats("RETRY");
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalCount()).isEqualTo(1);
        assertThat(stats.getSuccessCount()).isEqualTo(1);
        assertThat(stats.getFailureCount()).isEqualTo(0);
        assertThat(stats.getSuccessRate()).isEqualTo(1.0);
        assertThat(stats.getAverageExecutionTime()).isEqualTo(150.0);
    }

    @Test
    void recordOperationComplete_WithFailure_ShouldUpdateFailureStats() {
        // Given
        String trackingId = operationTracker.recordOperationStart("RETRY", "test-user", "TXN-123");

        // When
        operationTracker.recordOperationComplete(trackingId, "RETRY", "test-user", false, 75L);

        // Then
        OperationTracker.OperationStatsSummary stats = operationTracker.getOperationStats("RETRY");
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalCount()).isEqualTo(1);
        assertThat(stats.getSuccessCount()).isEqualTo(0);
        assertThat(stats.getFailureCount()).isEqualTo(1);
        assertThat(stats.getSuccessRate()).isEqualTo(0.0);
        assertThat(stats.getAverageExecutionTime()).isEqualTo(75.0);
    }

    @Test
    void getOperationStats_WithMultipleOperations_ShouldCalculateCorrectAverages() {
        // Given
        String trackingId1 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-123");
        String trackingId2 = operationTracker.recordOperationStart("RETRY", "user2", "TXN-456");
        String trackingId3 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-789");

        // When
        operationTracker.recordOperationComplete(trackingId1, "RETRY", "user1", true, 100L);
        operationTracker.recordOperationComplete(trackingId2, "RETRY", "user2", false, 200L);
        operationTracker.recordOperationComplete(trackingId3, "RETRY", "user1", true, 300L);

        // Then
        OperationTracker.OperationStatsSummary stats = operationTracker.getOperationStats("RETRY");
        assertThat(stats.getTotalCount()).isEqualTo(3);
        assertThat(stats.getSuccessCount()).isEqualTo(2);
        assertThat(stats.getFailureCount()).isEqualTo(1);
        assertThat(stats.getSuccessRate()).isEqualTo(2.0 / 3.0);
        assertThat(stats.getAverageExecutionTime()).isEqualTo(200.0); // (100 + 200 + 300) / 3
    }

    @Test
    void getUserStats_ShouldTrackUserSpecificStatistics() {
        // Given
        String trackingId1 = operationTracker.recordOperationStart("RETRY", "test-user", "TXN-123");
        String trackingId2 = operationTracker.recordOperationStart("ACKNOWLEDGE", "test-user", "TXN-456");

        // When
        operationTracker.recordOperationComplete(trackingId1, "RETRY", "test-user", true, 100L);
        operationTracker.recordOperationComplete(trackingId2, "ACKNOWLEDGE", "test-user", false, 50L);

        // Then
        OperationTracker.UserOperationStatsSummary userStats = operationTracker.getUserStats("test-user");
        assertThat(userStats).isNotNull();
        assertThat(userStats.getTotalOperations()).isEqualTo(2);
        assertThat(userStats.getSuccessfulOperations()).isEqualTo(1);
        assertThat(userStats.getFailedOperations()).isEqualTo(1);
        assertThat(userStats.getSuccessRate()).isEqualTo(0.5);
        assertThat(userStats.getOperationCounts().get("RETRY").get()).isEqualTo(1);
        assertThat(userStats.getOperationCounts().get("ACKNOWLEDGE").get()).isEqualTo(1);
    }

    @Test
    void getGlobalStats_ShouldProvideOverallStatistics() {
        // Given
        operationTracker.recordOperationStart("RETRY", "user1", "TXN-123");
        operationTracker.recordOperationStart("ACKNOWLEDGE", "user2", "TXN-456");
        operationTracker.recordOperationStart("RESOLVE", "user1", "TXN-789");

        // When
        OperationTracker.GlobalStatsSummary globalStats = operationTracker.getGlobalStats();

        // Then
        assertThat(globalStats.getTotalOperations()).isEqualTo(3);
        assertThat(globalStats.getUniqueOperationTypes()).isEqualTo(3); // RETRY, ACKNOWLEDGE, RESOLVE
        assertThat(globalStats.getUniqueUsers()).isEqualTo(2); // user1, user2
    }

    @Test
    void getOperationStats_ForNonExistentOperation_ShouldReturnNull() {
        // When
        OperationTracker.OperationStatsSummary stats = operationTracker.getOperationStats("NON_EXISTENT");

        // Then
        assertThat(stats).isNull();
    }

    @Test
    void getUserStats_ForNonExistentUser_ShouldReturnNull() {
        // When
        OperationTracker.UserOperationStatsSummary userStats = operationTracker.getUserStats("non-existent-user");

        // Then
        assertThat(userStats).isNull();
    }

    @Test
    void clearAll_ShouldResetAllStatistics() {
        // Given
        String trackingId = operationTracker.recordOperationStart("RETRY", "test-user", "TXN-123");
        operationTracker.recordOperationComplete(trackingId, "RETRY", "test-user", true, 100L);

        // When
        operationTracker.clearAll();

        // Then
        assertThat(operationTracker.getOperationStats("RETRY")).isNull();
        assertThat(operationTracker.getUserStats("test-user")).isNull();
        
        OperationTracker.GlobalStatsSummary globalStats = operationTracker.getGlobalStats();
        assertThat(globalStats.getTotalOperations()).isEqualTo(0);
        assertThat(globalStats.getTotalSuccessfulOperations()).isEqualTo(0);
        assertThat(globalStats.getTotalFailedOperations()).isEqualTo(0);
    }

    @Test
    void recordOperationComplete_WithoutStart_ShouldHandleGracefully() {
        // When - Complete operation without starting it
        operationTracker.recordOperationComplete("non-existent-id", "RETRY", "test-user", true, 100L);

        // Then - Should not crash and global stats should still work
        OperationTracker.GlobalStatsSummary globalStats = operationTracker.getGlobalStats();
        assertThat(globalStats.getTotalSuccessfulOperations()).isEqualTo(1);
    }

    @Test
    void operationStatsSummary_ShouldCalculateCorrectSuccessRate() {
        // Given
        String trackingId1 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-1");
        String trackingId2 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-2");
        String trackingId3 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-3");
        String trackingId4 = operationTracker.recordOperationStart("RETRY", "user1", "TXN-4");

        // When - 3 successes, 1 failure
        operationTracker.recordOperationComplete(trackingId1, "RETRY", "user1", true, 100L);
        operationTracker.recordOperationComplete(trackingId2, "RETRY", "user1", true, 150L);
        operationTracker.recordOperationComplete(trackingId3, "RETRY", "user1", false, 75L);
        operationTracker.recordOperationComplete(trackingId4, "RETRY", "user1", true, 200L);

        // Then
        OperationTracker.OperationStatsSummary stats = operationTracker.getOperationStats("RETRY");
        assertThat(stats.getSuccessRate()).isEqualTo(0.75); // 3/4
        assertThat(stats.getTotalExecutionTime()).isEqualTo(525L); // 100+150+75+200
        assertThat(stats.getAverageExecutionTime()).isEqualTo(131.25); // 525/4
    }
}