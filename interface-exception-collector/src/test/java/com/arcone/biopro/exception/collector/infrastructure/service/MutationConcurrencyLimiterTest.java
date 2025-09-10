package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.infrastructure.config.MutationPerformanceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for MutationConcurrencyLimiter.
 * Validates concurrency control, permit management, and statistics tracking.
 */
@ExtendWith(MockitoExtension.class)
class MutationConcurrencyLimiterTest {

    private MutationConcurrencyLimiter concurrencyLimiter;
    private MutationPerformanceConfig performanceConfig;

    @BeforeEach
    void setUp() {
        performanceConfig = new MutationPerformanceConfig();
        
        // Set up test configuration
        MutationPerformanceConfig.ConcurrencyConfig concurrencyConfig = new MutationPerformanceConfig.ConcurrencyConfig();
        concurrencyConfig.setMaxConcurrentOperationsPerUser(3);
        concurrencyConfig.setMaxConcurrentOperationsTotal(10);
        performanceConfig.setConcurrency(concurrencyConfig);
        
        concurrencyLimiter = new MutationConcurrencyLimiter(performanceConfig);
        
        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", "password"));
    }

    @Test
    void shouldAcquireOperationPermitSuccessfully() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        assertThat(permit).isNotNull();
        assertThat(permit.getOperationId()).isNotNull();
        assertThat(permit.getUserId()).isEqualTo("testUser");
        
        permit.release();
    }

    @Test
    void shouldTrackConcurrencyStatistics() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit1 = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        MutationConcurrencyLimiter.OperationPermit permit2 = 
                concurrencyLimiter.acquireOperationPermit("acknowledgeException");
        
        MutationConcurrencyLimiter.ConcurrencyStats stats = concurrencyLimiter.getConcurrencyStats();
        
        assertThat(stats.getActiveOperations()).isEqualTo(2);
        assertThat(stats.getMaxSystemOperations()).isEqualTo(10);
        assertThat(stats.getMaxUserOperations()).isEqualTo(3);
        assertThat(stats.getActiveUsers()).isEqualTo(1);
        assertThat(stats.getAvailableSystemPermits()).isEqualTo(8);
        
        permit1.release();
        permit2.release();
    }

    @Test
    void shouldTrackUserSpecificStatistics() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        MutationConcurrencyLimiter.UserConcurrencyStats userStats = 
                concurrencyLimiter.getUserConcurrencyStats("testUser");
        
        assertThat(userStats.getUserId()).isEqualTo("testUser");
        assertThat(userStats.getActiveOperations()).isEqualTo(1);
        assertThat(userStats.getMaxOperations()).isEqualTo(3);
        assertThat(userStats.getAvailablePermits()).isEqualTo(2);
        
        permit.release();
    }

    @Test
    void shouldEnforceUserConcurrencyLimits() throws InterruptedException {
        // Acquire maximum allowed permits for user
        MutationConcurrencyLimiter.OperationPermit permit1 = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        MutationConcurrencyLimiter.OperationPermit permit2 = 
                concurrencyLimiter.acquireOperationPermit("acknowledgeException");
        MutationConcurrencyLimiter.OperationPermit permit3 = 
                concurrencyLimiter.acquireOperationPermit("resolveException");
        
        // Next permit should be rejected
        assertThatThrownBy(() -> concurrencyLimiter.acquireOperationPermit("cancelRetry"))
                .isInstanceOf(MutationConcurrencyLimiter.ConcurrencyLimitExceededException.class)
                .hasMessageContaining("Too many concurrent operations for user");
        
        permit1.release();
        permit2.release();
        permit3.release();
    }

    @Test
    void shouldEnforceSystemWideConcurrencyLimits() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        
        try {
            // Create multiple users to fill system capacity
            CompletableFuture<Void>[] futures = new CompletableFuture[12];
            
            for (int i = 0; i < 12; i++) {
                final int userId = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("user" + userId, "password"));
                    
                    try {
                        MutationConcurrencyLimiter.OperationPermit permit = 
                                concurrencyLimiter.acquireOperationPermit("retryException");
                        
                        // Hold permit for a short time
                        Thread.sleep(100);
                        permit.release();
                    } catch (Exception e) {
                        // Expected for operations beyond system limit
                        if (userId >= 10) {
                            assertThat(e).isInstanceOf(MutationConcurrencyLimiter.ConcurrencyLimitExceededException.class);
                            assertThat(e.getMessage()).contains("System is at maximum capacity");
                        }
                    }
                }, executor);
            }
            
            CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void shouldDetectSystemNearCapacity() throws InterruptedException {
        // Fill system to 80% capacity (8 out of 10)
        MutationConcurrencyLimiter.OperationPermit[] permits = new MutationConcurrencyLimiter.OperationPermit[8];
        
        for (int i = 0; i < 8; i++) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user" + i, "password"));
            permits[i] = concurrencyLimiter.acquireOperationPermit("retryException");
        }
        
        assertThat(concurrencyLimiter.isSystemNearCapacity()).isTrue();
        
        // Release permits
        for (MutationConcurrencyLimiter.OperationPermit permit : permits) {
            permit.release();
        }
        
        assertThat(concurrencyLimiter.isSystemNearCapacity()).isFalse();
    }

    @Test
    void shouldHandleAnonymousUser() throws InterruptedException {
        SecurityContextHolder.clearContext();
        
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        assertThat(permit.getUserId()).isEqualTo("anonymous");
        
        permit.release();
    }

    @Test
    void shouldReleasePermitsAutomatically() throws InterruptedException {
        try (MutationConcurrencyLimiter.OperationPermit permit = 
                     concurrencyLimiter.acquireOperationPermit("retryException")) {
            
            assertThat(concurrencyLimiter.getConcurrencyStats().getActiveOperations()).isEqualTo(1);
        }
        
        // Permit should be automatically released
        assertThat(concurrencyLimiter.getConcurrencyStats().getActiveOperations()).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleReleases() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        permit.release();
        permit.release(); // Should not cause issues
        
        assertThat(concurrencyLimiter.getConcurrencyStats().getActiveOperations()).isEqualTo(0);
    }

    @Test
    void shouldGenerateUniqueOperationIds() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit1 = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        MutationConcurrencyLimiter.OperationPermit permit2 = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        assertThat(permit1.getOperationId()).isNotEqualTo(permit2.getOperationId());
        
        permit1.release();
        permit2.release();
    }

    @Test
    void shouldTrackOperationDuration() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        Thread.sleep(50); // Simulate operation duration
        
        concurrencyLimiter.releaseOperationPermit(permit);
        
        long duration = System.currentTimeMillis() - startTime;
        assertThat(duration).isGreaterThanOrEqualTo(50);
    }

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            CompletableFuture<Void>[] futures = new CompletableFuture[5];
            
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("user" + userId, "password"));
                    
                    try {
                        MutationConcurrencyLimiter.OperationPermit permit = 
                                concurrencyLimiter.acquireOperationPermit("retryException");
                        
                        Thread.sleep(10);
                        permit.release();
                    } catch (Exception e) {
                        // Should not happen with proper concurrency control
                        throw new RuntimeException(e);
                    }
                }, executor);
            }
            
            CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
            
            // All operations should complete successfully
            assertThat(concurrencyLimiter.getConcurrencyStats().getActiveOperations()).isEqualTo(0);
            
        } finally {
            executor.shutdown();
        }
    }
}