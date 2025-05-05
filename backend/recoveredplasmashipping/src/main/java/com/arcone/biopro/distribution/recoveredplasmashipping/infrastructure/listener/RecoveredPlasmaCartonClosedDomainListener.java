package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonPackedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaCartonEventMapper;
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
public class RecoveredPlasmaCartonClosedDomainListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonPackedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper;

    public RecoveredPlasmaCartonClosedDomainListener(@Qualifier(KafkaConfiguration.RPS_CARTON_CLOSED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonPackedOutputEvent> producerTemplate,
                                                     @Value("${topics.recovered-plasma-shipment.carton-closed.topic-name:RecoveredPlasmaCartonPacked}") String topicName , RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaCartonEventMapper = recoveredPlasmaCartonEventMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaCartonPacked",
        description = "Recovered Plasma Shipment Carton Packed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonPackedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaCartonPacked",
            title = "RecoveredPlasmaCartonPacked",
            description = "Recovered Plasma Carton Packed Payload"
        ),payloadType = RecoveredPlasmaCartonPackedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleCartonClosedEvent(RecoveredPlasmaCartonClosedEvent event) {
        log.debug("Carton Packed event trigger Event ID {}", event);

        var message =  new RecoveredPlasmaCartonPackedOutputEvent(recoveredPlasmaCartonEventMapper.modelToPackedEventDTO(event.getPayload()));

        log.debug("Carton Packed event sent {}",message);

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
