package com.arcone.biopro.exception.collector.infrastructure;

import com.arcone.biopro.exception.collector.infrastructure.config.MutationPerformanceConfig;
import com.arcone.biopro.exception.collector.infrastructure.interceptor.MutationTimeoutInterceptor;
import com.arcone.biopro.exception.collector.infrastructure.service.MutationConcurrencyLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for mutation performance optimization components.
 * Validates that all performance-related beans are properly configured and work together.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "graphql.mutation.performance.timeout.operation-timeout=25s",
        "graphql.mutation.performance.timeout.query-timeout=8s",
        "graphql.mutation.performance.concurrency.max-concurrent-operations-total=50",
        "graphql.mutation.performance.concurrency.max-concurrent-operations-per-user=3",
        "graphql.mutation.performance.concurrency.core-pool-size=8",
        "graphql.mutation.performance.concurrency.max-pool-size=25",
        "graphql.mutation.performance.database.max-connections=15",
        "graphql.mutation.performance.database.optimization-enabled=true",
        "graphql.mutation.performance.jvm.gc-optimization-enabled=true"
})
class MutationPerformanceIntegrationTest {

    @Autowired
    private MutationPerformanceConfig performanceConfig;

    @Autowired
    private MutationConcurrencyLimiter concurrencyLimiter;

    @Autowired
    private MutationTimeoutInterceptor timeoutInterceptor;

    @Autowired
    private Executor mutationExecutor;

    @Test
    void shouldLoadPerformanceConfiguration() {
        assertThat(performanceConfig).isNotNull();
        
        // Verify timeout configuration
        assertThat(performanceConfig.getTimeout().getOperationTimeout())
                .isEqualTo(Duration.ofSeconds(25));
        assertThat(performanceConfig.getTimeout().getQueryTimeout())
                .isEqualTo(Duration.ofSeconds(8));
        
        // Verify concurrency configuration
        assertThat(performanceConfig.getConcurrency().getMaxConcurrentOperationsTotal())
                .isEqualTo(50);
        assertThat(performanceConfig.getConcurrency().getMaxConcurrentOperationsPerUser())
                .isEqualTo(3);
        assertThat(performanceConfig.getConcurrency().getCorePoolSize())
                .isEqualTo(8);
        assertThat(performanceConfig.getConcurrency().getMaxPoolSize())
                .isEqualTo(25);
        
        // Verify database configuration
        assertThat(performanceConfig.getDatabase().getMaxConnections())
                .isEqualTo(15);
        
        // Verify JVM configuration
        assertThat(performanceConfig.getJvm().isGcOptimizationEnabled())
                .isTrue();
    }

    @Test
    void shouldCreateConcurrencyLimiter() {
        assertThat(concurrencyLimiter).isNotNull();
        
        MutationConcurrencyLimiter.ConcurrencyStats stats = concurrencyLimiter.getConcurrencyStats();
        assertThat(stats.getMaxSystemOperations()).isEqualTo(50);
        assertThat(stats.getMaxUserOperations()).isEqualTo(3);
        assertThat(stats.getActiveOperations()).isEqualTo(0);
    }

    @Test
    void shouldCreateTimeoutInterceptor() {
        assertThat(timeoutInterceptor).isNotNull();
    }

    @Test
    void shouldCreateMutationExecutor() {
        assertThat(mutationExecutor).isNotNull();
        assertThat(mutationExecutor).isInstanceOf(ThreadPoolExecutor.class);
        
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) mutationExecutor;
        assertThat(threadPoolExecutor.getCorePoolSize()).isEqualTo(8);
        assertThat(threadPoolExecutor.getMaximumPoolSize()).isEqualTo(25);
    }

    @Test
    void shouldValidateConfigurationOnStartup() {
        // Configuration validation should pass without exceptions
        performanceConfig.validateConfiguration();
    }

    @Test
    void shouldIntegrateWithConcurrencyLimiter() throws InterruptedException {
        // Test that concurrency limiter works with the configured limits
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        assertThat(permit).isNotNull();
        
        MutationConcurrencyLimiter.ConcurrencyStats stats = concurrencyLimiter.getConcurrencyStats();
        assertThat(stats.getActiveOperations()).isEqualTo(1);
        assertThat(stats.getAvailableSystemPermits()).isEqualTo(49);
        
        permit.release();
        
        stats = concurrencyLimiter.getConcurrencyStats();
        assertThat(stats.getActiveOperations()).isEqualTo(0);
        assertThat(stats.getAvailableSystemPermits()).isEqualTo(50);
    }

    @Test
    void shouldDetectSystemCapacity() throws InterruptedException {
        // Initially system should not be near capacity
        assertThat(concurrencyLimiter.isSystemNearCapacity()).isFalse();
        
        // Acquire permits to reach 80% capacity (40 out of 50)
        MutationConcurrencyLimiter.OperationPermit[] permits = 
                new MutationConcurrencyLimiter.OperationPermit[40];
        
        for (int i = 0; i < 40; i++) {
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
    void shouldExecuteTasksInMutationExecutor() throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) mutationExecutor;
        
        int initialCompletedTasks = (int) executor.getCompletedTaskCount();
        
        // Submit a task
        executor.submit(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Wait for task completion
        Thread.sleep(50);
        
        assertThat(executor.getCompletedTaskCount()).isGreaterThan(initialCompletedTasks);
    }

    @Test
    void shouldHandleMultipleConcurrentOperations() throws InterruptedException {
        int numberOfOperations = 5;
        MutationConcurrencyLimiter.OperationPermit[] permits = 
                new MutationConcurrencyLimiter.OperationPermit[numberOfOperations];
        
        // Acquire multiple permits
        for (int i = 0; i < numberOfOperations; i++) {
            permits[i] = concurrencyLimiter.acquireOperationPermit("retryException");
        }
        
        MutationConcurrencyLimiter.ConcurrencyStats stats = concurrencyLimiter.getConcurrencyStats();
        assertThat(stats.getActiveOperations()).isEqualTo(numberOfOperations);
        
        // Release all permits
        for (MutationConcurrencyLimiter.OperationPermit permit : permits) {
            permit.release();
        }
        
        stats = concurrencyLimiter.getConcurrencyStats();
        assertThat(stats.getActiveOperations()).isEqualTo(0);
    }

    @Test
    void shouldProvideUserSpecificStatistics() throws InterruptedException {
        MutationConcurrencyLimiter.OperationPermit permit = 
                concurrencyLimiter.acquireOperationPermit("retryException");
        
        MutationConcurrencyLimiter.UserConcurrencyStats userStats = 
                concurrencyLimiter.getUserConcurrencyStats("anonymous");
        
        assertThat(userStats.getActiveOperations()).isEqualTo(1);
        assertThat(userStats.getMaxOperations()).isEqualTo(3);
        assertThat(userStats.getAvailablePermits()).isEqualTo(2);
        
        permit.release();
        
        userStats = concurrencyLimiter.getUserConcurrencyStats("anonymous");
        assertThat(userStats.getActiveOperations()).isEqualTo(0);
        assertThat(userStats.getAvailablePermits()).isEqualTo(3);
    }

    @Test
    void shouldMaintainThreadPoolHealth() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) mutationExecutor;
        
        assertThat(executor.isShutdown()).isFalse();
        assertThat(executor.isTerminated()).isFalse();
        assertThat(executor.getCorePoolSize()).isEqualTo(8);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(25);
        assertThat(executor.getActiveCount()).isGreaterThanOrEqualTo(0);
    }
}