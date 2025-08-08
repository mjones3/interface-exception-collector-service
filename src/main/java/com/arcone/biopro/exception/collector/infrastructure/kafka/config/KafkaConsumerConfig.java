package com.arcone.biopro.exception.collector.infrastructure.kafka.config;

import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for the Interface Exception Collector Service.
 * Configures consumer properties, deserializers, and listener container factory
 * with proper error handling and retry mechanisms as per requirements US-018.
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private int maxPollRecords;

    @Value("${spring.kafka.listener.concurrency}")
    private int concurrency;

    /**
     * Consumer factory configuration with JSON deserialization for inbound events.
     * Configures trusted packages and type mappings for event deserialization.
     *
     * @return configured ConsumerFactory for inbound events
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Basic Kafka consumer configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Serialization configuration
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // JSON deserializer configuration for trusted packages
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");

        // Type mappings for event deserialization
        configProps.put(JsonDeserializer.TYPE_MAPPINGS,
                "OrderRejectedEvent:com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent," +
                        "OrderCancelledEvent:com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent,"
                        +
                        "CollectionRejectedEvent:com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent,"
                        +
                        "DistributionFailedEvent:com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent,"
                        +
                        "ValidationErrorEvent:com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent");

        // Error handling configuration
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        // Retry and resilience configuration
        configProps.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 10000);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka listener container factory with manual acknowledgment and error
     * handling.
     * Configures concurrency, acknowledgment mode, and error handling for
     * consumers with dead letter queue support.
     *
     * @return configured ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);

        // Configure manual acknowledgment for better error handling
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Configure dead letter queue error handling
        DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> {
                    String originalTopic = consumerRecord.topic();
                    String deadLetterTopic = originalTopic + ".DLT";
                    log.error("Publishing message to dead letter topic: {} due to error: {}",
                            deadLetterTopic, exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(deadLetterTopic, 0);
                });

        // Configure exponential backoff with maximum 5 retry attempts
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(1000L, 2.0);
        exponentialBackOff.setMaxAttempts(5);
        exponentialBackOff.setMaxInterval(30000L); // Max 30 seconds between retries

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterRecoverer, exponentialBackOff);

        // Add specific exceptions that should not be retried (go directly to DLT)
        errorHandler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                org.springframework.messaging.converter.MessageConversionException.class,
                IllegalArgumentException.class);

        factory.setCommonErrorHandler(errorHandler);

        // Configure graceful shutdown
        factory.getContainerProperties().setShutdownTimeout(30000);

        log.info("Configured Kafka listener container factory with concurrency: {}, group-id: {}, DLT enabled",
                concurrency, groupId);

        return factory;
    }

    /**
     * Specific consumer factory for OrderRejectedEvent with type-safe
     * deserialization.
     *
     * @return ConsumerFactory for OrderRejectedEvent
     */
    @Bean
    public ConsumerFactory<String, OrderRejectedEvent> orderRejectedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderRejectedEvent.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Specific consumer factory for OrderCancelledEvent with type-safe
     * deserialization.
     *
     * @return ConsumerFactory for OrderCancelledEvent
     */
    @Bean
    public ConsumerFactory<String, OrderCancelledEvent> orderCancelledConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCancelledEvent.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Specific consumer factory for CollectionRejectedEvent with type-safe
     * deserialization.
     *
     * @return ConsumerFactory for CollectionRejectedEvent
     */
    @Bean
    public ConsumerFactory<String, CollectionRejectedEvent> collectionRejectedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CollectionRejectedEvent.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Specific consumer factory for DistributionFailedEvent with type-safe
     * deserialization.
     *
     * @return ConsumerFactory for DistributionFailedEvent
     */
    @Bean
    public ConsumerFactory<String, DistributionFailedEvent> distributionFailedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DistributionFailedEvent.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Specific consumer factory for ValidationErrorEvent with type-safe
     * deserialization.
     *
     * @return ConsumerFactory for ValidationErrorEvent
     */
    @Bean
    public ConsumerFactory<String, ValidationErrorEvent> validationErrorConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arcone.biopro.exception.collector.domain.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ValidationErrorEvent.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }
}