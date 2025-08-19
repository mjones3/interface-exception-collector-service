package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory cache configuration for the Interface Exception Collector Service.
 * Uses ConcurrentMapCacheManager instead of Redis to avoid Lettuce
 * dependencies.
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Cache names used throughout the application
     */
    public static final String EXCEPTION_DETAILS_CACHE = "exception-details";
    public static final String PAYLOAD_CACHE = "payload-cache";
    public static final String EXCEPTION_SUMMARY_CACHE = "exception-summary";
    public static final String SEARCH_RESULTS_CACHE = "search-results";
    public static final String RELATED_EXCEPTIONS_CACHE = "related-exceptions";

    // DataLoader specific caches
    public static final String DATALOADER_EXCEPTION_CACHE = "dataloader-exception";
    public static final String DATALOADER_PAYLOAD_CACHE = "dataloader-payload";
    public static final String DATALOADER_RETRY_HISTORY_CACHE = "dataloader-retry-history";

    /**
     * Configures an in-memory cache manager using ConcurrentMapCacheManager.
     * This provides basic caching without Redis dependencies.
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring in-memory cache manager (Redis disabled)");

        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                EXCEPTION_DETAILS_CACHE,
                PAYLOAD_CACHE,
                EXCEPTION_SUMMARY_CACHE,
                SEARCH_RESULTS_CACHE,
                RELATED_EXCEPTIONS_CACHE,
                DATALOADER_EXCEPTION_CACHE,
                DATALOADER_PAYLOAD_CACHE,
                DATALOADER_RETRY_HISTORY_CACHE);

        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }
}