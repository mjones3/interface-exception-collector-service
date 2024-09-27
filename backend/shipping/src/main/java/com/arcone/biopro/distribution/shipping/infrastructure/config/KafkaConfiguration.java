package com.arcone.biopro.distribution.shipping.infrastructure.config;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedEventDTO;
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
public class KafkaConfiguration {

    public static final String SHIPMENT_COMPLETED_PRODUCER = "shipment-completed";
    public static final String SHIPMENT_CREATED_PRODUCER = "shipment-created";

    @Bean
    NewTopic orderFulfilledTopic(
        @Value("${topics.order.order-fulfilled.partitions:1}") Integer partitions,
        @Value("${topics.order.order-fulfilled.replicas:1}") Integer replicas,
        @Value("${topics.order.order-fulfilled.topic-name:OrderFulfilled}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic shipmentCompletedTopic(
        @Value("${topics.shipment.shipment-completed.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-completed.replicas:1}") Integer replicas ,
        @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic shipmentCreatedTopic(
        @Value("${topics.shipment.shipment-created.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-created.replicas:1}") Integer replicas ,
        @Value("${topics.shipment.shipment-created.topic-name:ShipmentCreated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> shippingServiceReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-fulfilled.topic-name:OrderFulfilled}") String topicName) {
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return ReceiverOptions.<String, String>create(props)
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(topicName));
    }



    @Bean
    ReactiveKafkaConsumerTemplate<String, String> shippingServiceConsumerTemplate(
        ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, ShipmentCompletedDTO> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, ShipmentCompletedDTO>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, ShipmentCreatedEventDTO> shipmentCreatedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, ShipmentCreatedEventDTO>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }


    @Bean(name = SHIPMENT_COMPLETED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, ShipmentCompletedDTO> producerTemplate(
        SenderOptions<String, ShipmentCompletedDTO> kafkaSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(kafkaSenderOptions);
    }

    @Bean(name = SHIPMENT_CREATED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> shipmentCreatedProducerTemplate(
        SenderOptions<String, ShipmentCreatedEventDTO> shipmentCreatedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(shipmentCreatedSenderOptions);
    }

}
