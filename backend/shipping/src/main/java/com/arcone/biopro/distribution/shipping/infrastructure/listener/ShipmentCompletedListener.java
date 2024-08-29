package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedPayload;
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
public class ShipmentCompletedListener {

    private final ReactiveKafkaProducerTemplate<String, ShipmentCompletedDTO> producerTemplate;
    private final String topicName;

    public ShipmentCompletedListener(@Qualifier(KafkaConfiguration.SHIPMENT_COMPLETED_PRODUCER)
                                ReactiveKafkaProducerTemplate<String, ShipmentCompletedDTO> producerTemplate,
                                @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @EventListener
    public void handleShipmentCompletedEvent(ShipmentCompletedEvent event) {
        log.info("Shipment Completed event trigger Event ID {}", event.getEventId());

        var payload = event.getPayload();

        var message = ShipmentCompletedDTO
            .builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .eventVersion(event.getEventVersion())
            .occurredOn(event.getOccurredOn())
            .payload(ShipmentCompletedPayload
                .builder()
                .orderNumber(payload.orderNumber())
                .shipmentId(payload.shipmentId())
                .createDate(payload.createDate())
                .performedBy(payload.performedBy())
                .productCode(payload.productCode())
                .unitNumber(payload.unitNumber())
                .bloodType(payload.bloodType())
                .productFamily(payload.productFamily())
                .build())
            .build();

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
