package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.DeviceCreated;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.ProductStored;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${topic.device.created.name}")
    private String deviceCreatedTopic;

    @Value("${topic.product.stored.name}")
    private String productStoredTopic;

    @Value("${topic.product.quarantine.name}")
    private String quarantineProductTopic;

    @Value("${topic.product.modified.name}")
    private String productModifiedTopic;

    @Bean
    @Qualifier("deviceCreatedTopic")
    ReceiverOptions<String, String> deviceCreatedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(deviceCreatedTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "DeviceCreated",
        description = "DeviceCreated has been listened and products were created",
        payloadType = DeviceCreated.class
    ))
    @Bean
    @Qualifier("deviceCreatedTopic")
    ReactiveKafkaConsumerTemplate<String, String> deviceCreatedConsumerTemplate(@Qualifier("deviceCreatedTopic") ReceiverOptions<String, String> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    @Qualifier("productStoredTopic")
    ReceiverOptions<String, String> productStoredReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(productStoredTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductStored",
        description = "ProductStored has been listened and logged",
        payloadType = ProductStored.class
    ))
    @Bean
    @Qualifier("productStoredTopic")
    ReactiveKafkaConsumerTemplate<String, String> productStoredConsumerTemplate(@Qualifier("productStoredTopic") ReceiverOptions<String, String> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, String>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    ReactiveKafkaProducerTemplate<String, String> producerTemplate(SenderOptions<String, String> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }

    @Bean
    SenderOptions<String, EventMessage<QuarantineProduct>> quarantineProductSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, EventMessage<QuarantineProduct>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1)
            .producerListener(new MicrometerProducerListener(meterRegistry));
    }

    @Bean
    SenderOptions<String, EventMessage<ProductModified>> productModifiedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, EventMessage<ProductModified>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1)
            .producerListener(new MicrometerProducerListener(meterRegistry));
    }

    @Bean
    NewTopic quarantineProductTopic(
        @Value("${topic.product.quarantine.partitions:1}") Integer partitions,
        @Value("${topic.product.quarantine.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name(quarantineProductTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    NewTopic productModifiedTopic(
        @Value("${topic.product.modified.partitions:1}") Integer partitions,
        @Value("${topic.product.modified.replicas:1}") Integer replicas
    ) {
        return TopicBuilder.name(productModifiedTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "QuarantineProduct",
        description = "Message for product quarantine process",
        payloadType = QuarantineProduct.class
    ))
    @Bean
    ReactiveKafkaProducerTemplate<String, EventMessage<QuarantineProduct>> producerQuarantineProductTemplate(
        SenderOptions<String, EventMessage<QuarantineProduct>> quarantineProductSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(quarantineProductSenderOptions);
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ProductModified",
        description = "Message for product modification event",
        payloadType = ProductModified.class
    ))
    @Bean
    ReactiveKafkaProducerTemplate<String, EventMessage<ProductModified>> producerProductModifiedTemplate(
        SenderOptions<String, EventMessage<ProductModified>> productModifiedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(productModifiedSenderOptions);
    }
}








