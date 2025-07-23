package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.application.usecase.ProductStoredUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductStoredListener extends AbstractListener<ProductStoredUseCase.Input, Void, ProductStored> {

    public ProductStoredListener(@Qualifier("productStoredTopic") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                ObjectMapper objectMapper,
                                ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                ProductStoredUseCase useCase,
                                ProductStoredMapper mapper) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<ProductStored>> getMessageTypeReference() {
        return new TypeReference<EventMessage<ProductStored>>() {};
    }
}
