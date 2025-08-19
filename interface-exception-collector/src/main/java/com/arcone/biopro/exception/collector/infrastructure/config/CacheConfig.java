package com.arcone.biopro.exception.collector.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for the Interface Exception Collector Service.
 * Configures different cache regions with appropriate TTL values and
 * serialization.
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
         * Configures the Redis cache manager with different TTL settings for different
         * cache regions.
         */
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                log.info("Configuring Redis cache manager with custom TTL settings");

                // Create ObjectMapper for JSON serialization
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                // Create JSON serializer
                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                objectMapper);

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer))
                                .disableCachingNullValues();

                // Cache-specific configurations with different TTL values
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Exception details - cache for 10 minutes (frequently accessed)
                cacheConfigurations.put(EXCEPTION_DETAILS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));

                // Payload cache - cache for 30 minutes (expensive to retrieve)
                cacheConfigurations.put(PAYLOAD_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

                // Exception summary - cache for 2 minutes (aggregated data changes frequently)
                cacheConfigurations.put(EXCEPTION_SUMMARY_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(2)));

                // Search results - cache for 5 minutes (balance between freshness and
                // performance)
                cacheConfigurations.put(SEARCH_RESULTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

                // Related exceptions - cache for 15 minutes (relatively stable)
                cacheConfigurations.put(RELATED_EXCEPTIONS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));

                // DataLoader caches - shorter TTL for request-scoped caching
                cacheConfigurations.put(DATALOADER_EXCEPTION_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));
                cacheConfigurations.put(DATALOADER_PAYLOAD_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));
                cacheConfigurations.put(DATALOADER_RETRY_HISTORY_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }

        /**
         * Configures RedisTemplate for manual cache operations if needed.
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Use String serializer for keys
                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                // Use JSON serializer for values
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                objectMapper);
                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.afterPropertiesSet();
                return template;
        }

        /**
         * Customizes the Redis cache manager builder for additional configuration.
         */
        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
                return builder -> {
                        log.info("Applying Redis cache manager customizations");
                        builder
                                        .transactionAware()
                                        .enableStatistics();
                };
        }
}