package com.arcone.biopro.distribution.receiving.infrastructure.listener;

import com.arcone.biopro.distribution.receiving.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.receiving.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceCreatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
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
public class ShipmentCompletedListener extends AbstractKafkaListener {

    private final ObjectMapper objectMapper;

    private final ShipmentCompletedUseCase shipmentCompletedUseCase;

    public ShipmentCompletedListener(
        @Qualifier(KafkaConfiguration.SHIPMENT_COMPLETED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer
        , ObjectMapper objectMapper
        , ShipmentCompletedUseCase shipmentCompletedUseCase
        , @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate
        , @Value("${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}") String topicName) {

        super(consumer, objectMapper, producerTemplate, topicName);
        this.objectMapper = objectMapper;
        this.shipmentCompletedUseCase = shipmentCompletedUseCase;

    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ShipmentCompleted",
        description = "Shipment Completed received Events", // Optional
        payloadType = ShipmentCompletedMessage.class
    ))
    @KafkaAsyncOperationBinding
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {
            var message = objectMapper.readValue(event.value(), ShipmentCompletedMessage.class);
            return shipmentCompletedUseCase.processShipmentCompletedMessage(message)
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
                "with the following json: %s ", DeviceCreatedMessage.class.getSimpleName(), event), e);
            sendToDlq(event.value(), e.getMessage());
            return Mono.error(new RuntimeException(e));
        }
    }


}
