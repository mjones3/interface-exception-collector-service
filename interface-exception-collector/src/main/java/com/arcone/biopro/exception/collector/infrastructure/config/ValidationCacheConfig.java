package com.arcone.biopro.exception.collector.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import lombok.Data;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for validation caching without Redis dependency.
 * Configures simple in-memory caching with TTL settings for optimal performance.
 */
@Configuration
@EnableCaching
@Data
@ConfigurationProperties(prefix = "app.validation.cache")
public class ValidationCacheConfig {

    /**
     * TTL for exception existence validation cache (default: 5 minutes)
     */
    private Duration existenceTtl = Duration.ofMinutes(5);

    /**
     * TTL for exception retryable status cache (default: 10 minutes)
     */
    private Duration retryableTtl = Duration.ofMinutes(10);

    /**
     * TTL for retry count validation cache (default: 2 minutes)
     */
    private Duration retryCountTtl = Duration.ofMinutes(2);

    /**
     * TTL for pending retry validation cache (default: 1 minute)
     */
    private Duration pendingRetryTtl = Duration.ofMinutes(1);

    /**
     * TTL for exception status validation cache (default: 5 minutes)
     */
    private Duration statusTtl = Duration.ofMinutes(5);

    /**
     * TTL for complete validation result cache (default: 3 minutes)
     */
    private Duration validationResultTtl = Duration.ofMinutes(3);

    /**
     * Maximum number of entries per cache (default: 10000)
     */
    private int maxCacheSize = 10000;

    /**
     * Whether to enable cache statistics (default: true)
     */
    private boolean enableStatistics = true;

    /**
     * Whether to enable cache eviction logging (default: false)
     */
    private boolean enableEvictionLogging = false;

    /**
     * Cache cleanup interval in minutes (default: 10 minutes)
     */
    private Duration cleanupInterval = Duration.ofMinutes(10);

    /**
     * Creates a custom cache manager with TTL-aware caches.
     * Uses ConcurrentHashMap-based caches with automatic expiration.
     */
    @Bean
    @Primary
    public CacheManager validationCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        cacheManager.setCaches(Arrays.asList(
            createTtlCache("exception-existence", existenceTtl),
            createTtlCache("exception-retryable", retryableTtl),
            createTtlCache("retry-count", retryCountTtl),
            createTtlCache("pending-retry", pendingRetryTtl),
            createTtlCache("exception-status", statusTtl),
            createTtlCache("validation-result", validationResultTtl)
        ));
        
        return cacheManager;
    }

    /**
     * Creates a TTL-aware cache with automatic expiration.
     */
    private TtlConcurrentMapCache createTtlCache(String name, Duration ttl) {
        return new TtlConcurrentMapCache(name, ttl, maxCacheSize, enableStatistics);
    }

    /**
     * Custom cache implementation with TTL support.
     * Extends ConcurrentMapCache to add time-based expiration.
     */
    public static class TtlConcurrentMapCache extends ConcurrentMapCache {
        
        private final Duration ttl;
        private final int maxSize;
        private final boolean statisticsEnabled;
        private final ConcurrentHashMap<Object, CacheEntry> store;
        
        // Statistics
        private volatile long hitCount = 0;
        private volatile long missCount = 0;
        private volatile long evictionCount = 0;
        
        public TtlConcurrentMapCache(String name, Duration ttl, int maxSize, boolean statisticsEnabled) {
            super(name, new ConcurrentHashMap<>(), true);
            this.ttl = ttl;
            this.maxSize = maxSize;
            this.statisticsEnabled = statisticsEnabled;
            this.store = new ConcurrentHashMap<>();
        }

        @Override
        public ValueWrapper get(Object key) {
            if (statisticsEnabled) {
                CacheEntry entry = store.get(key);
                if (entry != null && !entry.isExpired()) {
                    hitCount++;
                    return entry.getValueWrapper();
                } else {
                    missCount++;
                    if (entry != null && entry.isExpired()) {
                        store.remove(key);
                        evictionCount++;
                    }
                    return null;
                }
            } else {
                CacheEntry entry = store.get(key);
                if (entry != null && !entry.isExpired()) {
                    return entry.getValueWrapper();
                } else {
                    if (entry != null && entry.isExpired()) {
                        store.remove(key);
                    }
                    return null;
                }
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                Object value = wrapper.get();
                if (type != null && !type.isInstance(value)) {
                    throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
                }
                return (T) value;
            }
            return null;
        }

        @Override
        public void put(Object key, Object value) {
            // Enforce max size by removing oldest entries
            if (store.size() >= maxSize) {
                cleanupExpiredEntries();
                if (store.size() >= maxSize) {
                    // Remove oldest entry if still at max size
                    Object oldestKey = store.keySet().iterator().next();
                    store.remove(oldestKey);
                    evictionCount++;
                }
            }
            
            store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl.toMillis()));
        }

        @Override
        public void evict(Object key) {
            if (store.remove(key) != null) {
                evictionCount++;
            }
        }

        @Override
        public void clear() {
            int size = store.size();
            store.clear();
            evictionCount += size;
        }

        /**
         * Cleanup expired entries to free memory.
         */
        public void cleanupExpiredEntries() {
            long now = System.currentTimeMillis();
            store.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired(now)) {
                    evictionCount++;
                    return true;
                }
                return false;
            });
        }

        /**
         * Get cache statistics.
         */
        public CacheStatistics getStatistics() {
            return new CacheStatistics(
                getName(),
                store.size(),
                hitCount,
                missCount,
                evictionCount,
                calculateHitRate()
            );
        }

        private double calculateHitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }

        /**
         * Cache entry with expiration time.
         */
        private static class CacheEntry {
            private final Object value;
            private final long expirationTime;

            public CacheEntry(Object value, long expirationTime) {
                this.value = value;
                this.expirationTime = expirationTime;
            }

            public boolean isExpired() {
                return isExpired(System.currentTimeMillis());
            }

            public boolean isExpired(long currentTime) {
                return currentTime > expirationTime;
            }

            public ValueWrapper getValueWrapper() {
                return new SimpleValueWrapper(value);
            }
        }

        /**
         * Simple value wrapper implementation.
         */
        private static class SimpleValueWrapper implements ValueWrapper {
            private final Object value;

            public SimpleValueWrapper(Object value) {
                this.value = value;
            }

            @Override
            public Object get() {
                return value;
            }
        }
    }

    /**
     * Cache statistics data class.
     */
    @Data
    public static class CacheStatistics {
        private final String cacheName;
        private final int size;
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        private final double hitRate;
    }
}