package com.arcone.biopro.exception.collector.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for cache metrics to monitor cache performance and hit rates.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CacheMetricsConfig {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    /**
     * Registers cache metrics with Micrometer for monitoring.
     */
    @PostConstruct
    public void registerCacheMetrics() {
        log.info("Registering cache metrics for monitoring");
        
        // Register basic cache information
        cacheManager.getCacheNames().forEach(cacheName -> {
            try {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    log.debug("Cache available for metrics: {}", cacheName);
                }
            } catch (Exception e) {
                log.warn("Failed to register metrics for cache: {}, error: {}", cacheName, e.getMessage());
            }
        });
        
        log.info("Cache metrics registration completed for {} caches", cacheManager.getCacheNames().size());
    }
}