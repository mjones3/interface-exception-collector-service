package com.arcone.biopro.distribution.orderservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.List;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
class KafkaConfiguration {

    @Bean
    NewTopic orderServiceTopic(
        @Value("${topic.order-service.partitions:1}") Integer partitions,
        @Value("${topic.order-service.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name("order-service.produced")
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    ReceiverOptions<String, String> orderServiceReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .commitInterval(Duration.ZERO) // Disable periodic commits
            .commitBatchSize(0) // Disable commits by batch size
            .subscription(List.of("topic.received"));
    }

    @Bean
    ReactiveKafkaConsumerTemplate<String, String> orderServiceConsumerTemplate(
        ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

}
