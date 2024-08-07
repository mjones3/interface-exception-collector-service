package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LabelAppliedListener extends AbstractListener<InventoryInput, InventoryOutput, EventMessage<LabelAppliedMessage>> {

    UseCase<Mono<InventoryOutput>, InventoryInput> handler;
    LabelAppliedMessageMapper mapper;

    public LabelAppliedListener(@Qualifier("LABEL_APPLIED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                ObjectMapper objectMapper,
                                ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                @Value("${topic.label-applied.name}") String topic,
                                LabelAppliedMessageMapper mapper,
                                UseCase<Mono<InventoryOutput>, InventoryInput> handler) {
        super(consumer, objectMapper, producerDLQTemplate, topic, new TypeReference<>() {});
        this.mapper = mapper;
        this.handler = handler;
    }

    @Override
    protected Mono<InventoryOutput> processInput(InventoryInput input) {
        return handler.execute(input);
    }

    @Override
    protected InventoryInput fromMessageToInput(EventMessage<LabelAppliedMessage> message) {
        return mapper.toInput(message.payload());
    }
}
