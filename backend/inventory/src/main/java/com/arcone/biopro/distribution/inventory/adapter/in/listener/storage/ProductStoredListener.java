package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
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
public class ProductStoredListener extends AbstractListener<ProductStorageInput, InventoryOutput, ProductStored> {

    public ProductStoredListener(@Qualifier("PRODUCT_STORED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                 ObjectMapper objectMapper,
                                 ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                 ProductStoredMessageMapper mapper,
                                 UseCase<Mono<InventoryOutput>, ProductStorageInput> useCase) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);

    }

    @Override
    protected TypeReference<EventMessage<ProductStored>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ProductStored>>() {};
    }
}
