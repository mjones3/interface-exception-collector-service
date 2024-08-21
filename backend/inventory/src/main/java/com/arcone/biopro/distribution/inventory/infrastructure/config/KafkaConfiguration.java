package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.label.LabelAppliedMessage;
import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

import java.util.List;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
class KafkaConfiguration {

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Value("${topic.shipment-completed.name}")
    private String shipmentCompletedTopic;

    @Bean
    @Qualifier("LABEL_APPLIED")
    ReceiverOptions<String, String> labelAppliedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(labelAppliedTopic));
    }

    @Bean
    @Qualifier("SHIPMENT_COMPLETED")
    ReceiverOptions<String, String> shipmentCompletedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(shipmentCompletedTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "LabelApplied",
        description = "Label Applied has been listened and an inventory was created",
        payloadType = LabelAppliedMessage.class
    ))
    @Bean(name = "LABEL_APPLIED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> labelAppliedConsumerTemplate(
        @Qualifier("LABEL_APPLIED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ShipmentCompleted",
        description = "Shipment Completed has been listened and an inventory status was updated to SHIPPED",
        payloadType = ShipmentCompletedInput.class
    ))
    @Bean(name = "SHIPMENT_COMPLETED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> shipmentCompletedConsumerTemplate(
        @Qualifier("SHIPMENT_COMPLETED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, String>create(props)
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    ReactiveKafkaProducerTemplate<String, String> producerTemplate(SenderOptions<String, String> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }

}
