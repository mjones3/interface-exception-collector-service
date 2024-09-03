package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.application.dto.QuarantineProductInput;
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
public class RemoveQuarantinedListener extends AbstractListener<QuarantineProductInput, InventoryOutput, EventMessage<RemoveQuarantinedMessage>> {

    UseCase<Mono<InventoryOutput>, QuarantineProductInput> removeQuarantinedProductUseCase;
    RemoveQuarantinedMessageMapper removeQuarantinedMessageMapper;

    public RemoveQuarantinedListener(@Qualifier("PRODUCT_REMOVE_QUARANTINED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                     ObjectMapper objectMapper,
                                     ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                     @Value("${topic.product-quarantined.name}") String productQuarantinedTopic,
                                     @Qualifier("REMOVE_QUARANTINE_USE_CASE") UseCase<Mono<InventoryOutput>, QuarantineProductInput> quarantinedProductUseCase,
                                     RemoveQuarantinedMessageMapper removeQuarantinedMessageMapper) {
        super(consumer, objectMapper, producerDLQTemplate, productQuarantinedTopic, new TypeReference<>() {
        });
        this.removeQuarantinedMessageMapper = removeQuarantinedMessageMapper;
        this.removeQuarantinedProductUseCase = quarantinedProductUseCase;
    }

    @Override
    protected Mono<InventoryOutput> processInput(QuarantineProductInput domainMessage) {
        return removeQuarantinedProductUseCase.execute(domainMessage);
    }

    @Override
    protected QuarantineProductInput fromMessageToInput(EventMessage<RemoveQuarantinedMessage> message) {
        return removeQuarantinedMessageMapper.toInput(message.payload());
    }
}
