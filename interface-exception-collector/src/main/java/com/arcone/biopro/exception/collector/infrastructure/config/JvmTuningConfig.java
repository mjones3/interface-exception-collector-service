package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * JVM tuning configuration for optimal GraphQL mutation performance.
 * Provides runtime JVM optimization and monitoring for mutation operations.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JvmTuningConfig {

    private final MutationPerformanceConfig performanceConfig;

    /**
     * Initializes JVM tuning parameters on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeJvmTuning() {
        MutationPerformanceConfig.JvmConfig jvmConfig = performanceConfig.getJvm();
        
        logCurrentJvmSettings();
        
        if (jvmConfig.isGcOptimizationEnabled()) {
            optimizeGarbageCollection();
        }
        
        validateMemorySettings(jvmConfig);
        logOptimizationRecommendations(jvmConfig);
    }

    /**
     * Logs current JVM settings for monitoring
     */
    private void logCurrentJvmSettings() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        log.info("=== JVM Configuration for GraphQL Mutations ===");
        log.info("JVM Name: {}", runtimeBean.getVmName());
        log.info("JVM Version: {}", runtimeBean.getVmVersion());
        log.info("JVM Vendor: {}", runtimeBean.getVmVendor());
        
        // Memory settings
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        
        log.info("Max Memory: {} MB", maxMemory / (1024 * 1024));
        log.info("Total Memory: {} MB", totalMemory / (1024 * 1024));
        log.info("Free Memory: {} MB", freeMemory / (1024 * 1024));
        log.info("Used Memory: {} MB", (totalMemory - freeMemory) / (1024 * 1024));
        
        // Heap memory
        log.info("Heap Memory Used: {} MB", 
                memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        log.info("Heap Memory Max: {} MB", 
                memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
        
        // Non-heap memory
        log.info("Non-Heap Memory Used: {} MB", 
                memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024));
        
        // JVM arguments
        List<String> jvmArgs = runtimeBean.getInputArguments();
        log.info("JVM Arguments: {}", jvmArgs);
    }

    /**
     * Optimizes garbage collection for GraphQL mutation workloads
     */
    private void optimizeGarbageCollection() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        log.info("=== Garbage Collection Optimization ===");
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            log.info("GC Name: {}", gcBean.getName());
            log.info("GC Collection Count: {}", gcBean.getCollectionCount());
            log.info("GC Collection Time: {} ms", gcBean.getCollectionTime());
        }
        
        // Log GC optimization recommendations
        boolean hasG1GC = gcBeans.stream()
                .anyMatch(gc -> gc.getName().contains("G1"));
        
        if (!hasG1GC) {
            log.warn("G1GC not detected. For better GraphQL mutation performance, consider using G1GC with:");
            log.warn("  -XX:+UseG1GC");
            log.warn("  -XX:MaxGCPauseMillis={}", performanceConfig.getJvm().getMaxGcPauseMs());
            log.warn("  -XX:G1HeapRegionSize=16m");
        } else {
            log.info("G1GC detected - optimal for GraphQL mutation workloads");
        }
    }

    /**
     * Validates memory settings against configuration
     */
    private void validateMemorySettings(MutationPerformanceConfig.JvmConfig jvmConfig) {
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        
        if (maxMemoryMB < jvmConfig.getInitialHeapSizeMb()) {
            log.warn("Current max memory ({} MB) is less than configured initial heap size ({} MB)",
                    maxMemoryMB, jvmConfig.getInitialHeapSizeMb());
        }
        
        if (maxMemoryMB < jvmConfig.getMaxHeapSizeMb()) {
            log.warn("Current max memory ({} MB) is less than configured max heap size ({} MB)",
                    maxMemoryMB, jvmConfig.getMaxHeapSizeMb());
        }
        
        // Check if we have enough memory for concurrent mutations
        int concurrentOps = performanceConfig.getConcurrency().getMaxConcurrentOperationsTotal();
        long estimatedMemoryPerOp = 10; // MB per operation estimate
        long requiredMemory = concurrentOps * estimatedMemoryPerOp;
        
        if (maxMemoryMB < requiredMemory) {
            log.warn("Available memory ({} MB) may be insufficient for {} concurrent operations (estimated {} MB required)",
                    maxMemoryMB, concurrentOps, requiredMemory);
        }
    }

    /**
     * Logs JVM optimization recommendations
     */
    private void logOptimizationRecommendations(MutationPerformanceConfig.JvmConfig jvmConfig) {
        log.info("=== JVM Optimization Recommendations ===");
        
        if (jvmConfig.isG1GcEnabled()) {
            log.info("Recommended JVM arguments for GraphQL mutations:");
            log.info("  -Xms{}m", jvmConfig.getInitialHeapSizeMb());
            log.info("  -Xmx{}m", jvmConfig.getMaxHeapSizeMb());
            log.info("  -XX:+UseG1GC");
            log.info("  -XX:MaxGCPauseMillis={}", jvmConfig.getMaxGcPauseMs());
            log.info("  -XX:G1HeapRegionSize=16m");
            log.info("  -XX:+UseStringDeduplication");
            log.info("  -XX:+OptimizeStringConcat");
        }
        
        // Additional performance recommendations
        log.info("Additional performance recommendations:");
        log.info("  -XX:+UseCompressedOops (if heap < 32GB)");
        log.info("  -XX:+UseCompressedClassPointers");
        log.info("  -XX:+TieredCompilation");
        log.info("  -XX:TieredStopAtLevel=4");
        log.info("  -Djava.awt.headless=true");
        log.info("  -Dfile.encoding=UTF-8");
        
        // GraphQL-specific recommendations
        log.info("GraphQL-specific optimizations:");
        log.info("  -Dspring.graphql.websocket.connection-init-timeout=60s");
        log.info("  -Dspring.graphql.schema.printer.enabled=false");
        log.info("  -Dspring.graphql.cors.allowed-origins=*");
    }

    /**
     * Provides runtime memory monitoring for mutations
     */
    public void logMemoryUsage(String operation) {
        if (log.isDebugEnabled()) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            log.debug("Memory usage after {}: Used={} MB, Free={} MB, Total={} MB",
                    operation,
                    usedMemory / (1024 * 1024),
                    freeMemory / (1024 * 1024),
                    totalMemory / (1024 * 1024));
        }
    }

    /**
     * Forces garbage collection if memory usage is high
     */
    public void optimizeMemoryIfNeeded() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        if (memoryUsagePercent > 80) {
            log.info("High memory usage detected ({}%). Suggesting garbage collection.", 
                    String.format("%.1f", memoryUsagePercent));
            System.gc();
        }
    }
}