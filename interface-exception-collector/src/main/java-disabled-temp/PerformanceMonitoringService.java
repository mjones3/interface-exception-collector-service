package com.arcone.biopro.exception.collector.api.graphql.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for monitoring GraphQL API performance metrics.
 * Tracks database connection pool usage, cache hit rates,
 * and query performance to validate optimization effectiveness.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringService {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final CacheManager cacheManager;

    // Performance counters
    private Counter queryExecutionCounter;
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;
    private Counter dataLoaderBatchCounter;
    private Timer queryExecutionTimer;
    private Timer dataLoaderExecutionTimer;
    private Timer cacheOperationTimer;

    // Performance gauges
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);

    @PostConstruct
    public void initializeMetrics() {
        log.info("Initializing GraphQL performance monitoring metrics");

        // Query execution metrics
        queryExecutionCounter = Counter.builder("graphql.query.executions")
                .description("Total number of GraphQL query executions")
                .tag("component", "graphql")
                .register(meterRegistry);

        queryExecutionTimer = Timer.builder("graphql.query.duration")
                .description("GraphQL query execution duration")
                .tag("component", "graphql")
                .register(meterRegistry);

        // Cache performance metrics
        cacheHitCounter = Counter.builder("graphql.cache.hits")
                .description("Number of cache hits")
                .tag("component", "cache")
                .register(meterRegistry);

        cacheMissCounter = Counter.builder("graphql.cache.misses")
                .description("Number of cache misses")
                .tag("component", "cache")
                .register(meterRegistry);

        cacheOperationTimer = Timer.builder("graphql.cache.operation.duration")
                .description("Cache operation duration")
                .tag("component", "cache")
                .register(meterRegistry);

        // DataLoader performance metrics
        dataLoaderBatchCounter = Counter.builder("graphql.dataloader.batches")
                .description("Number of DataLoader batch operations")
                .tag("component", "dataloader")
                .register(meterRegistry);

        dataLoaderExecutionTimer = Timer.builder("graphql.dataloader.duration")
                .description("DataLoader execution duration")
                .tag("component", "dataloader")
                .register(meterRegistry);

        // Database connection pool metrics
        Gauge.builder("graphql.database.connections.active")
                .description("Number of active database connections")
                .tag("component", "database")
                .register(meterRegistry, this, PerformanceMonitoringService::getActiveConnections);

        Gauge.builder("graphql.database.connections.idle")
                .description("Number of idle database connections")
                .tag("component", "database")
                .register(meterRegistry, this, PerformanceMonitoringService::getIdleConnections);

        // Query performance gauges
        Gauge.builder("graphql.queries.total")
                .description("Total number of queries executed")
                .tag("component", "graphql")
                .register(meterRegistry, totalQueries, AtomicLong::get);

        Gauge.builder("graphql.queries.slow")
                .description("Number of slow queries (>1s)")
                .tag("component", "graphql")
                .register(meterRegistry, slowQueries, AtomicLong::get);

        // Cache hit rate gauge
        Gauge.builder("graphql.cache.hit.rate")
                .description("Cache hit rate percentage")
                .tag("component", "cache")
                .register(meterRegistry, this, PerformanceMonitoringService::getCacheHitRate);

        log.info("GraphQL performance monitoring metrics initialized successfully");
    }

    /**
     * Records a GraphQL query execution with timing information.
     *
     * @param queryName       the name of the executed query
     * @param executionTimeMs execution time in milliseconds
     * @param success         whether the query was successful
     */
    public void recordQueryExecution(String queryName, long executionTimeMs, boolean success) {
        queryExecutionCounter.increment(
                "query", queryName,
                "status", success ? "success" : "error");

        queryExecutionTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        totalQueries.incrementAndGet();

        if (executionTimeMs > 1000) {
            slowQueries.incrementAndGet();
            log.warn("Slow GraphQL query detected: {} took {}ms", queryName, executionTimeMs);
        }

        log.debug("Recorded GraphQL query execution: {} ({}ms, {})",
                queryName, executionTimeMs, success ? "success" : "error");
    }

    /**
     * Records cache operation metrics.
     *
     * @param cacheType       the type of cache operation
     * @param hit             whether it was a cache hit or miss
     * @param operationTimeMs operation time in milliseconds
     */
    public void recordCacheOperation(String cacheType, boolean hit, long operationTimeMs) {
        if (hit) {
            cacheHitCounter.increment("cache_type", cacheType);
        } else {
            cacheMissCounter.increment("cache_type", cacheType);
        }

        cacheOperationTimer.record(operationTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        log.debug("Recorded cache operation: {} ({}, {}ms)",
                cacheType, hit ? "hit" : "miss", operationTimeMs);
    }

    /**
     * Records DataLoader batch operation metrics.
     *
     * @param loaderType      the type of DataLoader
     * @param batchSize       the size of the batch
     * @param executionTimeMs execution time in milliseconds
     */
    public void recordDataLoaderBatch(String loaderType, int batchSize, long executionTimeMs) {
        dataLoaderBatchCounter.increment(
                "loader_type", loaderType,
                "batch_size_range", getBatchSizeRange(batchSize));

        dataLoaderExecutionTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        log.debug("Recorded DataLoader batch: {} (size: {}, {}ms)",
                loaderType, batchSize, executionTimeMs);
    }

    /**
     * Gets the current number of active database connections.
     */
    private double getActiveConnections() {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                return hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve active connections count: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Gets the current number of idle database connections.
     */
    private double getIdleConnections() {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                return hikariDataSource.getHikariPoolMXBean().getIdleConnections();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve idle connections count: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates the current cache hit rate as a percentage.
     */
    private double getCacheHitRate() {
        double hits = cacheHitCounter.count();
        double misses = cacheMissCounter.count();
        double total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (hits / total) * 100.0;
    }

    /**
     * Categorizes batch sizes into ranges for metrics.
     */
    private String getBatchSizeRange(int batchSize) {
        if (batchSize <= 10)
            return "small";
        if (batchSize <= 50)
            return "medium";
        if (batchSize <= 100)
            return "large";
        return "xlarge";
    }

    /**
     * Gets performance summary for health checks and monitoring.
     */
    public PerformanceSummary getPerformanceSummary() {
        return PerformanceSummary.builder()
                .totalQueries(totalQueries.get())
                .slowQueries(slowQueries.get())
                .cacheHitRate(getCacheHitRate())
                .activeConnections((long) getActiveConnections())
                .idleConnections((long) getIdleConnections())
                .averageQueryTime(queryExecutionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
                .build();
    }

    /**
     * Performance summary data structure.
     */
    @lombok.Builder
    @lombok.Data
    public static class PerformanceSummary {
        private long totalQueries;
        private long slowQueries;
        private double cacheHitRate;
        private long activeConnections;
        private long idleConnections;
        private double averageQueryTime;
    }
}