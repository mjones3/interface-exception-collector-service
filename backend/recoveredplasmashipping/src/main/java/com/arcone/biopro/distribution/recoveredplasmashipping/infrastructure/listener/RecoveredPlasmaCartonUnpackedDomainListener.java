package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonUnpackedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonUnpackedOutputEvent;
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
public class RecoveredPlasmaCartonUnpackedDomainListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonUnpackedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper;
    private final RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository;

    public RecoveredPlasmaCartonUnpackedDomainListener(@Qualifier(KafkaConfiguration.RPS_CARTON_UNPACKED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonUnpackedOutputEvent> producerTemplate,
                                                       @Value("${topics.recovered-plasma-shipment.carton-unpacked.topic-name:RecoveredPlasmaCartonUnpacked}") String topicName , RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper , RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaCartonEventMapper = recoveredPlasmaCartonEventMapper;
        this.recoveredPlasmaShipmentEntityRepository = recoveredPlasmaShipmentEntityRepository;

    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonUnpacked",
        description = "Recovered Plasma Shipment Carton Unpacked Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonUnpackedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaCartonUnpacked",
            title = "RecoveredPlasmaCartonUnpacked",
            description = "Recovered Plasma Carton Unpacked Payload"
        ),payloadType = RecoveredPlasmaCartonUnpackedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public Mono<Void> handleCartonUnpackedEvent(RecoveredPlasmaCartonUnpackedEvent event) {
        log.debug("Carton UnPacked event trigger Event ID {}", event);

        var payload = event.getPayload();

        return recoveredPlasmaShipmentEntityRepository.findById(payload.getShipmentId())
            .map(recoveredPlasmaShipmentEntity -> new RecoveredPlasmaCartonUnpackedOutputEvent(recoveredPlasmaCartonEventMapper.modelToUnPackedEventDTO(event.getPayload(), recoveredPlasmaShipmentEntity.getLocationCode())))
            .map(eventPayload -> {
                log.debug("Carton Unpacked event sent {}",eventPayload);
                var producerRecord = new ProducerRecord<>(topicName, String.format("%s", eventPayload.getEventId()), eventPayload);
                return producerTemplate.send(producerRecord)
                    .log()
                    .subscribe();
            })
            .then();

    }
}
