package com.arcone.biopro.distribution.order.infrastructure.config;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCreatedDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderRejectedDTO;
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
    ReceiverOptions<String, String> orderServiceReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.order.order-received.topic-name:OrderReceived}") String orderReceivedTopicName) {
        return buildReceiverOptions(kafkaProperties, orderReceivedTopicName);
    }

    @Bean
    ReceiverOptions<String, String> shipmentCreatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.shipment.shipment-created.topic-name:OrderReceived}") String shipmentCreatedTopicName) {
       return buildReceiverOptions(kafkaProperties, shipmentCreatedTopicName);
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
    SenderOptions<String, OrderRejectedDTO> rejectedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, OrderRejectedDTO>create(props)
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
    ReactiveKafkaProducerTemplate<String, OrderRejectedDTO> orderRejectedProducerTemplate(
        SenderOptions<String, OrderRejectedDTO> rejectedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(rejectedSenderOptions);
    }

    @Bean(name = ORDER_FULFILLED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, OrderFulfilledEventDTO> orderFulfilledProducerTemplate(
        SenderOptions<String, OrderFulfilledEventDTO> orderFulfilledSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(orderFulfilledSenderOptions);
    }

}
