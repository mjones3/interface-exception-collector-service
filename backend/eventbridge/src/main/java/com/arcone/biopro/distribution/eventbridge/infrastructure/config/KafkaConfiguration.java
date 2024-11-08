package com.arcone.biopro.distribution.eventbridge.infrastructure.config;

import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;
import java.util.List;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfiguration {
    public static final String SHIPMENT_COMPLETED_CONSUMER = "shipment-completed";
    public static final String DLQ_PRODUCER = "dql-producer";
    public static final String SHIPMENT_COMPLETED_OUTBOUND_PRODUCER = "shipment-completed-outbound";


    @Bean
    NewTopic shipmentCompletedTopic(
        @Value("${topics.shipment.shipment-completed.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-completed.replicas:1}") Integer replicas,
        @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic shipmentCompletedOutboundTopic(
        @Value("${topics.shipment.shipment-completed-outbound.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-completed-outbound.replicas:1}") Integer replicas ,
        @Value("${topics.shipment.shipment-completed-outbound.topic-name:ShipmentCompletedOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }


    @Bean
    ReceiverOptions<String, String> shipmentCompletedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String shipmentCompletedTopicName) {
        return buildReceiverOptions(kafkaProperties, shipmentCompletedTopicName);
    }


    private ReceiverOptions<String, String> buildReceiverOptions(KafkaProperties kafkaProperties , String topicName){
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return ReceiverOptions.<String, String>create(props)
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(topicName));
    }

    @Bean(SHIPMENT_COMPLETED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> shipmentCompletedConsumerTemplate(
        ReceiverOptions<String, String> shipmentCompletedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(shipmentCompletedReceiverOptions);
    }

    @Bean
    SenderOptions<String, String> senderOptions(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, String>create(props)
                .withValueSerializer(new JsonSerializer<>(objectMapper))
                .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    @Bean
    SenderOptions<String, ShipmentCompletedOutboundPayload> senderOptionsShipmentCompletedOutbound(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, ShipmentCompletedOutboundPayload>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    @Bean(name = DLQ_PRODUCER )
    ReactiveKafkaProducerTemplate<String, String> dlqProducerTemplate(
            SenderOptions<String, String> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean(name = SHIPMENT_COMPLETED_OUTBOUND_PRODUCER )
    ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundPayload> shipmentCompletedOutboundProducerTemplate(
        SenderOptions<String, ShipmentCompletedOutboundPayload> senderOptionsShipmentCompletedOutbound) {
        return new ReactiveKafkaProducerTemplate<>(senderOptionsShipmentCompletedOutbound);
    }
}
