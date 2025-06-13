package com.arcone.biopro.distribution.inventory.adapter.in.listener.modified;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductModifiedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductModifiedListener extends AbstractListener<ProductModifiedInput, InventoryOutput, ProductModifiedMessage> {

    public ProductModifiedListener(@Qualifier("PRODUCT_MODIFIED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                           ObjectMapper objectMapper,
                           ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                           ProductModifiedMessageMapper mapper,
                           UseCase<Mono<InventoryOutput>, ProductModifiedInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<ProductModifiedMessage>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ProductModifiedMessage>>() {};
    }
}