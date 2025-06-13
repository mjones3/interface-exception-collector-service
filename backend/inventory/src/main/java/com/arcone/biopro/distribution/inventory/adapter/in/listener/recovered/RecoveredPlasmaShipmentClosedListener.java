package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaShipmentClosedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaShipmentClosedUseCase;
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
public class RecoveredPlasmaShipmentClosedListener extends AbstractListener<RecoveredPlasmaShipmentClosedInput, InventoryOutput, RecoveredPlasmaShipmentClosedMessage> {

    public RecoveredPlasmaShipmentClosedListener(
            @Qualifier("RECOVERED_PLASMA_SHIPMENT_CLOSED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
            RecoveredPlasmaShipmentClosedMessageMapper mapper,
            RecoveredPlasmaShipmentClosedUseCase useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<RecoveredPlasmaShipmentClosedMessage>> getMessageTypeReference() {
        return new TypeReference<>() {
        };
    }
}
