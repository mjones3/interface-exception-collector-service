package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
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
public class ProductStoredListener extends AbstractListener<ProductStorageInput, InventoryOutput, EventMessage<ProductStoredMessage>> {

    UseCase<Mono<InventoryOutput>, ProductStorageInput> productStorageUseCase;
    ProductStoredMessageMapper productStoredMessageMapper;

    public ProductStoredListener(@Qualifier("PRODUCT_STORED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                 ObjectMapper objectMapper,
                                 ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                 @Value("${topic.product-stored.name}") String topic,
                                 ProductStoredMessageMapper productStoredMessageMapper,
                                 UseCase<Mono<InventoryOutput>, ProductStorageInput> productStorageUseCase) {
        super(consumer, objectMapper, producerDLQTemplate, topic, new TypeReference<>() {});
        this.productStoredMessageMapper = productStoredMessageMapper;
        this.productStorageUseCase = productStorageUseCase;
    }

    @Override
    protected Mono<InventoryOutput> processInput(ProductStorageInput input) {
        return productStorageUseCase.execute(input);

    }

    @Override
    protected ProductStorageInput fromMessageToInput(EventMessage<ProductStoredMessage> message) {
        return productStoredMessageMapper.toInput(message.payload());
    }
}
