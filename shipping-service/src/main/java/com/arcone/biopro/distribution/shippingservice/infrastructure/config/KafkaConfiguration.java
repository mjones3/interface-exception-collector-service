package com.arcone.biopro.distribution.shippingservice.infrastructure.config;

import com.arcone.biopro.distribution.shippingservice.domain.event.ShippingServiceCreatedEvent;
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
    NewTopic shippingServiceTopic(
        @Value("${topic.shipping-service.partitions:1}") Integer partitions,
        @Value("${topic.shipping-service.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name("shipping-service.produced")
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    NewTopic orderFulfilledTopic(
        @Value("${order.fulfilled.partitions:1}") Integer partitions,
        @Value("${order.fulfilled.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name("order.fulfilled").partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> shippingServiceReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .commitInterval(Duration.ZERO) // Disable periodic commits
            .commitBatchSize(0) // Disable commits by batch size
            .subscription(List.of("topic.received","order.fulfilled"));
    }

    @Bean
    ReactiveKafkaConsumerTemplate<String, String> shippingServiceConsumerTemplate(
        ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, ShippingServiceCreatedEvent> senderOptions(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, ShippingServiceCreatedEvent>create(props)
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    ReactiveKafkaProducerTemplate<String, ShippingServiceCreatedEvent> producerTemplate(SenderOptions<String, ShippingServiceCreatedEvent> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }
}
