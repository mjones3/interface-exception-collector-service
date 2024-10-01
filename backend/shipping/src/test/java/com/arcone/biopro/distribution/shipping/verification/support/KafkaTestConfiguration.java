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
    ReactiveKafkaProducerTemplate<String, Object> producerTemplateOrder(KafkaProperties kafkaProperties, ObjectMapper objectMapper , MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.<String, Object>create(props).withValueSerializer(new JsonSerializer<>(objectMapper)).maxInFlight(1)
            .producerListener(new MicrometerProducerListener(meterRegistry))
        );
    }

}
