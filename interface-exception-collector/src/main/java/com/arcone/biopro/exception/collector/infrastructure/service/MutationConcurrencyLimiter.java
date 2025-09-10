package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.infrastructure.config.MutationPerformanceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing concurrency limits on GraphQL mutation operations.
 * Enforces per-user and system-wide concurrency constraints to prevent overload.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutationConcurrencyLimiter {

    private final MutationPerformanceConfig performanceConfig;
    
    // System-wide semaphore for total concurrent operations
    private final Semaphore systemSemaphore;
    
    // Per-user semaphores for user-specific limits
    private final ConcurrentHashMap<String, Semaphore> userSemaphores = new ConcurrentHashMap<>();
    
    // Tracking active operations
    private final AtomicInteger activeOperations = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> userActiveOperations = new ConcurrentHashMap<>();
    
    // Operation tracking for monitoring
    private final ConcurrentHashMap<String, OperationInfo> activeOperationDetails = new ConcurrentHashMap<>();

    public MutationConcurrencyLimiter(MutationPerformanceConfig performanceConfig) {
        this.performanceConfig = performanceConfig;
        this.systemSemaphore = new Semaphore(
                performanceConfig.getConcurrency().getMaxConcurrentOperationsTotal(), true);
    }

    /**
     * Acquires permission to execute a mutation operation
     */
    public OperationPermit acquireOperationPermit(String operationType) throws InterruptedException {
        String userId = getCurrentUserId();
        String operationId = generateOperationId(operationType, userId);
        
        log.debug("Attempting to acquire permit for operation: {} (user: {})", operationType, userId);
        
        // Try to acquire system-wide permit first
        boolean systemPermitAcquired = systemSemaphore.tryAcquire(5, TimeUnit.SECONDS);
        if (!systemPermitAcquired) {
            log.warn("System concurrency limit reached. Rejecting operation: {} (user: {})", 
                    operationType, userId);
            throw new ConcurrencyLimitExceededException(
                    "System is at maximum capacity. Please try again later.");
        }
        
        try {
            // Try to acquire user-specific permit
            Semaphore userSemaphore = getUserSemaphore(userId);
            boolean userPermitAcquired = userSemaphore.tryAcquire(2, TimeUnit.SECONDS);
            
            if (!userPermitAcquired) {
                systemSemaphore.release(); // Release system permit
                log.warn("User concurrency limit reached for user: {} operation: {}", userId, operationType);
                throw new ConcurrencyLimitExceededException(
                        "Too many concurrent operations for user. Please wait for existing operations to complete.");
            }
            
            // Track the operation
            OperationInfo operationInfo = new OperationInfo(operationId, operationType, userId, Instant.now());
            activeOperationDetails.put(operationId, operationInfo);
            
            // Update counters
            activeOperations.incrementAndGet();
            userActiveOperations.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
            
            log.info("Acquired permits for operation: {} (user: {}, active: system={}, user={})",
                    operationType, userId, activeOperations.get(), 
                    userActiveOperations.get(userId).get());
            
            return new OperationPermit(operationId, userId, systemSemaphore, userSemaphore);
            
        } catch (Exception e) {
            systemSemaphore.release(); // Ensure system permit is released on error
            throw e;
        }
    }

    /**
     * Gets or creates a semaphore for a specific user
     */
    private Semaphore getUserSemaphore(String userId) {
        return userSemaphores.computeIfAbsent(userId, 
                k -> new Semaphore(performanceConfig.getConcurrency().getMaxConcurrentOperationsPerUser(), true));
    }

    /**
     * Gets the current user ID from security context
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    /**
     * Generates a unique operation ID
     */
    private String generateOperationId(String operationType, String userId) {
        return String.format("%s-%s-%d", operationType, userId, System.nanoTime());
    }

    /**
     * Releases operation permits and updates tracking
     */
    public void releaseOperationPermit(OperationPermit permit) {
        try {
            // Remove from tracking
            OperationInfo operationInfo = activeOperationDetails.remove(permit.getOperationId());
            if (operationInfo != null) {
                long durationMs = java.time.Duration.between(operationInfo.getStartTime(), Instant.now()).toMillis();
                log.info("Released permits for operation: {} (user: {}, duration: {}ms)",
                        operationInfo.getOperationType(), permit.getUserId(), durationMs);
            }
            
            // Update counters
            activeOperations.decrementAndGet();
            AtomicInteger userCounter = userActiveOperations.get(permit.getUserId());
            if (userCounter != null) {
                userCounter.decrementAndGet();
            }
            
            // Release permits
            permit.release();
            
        } catch (Exception e) {
            log.error("Error releasing operation permit: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets current system concurrency statistics
     */
    public ConcurrencyStats getConcurrencyStats() {
        return ConcurrencyStats.builder()
                .activeOperations(activeOperations.get())
                .maxSystemOperations(performanceConfig.getConcurrency().getMaxConcurrentOperationsTotal())
                .maxUserOperations(performanceConfig.getConcurrency().getMaxConcurrentOperationsPerUser())
                .activeUsers(userActiveOperations.size())
                .availableSystemPermits(systemSemaphore.availablePermits())
                .build();
    }

    /**
     * Gets concurrency statistics for a specific user
     */
    public UserConcurrencyStats getUserConcurrencyStats(String userId) {
        AtomicInteger userCounter = userActiveOperations.get(userId);
        Semaphore userSemaphore = userSemaphores.get(userId);
        
        return UserConcurrencyStats.builder()
                .userId(userId)
                .activeOperations(userCounter != null ? userCounter.get() : 0)
                .maxOperations(performanceConfig.getConcurrency().getMaxConcurrentOperationsPerUser())
                .availablePermits(userSemaphore != null ? userSemaphore.availablePermits() : 
                        performanceConfig.getConcurrency().getMaxConcurrentOperationsPerUser())
                .build();
    }

    /**
     * Checks if system is approaching capacity limits
     */
    public boolean isSystemNearCapacity() {
        int maxOps = performanceConfig.getConcurrency().getMaxConcurrentOperationsTotal();
        int currentOps = activeOperations.get();
        return (double) currentOps / maxOps > 0.8; // 80% threshold
    }

    /**
     * Operation permit that manages semaphore releases
     */
    public static class OperationPermit implements AutoCloseable {
        private final String operationId;
        private final String userId;
        private final Semaphore systemSemaphore;
        private final Semaphore userSemaphore;
        private boolean released = false;

        public OperationPermit(String operationId, String userId, 
                              Semaphore systemSemaphore, Semaphore userSemaphore) {
            this.operationId = operationId;
            this.userId = userId;
            this.systemSemaphore = systemSemaphore;
            this.userSemaphore = userSemaphore;
        }

        public void release() {
            if (!released) {
                userSemaphore.release();
                systemSemaphore.release();
                released = true;
            }
        }

        @Override
        public void close() {
            release();
        }

        public String getOperationId() { return operationId; }
        public String getUserId() { return userId; }
    }

    /**
     * Information about an active operation
     */
    private static class OperationInfo {
        private final String operationId;
        private final String operationType;
        private final String userId;
        private final Instant startTime;

        public OperationInfo(String operationId, String operationType, String userId, Instant startTime) {
            this.operationId = operationId;
            this.operationType = operationType;
            this.userId = userId;
            this.startTime = startTime;
        }

        public String getOperationType() { return operationType; }
        public Instant getStartTime() { return startTime; }
    }

    /**
     * System-wide concurrency statistics
     */
    public static class ConcurrencyStats {
        private final int activeOperations;
        private final int maxSystemOperations;
        private final int maxUserOperations;
        private final int activeUsers;
        private final int availableSystemPermits;

        private ConcurrencyStats(int activeOperations, int maxSystemOperations, 
                               int maxUserOperations, int activeUsers, int availableSystemPermits) {
            this.activeOperations = activeOperations;
            this.maxSystemOperations = maxSystemOperations;
            this.maxUserOperations = maxUserOperations;
            this.activeUsers = activeUsers;
            this.availableSystemPermits = availableSystemPermits;
        }

        public static ConcurrencyStatsBuilder builder() {
            return new ConcurrencyStatsBuilder();
        }

        // Getters
        public int getActiveOperations() { return activeOperations; }
        public int getMaxSystemOperations() { return maxSystemOperations; }
        public int getMaxUserOperations() { return maxUserOperations; }
        public int getActiveUsers() { return activeUsers; }
        public int getAvailableSystemPermits() { return availableSystemPermits; }

        public static class ConcurrencyStatsBuilder {
            private int activeOperations;
            private int maxSystemOperations;
            private int maxUserOperations;
            private int activeUsers;
            private int availableSystemPermits;

            public ConcurrencyStatsBuilder activeOperations(int activeOperations) {
                this.activeOperations = activeOperations;
                return this;
            }

            public ConcurrencyStatsBuilder maxSystemOperations(int maxSystemOperations) {
                this.maxSystemOperations = maxSystemOperations;
                return this;
            }

            public ConcurrencyStatsBuilder maxUserOperations(int maxUserOperations) {
                this.maxUserOperations = maxUserOperations;
                return this;
            }

            public ConcurrencyStatsBuilder activeUsers(int activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }

            public ConcurrencyStatsBuilder availableSystemPermits(int availableSystemPermits) {
                this.availableSystemPermits = availableSystemPermits;
                return this;
            }

            public ConcurrencyStats build() {
                return new ConcurrencyStats(activeOperations, maxSystemOperations, 
                        maxUserOperations, activeUsers, availableSystemPermits);
            }
        }
    }

    /**
     * User-specific concurrency statistics
     */
    public static class UserConcurrencyStats {
        private final String userId;
        private final int activeOperations;
        private final int maxOperations;
        private final int availablePermits;

        private UserConcurrencyStats(String userId, int activeOperations, 
                                   int maxOperations, int availablePermits) {
            this.userId = userId;
            this.activeOperations = activeOperations;
            this.maxOperations = maxOperations;
            this.availablePermits = availablePermits;
        }

        public static UserConcurrencyStatsBuilder builder() {
            return new UserConcurrencyStatsBuilder();
        }

        // Getters
        public String getUserId() { return userId; }
        public int getActiveOperations() { return activeOperations; }
        public int getMaxOperations() { return maxOperations; }
        public int getAvailablePermits() { return availablePermits; }

        public static class UserConcurrencyStatsBuilder {
            private String userId;
            private int activeOperations;
            private int maxOperations;
            private int availablePermits;

            public UserConcurrencyStatsBuilder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public UserConcurrencyStatsBuilder activeOperations(int activeOperations) {
                this.activeOperations = activeOperations;
                return this;
            }

            public UserConcurrencyStatsBuilder maxOperations(int maxOperations) {
                this.maxOperations = maxOperations;
                return this;
            }

            public UserConcurrencyStatsBuilder availablePermits(int availablePermits) {
                this.availablePermits = availablePermits;
                return this;
            }

            public UserConcurrencyStats build() {
                return new UserConcurrencyStats(userId, activeOperations, maxOperations, availablePermits);
            }
        }
    }

    /**
     * Exception thrown when concurrency limits are exceeded
     */
    public static class ConcurrencyLimitExceededException extends RuntimeException {
        public ConcurrencyLimitExceededException(String message) {
            super(message);
        }
    }
}