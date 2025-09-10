package com.arcone.biopro.exception.collector.api.graphql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic operation tracking service for mutation operations.
 * Tracks operation counts, timing, and basic statistics without complex permission checks.
 * Provides simple monitoring and compliance tracking.
 * 
 * Requirements: 5.3, 5.5
 */
@Component
@Slf4j
public class OperationTracker {

    // Simple in-memory tracking for basic statistics
    private final ConcurrentHashMap<String, OperationStats> operationStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserOperationStats> userStats = new ConcurrentHashMap<>();
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong totalSuccessfulOperations = new AtomicLong(0);
    private final AtomicLong totalFailedOperations = new AtomicLong(0);

    /**
     * Records the start of a mutation operation.
     * 
     * @param operationType the type of operation (RETRY, ACKNOWLEDGE, etc.)
     * @param userId the user performing the operation
     * @param transactionId the transaction being operated on
     * @return operation tracking ID for correlation
     */
    public String recordOperationStart(String operationType, String userId, String transactionId) {
        String trackingId = generateTrackingId(operationType, userId);
        
        // Update operation statistics
        operationStats.computeIfAbsent(operationType, k -> new OperationStats()).recordStart();
        
        // Update user statistics
        userStats.computeIfAbsent(userId, k -> new UserOperationStats()).recordOperation(operationType);
        
        // Update global counters
        totalOperations.incrementAndGet();
        
        log.debug("Operation started: type={}, user={}, transactionId={}, trackingId={}", 
                operationType, userId, transactionId, trackingId);
        
        return trackingId;
    }

    /**
     * Records the completion of a mutation operation.
     * 
     * @param trackingId the tracking ID from recordOperationStart
     * @param operationType the type of operation
     * @param userId the user who performed the operation
     * @param success whether the operation was successful
     * @param executionTimeMs execution time in milliseconds
     */
    public void recordOperationComplete(String trackingId, String operationType, 
                                       String userId, boolean success, long executionTimeMs) {
        
        // Update operation statistics
        OperationStats stats = operationStats.get(operationType);
        if (stats != null) {
            stats.recordComplete(success, executionTimeMs);
        }
        
        // Update user statistics
        UserOperationStats userStat = userStats.get(userId);
        if (userStat != null) {
            userStat.recordResult(success);
        }
        
        // Update global counters
        if (success) {
            totalSuccessfulOperations.incrementAndGet();
        } else {
            totalFailedOperations.incrementAndGet();
        }
        
        log.debug("Operation completed: trackingId={}, type={}, user={}, success={}, executionTime={}ms", 
                trackingId, operationType, userId, success, executionTimeMs);
    }

    /**
     * Gets operation statistics for a specific operation type.
     * 
     * @param operationType the operation type
     * @return operation statistics or null if not found
     */
    public OperationStatsSummary getOperationStats(String operationType) {
        OperationStats stats = operationStats.get(operationType);
        if (stats == null) {
            return null;
        }
        
        return new OperationStatsSummary(
                operationType,
                stats.totalCount.get(),
                stats.successCount.get(),
                stats.failureCount.get(),
                stats.totalExecutionTime.get(),
                stats.getAverageExecutionTime(),
                stats.lastOperationTime
        );
    }

    /**
     * Gets operation statistics for a specific user.
     * 
     * @param userId the user ID
     * @return user operation statistics or null if not found
     */
    public UserOperationStatsSummary getUserStats(String userId) {
        UserOperationStats stats = userStats.get(userId);
        if (stats == null) {
            return null;
        }
        
        return new UserOperationStatsSummary(
                userId,
                stats.totalOperations.get(),
                stats.successfulOperations.get(),
                stats.failedOperations.get(),
                stats.lastOperationTime,
                stats.operationCounts
        );
    }

    /**
     * Gets global operation statistics.
     * 
     * @return global statistics summary
     */
    public GlobalStatsSummary getGlobalStats() {
        return new GlobalStatsSummary(
                totalOperations.get(),
                totalSuccessfulOperations.get(),
                totalFailedOperations.get(),
                operationStats.size(),
                userStats.size()
        );
    }

    /**
     * Clears all tracking data (for testing purposes).
     */
    public void clearAll() {
        operationStats.clear();
        userStats.clear();
        totalOperations.set(0);
        totalSuccessfulOperations.set(0);
        totalFailedOperations.set(0);
        log.debug("All operation tracking data cleared");
    }

    /**
     * Generates a unique tracking ID for an operation.
     */
    private String generateTrackingId(String operationType, String userId) {
        return String.format("%s_%s_%d", 
                operationType.toUpperCase(), 
                userId.replaceAll("[^a-zA-Z0-9]", ""), 
                System.currentTimeMillis());
    }

