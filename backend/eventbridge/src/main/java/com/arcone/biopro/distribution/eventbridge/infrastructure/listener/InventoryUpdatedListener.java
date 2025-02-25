package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.service.InventoryUpdatedService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationInventoryUpdatedService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
@Profile("prod")
public class InventoryUpdatedListener extends AbstractKafkaListener {

    private final ObjectMapper objectMapper;
    private final InventoryUpdatedService inventoryUpdatedService;
    private final SchemaValidationInventoryUpdatedService schemaValidationService;

    public InventoryUpdatedListener(
            @Qualifier(KafkaConfiguration.INVENTORY_UPDATED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer
            , ObjectMapper objectMapper
            , InventoryUpdatedService inventoryUpdatedService
            , @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate
            , @Value("${topics.inventory.inventory-updated.topic-name:InventoryUpdated}") String topicName , SchemaValidationInventoryUpdatedService schemaValidationService) {

        super(consumer, objectMapper, producerTemplate, topicName);
        this.objectMapper = objectMapper;
        this.inventoryUpdatedService = inventoryUpdatedService;
        this.schemaValidationService = schemaValidationService;

    }


    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {
            var message = objectMapper.readValue(event.value(), InventoryUpdatedEventDTO.class);
            return schemaValidationService.validateInventoryUpdatedSchema(event.value())
                .then(Mono.defer(() -> inventoryUpdatedService
                            .processInventoryUpdatedEvent(message.payload())))
                .then(Mono.just(event))
                    .retryWhen(Retry
                            .fixedDelay(3, Duration.ofSeconds(60))
                            .doBeforeRetry(retrySignal ->
                                    log.warn("Retrying due to error: {}. Attempt: {}",
                                            retrySignal.failure().getMessage(),
                                            retrySignal.totalRetries())))
                    .doOnSuccess(product -> log.info("Processed message = {}", event))
                    .onErrorResume(e -> {
                        log.error("Skipping message processing. Reason: {} Message: {}", e.getMessage(), event);
                        sendToDlq(event.value(), e.getMessage());
                        return Mono.empty();
                    });

        } catch (JsonProcessingException e) {
            log.error(String.format("Problem deserializing an instance of [%s] " +
                    "with the following json: %s ", InventoryUpdatedPayload.class.getSimpleName(), event), e);
            sendToDlq(event.value(), e.getMessage());
            return Mono.error(new RuntimeException(e));
        }
    }

}
