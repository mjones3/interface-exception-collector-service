package com.arcone.biopro.distribution.inventory.adapter.in.listener.completed;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductCompletedListener extends AbstractListener<ProductCompletedInput, InventoryOutput, ProductCompletedMessage> {

    public ProductCompletedListener(@Qualifier("PRODUCT_COMPLETED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                    ObjectMapper objectMapper,
                                    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                    ProductCompletedMessageMapper productCompletedMessageMapper,
                                    UseCase<Mono<InventoryOutput>, ProductCompletedInput>  productCompleteUseCase) {
        super(consumer, objectMapper, producerDLQTemplate, productCompleteUseCase, productCompletedMessageMapper);
    }

    @Override
    protected TypeReference<EventMessage<ProductCompletedMessage>> getMessageTypeReference() {
        return new TypeReference<>() {
        };
    }
}
