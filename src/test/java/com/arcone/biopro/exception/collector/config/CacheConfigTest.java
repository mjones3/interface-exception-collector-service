package com.arcone.biopro.exception.collector.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for cache configuration.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CacheConfigTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void shouldConfigureCacheManager() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder(
                CacheConfig.EXCEPTION_DETAILS_CACHE,
                CacheConfig.PAYLOAD_CACHE,
                CacheConfig.EXCEPTION_SUMMARY_CACHE,
                CacheConfig.SEARCH_RESULTS_CACHE,
                CacheConfig.RELATED_EXCEPTIONS_CACHE);
    }

    @Test
    void shouldConfigureRedisTemplate() {
        assertThat(redisTemplate).isNotNull();
        assertThat(redisTemplate.getConnectionFactory()).isNotNull();
    }

    @Test
    void shouldHaveCorrectCacheConfiguration() {
        // Test that caches are properly configured
        assertThat(cacheManager.getCache(CacheConfig.EXCEPTION_DETAILS_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.PAYLOAD_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.EXCEPTION_SUMMARY_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.SEARCH_RESULTS_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.RELATED_EXCEPTIONS_CACHE)).isNotNull();
    }

    @Test
    void shouldConnectToRedis() {
        // Test Redis connectivity
        redisTemplate.opsForValue().set("test-key", "test-value");
        String value = (String) redisTemplate.opsForValue().get("test-key");
        assertThat(value).isEqualTo("test-value");

        // Cleanup
        redisTemplate.delete("test-key");
    }
}