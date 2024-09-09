package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductRecoveredInput;
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
public class ProductRecoveredListener extends AbstractListener<ProductRecoveredInput, InventoryOutput, EventMessage< ProductRecoveredMessage>> {

    UseCase<Mono<InventoryOutput>, ProductRecoveredInput> productRecoveredUseCase;
     ProductRecoveredMessageMapper productMessageMapper;

    public ProductRecoveredListener(@Qualifier("PRODUCT_RECOVERED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                    ObjectMapper objectMapper,
                                    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                    @Value("${topic.product-recovered.name}") String topic,
                                     ProductRecoveredMessageMapper productMessageMapper,
                                    UseCase<Mono<InventoryOutput>, ProductRecoveredInput> productRecoveredUseCase) {
        super(consumer, objectMapper, producerDLQTemplate, topic, new TypeReference<>() {});
        this.productMessageMapper = productMessageMapper;
        this.productRecoveredUseCase = productRecoveredUseCase;
    }

    @Override
    protected Mono<InventoryOutput> processInput(ProductRecoveredInput input) {
        return productRecoveredUseCase.execute(input);

    }

    @Override
    protected ProductRecoveredInput fromMessageToInput(EventMessage<ProductRecoveredMessage> message) {
        return productMessageMapper.toInput(message.payload());
    }
}
