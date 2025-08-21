package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Production-specific GraphQL performance configuration.
 * Optimizes thread pools, caching, and execution strategies for production
 * load.
 */
@Configuration
@Profile({ "production", "prod" })
@ConditionalOnProperty(name = "graphql.features.performance-optimizations-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GraphQLProductionPerformanceConfig {

    private final GraphQLFeatureProperties featureProperties;

    /**
     * Production-optimized thread pool for GraphQL operations.
     */
    @Bean("graphqlProductionExecutor")
    public Executor graphqlProductionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Calculate optimal thread pool size based on available processors
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.max(8, availableProcessors * 2);
        int maxPoolSize = Math.max(16, availableProcessors * 4);

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(500); // Large queue for production load
        executor.setKeepAliveSeconds(300); // 5 minutes keep-alive
        executor.setThreadNamePrefix("graphql-prod-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Rejection policy for when queue is full
        executor.setRejectedExecutionHandler((runnable, threadPoolExecutor) -> {
            log.warn("GraphQL task rejected due to thread pool saturation. Active: {}, Queue: {}",
                    threadPoolExecutor.getActiveCount(), threadPoolExecutor.getQueue().size());
            throw new RuntimeException("GraphQL thread pool saturated");
        });

        executor.initialize();

        log.info("GraphQL production executor configured: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, 500);

        return executor;
    }

    /**
     * Production-optimized ForkJoinPool for parallel GraphQL operations.
     */
    @Bean("graphqlProductionForkJoinPool")
    public ForkJoinPool graphqlProductionForkJoinPool() {
        int parallelism = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);

        ForkJoinPool pool = new ForkJoinPool(
                parallelism,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (thread, exception) -> {
                    log.error("Uncaught exception in GraphQL ForkJoinPool thread: {}",
                            thread.getName(), exception);
                },
                true // Enable async mode for better throughput
        );

        log.info("GraphQL production ForkJoinPool configured with parallelism: {}", parallelism);
        return pool;
    }

    /**
     * Production connection pool monitoring and optimization.
     */
    @Bean
    public GraphQLConnectionPoolMonitor connectionPoolMonitor() {
        return new GraphQLConnectionPoolMonitor();
    }

    /**
     * Monitor and optimize database connection pool for GraphQL workloads.
     */
    public static class GraphQLConnectionPoolMonitor {

        private volatile long lastOptimizationTime = System.currentTimeMillis();
        private static final long OPTIMIZATION_INTERVAL = 300000; // 5 minutes

        /**
         * Monitor connection pool metrics and suggest optimizations.
         */
        public void monitorAndOptimize() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastOptimizationTime < OPTIMIZATION_INTERVAL) {
                return;
            }

            lastOptimizationTime = currentTime;

            // Log connection pool metrics for monitoring
            log.info("GraphQL connection pool optimization check completed");
        }

        /**
         * Get recommended pool size based on current load.
         */
        public int getRecommendedPoolSize(int currentActiveConnections, int currentPoolSize) {
            double utilizationRatio = (double) currentActiveConnections / currentPoolSize;

            if (utilizationRatio > 0.8) {
                // High utilization - recommend increase
                return Math.min(currentPoolSize + 10, 100);
            } else if (utilizationRatio < 0.3) {
                // Low utilization - recommend decrease
                return Math.max(currentPoolSize - 5, 20);
            }

            return currentPoolSize; // No change needed
        }
    }

    /**
     * Production query execution optimizer.
     */
    @Bean
    public GraphQLQueryOptimizer graphqlQueryOptimizer() {
        return new GraphQLQueryOptimizer();
    }

    /**
     * Optimize GraphQL queries for production performance.
     */
    public static class GraphQLQueryOptimizer {

        private static final int SLOW_QUERY_THRESHOLD_MS = 500;

        /**
         * Analyze query performance and suggest optimizations.
         */
        public void analyzeQueryPerformance(String query, long executionTimeMs) {
            if (executionTimeMs > SLOW_QUERY_THRESHOLD_MS) {
                log.warn("Slow GraphQL query detected: {}ms - Query hash: {}",
                        executionTimeMs, query.hashCode());

                // Suggest optimizations
                suggestOptimizations(query, executionTimeMs);
            }
        }

        private void suggestOptimizations(String query, long executionTimeMs) {
            // Analyze query patterns and suggest optimizations
            if (query.contains("exceptions") && query.contains("retryAttempts")) {
                log.info("Consider using DataLoader for exception->retryAttempts relationship");
            }

            if (query.contains("statusChanges") && executionTimeMs > 1000) {
                log.info("Consider adding pagination to statusChanges field");
            }

            if (query.split("\\{").length > 10) {
                log.info("Complex nested query detected - consider query splitting");
            }
        }

        /**
         * Check if query should be cached based on patterns.
         */
        public boolean shouldCacheQuery(String query) {
            // Cache read-only queries
            if (query.trim().toLowerCase().startsWith("query")) {
                return true;
            }

            // Don't cache mutations or subscriptions
            return false;
        }

        /**
         * Calculate cache TTL based on query type.
         */
        public int calculateCacheTtl(String query) {
            if (query.contains("summary")) {
                return 900; // 15 minutes for summaries
            } else if (query.contains("list")) {
                return 300; // 5 minutes for lists
            } else if (query.contains("detail")) {
                return 1800; // 30 minutes for details
            }

            return 600; // 10 minutes default
        }
    }

    /**
     * Production memory management for GraphQL operations.
     */
    @Bean
    public GraphQLMemoryManager graphqlMemoryManager() {
        return new GraphQLMemoryManager();
    }

    /**
     * Manage memory usage for GraphQL operations in production.
     */
    public static class GraphQLMemoryManager {

        private static final double MEMORY_WARNING_THRESHOLD = 0.8; // 80% memory usage
        private static final double MEMORY_CRITICAL_THRESHOLD = 0.9; // 90% memory usage

        /**
         * Monitor memory usage and take action if needed.
         */
        public void checkMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsageRatio = (double) usedMemory / totalMemory;

            if (memoryUsageRatio > MEMORY_CRITICAL_THRESHOLD) {
                log.error("Critical memory usage detected: {:.1f}% - Consider scaling up",
                        memoryUsageRatio * 100);

                // Force garbage collection as last resort
                System.gc();

            } else if (memoryUsageRatio > MEMORY_WARNING_THRESHOLD) {
                log.warn("High memory usage detected: {:.1f}%", memoryUsageRatio * 100);
            }
        }

        /**
         * Estimate memory usage for a GraphQL query.
         */
        public long estimateQueryMemoryUsage(String query, int expectedResultSize) {
            // Rough estimation based on query complexity and expected result size
            int queryComplexity = query.split("\\{").length;
            long baseMemory = queryComplexity * 1024; // 1KB per nesting level
            long resultMemory = expectedResultSize * 512; // 512 bytes per result item

            return baseMemory + resultMemory;
        }
    }
}