package com.arcone.biopro.distribution.irradiation.adapter.in.listener.labelinvalidated;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.LabelInvalidatedInput;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;

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
public class LabelInvalidatedListener extends AbstractListener<LabelInvalidatedInput, InventoryOutput, LabelInvalidated> {

    public LabelInvalidatedListener(@Qualifier("LABEL_INVALIDATED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                  ObjectMapper objectMapper,
                                  ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                  LabelInvalidatedMessageMapper mapper,
                                  UseCase<Mono<InventoryOutput>, LabelInvalidatedInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<LabelInvalidated>> getMessageTypeReference() {
        return new TypeReference<EventMessage<LabelInvalidated>>() {};
    }
}
