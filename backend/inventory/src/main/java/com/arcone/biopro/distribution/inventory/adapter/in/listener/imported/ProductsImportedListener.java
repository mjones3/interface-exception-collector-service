package com.arcone.biopro.distribution.inventory.adapter.in.listener.imported;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsImportedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductsImportedListener extends AbstractListener<ProductsImportedInput, InventoryOutput, ProductsImportedMessage> {

    public ProductsImportedListener(@Qualifier("PRODUCTS_IMPORTED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                  ObjectMapper objectMapper,
                                  ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                  ProductsImportedMessageMapper mapper,
                                  UseCase<Mono<InventoryOutput>, ProductsImportedInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<ProductsImportedMessage>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ProductsImportedMessage>>() {};
    }
}