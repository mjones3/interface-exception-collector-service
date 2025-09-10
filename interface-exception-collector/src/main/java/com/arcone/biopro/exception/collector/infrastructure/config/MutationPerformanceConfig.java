package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Configuration for GraphQL mutation performance optimization.
 * Provides timeout settings, concurrency limits, and thread pool configuration
 * specifically tuned for GraphQL mutation operations.
 */
@Configuration
@ConfigurationProperties(prefix = "graphql.mutation.performance")
@Data
@Slf4j
public class MutationPerformanceConfig {

    /**
     * Timeout configuration for mutation operations
     */
    private TimeoutConfig timeout = new TimeoutConfig();

    /**
     * Concurrency limits for mutation operations
     */
    private ConcurrencyConfig concurrency = new ConcurrencyConfig();

    /**
     * Database connection pool configuration for mutations
     */
    private DatabaseConfig database = new DatabaseConfig();

    /**
     * JVM tuning parameters for GraphQL mutations
     */
    private JvmConfig jvm = new JvmConfig();

    @Data
    public static class TimeoutConfig {
        /**
         * Maximum time allowed for a single mutation operation
         */
        private Duration operationTimeout = Duration.ofSeconds(30);

        /**
         * Timeout for database queries within mutations
         */
        private Duration queryTimeout = Duration.ofSeconds(10);

        /**
         * Timeout for validation operations
         */
        private Duration validationTimeout = Duration.ofSeconds(5);

        /**
         * Timeout for audit logging operations
         */
        private Duration auditTimeout = Duration.ofSeconds(3);
    }

    @Data
    public static class ConcurrencyConfig {
        /**
         * Maximum number of concurrent mutation operations per user
         */
        private int maxConcurrentOperationsPerUser = 5;

        /**
         * Maximum number of concurrent mutation operations system-wide
         */
        private int maxConcurrentOperationsTotal = 100;

        /**
         * Core thread pool size for mutation operations
         */
        private int corePoolSize = 10;

        /**
         * Maximum thread pool size for mutation operations
         */
        private int maxPoolSize = 50;

        /**
         * Queue capacity for pending mutation operations
         */
        private int queueCapacity = 200;

        /**
         * Thread keep-alive time in seconds
         */
        private int keepAliveSeconds = 60;
    }

    @Data
    public static class DatabaseConfig {
        /**
         * Maximum number of database connections for mutation operations
         */
        private int maxConnections = 20;

        /**
         * Minimum number of idle connections
         */
        private int minIdleConnections = 5;

        /**
         * Connection timeout in milliseconds
         */
        private long connectionTimeoutMs = 5000;

        /**
         * Maximum lifetime of a connection in milliseconds
         */
        private long maxLifetimeMs = 1800000; // 30 minutes

        /**
         * Leak detection threshold in milliseconds
         */
        private long leakDetectionThresholdMs = 60000; // 1 minute
    }

    @Data
    public static class JvmConfig {
        /**
         * Initial heap size for GraphQL operations (in MB)
         */
        private int initialHeapSizeMb = 512;

        /**
         * Maximum heap size for GraphQL operations (in MB)
         */
        private int maxHeapSizeMb = 2048;

        /**
         * Garbage collection optimization enabled
         */
        private boolean gcOptimizationEnabled = true;

        /**
         * G1GC enabled for better latency
         */
        private boolean g1GcEnabled = true;

        /**
         * Maximum GC pause time target in milliseconds
         */
        private int maxGcPauseMs = 200;
    }

    /**
     * Creates a dedicated thread pool executor for GraphQL mutation operations
     */
    @Bean("mutationExecutor")
    public Executor mutationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency.getCorePoolSize());
        executor.setMaxPoolSize(concurrency.getMaxPoolSize());
        executor.setQueueCapacity(concurrency.getQueueCapacity());
        executor.setKeepAliveSeconds(concurrency.getKeepAliveSeconds());
        executor.setThreadNamePrefix("mutation-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Mutation operation rejected due to thread pool exhaustion. " +
                    "Consider increasing pool size or implementing backpressure.");
            throw new RuntimeException("Mutation operation rejected - system overloaded");
        });
        executor.initialize();
        
        log.info("Initialized mutation thread pool with core={}, max={}, queue={}",
                concurrency.getCorePoolSize(),
                concurrency.getMaxPoolSize(),
                concurrency.getQueueCapacity());
        
        return executor;
    }

    /**
     * Validates configuration values on startup
     */
    public void validateConfiguration() {
        if (timeout.getOperationTimeout().toSeconds() > 300) {
            log.warn("Operation timeout is very high ({}s). Consider reducing for better user experience.",
                    timeout.getOperationTimeout().toSeconds());
        }

        if (concurrency.getMaxConcurrentOperationsTotal() > 500) {
            log.warn("Maximum concurrent operations is very high ({}). Ensure system can handle the load.",
                    concurrency.getMaxConcurrentOperationsTotal());
        }

        if (database.getMaxConnections() > 50) {
            log.warn("Database connection pool is large ({}). Monitor connection usage.",
                    database.getMaxConnections());
        }

        log.info("Mutation performance configuration validated successfully");
    }
}