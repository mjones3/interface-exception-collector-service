package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.ShipmentCompletedOutboundMapper;
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
public class ShipmentCompletedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundPayload> producerTemplate;
    private final String topicName;
    private final ShipmentCompletedOutboundMapper shipmentCompletedOutboundMapper;

    public ShipmentCompletedOutboundEventListener(@Qualifier(KafkaConfiguration.SHIPMENT_COMPLETED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundPayload> producerTemplate,
                                     @Value("${topics.shipment.shipment-completed-outbound.topic-name:ShipmentCompletedOutbound}") String topicName
    ,ShipmentCompletedOutboundMapper shipmentCompletedOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.shipmentCompletedOutboundMapper = shipmentCompletedOutboundMapper;
    }


    @EventListener
    public void handleShipmentCompletedOutboundEvent(ShipmentCompletedOutboundEvent event) {
        log.debug("Shipment Completed Outbound event trigger Event ID {}", event.getEventId());

        var message = shipmentCompletedOutboundMapper.toDto(event.getPayload());

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
