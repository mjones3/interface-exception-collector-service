package com.arcone.biopro.distribution.eventbridge.infrastructure.config;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.InventoryUpdatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCreatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.event.ShipmentCompletedOutboundOutputEvent;
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
    public static final String INVENTORY_UPDATED_CONSUMER = "inventory-updated";
    public static final String ORDER_CANCELLED_CONSUMER = "order-cancelled";
    public static final String ORDER_CREATED_CONSUMER = "order-created";
    public static final String ORDER_MODIFIED_CONSUMER = "order-modified";
    public static final String DLQ_PRODUCER = "dlq-producer";
    public static final String SHIPMENT_COMPLETED_OUTBOUND_PRODUCER = "shipment-completed-outbound";
    public static final String INVENTORY_UPDATED_OUTBOUND_PRODUCER = "inventory-updated-outbound";
    public static final String ORDER_CANCELLED_OUTBOUND_PRODUCER = "order-cancelled-outbound";
    public static final String ORDER_CREATED_OUTBOUND_PRODUCER = "order-created-outbound";
    public static final String ORDER_MODIFIED_OUTBOUND_PRODUCER = "order-modified-outbound";
    public static final String RPS_SHIPMENT_CLOSED_CONSUMER = "recovered-plasma-shipment-closed";
    public static final String RPS_SHIPMENT_CLOSED_OUTBOUND_PRODUCER = "recovered-plasma-shipment-closed-outbound";


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

    @Bean
    NewTopic inventoryUpdatedTopic(
        @Value("${topics.inventory.inventory-updated.partitions:1}") Integer partitions,
        @Value("${topics.inventory.inventory-updated.replicas:1}") Integer replicas,
        @Value("${topics.inventory.inventory-updated.topic-name:InventoryUpdated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic inventoryUpdatedOutboundTopic(
        @Value("${topics.inventory.inventory-updated-outbound.partitions:1}") Integer partitions,
        @Value("${topics.inventory.inventory-updated-outbound.replicas:1}") Integer replicas ,
        @Value("${topics.inventory.inventory-updated-outbound.topic-name:InventoryUpdatedOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic recoveredPlasmaShipmentClosedTopic(
        @Value("${topics.recovered-plasma-shipment.shipment-closed.partitions:1}") Integer partitions,
        @Value("${topics.recovered-plasma-shipment.shipment-closed.replicas:1}") Integer replicas,
        @Value("${topics.recovered-plasma-shipment.shipment-closed.topic-name:RecoveredPlasmaShipmentClosed}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> recoveredPlasmaShipmentClosedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.recovered-plasma-shipment.shipment-closed.topic-name:RecoveredPlasmaShipmentClosed}") String recoveredPlasmaShipmentClosedTopicName) {
        return buildReceiverOptions(kafkaProperties, recoveredPlasmaShipmentClosedTopicName);
    }

    @Bean
    NewTopic recoveredPlasmaShipmentOutboundClosedTopic(
        @Value("${topics.recovered-plasma-shipment.shipment-closed-outbound.partitions:1}") Integer partitions,
        @Value("${topics.recovered-plasma-shipment.shipment-closed-outbound.replicas:1}") Integer replicas,
        @Value("${topics.recovered-plasma-shipment.shipment-closed-outbound.topic-name:RecoveredPlasmaShipmentClosedOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> inventoryUpdatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.inventory.inventory-updated.topic-name:InventoryUpdated}") String inventoryUpdatedTopicName) {
        return buildReceiverOptions(kafkaProperties, inventoryUpdatedTopicName);
    }

    @Bean
    NewTopic orderCancelledTopic(
        @Value("${topics.order.order-cancelled.partitions:1}") Integer partitions,
        @Value("${topics.order.order-cancelled.replicas:1}") Integer replicas,
        @Value("${topics.order.order-cancelled.topic-name:OrderCancelled}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic orderCancelledOutboundTopic(
        @Value("${topics.order.order-cancelled-outbound.partitions:1}") Integer partitions,
        @Value("${topics.order.order-cancelled-outbound.replicas:1}") Integer replicas,
        @Value("${topics.order.order-cancelled-outbound.topic-name:OrderCancelledOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> orderCancelledReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-cancelled.topic-name:OrderCancelled}") String orderCancelledTopicName) {
        return buildReceiverOptions(kafkaProperties, orderCancelledTopicName);
    }

    @Bean
    NewTopic orderCreatedTopic(
        @Value("${topics.order.order-created.partitions:1}") Integer partitions,
        @Value("${topics.order.order-created.replicas:1}") Integer replicas,
        @Value("${topics.order.order-created.topic-name:OrderCreated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic orderCreatedOutboundTopic(
        @Value("${topics.order.order-created-outbound.partitions:1}") Integer partitions,
        @Value("${topics.order.order-created-outbound.replicas:1}") Integer replicas,
        @Value("${topics.order.order-created-outbound.topic-name:OrderCreatedOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> orderCreatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-created.topic-name:OrderCreated}") String orderCreatedTopicName) {
        return buildReceiverOptions(kafkaProperties, orderCreatedTopicName);
    }

    @Bean
    NewTopic orderModifiedTopic(
        @Value("${topics.order.order-modified.partitions:1}") Integer partitions,
        @Value("${topics.order.order-modified.replicas:1}") Integer replicas,
        @Value("${topics.order.order-modified.topic-name:OrderModified}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic orderModifiedOutboundTopic(
        @Value("${topics.order.order-modified-outbound.partitions:1}") Integer partitions,
        @Value("${topics.order.order-modified-outbound.replicas:1}") Integer replicas,
        @Value("${topics.order.order-modified-outbound.topic-name:OrderModifiedOutbound}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ReceiverOptions<String, String> orderModifiedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-modified.topic-name:OrderModified}") String orderModifiedTopicName) {
        return buildReceiverOptions(kafkaProperties, orderModifiedTopicName);
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

    @Bean(INVENTORY_UPDATED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> inventoryUpdatedConsumerTemplate(
        ReceiverOptions<String, String> inventoryUpdatedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(inventoryUpdatedReceiverOptions);
    }

    @Bean(ORDER_CANCELLED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> orderCancelledConsumerTemplate(
        ReceiverOptions<String, String> orderCancelledReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(orderCancelledReceiverOptions);
    }

    @Bean(ORDER_CREATED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> orderCreatedConsumerTemplate(
        ReceiverOptions<String, String> orderCreatedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(orderCreatedReceiverOptions);
    }

    @Bean(ORDER_MODIFIED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> orderModifiedConsumerTemplate(
        ReceiverOptions<String, String> orderModifiedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(orderModifiedReceiverOptions);
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
    SenderOptions<String, ShipmentCompletedOutboundOutputEvent> senderOptionsShipmentCompletedOutbound(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, ShipmentCompletedOutboundOutputEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    @Bean
    SenderOptions<String, EventMessage<InventoryUpdatedOutboundPayload>> senderOptionsInventoryUpdatedOutbound(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, EventMessage<InventoryUpdatedOutboundPayload>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }



    @Bean
    SenderOptions<String, EventMessage<OrderCreatedOutboundPayload>> senderOptionsOrderCreatedOutbound(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, EventMessage<OrderCreatedOutboundPayload>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    @Bean(name = DLQ_PRODUCER )
    ReactiveKafkaProducerTemplate<String, String> dlqProducerTemplate(
            SenderOptions<String, String> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean(name = SHIPMENT_COMPLETED_OUTBOUND_PRODUCER )
    ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundOutputEvent> shipmentCompletedOutboundProducerTemplate(
        SenderOptions<String, ShipmentCompletedOutboundOutputEvent> senderOptionsShipmentCompletedOutbound) {
        return new ReactiveKafkaProducerTemplate<>(senderOptionsShipmentCompletedOutbound);
    }

    @Bean(name = INVENTORY_UPDATED_OUTBOUND_PRODUCER )
    ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdatedOutboundPayload>> inventoryUpdatedOutboundProducerTemplate(
        SenderOptions<String, EventMessage<InventoryUpdatedOutboundPayload>> senderOptionsInventoryUpdatedOutbound) {
        return new ReactiveKafkaProducerTemplate<>(senderOptionsInventoryUpdatedOutbound);
    }

    @Bean(RPS_SHIPMENT_CLOSED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> recoveredPlasmaShipmentClosedConsumerTemplate(
        ReceiverOptions<String, String> recoveredPlasmaShipmentClosedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(recoveredPlasmaShipmentClosedReceiverOptions);
    }

    @Bean
    SenderOptions<String, com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent> senderOptionsRecoveredPlasmaShipmentClosedOutbound(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, RecoveredPlasmaShipmentClosedOutboundEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    @Bean(name = RPS_SHIPMENT_CLOSED_OUTBOUND_PRODUCER )
    ReactiveKafkaProducerTemplate<String, com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent> recoveredPlasmaShipmentClosedOutboundProducerTemplate(
        SenderOptions<String, com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent> senderOptionsRecoveredPlasmaShipmentClosedOutbound) {
        return new ReactiveKafkaProducerTemplate<>(senderOptionsRecoveredPlasmaShipmentClosedOutbound);
    }
}
