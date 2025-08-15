package com.arcone.biopro.exception.collector.infrastructure.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for the Interface Exception Collector Service.
 * Configures producer properties, serializers, and KafkaTemplate for publishing
 * outbound events with proper error handling and retry mechanisms.
 */
@Configuration
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.retries}")
    private int retries;

    /**
     * Producer factory configuration with JSON serialization for outbound events.
     * Configures reliability settings including acknowledgments and retries.
     *
     * @return configured ProducerFactory for outbound events
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Basic Kafka producer configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance and batching configuration
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // Timeout configuration
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // Retry and resilience configuration
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 10000);

        // Compression for better throughput
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        log.info("Configured Kafka producer factory with bootstrap servers: {}, acks: {}, retries: {}",
                bootstrapServers, acks, retries);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for publishing outbound events with error handling.
     * Provides a high-level API for sending messages to Kafka topics.
     *
     * @return configured KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());

        // Configure default topic if needed
        // template.setDefaultTopic("default-topic");

        // Producer listeners are handled via CompletableFuture callbacks in the
        // publishers

        log.info("Configured KafkaTemplate for outbound event publishing");
        return template;
    }
}