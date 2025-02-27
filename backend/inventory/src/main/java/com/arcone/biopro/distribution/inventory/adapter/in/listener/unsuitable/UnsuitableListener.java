package com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UnsuitableListener extends AbstractListener<UnsuitableInput, Void, UnsuitableMessage> {

    public UnsuitableListener(@Qualifier("UNSUITABLE_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                              ObjectMapper objectMapper,
                              ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                              UnsuitableMessageMapper mapper,
                              UseCase<Mono<Void>, UnsuitableInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<UnsuitableMessage>> getMessageTypeReference() {
        return new TypeReference<EventMessage<UnsuitableMessage>>() {
        };
    }
}