    /**
     * Internal class to track statistics for a specific operation type.
     */
    private static class OperationStats {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private volatile Instant lastOperationTime;

        void recordStart() {
            totalCount.incrementAndGet();
            lastOperationTime = Instant.now();
        }

        void recordComplete(boolean success, long executionTimeMs) {
            if (success) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            totalExecutionTime.addAndGet(executionTimeMs);
        }

        double getAverageExecutionTime() {
            long total = totalCount.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
        }
    }

    /**
     * Internal class to track statistics for a specific user.
     */
    private static class UserOperationStats {
        private final AtomicLong totalOperations = new AtomicLong(0);
        private final AtomicLong successfulOperations = new AtomicLong(0);
        private final AtomicLong failedOperations = new AtomicLong(0);
        private final ConcurrentHashMap<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
        private volatile Instant lastOperationTime;

        void recordOperation(String operationType) {
            totalOperations.incrementAndGet();
            operationCounts.computeIfAbsent(operationType, k -> new AtomicLong(0)).incrementAndGet();
            lastOperationTime = Instant.now();
        }

        void recordResult(boolean success) {
            if (success) {
                successfulOperations.incrementAndGet();
            } else {
                failedOperations.incrementAndGet();
            }
        }
    }

    /**
     * Summary of operation statistics for a specific operation type.
     */
    public static class OperationStatsSummary {
        private final String operationType;
        private final long totalCount;
        private final long successCount;
        private final long failureCount;
        private final long totalExecutionTime;
        private final double averageExecutionTime;
        private final Instant lastOperationTime;

        public OperationStatsSummary(String operationType, long totalCount, long successCount, 
                                   long failureCount, long totalExecutionTime, 
                                   double averageExecutionTime, Instant lastOperationTime) {
            this.operationType = operationType;
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.totalExecutionTime = totalExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
            this.lastOperationTime = lastOperationTime;
        }

        // Getters
        public String getOperationType() { return operationType; }
        public long getTotalCount() { return totalCount; }
        public long getSuccessCount() { return successCount; }
        public long getFailureCount() { return failureCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public double getAverageExecutionTime() { return averageExecutionTime; }
        public Instant getLastOperationTime() { return lastOperationTime; }
        public double getSuccessRate() { 
            return totalCount > 0 ? (double) successCount / totalCount : 0.0; 
        }
    }

    /**
     * Summary of operation statistics for a specific user.
     */
    public static class UserOperationStatsSummary {
        private final String userId;
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final Instant lastOperationTime;
        private final ConcurrentHashMap<String, AtomicLong> operationCounts;

        public UserOperationStatsSummary(String userId, long totalOperations, 
                                       long successfulOperations, long failedOperations,
                                       Instant lastOperationTime, 
                                       ConcurrentHashMap<String, AtomicLong> operationCounts) {
            this.userId = userId;
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.lastOperationTime = lastOperationTime;
            this.operationCounts = operationCounts;
        }

        // Getters
        public String getUserId() { return userId; }
        public long getTotalOperations() { return totalOperations; }
        public long getSuccessfulOperations() { return successfulOperations; }
        public long getFailedOperations() { return failedOperations; }
        public Instant getLastOperationTime() { return lastOperationTime; }
        public ConcurrentHashMap<String, AtomicLong> getOperationCounts() { return operationCounts; }
        public double getSuccessRate() { 
            return totalOperations > 0 ? (double) successfulOperations / totalOperations : 0.0; 
        }
    }

    /**
     * Summary of global operation statistics.
     */
    public static class GlobalStatsSummary {
        private final long totalOperations;
        private final long totalSuccessfulOperations;
        private final long totalFailedOperations;
        private final int uniqueOperationTypes;
        private final int uniqueUsers;

        public GlobalStatsSummary(long totalOperations, long totalSuccessfulOperations, 
                                long totalFailedOperations, int uniqueOperationTypes, int uniqueUsers) {
            this.totalOperations = totalOperations;
            this.totalSuccessfulOperations = totalSuccessfulOperations;
            this.totalFailedOperations = totalFailedOperations;
            this.uniqueOperationTypes = uniqueOperationTypes;
            this.uniqueUsers = uniqueUsers;
        }

        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getTotalSuccessfulOperations() { return totalSuccessfulOperations; }
        public long getTotalFailedOperations() { return totalFailedOperations; }
        public int getUniqueOperationTypes() { return uniqueOperationTypes; }
        public int getUniqueUsers() { return uniqueUsers; }
        public double getGlobalSuccessRate() { 
            return totalOperations > 0 ? (double) totalSuccessfulOperations / totalOperations : 0.0; 
        }
    }
}