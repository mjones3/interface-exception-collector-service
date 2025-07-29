package com.arcone.biopro.distribution.receiving.infrastructure.config;

import com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedOutputMessage;
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

    public static final String DLQ_PRODUCER = "dlq-producer";
    public static final String DEVICE_CREATED_CONSUMER = "device-created";
    public static final String DEVICE_UPDATED_CONSUMER = "device-updated";
    public static final String IMPORT_COMPLETED_PRODUCER = "import-completed-producer";
    public static final String SHIPMENT_COMPLETED_CONSUMER = "shipment-completed";



    @Bean
    NewTopic productsImportedTopic(
        @Value("${topics.receiving.products-imported.partitions:1}") Integer partitions,
        @Value("${topics.receiving.products-imported.replicas:1}") Integer replicas,
        @Value("${topics.receiving.products-imported.topic-name:ProductsImported}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic deviceCreatedTopic(
        @Value("${topics.device.device-created.partitions:1}") Integer partitions,
        @Value("${topics.device.device-created.replicas:1}") Integer replicas,
        @Value("${topics.device.device-created.topic-name:DeviceCreated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic deviceUpdatedTopic(
        @Value("${topics.device.device-updated.partitions:1}") Integer partitions,
        @Value("${topics.device.device-updated.replicas:1}") Integer replicas,
        @Value("${topics.device.device-updated.topic-name:DeviceUpdated}") String topicName
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
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, String>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }

    private ReceiverOptions<String, String> buildReceiverOptions(KafkaProperties kafkaProperties , String topicName){
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return ReceiverOptions.<String, String>create(props)
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(1)
            .subscription(List.of(topicName));
    }

    @Bean
    ReceiverOptions<String, String> deviceCreatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.device.device-created.topic-name:DeviceCreated}") String topicName) {
        return buildReceiverOptions(kafkaProperties, topicName);
    }

    @Bean
    ReceiverOptions<String, String> deviceUpdatedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.device.device-updated.topic-name:DeviceUpdated}") String topicName) {
        return buildReceiverOptions(kafkaProperties, topicName);
    }

    @Bean
    ReceiverOptions<String, String> shipmentCompletedReceiverOptions(KafkaProperties kafkaProperties
        , @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String shipmentCompletedTopicName) {
        return buildReceiverOptions(kafkaProperties, shipmentCompletedTopicName);
    }

    @Bean(DEVICE_CREATED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> deviceCreatedConsumerTemplate(
        ReceiverOptions<String, String> deviceCreatedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(deviceCreatedReceiverOptions);
    }

    @Bean(DEVICE_UPDATED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> deviceUpdatedConsumerTemplate(
        ReceiverOptions<String, String> deviceUpdatedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(deviceUpdatedReceiverOptions);
    }

    @Bean(SHIPMENT_COMPLETED_CONSUMER)
    ReactiveKafkaConsumerTemplate<String, String> shipmentCompletedConsumerTemplate(
        ReceiverOptions<String, String> shipmentCompletedReceiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(shipmentCompletedReceiverOptions);
    }

    @Bean(name = DLQ_PRODUCER )
    ReactiveKafkaProducerTemplate<String, String> dlqProducerTemplate(
        SenderOptions<String, String> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean
    SenderOptions<String, ProductsImportedOutputMessage> importProductsSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, ProductsImportedOutputMessage>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean(name = IMPORT_COMPLETED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, ProductsImportedOutputMessage> importProductsProducerTemplate(
        SenderOptions<String, ProductsImportedOutputMessage> importProductsSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(importProductsSenderOptions);
    }
}
