package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.shipping.infrastructure.event.ShipmentCreatedOutputEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedPayloadDTO;
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

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class ShipmentCreatedEventListener {


    private final ReactiveKafkaProducerTemplate<String, ShipmentCreatedOutputEvent> producerTemplate;
    private final String topicName;

    public ShipmentCreatedEventListener(@Qualifier(KafkaConfiguration.SHIPMENT_CREATED_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, ShipmentCreatedOutputEvent> producerTemplate,
                                     @Value("${topics.shipment.shipment-created.topic-name:ShipmentCreated}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ShipmentCreated",
        description = "Shipment Created Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.ShipmentCreatedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "ShipmentCreated",
            title = "ShipmentCreated",
            description = "Shipment Created Event Payload"
        ),payloadType = ShipmentCreatedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding

    @EventListener
    public void handleShipmentCreatedEvent(ShipmentCreatedEvent event) {
        log.info("Shipment Created event trigger Event ID {}", event.getEventId());

        var shipment = event.getPayload();

        var message = new ShipmentCreatedOutputEvent(
            ShipmentCreatedPayloadDTO
                .builder()
                .orderNumber(shipment.getOrderNumber())
                .shipmentId(shipment.getId())
                .shipmentStatus(shipment.getStatus().name())
                .build()
            );

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
