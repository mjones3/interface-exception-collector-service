package com.arcone.biopro.distribution.inventory.adapter.in.listener.created;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductCreatedListener extends AbstractListener<ProductCreatedInput, InventoryOutput, ProductCreatedMessage> {

    public ProductCreatedListener(@Qualifier("PRODUCT_CREATED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                           ObjectMapper objectMapper,
                           ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                           ProductCreatedMessageMapper mapper,
                           UseCase<Mono<InventoryOutput>, ProductCreatedInput>  useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<ProductCreatedMessage>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ProductCreatedMessage>>() {};
    }
}
