package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonRemovedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaCartonRemovedUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecoveredPlasmaCartonRemovedListener extends AbstractListener<RecoveredPlasmaCartonRemovedInput, InventoryOutput, RecoveredPlasmaCartonRemoved> {

    public RecoveredPlasmaCartonRemovedListener(
            @Qualifier("RECOVERED_PLASMA_CARTON_REMOVED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
            RecoveredPlasmaCartonRemovedMessageMapper mapper,
            RecoveredPlasmaCartonRemovedUseCase useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<RecoveredPlasmaCartonRemoved>> getMessageTypeReference() {
        return new TypeReference<EventMessage<RecoveredPlasmaCartonRemoved>>() {};
    }
}
