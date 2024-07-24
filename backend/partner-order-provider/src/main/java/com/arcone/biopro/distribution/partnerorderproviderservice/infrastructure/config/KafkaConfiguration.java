package com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.config;

import com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.event.OrderReceivedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
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

    @Value("${topics.order-received.name}")
    private String orderReceivedTopicName;

    @Bean
    NewTopic orderReceivedTopic(
        @Value("${topics.order-received.partitions:1}") Integer partitions,
        @Value("${topics.order-received.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name(orderReceivedTopicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> partnerOrderProviderServiceReceiverOptions(KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return ReceiverOptions.<String, String>create(props)
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(orderReceivedTopicName));
    }



    @Bean
    ReactiveKafkaConsumerTemplate<String, String> partnerOrderProviderConsumerTemplate(
        ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, OrderReceivedEvent> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderReceivedEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate(
        SenderOptions<String, OrderReceivedEvent> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }

}
