package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentClosedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CartonItemEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntityRepository;
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
public class RecoveredPlasmaShipmentClosedListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentClosedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaShipmentEventMapper recoveredPlasmaShipmentEventMapper;
    private final CartonItemEntityRepository cartonItemEntityRepository;
    private final CartonItemEntityMapper cartonItemEntityMapper;

    public RecoveredPlasmaShipmentClosedListener(@Qualifier(KafkaConfiguration.RPS_SHIPMENT_CLOSED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentClosedOutputEvent> producerTemplate,
                                                 @Value("${topics.recovered-plasma-shipment.shipment-closed.topic-name:RecoveredPlasmaShipmentClosed}") String topicName
        , RecoveredPlasmaShipmentEventMapper recoveredPlasmaShipmentEventMapper
        , CartonItemEntityRepository cartonItemEntityRepository , CartonItemEntityMapper cartonItemEntityMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaShipmentEventMapper = recoveredPlasmaShipmentEventMapper;
        this.cartonItemEntityRepository = cartonItemEntityRepository;
        this.cartonItemEntityMapper = cartonItemEntityMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaShipmentClosed",
        description = "Recovered Plasma Shipment Closed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentClosedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaShipmentClosed",
            title = "RecoveredPlasmaShipmentClosed",
            description = "Recovered Plasma Shipment Closed Payload"
        ),payloadType = RecoveredPlasmaShipmentClosedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleShipmentClosedEvent(RecoveredPlasmaShipmentClosedEvent event) {
        log.debug("Shipment Closed event trigger Event ID {}", event);

        // todo loop into cartons from shipment model from event payload and populate carton items


/*

            Flux.fromIterable(event.getPayload().getCartonList())
            .flatMap(carton -> Flux.from(cartonItemEntityRepository.findAllByCartonIdOrderByCreateDateAsc(carton.getId()))
                .map(cartonItemEntityMapper::entityToModel)
                .collectList()
                .map(carton.getProducts()::addAll)
*/

        var message =  new RecoveredPlasmaShipmentClosedOutputEvent(recoveredPlasmaShipmentEventMapper.modelToCloseEventDTO(event.getPayload()));

        log.debug("Shipment Closed event sent {}",message);

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
