package com.arcone.biopro.distribution.irradiation.adapter.in.listener.label;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
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
public class LabelAppliedListener extends AbstractListener<InventoryInput, InventoryOutput, LabelApplied> {

    public LabelAppliedListener(@Qualifier("LABEL_APPLIED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                ObjectMapper objectMapper,
                                ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                LabelAppliedMessageMapper mapper,
                                UseCase<Mono<InventoryOutput>, InventoryInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<LabelApplied>> getMessageTypeReference() {
        return new TypeReference<EventMessage<LabelApplied>>() {};
    }
}
