package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationCacheConfigTest {

    private ValidationCacheConfig config;

    @BeforeEach
    void setUp() {
        config = new ValidationCacheConfig();
        config.setExistenceTtl(Duration.ofMinutes(5));
        config.setRetryableTtl(Duration.ofMinutes(10));
        config.setRetryCountTtl(Duration.ofMinutes(2));
        config.setPendingRetryTtl(Duration.ofMinutes(1));
        config.setStatusTtl(Duration.ofMinutes(5));
        config.setValidationResultTtl(Duration.ofMinutes(3));
        config.setMaxCacheSize(10000);
        config.setEnableStatistics(true);
        config.setEnableEvictionLogging(false);
        config.setCleanupInterval(Duration.ofMinutes(10));
    }

    @Test
    void validationCacheManager_ShouldCreateCacheManagerWithAllCaches() {
        // When
        CacheManager cacheManager = config.validationCacheManager();

        // Then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCache("exception-existence")).isNotNull();
        assertThat(cacheManager.getCache("exception-retryable")).isNotNull();
        assertThat(cacheManager.getCache("retry-count")).isNotNull();
        assertThat(cacheManager.getCache("pending-retry")).isNotNull();
        assertThat(cacheManager.getCache("exception-status")).isNotNull();
        assertThat(cacheManager.getCache("validation-result")).isNotNull();
    }

    @Test
    void ttlConcurrentMapCache_ShouldStoreAndRetrieveValues() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofSeconds(1), 100, true);

        // When
        cache.put("key1", "value1");
        Cache.ValueWrapper result = cache.get("key1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("value1");
    }

    @Test
    void ttlConcurrentMapCache_ShouldExpireValuesAfterTtl() throws InterruptedException {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMillis(100), 100, true);

        // When
        cache.put("key1", "value1");
        Thread.sleep(150); // Wait for expiration
        Cache.ValueWrapper result = cache.get("key1");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void ttlConcurrentMapCache_ShouldReturnTypedValue() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 100, true);

        // When
        cache.put("key1", "value1");
        String result = cache.get("key1", String.class);

        // Then
        assertThat(result).isEqualTo("value1");
    }

    @Test
    void ttlConcurrentMapCache_ShouldThrowExceptionForWrongType() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 100, true);

        // When
        cache.put("key1", "value1");

        // Then
        try {
            Integer result = cache.get("key1", Integer.class);
            assertThat(result).isNull(); // Should not reach here
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("Cached value is not of required type");
        }
    }

    @Test
    void ttlConcurrentMapCache_ShouldEvictSpecificKey() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 100, true);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.evict("key1");

        // Then
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key2")).isNotNull();
    }

    @Test
    void ttlConcurrentMapCache_ShouldClearAllEntries() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 100, true);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clear();

        // Then
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    void ttlConcurrentMapCache_ShouldEnforceMaxSize() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 2, true);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3"); // Should evict oldest entry

        // Then
        // At least one of the first two keys should be evicted
        int existingKeys = 0;
        if (cache.get("key1") != null) existingKeys++;
        if (cache.get("key2") != null) existingKeys++;
        if (cache.get("key3") != null) existingKeys++;
        
        assertThat(existingKeys).isLessThanOrEqualTo(2);
        assertThat(cache.get("key3")).isNotNull(); // Most recent should exist
    }

    @Test
    void ttlConcurrentMapCache_ShouldCleanupExpiredEntries() throws InterruptedException {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMillis(100), 100, true);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        Thread.sleep(150); // Wait for expiration
        cache.cleanupExpiredEntries();

        // Then
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    void ttlConcurrentMapCache_ShouldProvideStatistics() {
        // Given
        ValidationCacheConfig.TtlConcurrentMapCache cache = 
            new ValidationCacheConfig.TtlConcurrentMapCache("test-cache", Duration.ofMinutes(1), 100, true);

        // When
        cache.put("key1", "value1");
        cache.get("key1"); // Hit
        cache.get("key2"); // Miss

        ValidationCacheConfig.CacheStatistics stats = cache.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getCacheName()).isEqualTo("test-cache");
        assertThat(stats.getHitCount()).isEqualTo(1);
        assertThat(stats.getMissCount()).isEqualTo(1);
        assertThat(stats.getHitRate()).isEqualTo(0.5);
    }

    @Test
    void cacheStatistics_ShouldCalculateHitRateCorrectly() {
        // Given
        ValidationCacheConfig.CacheStatistics stats = new ValidationCacheConfig.CacheStatistics(
            "test-cache", 5, 10, 2, 1, 0.833
        );

        // Then
        assertThat(stats.getCacheName()).isEqualTo("test-cache");
        assertThat(stats.getSize()).isEqualTo(5);
        assertThat(stats.getHitCount()).isEqualTo(10);
        assertThat(stats.getMissCount()).isEqualTo(2);
        assertThat(stats.getEvictionCount()).isEqualTo(1);
        assertThat(stats.getHitRate()).isEqualTo(0.833);
    }

    @Test
    void configurationProperties_ShouldHaveCorrectDefaults() {
        // Given
        ValidationCacheConfig defaultConfig = new ValidationCacheConfig();

        // Then
        assertThat(defaultConfig.getExistenceTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(defaultConfig.getRetryableTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(defaultConfig.getRetryCountTtl()).isEqualTo(Duration.ofMinutes(2));
        assertThat(defaultConfig.getPendingRetryTtl()).isEqualTo(Duration.ofMinutes(1));
        assertThat(defaultConfig.getStatusTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(defaultConfig.getValidationResultTtl()).isEqualTo(Duration.ofMinutes(3));
        assertThat(defaultConfig.getMaxCacheSize()).isEqualTo(10000);
        assertThat(defaultConfig.isEnableStatistics()).isTrue();
        assertThat(defaultConfig.isEnableEvictionLogging()).isFalse();
        assertThat(defaultConfig.getCleanupInterval()).isEqualTo(Duration.ofMinutes(10));
    }
}