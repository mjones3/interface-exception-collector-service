package com.arcone.biopro.distribution.irradiation.adapter.in.listener.checkin;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.dto.CheckInCompletedInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CheckInCompletedListener extends AbstractListener<CheckInCompletedInput, InventoryOutput, CheckInCompleted> {

    public CheckInCompletedListener(@Qualifier("CHECK_IN_COMPLETED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                    ObjectMapper objectMapper,
                                    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                    CheckInCompletedMessageMapper mapper,
                                    UseCase<Mono<InventoryOutput>, CheckInCompletedInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<CheckInCompleted>> getMessageTypeReference() {
        return new TypeReference<EventMessage<CheckInCompleted>>() {
        };
    }
}
