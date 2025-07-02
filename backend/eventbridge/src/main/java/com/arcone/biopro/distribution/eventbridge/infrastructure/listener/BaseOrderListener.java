package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
public abstract class BaseOrderListener<T> extends AbstractKafkaListener {

    protected final ObjectMapper objectMapper;
    protected final OrderService orderService;
    protected final SchemaValidationService schemaValidationService;

    protected BaseOrderListener(
            ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            OrderService orderService,
            ReactiveKafkaProducerTemplate<String, String> producerTemplate,
            String topicName,
            SchemaValidationService schemaValidationService) {

        super(consumer, objectMapper, producerTemplate, topicName);
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.schemaValidationService = schemaValidationService;
    }

    protected abstract Class<T> getEventDTOClass();
    protected abstract String getSchemaPath();
    protected abstract Mono<Void> processEvent(T message);

    @Override
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {
            var message = objectMapper.readValue(event.value(), getEventDTOClass());
            return schemaValidationService.validateSchema(event.value(), getSchemaPath())
                .then(Mono.defer(() -> processEvent(message)))
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
                    "with the following json: %s ", getEventDTOClass().getSimpleName(), event), e);
            sendToDlq(event.value(), e.getMessage());
            return Mono.error(new RuntimeException(e));
        }
    }
}