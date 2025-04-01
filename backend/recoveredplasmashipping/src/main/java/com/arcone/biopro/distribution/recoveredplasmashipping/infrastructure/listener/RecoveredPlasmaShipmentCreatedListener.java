package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentCreatedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEventMapper;
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
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class RecoveredPlasmaShipmentCreatedListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentCreatedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaShipmentEventMapper recoveredPlasmaShipmentEventMapper;

    public RecoveredPlasmaShipmentCreatedListener(@Qualifier(KafkaConfiguration.RSP_SHIPMENT_CREATED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentCreatedOutputEvent> producerTemplate,
                                                  @Value("${topics.recovered-plasma-shipment.shipment-created.topic-name:RecoveredPlasmaShipmentCreated}") String topicName , RecoveredPlasmaShipmentEventMapper recoveredPlasmaShipmentEventMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaShipmentEventMapper = recoveredPlasmaShipmentEventMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaShipmentCreated",
        description = "Recovered Plasma Shipment Created Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentCreatedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaShipmentCreated",
            title = "RecoveredPlasmaShipmentCreated",
            description = "Recovered Plasma Shipment Created Payload"
        ),payloadType = RecoveredPlasmaShipmentCreatedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleShipmentCreatedEvent(RecoveredPlasmaShipmentCreatedEvent event) {
        log.debug("Shipment Created event trigger Event ID {}", event.getEventId());

        var message =  new RecoveredPlasmaShipmentCreatedOutputEvent(recoveredPlasmaShipmentEventMapper.modelToEventDTO(event.getPayload()));

        log.debug("Shipment Created event sent {}",message);

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
