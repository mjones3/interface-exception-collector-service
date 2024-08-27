package com.arcone.biopro.distribution.order.verification.support;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
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
    SenderOptions<String, OrderReceivedEventDTO> senderOptions(KafkaProperties kafkaProperties, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderReceivedEventDTO>create(props).withValueSerializer(new JsonSerializer<>(objectMapper)).maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, ShipmentCreatedEventDTO> senderShipmentCreatedOptions(KafkaProperties kafkaProperties, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, ShipmentCreatedEventDTO>create(props).withValueSerializer(new JsonSerializer<>(objectMapper)).maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    @Qualifier("partner-order")
    ReactiveKafkaProducerTemplate<String, OrderReceivedEventDTO> producerTemplate(SenderOptions<String, OrderReceivedEventDTO> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }

    @Bean
    @Qualifier("shipment-created")
    ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> shipmentCreatedProducerTemplate(SenderOptions<String, ShipmentCreatedEventDTO> senderShipmentCreatedOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderShipmentCreatedOptions);
    }


}
