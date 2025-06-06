package com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedOutput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShipmentCompletedListener extends AbstractListener<ShipmentCompletedInput, ShipmentCompletedOutput, ShipmentCompleted> {

    public ShipmentCompletedListener(@Qualifier("SHIPMENT_COMPLETED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                     ObjectMapper objectMapper,
                                     ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                     ShipmentCompletedMessageMapper mapper,
                                     UseCase<Mono<ShipmentCompletedOutput>, ShipmentCompletedInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<ShipmentCompleted>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ShipmentCompleted>>() {};
    }
}
