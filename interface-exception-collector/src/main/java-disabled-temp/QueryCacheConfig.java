package com.arcone.biopro.exception.collector.api.graphql.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serialization.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serialization.RedisSerializationContext;
import org.springframework.data.redis.serialization.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for GraphQL query result caching.
 * Implements multi-level caching strategy with different TTL values
 * for different types of GraphQL queries to optimize performance.
 */
// @Configuration // Disabled due to Redis dependency
// @EnableCaching // Disabled due to Redis dependency
@Slf4j
public class QueryCacheConfig {

        @Value("${graphql.query-cache.enabled:true}")
        private boolean queryCacheEnabled;

        @Value("${graphql.query-cache.default-ttl-seconds:300}")
        private int defaultTtlSeconds;

        @Value("${graphql.query-cache.ttl-by-type.exception-list:180}")
        private int exceptionListTtl;

        @Value("${graphql.query-cache.ttl-by-type.exception-summary:300}")
        private int exceptionSummaryTtl;

        @Value("${graphql.query-cache.ttl-by-type.exception-detail:600}")
        private int exceptionDetailTtl;

        @Value("${graphql.query-cache.ttl-by-type.retry-history:900}")
        private int retryHistoryTtl;

        @Value("${graphql.query-cache.ttl-by-type.status-history:900}")
        private int statusHistoryTtl;

        /**
         * Cache names used throughout the GraphQL API
         */
        public static final String EXCEPTION_LIST_CACHE = "graphql:exception-list";
        public static final String EXCEPTION_SUMMARY_CACHE = "graphql:exception-summary";
        public static final String EXCEPTION_DETAIL_CACHE = "graphql:exception-detail";
        public static final String RETRY_HISTORY_CACHE = "graphql:retry-history";
        public static final String STATUS_HISTORY_CACHE = "graphql:status-history";
        public static final String PAYLOAD_CACHE = "graphql:payload";
        public static final String AGGREGATION_CACHE = "graphql:aggregation";

        /**
         * Creates a specialized cache manager for GraphQL query results.
         * Configures different TTL values for different types of cached data.
         *
         * @param connectionFactory Redis connection factory
         * @return configured cache manager
         */
        @Bean("graphqlCacheManager")
        public CacheManager graphqlCacheManager(RedisConnectionFactory connectionFactory) {
                if (!queryCacheEnabled) {
                        log.info("GraphQL query caching is disabled");
                        return null;
                }

                log.info("Configuring GraphQL query cache manager with TTL settings");

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(defaultTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                                .prefixCacheNameWith("graphql:")
                                .disableCachingNullValues();

                // Cache-specific configurations with different TTL values
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Exception list queries - shorter TTL for real-time updates
                cacheConfigurations.put(EXCEPTION_LIST_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(exceptionListTtl)));

                // Exception summary queries - medium TTL for dashboard statistics
                cacheConfigurations.put(EXCEPTION_SUMMARY_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(exceptionSummaryTtl)));

                // Exception detail queries - longer TTL as details change less frequently
                cacheConfigurations.put(EXCEPTION_DETAIL_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(exceptionDetailTtl)));

                // Retry history - longest TTL as history is immutable
                cacheConfigurations.put(RETRY_HISTORY_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(retryHistoryTtl)));

                // Status history - longest TTL as history is immutable
                cacheConfigurations.put(STATUS_HISTORY_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(statusHistoryTtl)));

                // Payload cache - very long TTL as payloads are immutable
                cacheConfigurations.put(PAYLOAD_CACHE, defaultConfig
                                .entryTtl(Duration.ofHours(24)));

                // Aggregation cache - medium TTL for computed statistics
                cacheConfigurations.put(AGGREGATION_CACHE, defaultConfig
                                .entryTtl(Duration.ofSeconds(exceptionSummaryTtl)));

                RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .transactionAware()
                                .build();

                log.info("GraphQL cache manager configured with {} cache types", cacheConfigurations.size());
                log.debug("Cache TTL settings - List: {}s, Summary: {}s, Detail: {}s, History: {}s",
                                exceptionListTtl, exceptionSummaryTtl, exceptionDetailTtl, retryHistoryTtl);

                return cacheManager;
        }

        /**
         * Utility method to generate cache keys for GraphQL queries.
         * Creates consistent cache keys based on query parameters.
         *
         * @param queryType  the type of GraphQL query
         * @param parameters query parameters for key generation
         * @return generated cache key
         */
        public static String generateCacheKey(String queryType, Object... parameters) {
                StringBuilder keyBuilder = new StringBuilder(queryType);

                for (Object param : parameters) {
                        if (param != null) {
                                keyBuilder.append(":").append(param.toString().hashCode());
                        }
                }

                return keyBuilder.toString();
        }

        /**
         * Utility method to generate cache key for exception list queries.
         * Includes filters and pagination parameters in the key.
         */
        public static String generateExceptionListCacheKey(Object filters, Object pagination, Object sorting) {
                return generateCacheKey("exception-list", filters, pagination, sorting);
        }

        /**
         * Utility method to generate cache key for summary queries.
         * Includes time range and filters in the key.
         */
        public static String generateSummaryCacheKey(Object timeRange, Object filters) {
                return generateCacheKey("exception-summary", timeRange, filters);
        }

        /**
         * Utility method to generate cache key for detail queries.
         * Uses transaction ID as the primary key component.
         */
        public static String generateDetailCacheKey(String transactionId) {
                return generateCacheKey("exception-detail", transactionId);
        }
}