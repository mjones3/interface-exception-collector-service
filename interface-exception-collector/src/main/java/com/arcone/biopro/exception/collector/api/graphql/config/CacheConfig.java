package com.arcone.biopro.exception.collector.api.graphql.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for GraphQL API components.
 * Implements requirement 5.4: Redis caching with appropriate TTL values.
 * 
 * Cache TTL Configuration:
 * - exception-summary: 5 minutes (dashboard statistics)
 * - exception-details: 1 hour (exception details)
 * - payload-data: 24 hours (original payloads)
 * - trend-data: 15 minutes (time-series data)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Redis cache manager with specific TTL values for different cache
     * types.
     * 
     * @param connectionFactory Redis connection factory
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // Default 5 minutes TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Specific cache configurations with different TTL values
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard summary statistics - 5 minutes TTL for real-time feel
        cacheConfigurations.put("exception-summary", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        // Exception details - 1 hour TTL as they don't change frequently
        cacheConfigurations.put("exception-details", defaultConfig
                .entryTtl(Duration.ofHours(1)));

        // Original payload data - 24 hours TTL as it's expensive to retrieve
        cacheConfigurations.put("payload-data", defaultConfig
                .entryTtl(Duration.ofHours(24)));

        // Trend data - 15 minutes TTL for dashboard charts
        cacheConfigurations.put("trend-data", defaultConfig
                .entryTtl(Duration.ofMinutes(15)));

        // Key metrics - 10 minutes TTL for KPI displays
        cacheConfigurations.put("key-metrics", defaultConfig
                .entryTtl(Duration.ofMinutes(10)));

        // Interface type summaries - 5 minutes TTL
        cacheConfigurations.put("interface-summary", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        // Severity summaries - 5 minutes TTL
        cacheConfigurations.put("severity-summary", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        // Status summaries - 5 minutes TTL
        cacheConfigurations.put("status-summary", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}