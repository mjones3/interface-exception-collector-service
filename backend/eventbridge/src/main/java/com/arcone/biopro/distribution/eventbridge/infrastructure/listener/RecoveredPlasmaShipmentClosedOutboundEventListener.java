package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.RecoveredPlasmaShipmentClosedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.RecoveredPlasmaShipmentClosedOutboundEventMapper;
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
public class RecoveredPlasmaShipmentClosedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaShipmentClosedOutboundEventMapper recoveredPlasmaShipmentClosedOutboundEventMapper;

    public RecoveredPlasmaShipmentClosedOutboundEventListener(@Qualifier(KafkaConfiguration.RPS_SHIPMENT_CLOSED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent> producerTemplate,
                                                              @Value("${topics.recovered-plasma-shipment.shipment-closed-outbound.topic-name:RecoveredPlasmaShipmentClosedOutbound}") String topicName
    , RecoveredPlasmaShipmentClosedOutboundEventMapper recoveredPlasmaShipmentClosedOutboundEventMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaShipmentClosedOutboundEventMapper = recoveredPlasmaShipmentClosedOutboundEventMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaShipmentClosedOutbound",
        description = "Recovered Plasma Shipment Closed Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaShipmentClosedOutbound",
            title = "RecoveredPlasmaShipmentClosedOutbound",
            description = "Recovered Plasma Shipment Closed Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleRecoveredPlasmaShipmentClosedOutboundEvent(RecoveredPlasmaShipmentClosedOutboundEvent event) {
        log.debug("Recovered Plasma Shipment Closed Outbound event trigger Event ID {}", event.getEventId());

        var message = new com.arcone.biopro.distribution.eventbridge.infrastructure.event.RecoveredPlasmaShipmentClosedOutboundEvent(recoveredPlasmaShipmentClosedOutboundEventMapper.modelToCloseEventDTO(event.getPayload()));

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
