package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;
import java.util.List;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
class KafkaConfiguration {

    @Bean
    NewTopic inventoryTopic(
        @Value("${topic.inventory.partitions:1}") Integer partitions,
        @Value("${topic.inventory.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name("inventory.produced")
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    ReceiverOptions<String, String> inventoryReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .commitInterval(Duration.ZERO) // Disable periodic commits
            .commitBatchSize(0) // Disable commits by batch size
            .subscription(List.of("topic.received"));
    }

    @Bean
    ReactiveKafkaConsumerTemplate<String, String> inventoryConsumerTemplate(
        ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, InventoryCreatedEvent> senderOptions(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, InventoryCreatedEvent>create(props)
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    ReactiveKafkaProducerTemplate<String, InventoryCreatedEvent> producerTemplate(SenderOptions<String, InventoryCreatedEvent> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }
}
