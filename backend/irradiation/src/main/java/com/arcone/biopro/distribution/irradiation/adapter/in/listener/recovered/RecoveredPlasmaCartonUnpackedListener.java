package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.RecoveredPlasmaCartonUnpackedInput;
import com.arcone.biopro.distribution.irradiation.application.usecase.RecoveredPlasmaCartonUnpackedUseCase;
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
public class RecoveredPlasmaCartonUnpackedListener extends AbstractListener<RecoveredPlasmaCartonUnpackedInput, InventoryOutput, RecoveredPlasmaCartonUnpacked> {

    public RecoveredPlasmaCartonUnpackedListener(
            @Qualifier("RECOVERED_PLASMA_CARTON_UNPACKED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
            RecoveredPlasmaCartonUnpackedMessageMapper mapper,
            RecoveredPlasmaCartonUnpackedUseCase useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<RecoveredPlasmaCartonUnpacked>> getMessageTypeReference() {
        return new TypeReference<EventMessage<RecoveredPlasmaCartonUnpacked>>() {};
    }
}
