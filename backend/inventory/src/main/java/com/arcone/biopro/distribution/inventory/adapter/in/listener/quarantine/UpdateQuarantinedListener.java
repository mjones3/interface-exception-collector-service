package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.AbstractListener;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.UpdateQuarantineInput;
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
public class UpdateQuarantinedListener extends AbstractListener<UpdateQuarantineInput, InventoryOutput, EventMessage<UpdateQuarantinedMessage>> {

    UseCase<Mono<InventoryOutput>, UpdateQuarantineInput> updateQuarantineUseCase;
    QuarantinedMessageMapper quarantinedMessageMapper;

    public UpdateQuarantinedListener(@Qualifier("PRODUCT_UPDATE_QUARANTINED_CONSUMER") ReactiveKafkaConsumerTemplate<String, String> consumer,
                                     ObjectMapper objectMapper,
                                     ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                     @Value("${topic.product-update-quarantined.name}") String productQuarantinedTopic,
                                     UseCase<Mono<InventoryOutput>, UpdateQuarantineInput> updateQuarantineUseCase,
                                     QuarantinedMessageMapper quarantinedMessageMapper) {
        super(consumer, objectMapper, producerDLQTemplate, productQuarantinedTopic, new TypeReference<>() {
        });
        this.quarantinedMessageMapper = quarantinedMessageMapper;
        this.updateQuarantineUseCase = updateQuarantineUseCase;
    }

    @Override
    protected Mono<InventoryOutput> processInput(UpdateQuarantineInput domainMessage) {
        return updateQuarantineUseCase.execute(domainMessage);
    }

    @Override
    protected UpdateQuarantineInput fromMessageToInput(EventMessage<UpdateQuarantinedMessage> message) {
        return quarantinedMessageMapper.toInput(message.payload());
    }



}
