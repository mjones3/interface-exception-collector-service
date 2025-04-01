package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentCreatedOutputEvent;
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
    public static final String RSP_SHIPMENT_CREATED_PRODUCER = "rps-shipment-created-producer";


    @Bean
    NewTopic shipmentCreatedTopic(
        @Value("${topics.recovered-plasma-shipment.shipment-created.partitions:1}") Integer partitions,
        @Value("${topics.recovered-plasma-shipment.shipment-created.replicas:1}") Integer replicas,
        @Value("${topics.recovered-plasma-shipment.shipment-created.topic-name:RecoveredPlasmaShipmentCreated}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
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
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, String>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }


    @Bean
    SenderOptions<String, RecoveredPlasmaShipmentCreatedOutputEvent> shipmentCreatedSenderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, RecoveredPlasmaShipmentCreatedOutputEvent>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @Bean(name = RSP_SHIPMENT_CREATED_PRODUCER )
    ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentCreatedOutputEvent> shipmentCreatedProducerTemplate(
        SenderOptions<String, RecoveredPlasmaShipmentCreatedOutputEvent> shipmentCreatedSenderOptions) {
        return new ReactiveKafkaProducerTemplate<>(shipmentCreatedSenderOptions);
    }

}
