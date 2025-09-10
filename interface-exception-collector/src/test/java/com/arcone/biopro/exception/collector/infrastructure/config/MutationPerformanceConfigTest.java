package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for MutationPerformanceConfig.
 * Validates configuration loading, validation, and thread pool creation.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "graphql.mutation.performance.timeout.operation-timeout=45s",
        "graphql.mutation.performance.timeout.query-timeout=15s",
        "graphql.mutation.performance.timeout.validation-timeout=8s",
        "graphql.mutation.performance.timeout.audit-timeout=5s",
        "graphql.mutation.performance.concurrency.max-concurrent-operations-per-user=10",
        "graphql.mutation.performance.concurrency.max-concurrent-operations-total=200",
        "graphql.mutation.performance.concurrency.core-pool-size=15",
        "graphql.mutation.performance.concurrency.max-pool-size=75",
        "graphql.mutation.performance.concurrency.queue-capacity=300",
        "graphql.mutation.performance.concurrency.keep-alive-seconds=90",
        "graphql.mutation.performance.database.max-connections=30",
        "graphql.mutation.performance.database.min-idle-connections=8",
        "graphql.mutation.performance.database.connection-timeout-ms=8000",
        "graphql.mutation.performance.database.max-lifetime-ms=2400000",
        "graphql.mutation.performance.database.leak-detection-threshold-ms=90000",
        "graphql.mutation.performance.jvm.initial-heap-size-mb=1024",
        "graphql.mutation.performance.jvm.max-heap-size-mb=4096",
        "graphql.mutation.performance.jvm.gc-optimization-enabled=true",
        "graphql.mutation.performance.jvm.g1-gc-enabled=true",
        "graphql.mutation.performance.jvm.max-gc-pause-ms=150"
})
class MutationPerformanceConfigTest {

    private MutationPerformanceConfig config;

    @BeforeEach
    void setUp() {
        config = new MutationPerformanceConfig();
        
        // Set up test configuration values
        MutationPerformanceConfig.TimeoutConfig timeoutConfig = new MutationPerformanceConfig.TimeoutConfig();
        timeoutConfig.setOperationTimeout(Duration.ofSeconds(45));
        timeoutConfig.setQueryTimeout(Duration.ofSeconds(15));
        timeoutConfig.setValidationTimeout(Duration.ofSeconds(8));
        timeoutConfig.setAuditTimeout(Duration.ofSeconds(5));
        config.setTimeout(timeoutConfig);

        MutationPerformanceConfig.ConcurrencyConfig concurrencyConfig = new MutationPerformanceConfig.ConcurrencyConfig();
        concurrencyConfig.setMaxConcurrentOperationsPerUser(10);
        concurrencyConfig.setMaxConcurrentOperationsTotal(200);
        concurrencyConfig.setCorePoolSize(15);
        concurrencyConfig.setMaxPoolSize(75);
        concurrencyConfig.setQueueCapacity(300);
        concurrencyConfig.setKeepAliveSeconds(90);
        config.setConcurrency(concurrencyConfig);

        MutationPerformanceConfig.DatabaseConfig databaseConfig = new MutationPerformanceConfig.DatabaseConfig();
        databaseConfig.setMaxConnections(30);
        databaseConfig.setMinIdleConnections(8);
        databaseConfig.setConnectionTimeoutMs(8000);
        databaseConfig.setMaxLifetimeMs(2400000);
        databaseConfig.setLeakDetectionThresholdMs(90000);
        config.setDatabase(databaseConfig);

        MutationPerformanceConfig.JvmConfig jvmConfig = new MutationPerformanceConfig.JvmConfig();
        jvmConfig.setInitialHeapSizeMb(1024);
        jvmConfig.setMaxHeapSizeMb(4096);
        jvmConfig.setGcOptimizationEnabled(true);
        jvmConfig.setG1GcEnabled(true);
        jvmConfig.setMaxGcPauseMs(150);
        config.setJvm(jvmConfig);
    }

