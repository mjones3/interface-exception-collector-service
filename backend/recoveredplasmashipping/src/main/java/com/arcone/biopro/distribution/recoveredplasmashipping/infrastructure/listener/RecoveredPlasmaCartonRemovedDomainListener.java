package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonRemovedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonRemovedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaCartonEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntityRepository;
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
import reactor.core.publisher.Mono;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class RecoveredPlasmaCartonRemovedDomainListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonRemovedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper;
    private final RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository;

    public RecoveredPlasmaCartonRemovedDomainListener(@Qualifier(KafkaConfiguration.RPS_CARTON_REMOVED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonRemovedOutputEvent> producerTemplate,
                                                      @Value("${topics.recovered-plasma-shipment.carton-removed.topic-name:RecoveredPlasmaCartonRemoved}") String topicName , RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper , RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaCartonEventMapper = recoveredPlasmaCartonEventMapper;
        this.recoveredPlasmaShipmentEntityRepository = recoveredPlasmaShipmentEntityRepository;

    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonRemoved",
        description = "Recovered Plasma Shipment Carton Removed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonRemovedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaCartonRemoved",
            title = "RecoveredPlasmaCartonRemoved",
            description = "Recovered Plasma Carton Removed Payload"
        ),payloadType = RecoveredPlasmaCartonRemovedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public Mono<Void> handleCartonRemovedEvent(RecoveredPlasmaCartonRemovedEvent event) {
        log.debug("Carton Removed event trigger Event ID {}", event);

        var payload = event.getPayload();

        return recoveredPlasmaShipmentEntityRepository.findById(payload.getShipmentId())
            .map(recoveredPlasmaShipmentEntity -> new RecoveredPlasmaCartonRemovedOutputEvent(recoveredPlasmaCartonEventMapper.modelToRemovedEventDTO(event.getPayload(), recoveredPlasmaShipmentEntity.getLocationCode() , recoveredPlasmaShipmentEntity.getProductType())))
            .map(eventPayload -> {
                log.debug("Carton Removed event sent {}",eventPayload);
                var producerRecord = new ProducerRecord<>(topicName, String.format("%s", eventPayload.getEventId()), eventPayload);
                return producerTemplate.send(producerRecord)
                    .log()
                    .subscribe();
            })
            .then();

    }
}
