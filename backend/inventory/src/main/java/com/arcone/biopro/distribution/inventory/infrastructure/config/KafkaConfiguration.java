package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin.CheckInCompletedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ProductCreatedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded.ProductDiscardedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.label.LabelAppliedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.AddQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.RemoveQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.UpdateQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.ProductRecoveredMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable.UnsuitableMessage;
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

    @Value("${topic.product-unsuitable.name}")
    private String productUnsuitableTopic;

    @Value("${topic.unit-unsuitable.name}")
    private String unitUnsuitableTopic;

    @Value("${topic.check-in.completed.name}")
    private String checkInCompletedTopic;

    @Value("${topic.product-created.apheresis.plasma.name}")
    private String apheresisPlasmaProductCreatedTopic;

    @Value("${topic.product-created.apheresis.rbc.name}")
    private String apheresisRBCProductCreatedTopic;

    @Value("${topic.product-created.wholeblood.name}")
    private String wholebloodCreatedTopic;

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Value("${topic.shipment-completed.name}")
    private String shipmentCompletedTopic;

    @Value("${topic.product-stored.name}")
    private String productStoredTopic;

    @Value("${topic.product-discarded.name}")
    private String productDiscardedTopic;

    @Value("${topic.product-recovered.name}")
    private String productRecoveredTopic;

    @Value("${topic.product-remove-quarantined.name}")
    private String removeQuarantinedTopic;

    @Value("${topic.product-update-quarantined.name}")
    private String updateQuarantinedTopic;

    @Value("${topic.product-quarantined.name}")
    private String addQuarantinedTopic;

    @Bean
    @Qualifier("UNSUITABLE")
    ReceiverOptions<String, String> unsuitableReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productUnsuitableTopic, unitUnsuitableTopic));
    }

    @Bean
    @Qualifier("CHECK_IN_COMPLETED")
    ReceiverOptions<String, String> checkInCompletedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(checkInCompletedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_CREATED")
    ReceiverOptions<String, String> productCreatedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(apheresisPlasmaProductCreatedTopic, apheresisRBCProductCreatedTopic, wholebloodCreatedTopic));
    }

    @Bean
    @Qualifier("LABEL_APPLIED")
    ReceiverOptions<String, String> labelAppliedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(labelAppliedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_STORED")
    ReceiverOptions<String, String> productStoredReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productStoredTopic));
    }

    @Bean
    @Qualifier("PRODUCT_DISCARDED")
    ReceiverOptions<String, String> productDiscardeddReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productDiscardedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_RECOVERED")
    ReceiverOptions<String, String> productRecoveredReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productRecoveredTopic));
    }

    @Bean
    @Qualifier("SHIPMENT_COMPLETED")
    ReceiverOptions<String, String> shipmentCompletedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(shipmentCompletedTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductUnsuitable",
        description = "Product Unsuitable event to change inventory status to UNSUITABLE",
        payloadType = UnsuitableMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "UnitUnsuitable",
        description = "Unit Unsuitable event to change inventories statuses to UNSUITABLE",
        payloadType = UnsuitableMessage.class
    ))
    @Bean(name = "UNSUITABLE_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> unsuitableConsumerTemplate(
        @Qualifier("UNSUITABLE") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "CheckInCompleted",
        description = "CheckIn completed product has been created.",
        payloadType = CheckInCompletedMessage.class
    ))
    @Bean(name = "CHECK_IN_COMPLETED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> checkInCompletedConsumerTemplate(
        @Qualifier("CHECK_IN_COMPLETED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlasmaProductCreated",
        description = "Apheresis Plasma Product has been created.",
        payloadType = ProductCreatedMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisRBCProductCreated",
        description = "Apheresis RBC Product has been created.",
        payloadType = ProductCreatedMessage.class
    ))
    @Bean(name = "PRODUCT_CREATED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productCreatedConsumerTemplate(
        @Qualifier("PRODUCT_CREATED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
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

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductStored",
        description = "Product Stored has been listened and an storage is created",
        payloadType = LabelAppliedMessage.class
    ))
    @Bean(name = "PRODUCT_STORED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productStoredConsumerTemplate(
        @Qualifier("PRODUCT_STORED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductDiscarded",
        description = "Product Discarded has been listened and an inventory status was updated to discarded",
        payloadType = ProductDiscardedMessage.class
    ))
    @Bean(name = "PRODUCT_DISCARDED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productDiscardedConsumerTemplate(
        @Qualifier("PRODUCT_DISCARDED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductRecovered",
        description = "Product Recovered has been listened and an inventory status was restored",
        payloadType = ProductRecoveredMessage.class
    ))
    @Bean(name = "PRODUCT_RECOVERED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productRecoveredConsumerTemplate(
        @Qualifier("PRODUCT_RECOVERED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    @Qualifier("PRODUCT_REMOVE_QUARANTINED")
    ReceiverOptions<String, String> productRemoveQuarantinedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(removeQuarantinedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_UPDATE_QUARANTINED")
    ReceiverOptions<String, String> productUpdateQuarantinedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(updateQuarantinedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_ADD_QUARANTINED")
    ReceiverOptions<String, String> productAddQuarantinedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(addQuarantinedTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "QuarantineRemoved",
        description = "Product Quarantine is removed.",
        payloadType = RemoveQuarantinedMessage.class
    ))
    @Bean(name = "PRODUCT_REMOVE_QUARANTINED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productRemoveQuarantinedConsumerTemplate(
        @Qualifier("PRODUCT_REMOVE_QUARANTINED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "QuarantineUpdated",
        description = "Product Quarantine is Updated.",
        payloadType = UpdateQuarantinedMessage.class
    ))
    @Bean(name = "PRODUCT_UPDATE_QUARANTINED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productUpdateQuarantinedConsumerTemplate(
        @Qualifier("PRODUCT_UPDATE_QUARANTINED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductQuarantined",
        description = "Product Quarantine is added.",
        payloadType = AddQuarantinedMessage.class
    ))
    @Bean(name = "PRODUCT_ADD_QUARANTINED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productAddQuarantinedConsumerTemplate(
        @Qualifier("PRODUCT_ADD_QUARANTINED") ReceiverOptions<String, String> receiverOptions
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