    @Test
    void shouldLoadTimeoutConfiguration() {
        MutationPerformanceConfig.TimeoutConfig timeoutConfig = config.getTimeout();
        
        assertThat(timeoutConfig.getOperationTimeout()).isEqualTo(Duration.ofSeconds(45));
        assertThat(timeoutConfig.getQueryTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(timeoutConfig.getValidationTimeout()).isEqualTo(Duration.ofSeconds(8));
        assertThat(timeoutConfig.getAuditTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void shouldLoadConcurrencyConfiguration() {
        MutationPerformanceConfig.ConcurrencyConfig concurrencyConfig = config.getConcurrency();
        
        assertThat(concurrencyConfig.getMaxConcurrentOperationsPerUser()).isEqualTo(10);
        assertThat(concurrencyConfig.getMaxConcurrentOperationsTotal()).isEqualTo(200);
        assertThat(concurrencyConfig.getCorePoolSize()).isEqualTo(15);
        assertThat(concurrencyConfig.getMaxPoolSize()).isEqualTo(75);
        assertThat(concurrencyConfig.getQueueCapacity()).isEqualTo(300);
        assertThat(concurrencyConfig.getKeepAliveSeconds()).isEqualTo(90);
    }

    @Test
    void shouldLoadDatabaseConfiguration() {
        MutationPerformanceConfig.DatabaseConfig databaseConfig = config.getDatabase();
        
        assertThat(databaseConfig.getMaxConnections()).isEqualTo(30);
        assertThat(databaseConfig.getMinIdleConnections()).isEqualTo(8);
        assertThat(databaseConfig.getConnectionTimeoutMs()).isEqualTo(8000);
        assertThat(databaseConfig.getMaxLifetimeMs()).isEqualTo(2400000);
        assertThat(databaseConfig.getLeakDetectionThresholdMs()).isEqualTo(90000);
    }

    @Test
    void shouldLoadJvmConfiguration() {
        MutationPerformanceConfig.JvmConfig jvmConfig = config.getJvm();
        
        assertThat(jvmConfig.getInitialHeapSizeMb()).isEqualTo(1024);
        assertThat(jvmConfig.getMaxHeapSizeMb()).isEqualTo(4096);
        assertThat(jvmConfig.isGcOptimizationEnabled()).isTrue();
        assertThat(jvmConfig.isG1GcEnabled()).isTrue();
        assertThat(jvmConfig.getMaxGcPauseMs()).isEqualTo(150);
    }

    @Test
    void shouldCreateMutationExecutor() {
        Executor executor = config.mutationExecutor();
        
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolExecutor.class);
        
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        assertThat(threadPoolExecutor.getCorePoolSize()).isEqualTo(15);
        assertThat(threadPoolExecutor.getMaximumPoolSize()).isEqualTo(75);
        assertThat(threadPoolExecutor.getQueue().remainingCapacity()).isEqualTo(300);
    }

    @Test
    void shouldValidateConfigurationSuccessfully() {
        // Should not throw any exceptions
        config.validateConfiguration();
    }

    @Test
    void shouldHaveDefaultValues() {
        MutationPerformanceConfig defaultConfig = new MutationPerformanceConfig();
        
        // Timeout defaults
        assertThat(defaultConfig.getTimeout().getOperationTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(defaultConfig.getTimeout().getQueryTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(defaultConfig.getTimeout().getValidationTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(defaultConfig.getTimeout().getAuditTimeout()).isEqualTo(Duration.ofSeconds(3));
        
        // Concurrency defaults
        assertThat(defaultConfig.getConcurrency().getMaxConcurrentOperationsPerUser()).isEqualTo(5);
        assertThat(defaultConfig.getConcurrency().getMaxConcurrentOperationsTotal()).isEqualTo(100);
        assertThat(defaultConfig.getConcurrency().getCorePoolSize()).isEqualTo(10);
        assertThat(defaultConfig.getConcurrency().getMaxPoolSize()).isEqualTo(50);
        assertThat(defaultConfig.getConcurrency().getQueueCapacity()).isEqualTo(200);
        assertThat(defaultConfig.getConcurrency().getKeepAliveSeconds()).isEqualTo(60);
        
        // Database defaults
        assertThat(defaultConfig.getDatabase().getMaxConnections()).isEqualTo(20);
        assertThat(defaultConfig.getDatabase().getMinIdleConnections()).isEqualTo(5);
        assertThat(defaultConfig.getDatabase().getConnectionTimeoutMs()).isEqualTo(5000);
        assertThat(defaultConfig.getDatabase().getMaxLifetimeMs()).isEqualTo(1800000);
        assertThat(defaultConfig.getDatabase().getLeakDetectionThresholdMs()).isEqualTo(60000);
        
        // JVM defaults
        assertThat(defaultConfig.getJvm().getInitialHeapSizeMb()).isEqualTo(512);
        assertThat(defaultConfig.getJvm().getMaxHeapSizeMb()).isEqualTo(2048);
        assertThat(defaultConfig.getJvm().isGcOptimizationEnabled()).isTrue();
        assertThat(defaultConfig.getJvm().isG1GcEnabled()).isTrue();
        assertThat(defaultConfig.getJvm().getMaxGcPauseMs()).isEqualTo(200);
    }

    @Test
    void shouldCreateExecutorWithCorrectThreadNamePrefix() {
        Executor executor = config.mutationExecutor();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        
        // Submit a task to verify thread naming
        threadPoolExecutor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            assertThat(threadName).startsWith("mutation-");
        });
    }

    @Test
    void shouldHandleRejectedExecutionPolicy() {
        // Create a small pool to test rejection
        MutationPerformanceConfig.ConcurrencyConfig smallConfig = new MutationPerformanceConfig.ConcurrencyConfig();
        smallConfig.setCorePoolSize(1);
        smallConfig.setMaxPoolSize(1);
        smallConfig.setQueueCapacity(1);
        smallConfig.setKeepAliveSeconds(60);
        
        MutationPerformanceConfig testConfig = new MutationPerformanceConfig();
        testConfig.setConcurrency(smallConfig);
        
        Executor executor = testConfig.mutationExecutor();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        
        // Fill up the pool and queue
        for (int i = 0; i < 3; i++) {
            threadPoolExecutor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // This should trigger the rejection handler
        try {
            threadPoolExecutor.submit(() -> {});
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Mutation operation rejected - system overloaded");
        }
    }
}