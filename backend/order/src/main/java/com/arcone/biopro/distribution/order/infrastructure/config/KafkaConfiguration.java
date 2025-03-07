package com.arcone.biopro.distribution.order.infrastructure.config;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCompletedDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCreatedDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderRejectedDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderCancelledOutputEvent;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderModifiedOutputEvent;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderRejectedOutputEvent;
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

    public static final String ORDER_CREATED_PRODUCER = "order-created";
    public static final String ORDER_REJECTED_PRODUCER = "order-rejected";
    public static final String ORDER_FULFILLED_PRODUCER = "order-fulfilled";
    public static final String ORDER_RECEIVED_CONSUMER = "order-received";
    public static final String SHIPMENT_CREATED_CONSUMER = "shipment-created";
    public static final String SHIPMENT_COMPLETED_CONSUMER = "shipment-completed";
    public static final String ORDER_COMPLETED_PRODUCER = "order-completed";
    public static final String CANCEL_ORDER_RECEIVED_CONSUMER = "cancel-order-received";
    public static final String DLQ_PRODUCER = "dlq-producer";
    public static final String ORDER_CANCELLED_PRODUCER = "order-cancelled";
    public static final String MODIFY_ORDER_RECEIVED_CONSUMER = "modify-order-received";
    public static final String ORDER_MODIFIED_PRODUCER = "order-modified";

    @Bean
    NewTopic orderReceivedTopic(
        @Value("${topics.order.order-received.partitions:1}") Integer partitions,
        @Value("${topics.order.order-received.replicas:1}") Integer replicas,
        @Value("${topics.order.order-received.topic-name:OrderReceived}") String orderReceivedTopicName
    ) {
        return TopicBuilder.name(orderReceivedTopicName).partitions(partitions).replicas(replicas).build();
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
    NewTopic orderRejectedTopic(
        @Value("${topics.order.order-rejected.partitions:1}") Integer partitions,
        @Value("${topics.order.order-rejected.replicas:1}") Integer replicas,
        @Value("${topics.order.order-rejected.topic-name:OrderRejected}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic orderFulfilledTopic(
        @Value("${topics.order.order-fulfilled.partitions:1}") Integer partitions,
        @Value("${topics.order.order-fulfilled.replicas:1}") Integer replicas,
        @Value("${topics.order.order-fulfilled.topic-name:OrderFulfilled}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic shipmentCreatedTopic(
        @Value("${topics.shipment.shipment-created.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-created.replicas:1}") Integer replicas,
        @Value("${topics.shipment.shipment-created.topic-name:ShipmentCreated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic shipmentCompletedTopic(
        @Value("${topics.shipment.shipment-completed.partitions:1}") Integer partitions,
        @Value("${topics.shipment.shipment-completed.replicas:1}") Integer replicas,
        @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic orderCompletedTopic(
        @Value("${topics.order.order-completed.partitions:1}") Integer partitions,
        @Value("${topics.order.order-completed.replicas:1}") Integer replicas,
        @Value("${topics.order.order-completed.topic-name:OrderCompleted}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic cancelOrderReceivedTopic(
        @Value("${topics.order.cancel-order-received.partitions:1}") Integer partitions,
        @Value("${topics.order.cancel-order-received.replicas:1}") Integer replicas,
        @Value("${topics.order.cancel-order-received.topic-name:CancelOrderReceived}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
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
    NewTopic modifyOrderReceivedTopic(
        @Value("${topics.order.modify-order-received.partitions:1}") Integer partitions,
        @Value("${topics.order.modify-order-received.replicas:1}") Integer replicas,
        @Value("${topics.order.modify-order-received.topic-name:ModifyOrderReceived}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
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
    ReceiverOptions<String, String> orderServiceReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-received.topic-name:OrderReceived}") String orderReceivedTopicName) {
        return buildReceiverOptions(kafkaProperties, orderReceivedTopicName);
    }

    @Bean
    ReceiverOptions<String, String> shipmentCreatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.shipment.shipment-created.topic-name:ShipmentCreated}") String shipmentCreatedTopicName) {
       return buildReceiverOptions(kafkaProperties, shipmentCreatedTopicName);
    }

    @Bean
    ReceiverOptions<String, String> shipmentCompletedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String shipmentCompletedTopicName) {
        return buildReceiverOptions(kafkaProperties, shipmentCompletedTopicName);
    }

    @Bean
    ReceiverOptions<String, String> cancelOrderReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.cancel-order-received.topic-name:CancelOrderReceived}") String cancelOrderReceivedTopicName) {
        return buildReceiverOptions(kafkaProperties, cancelOrderReceivedTopicName);
    }

    @Bean
    ReceiverOptions<String, String> modifyOrderReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.modify-order-received.topic-name:ModifyOrderReceived}") String modifyOrderReceivedTopicName) {
        return buildReceiverOptions(kafkaProperties, modifyOrderReceivedTopicName);
    }


    private ReceiverOptions<String, String> buildReceiverOptions(KafkaProperties kafkaProperties , String topicName){
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return ReceiverOptions.<String, String>create(props)
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(topicName));
    }

    @Bean(ORDER_RECEIVED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> orderServiceConsumerTemplate(
        ReceiverOptions<String, String> orderServiceReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(orderServiceReceiverOptions);
    }

    @Bean(SHIPMENT_CREATED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> shipmentCreatedConsumerTemplate(
        ReceiverOptions<String, String> shipmentCreatedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(shipmentCreatedReceiverOptions);
    }

    @Bean(SHIPMENT_COMPLETED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> shipmentCompletedConsumerTemplate(
        ReceiverOptions<String, String> shipmentCompletedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(shipmentCompletedReceiverOptions);
    }

    @Bean(CANCEL_ORDER_RECEIVED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> cancelOrderReceivedConsumerTemplate(
        ReceiverOptions<String, String> cancelOrderReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(cancelOrderReceiverOptions);
    }

    @Bean(MODIFY_ORDER_RECEIVED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> modifyOrderReceivedConsumerTemplate(
        ReceiverOptions<String, String> modifyOrderReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(modifyOrderReceiverOptions);
    }

    @Bean
    SenderOptions<String, OrderCreatedDTO> createdSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderCreatedDTO>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, OrderCompletedDTO> completedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderCompletedDTO>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, OrderCancelledOutputEvent> cancelledSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderCancelledOutputEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, OrderModifiedOutputEvent> modifiedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderModifiedOutputEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean
    SenderOptions<String, OrderRejectedOutputEvent> rejectedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderRejectedOutputEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }


    @Bean
    SenderOptions<String, OrderFulfilledEventDTO> orderFulfilledSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderFulfilledEventDTO>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean(name = ORDER_CREATED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> orderCreatedProducerTemplate(
        SenderOptions<String, OrderCreatedDTO> createdSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(createdSenderOptions);
    }

    @Bean(name = ORDER_REJECTED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderRejectedOutputEvent> orderRejectedProducerTemplate(
        SenderOptions<String, OrderRejectedOutputEvent> rejectedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(rejectedSenderOptions);
    }

    @Bean(name = ORDER_FULFILLED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderFulfilledEventDTO> orderFulfilledProducerTemplate(
        SenderOptions<String, OrderFulfilledEventDTO> orderFulfilledSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(orderFulfilledSenderOptions);
    }

    @Bean(name = ORDER_COMPLETED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderCompletedDTO> orderCompletedProducerTemplate(
        SenderOptions<String, OrderCompletedDTO> completedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(completedSenderOptions);
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

    @Bean(name = DLQ_PRODUCER )
    ReactiveKafkaProducerTemplate<String, String> dlqProducerTemplate(
        SenderOptions<String, String> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean(name = ORDER_CANCELLED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderCancelledOutputEvent> orderCancelledProducerTemplate(
        SenderOptions<String, OrderCancelledOutputEvent> cancelledSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(cancelledSenderOptions);
    }

    @Bean(name = ORDER_MODIFIED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderModifiedOutputEvent> orderModifiedProducerTemplate(
        SenderOptions<String, OrderModifiedOutputEvent> modifiedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(modifiedSenderOptions);
    }

}
