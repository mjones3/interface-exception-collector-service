package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.event.ShipmentCompletedOutboundOutputEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.ShipmentCompletedOutboundMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
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
public class ShipmentCompletedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String,ShipmentCompletedOutboundOutputEvent> producerTemplate;
    private final String topicName;
    private final ShipmentCompletedOutboundMapper shipmentCompletedOutboundMapper;

    public ShipmentCompletedOutboundEventListener(@Qualifier(KafkaConfiguration.SHIPMENT_COMPLETED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundOutputEvent> producerTemplate,
                                     @Value("${topics.shipment.shipment-completed-outbound.topic-name:ShipmentCompletedOutbound}") String topicName
    ,ShipmentCompletedOutboundMapper shipmentCompletedOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.shipmentCompletedOutboundMapper = shipmentCompletedOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ShipmentCompletedOutbound",
        description = "Shipment Completed Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "ShipmentCompletedOutbound",
            title = "ShipmentCompletedOutbound",
            description = "Shipment Completed Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleShipmentCompletedOutboundEvent(ShipmentCompletedOutboundEvent event) {
        log.debug("Shipment Completed Outbound event trigger Event ID {}", event.getEventId());

        var message = new ShipmentCompletedOutboundOutputEvent(shipmentCompletedOutboundMapper.toDto(event.getPayload()));

        log.debug("Outbound Payload {}",message);

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
