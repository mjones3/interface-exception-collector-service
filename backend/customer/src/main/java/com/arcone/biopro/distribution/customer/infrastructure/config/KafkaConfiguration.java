package com.arcone.biopro.distribution.customer.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Value("${kafka.topic.customer-data-received.name:CustomerDataReceived}")
    private String customerDataReceivedTopicName;

    @Value("${kafka.topic.customer-processed.name:CustomerProcessed}")
    private String customerProcessedTopicName;

    @Value("${kafka.topic.customer-data-received.partitions:1}")
    private int customerDataReceivedPartitions;

    @Value("${kafka.topic.customer-processed.partitions:1}")
    private int customerProcessedPartitions;

    @Value("${kafka.topic.customer-data-received.replicas:1}")
    private int customerDataReceivedReplicas;

    @Value("${kafka.topic.customer-processed.replicas:1}")
    private int customerProcessedReplicas;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic customerDataReceivedTopic() {
        return TopicBuilder.name(customerDataReceivedTopicName)
            .partitions(customerDataReceivedPartitions)
            .replicas(customerDataReceivedReplicas)
            .build();
    }

    @Bean
    public NewTopic customerProcessedTopic() {
        return TopicBuilder.name(customerProcessedTopicName)
            .partitions(customerProcessedPartitions)
            .replicas(customerProcessedReplicas)
            .build();
    }

    @Bean("CUSTOMER_DATA_RECEIVED")
    public ReactiveKafkaConsumerTemplate<String, String> customerDataReceivedConsumerTemplate(KafkaProperties kafkaProperties) {
        var updatedOptions = ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(customerDataReceivedTopicName));
        return new ReactiveKafkaConsumerTemplate<>(updatedOptions);
    }

    @Bean("CUSTOMER_PROCESSED")
    public ReactiveKafkaConsumerTemplate<String, String> customerProcessedConsumerTemplate(KafkaProperties kafkaProperties) {
        var updatedOptions = ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(customerProcessedTopicName));
        return new ReactiveKafkaConsumerTemplate<>(updatedOptions);
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }
}
