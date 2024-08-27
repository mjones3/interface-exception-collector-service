package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedEventDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedPayloadDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("prod")
public class ShipmentCreatedEventListener {


    private final ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> producerTemplate;
    private final String topicName;

    public ShipmentCreatedEventListener(@Qualifier(KafkaConfiguration.SHIPMENT_CREATED_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> producerTemplate,
                                     @Value("${topics.shipment.shipment-created.topic-name:ShipmentCreated}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @EventListener
    public void handleShipmentCreatedEvent(ShipmentCreatedEvent event) {
        log.info("Shipment Created event trigger Event ID {}", event.getEventId());

        var shipment = event.getPayload();

        var message = ShipmentCreatedEventDTO
            .builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .eventVersion(event.getEventVersion())
            .occurredOn(event.getOccurredOn())
            .payload(ShipmentCreatedPayloadDTO
                .builder()
                .orderNumber(shipment.getOrderNumber())
                .shipmentId(shipment.getId())
                .shipmentStatus(shipment.getStatus().name())
                .build())
            .build();

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
