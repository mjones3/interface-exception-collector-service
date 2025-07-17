package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.shipping.infrastructure.event.ShipmentCompletedOutputEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedItemPayload;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedItemProductPayload;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedPayload;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;

import static java.util.Optional.ofNullable;
import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class ShipmentCompletedListener {

    private final ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutputEvent> producerTemplate;
    private final String topicName;

    public ShipmentCompletedListener(@Qualifier(KafkaConfiguration.SHIPMENT_COMPLETED_PRODUCER)
                                ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutputEvent> producerTemplate,
                                @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ShipmentCompleted",
        description = "Shipment Completed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.ShipmentCompletedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "ShipmentCreated",
            title = "ShipmentCreated",
            description = "Shipment Created Event Payload"
        ),payloadType = ShipmentCompletedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding

    @EventListener
    public void handleShipmentCompletedEvent(ShipmentCompletedEvent event) {
        log.debug("Shipment Completed event trigger Event ID {}", event.getEventId());

        var payload = event.getPayload();

        var message = new ShipmentCompletedOutputEvent(ShipmentCompletedPayload
                .builder()
                .shipmentId(payload.shipmentId())
                .orderNumber(payload.orderNumber())
                .externalOrderId(payload.externalOrderId())
                .performedBy(payload.performedBy())
                .locationCode(payload.locationCode())
                .locationName(payload.locationName())
                .customerCode(payload.customerCode())
                .customerName(payload.customerName())
                .departmentCode(payload.departmentCode())
                .productCategory(payload.productCategory())
                .deliveryType(payload.deliveryType())
                .createDate(payload.createDate())
                .labelStatus(payload.labelStatus())
                .quarantinedProducts(payload.quarantinedProducts())
                .shipmentType(payload.shipmentType())
                .lineItems( ofNullable(payload.lineItems())
                    .filter(items -> !items.isEmpty())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(lineItem -> ShipmentCompletedItemPayload
                        .builder()
                        .productFamily(lineItem.productFamily())
                        .quantity(lineItem.quantity())
                        .bloodType(lineItem.bloodType())
                        .products(ofNullable(lineItem.products())
                            .filter(items -> !items.isEmpty())
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .map(product -> ShipmentCompletedItemProductPayload
                                .builder()
                                .unitNumber(product.unitNumber())
                                .productCode(product.productCode())
                                .productFamily(product.productFamily())
                                .aboRh(product.aboRh())
                                .collectionDate(product.collectionDate())
                                .expirationDate(product.expirationDate())
                                .createDate(ZonedDateTime.now())
                                .productDescription(product.productDescription())
                                .build()).toList())
                        .build()).toList())
                .build());

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();

        log.debug("Shipment Completed event Sent {}", message);
    }
}
