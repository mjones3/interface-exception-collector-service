package com.arcone.biopro.distribution.shipping.verification.support;

import com.arcone.biopro.distribution.shipping.verification.support.types.OrderFulfilledEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaTestConfiguration {

    @Bean
    SenderOptions<String, OrderFulfilledEventType> senderOptions(KafkaProperties kafkaProperties, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderFulfilledEventType>create(props).withValueSerializer(new JsonSerializer<>(objectMapper)).maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean("producer-template")
    ReactiveKafkaProducerTemplate<String, OrderFulfilledEventType> producerTemplate(SenderOptions<String, OrderFulfilledEventType> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

}
