package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin.CheckInCompletedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.completed.ProductCompletedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ProductCreatedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded.ProductDiscardedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.label.LabelAppliedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated.LabelInvalidatedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.modified.ProductModifiedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.AddQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.RemoveQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.UpdateQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.ProductRecoveredMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.RecoveredPlasmaCartonPackedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.RecoveredPlasmaCartonRemovedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.RecoveredPlasmaCartonUnpackedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.RecoveredPlasmaShipmentClosedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable.UnsuitableMessage;
import com.arcone.biopro.distribution.inventory.adapter.output.producer.event.InventoryUpdatedEvent;
import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
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
import org.springframework.kafka.support.serializer.JsonSerializer;
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

    @Value("${topic.product-created.apheresis.platelet.name}")
    private String apheresisPlateletProductCreatedTopic;

    @Value("${topic.product-completed.apheresis.plasma.name}")
    private String apheresisPlasmaProductCompletedTopic;

    @Value("${topic.product-completed.apheresis.rbc.name}")
    private String apheresisRBCProductCompletedTopic;

    @Value("${topic.product-completed.apheresis.platelet.name}")
    private String apheresisPlateletProductCompletedTopic;

    @Value("${topic.product-completed.wholeblood.name}")
    private String wholebloodCompletedTopic;

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

    @Value("${topic.recovered-plasma-carton-packed.name}")
    private String recoveredPlasmaCartonPackedTopic;

    @Value("${topic.recovered-plasma-shipment-closed.name}")
    private String recoveredPlasmaShipmentClosedTopic;

    @Value("${topic.recovered-plasma-carton-removed.name}")
    private String recoveredPlasmaCartonRemovedTopic;
    
    @Value("${topic.recovered-plasma-carton-unpacked.name}")
    private String recoveredPlasmaCartonUnpackedTopic;

    @Value("${topic.product-remove-quarantined.name}")
    private String removeQuarantinedTopic;

    @Value("${topic.product-update-quarantined.name}")
    private String updateQuarantinedTopic;

    @Value("${topic.product-quarantined.name}")
    private String addQuarantinedTopic;

    @Value("${topic.product-modified.name}")
    private String productModifiedTopic;

    @Value("${topic.label-invalidated.name}")
    private String labelInvalidatedTopic;

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
            .subscription(List.of(apheresisPlasmaProductCreatedTopic, apheresisRBCProductCreatedTopic, wholebloodCreatedTopic, apheresisPlateletProductCreatedTopic));
    }

    @Bean
    @Qualifier("PRODUCT_COMPLETED")
    ReceiverOptions<String, String> productCompletedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(apheresisPlasmaProductCompletedTopic, apheresisRBCProductCompletedTopic, wholebloodCompletedTopic, apheresisPlateletProductCompletedTopic));
    }

    @Bean
    @Qualifier("LABEL_APPLIED")
    ReceiverOptions<String, String> labelAppliedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(labelAppliedTopic));
    }

    @Bean
    @Qualifier("LABEL_INVALIDATED")
    ReceiverOptions<String, String> labelInvalidatedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(labelInvalidatedTopic));
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
    @Qualifier("RECOVERED_PLASMA_CARTON_PACKED")
    ReceiverOptions<String, String> recoveredPlasmaCartonPackedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(recoveredPlasmaCartonPackedTopic));
    }

    @Bean
    @Qualifier("RECOVERED_PLASMA_SHIPMENT_CLOSED")
    ReceiverOptions<String, String> recoveredPlasmaShipmentClosedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(recoveredPlasmaShipmentClosedTopic));
    }

    @Bean
    @Qualifier("RECOVERED_PLASMA_CARTON_REMOVED")
    ReceiverOptions<String, String> recoveredPlasmaCartonRemovedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(recoveredPlasmaCartonRemovedTopic));
    }
    
    @Bean
    @Qualifier("RECOVERED_PLASMA_CARTON_UNPACKED")
    ReceiverOptions<String, String> recoveredPlasmaCartonUnpackedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(recoveredPlasmaCartonUnpackedTopic));
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
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlateletProductCreated",
        description = "Apheresis Platelet Product has been created.",
        payloadType = ProductCreatedMessage.class
    ))

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ApheresisPlasmaProductCreated",
        description = "Apheresis Plasma Product Created Event",
        message = @AsyncMessage(
            name = "ApheresisPlasmaProductCreated",
            title = "ApheresisPlasmaProductCreated",
            description = "Apheresis Plasma Product Created Event Payload"
        ),payloadType = ProductCreatedMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "WholeBloodProductCreated",
        description = "Wholeblood Product has been created.",
        payloadType = ProductCreatedMessage.class
    ))
    @Bean(name = "PRODUCT_CREATED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productCreatedConsumerTemplate(
        @Qualifier("PRODUCT_CREATED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlasmaProductCompleted",
        description = "Apheresis Plasma Product has been completed.",
        payloadType = ProductCompletedMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisRBCProductCompleted",
        description = "Apheresis RBC Product has been completed.",
        payloadType = ProductCompletedMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlateletProductCompleted",
        description = "Apheresis Platelet Product has been completed.",
        payloadType = ProductCompletedMessage.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "WholeBloodProductCompleted",
        description = "Wholeblood Product has been completed.",
        payloadType = ProductCompletedMessage.class
    ))
    @Bean(name = "PRODUCT_COMPLETED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productCompletedConsumerTemplate(
        @Qualifier("PRODUCT_COMPLETED") ReceiverOptions<String, String> receiverOptions
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
        channelName = "Label Invalidated",
        description = "Label was invalidated",
        payloadType = LabelInvalidatedMessage.class
    ))
    @Bean(name = "LABEL_INVALIDATED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> labelInvalidatedConsumerTemplate(
        @Qualifier("LABEL_INVALIDATED") ReceiverOptions<String, String> receiverOptions
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

    @AsyncListener(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaShipmentClosed",
        description = "Recovered Plasma Shipment Closed event has been listened and inventory status was updated",
        payloadType = RecoveredPlasmaShipmentClosedMessage.class
    ))
    @Bean(name = "RECOVERED_PLASMA_SHIPMENT_CLOSED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> recoveredPlasmaShipmentClosedConsumerTemplate(
        @Qualifier("RECOVERED_PLASMA_SHIPMENT_CLOSED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonPacked",
        description = "Recovered Plasma Carton Packed event has been listened and inventory status was updated",
        payloadType = RecoveredPlasmaCartonPackedMessage.class
    ))
    @Bean(name = "RECOVERED_PLASMA_CARTON_PACKED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> recoveredPlasmaCartonPackedConsumerTemplate(
        @Qualifier("RECOVERED_PLASMA_CARTON_PACKED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonRemoved",
        description = "Recovered Plasma Carton Removed event has been listened and inventory status was updated",
        payloadType = RecoveredPlasmaCartonRemovedMessage.class
    ))
    @Bean(name = "RECOVERED_PLASMA_CARTON_REMOVED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> recoveredPlasmaCartonRemovedConsumerTemplate(
        @Qualifier("RECOVERED_PLASMA_CARTON_REMOVED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
    
    @AsyncListener(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonUnpacked",
        description = "Recovered Plasma Carton Unpacked event has been listened and inventory status was updated",
        payloadType = RecoveredPlasmaCartonUnpackedMessage.class
    ))
    @Bean(name = "RECOVERED_PLASMA_CARTON_UNPACKED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> recoveredPlasmaCartonUnpackedConsumerTemplate(
        @Qualifier("RECOVERED_PLASMA_CARTON_UNPACKED") ReceiverOptions<String, String> receiverOptions
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

    @Bean
    @Qualifier("PRODUCT_MODIFIED")
    ReceiverOptions<String, String> productModifiedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productModifiedTopic));
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

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductModified",
        description = "Product was modified.",
        payloadType = ProductModifiedMessage.class
    ))
    @Bean(name = "PRODUCT_MODIFIED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productModifiedConsumerTemplate(
        @Qualifier("PRODUCT_MODIFIED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper,
        MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
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
    SenderOptions<String, EventMessage<InventoryUpdatedEvent>> inventoryUpdatedOption(KafkaProperties kafkaProperties,
                                                                                      ObjectMapper objectMapper,
                                                                                      MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, EventMessage<InventoryUpdatedEvent>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "InventoryUpdatedEvent",
        description = "An inventory was created/updated.",
        payloadType = InventoryUpdatedEvent.class
    ))
    @Bean
    ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdatedEvent>> producerInventoryUpdatedTemplate(
        SenderOptions<String, EventMessage<InventoryUpdatedEvent>> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

}








