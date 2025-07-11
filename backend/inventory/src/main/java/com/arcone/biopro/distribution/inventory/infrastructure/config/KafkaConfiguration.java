package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin.CheckInCompleted;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.completed.apheresis.ApheresisPlasmaProductCompleted;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.completed.apheresis.ApheresisPlateletProductCompleted;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.completed.apheresis.ApheresisRBCProductCompleted;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.apheresis.ApheresisPlasmaProductCreated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.apheresis.ApheresisPlateletProductCreated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.apheresis.ApheresisRBCProductCreated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.wholeblood.WholeBloodProductCreated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded.ProductDiscarded;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.imported.ProductsImported;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.label.LabelApplied;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated.LabelInvalidated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.modified.ProductModified;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.ProductQuarantined;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.QuarantineRemoved;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.QuarantineUpdated;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.*;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment.ShipmentCompleted;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.storage.ProductStored;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable.ProductUnsuitable;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable.UnitUnsuitable;
import com.arcone.biopro.distribution.inventory.adapter.output.producer.event.InventoryUpdated;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
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

    @Value("${topic.products-imported.name}")
    private String productsImportedTopic;

    @Value("${topic.products-received.name}")
    private String productsReceivedTopic;

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
        payloadType = ProductUnsuitable.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "UnitUnsuitable",
        description = "Unit Unsuitable event to change inventories statuses to UNSUITABLE",
        payloadType = UnitUnsuitable.class
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
        payloadType = CheckInCompleted.class
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
        payloadType = ApheresisPlasmaProductCreated.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisRBCProductCreated",
        description = "Apheresis RBC Product has been created.",
        payloadType = ApheresisRBCProductCreated.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlateletProductCreated",
        description = "Apheresis Platelet Product has been created.",
        payloadType = ApheresisPlateletProductCreated.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "WholeBloodProductCreated",
        description = "Whole Blood Product Created Event Payload",
        payloadType = WholeBloodProductCreated.class
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
        payloadType = ApheresisPlasmaProductCompleted.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisRBCProductCompleted",
        description = "Apheresis RBC Product has been completed.",
        payloadType = ApheresisRBCProductCompleted.class
    ))
    @AsyncListener(operation = @AsyncOperation(
        channelName = "ApheresisPlateletProductCompleted",
        description = "Apheresis Platelet Product has been completed.",
        payloadType = ApheresisPlateletProductCompleted.class
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
        payloadType = LabelApplied.class
    ))
    @Bean(name = "LABEL_APPLIED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> labelAppliedConsumerTemplate(
        @Qualifier("LABEL_APPLIED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "LabelInvalidated",
        description = "Label was invalidated",
        payloadType = LabelInvalidated.class
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
        payloadType = ShipmentCompleted.class
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
        payloadType = ProductStored.class
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
        payloadType = ProductDiscarded.class
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
        payloadType = ProductRecovered.class
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
        payloadType = RecoveredPlasmaShipmentClosed.class
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
        payloadType = RecoveredPlasmaCartonPacked.class
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
        payloadType = RecoveredPlasmaCartonRemoved.class
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
        payloadType = RecoveredPlasmaCartonUnpacked.class
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
        payloadType = QuarantineRemoved.class
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
        payloadType = QuarantineUpdated.class
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
        payloadType = ProductQuarantined.class
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
        payloadType = ProductModified.class
    ))
    @Bean(name = "PRODUCT_MODIFIED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productModifiedConsumerTemplate(
        @Qualifier("PRODUCT_MODIFIED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    @Qualifier("PRODUCTS_IMPORTED")
    ReceiverOptions<String, String> productsImportedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productsImportedTopic));
    }

    @Bean
    @Qualifier("PRODUCTS_RECEIVED")
    ReceiverOptions<String, String> productsReceivedReceiverOptions(KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties(null))
            .subscription(List.of(productsReceivedTopic));
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductsImported",
        description = "Products have been imported.",
        payloadType = ProductsImported.class
    ))
    @Bean(name = "PRODUCTS_IMPORTED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productsImportedConsumerTemplate(
        @Qualifier("PRODUCTS_IMPORTED") ReceiverOptions<String, String> receiverOptions
    ) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ProductsReceived",
        description = "Products have been received.",
        payloadType = com.arcone.biopro.distribution.inventory.adapter.in.listener.received.ProductsReceived.class
    ))
    @Bean(name = "PRODUCTS_RECEIVED_CONSUMER")
    ReactiveKafkaConsumerTemplate<String, String> productsReceivedConsumerTemplate(
        @Qualifier("PRODUCTS_RECEIVED") ReceiverOptions<String, String> receiverOptions
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
    SenderOptions<String, EventMessage<InventoryUpdated>> inventoryUpdatedOption(KafkaProperties kafkaProperties,
                                                                                 ObjectMapper objectMapper,
                                                                                 MeterRegistry meterRegistry) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        return SenderOptions.<String, EventMessage<InventoryUpdated>>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1) // to keep ordering, prevent duplicate messages (and avoid data loss)
            .producerListener(new MicrometerProducerListener(meterRegistry)); // we want standard Kafka metrics
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "InventoryUpdated",
        description = "An inventory was created/updated.",
        payloadType = InventoryUpdated.class
    ))
    @Bean
    ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdated>> producerInventoryUpdatedTemplate(
        SenderOptions<String, EventMessage<InventoryUpdated>> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

}








