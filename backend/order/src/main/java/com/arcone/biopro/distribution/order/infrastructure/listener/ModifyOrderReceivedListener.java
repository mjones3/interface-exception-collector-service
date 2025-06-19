package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import com.arcone.biopro.distribution.order.domain.service.ModifyOrderService;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
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
public class ModifyOrderReceivedListener extends AbstractKafkaListener{


    private final ObjectMapper objectMapper;
    private final ModifyOrderService modifyOrderService;


    public ModifyOrderReceivedListener(
        @Qualifier(KafkaConfiguration.MODIFY_ORDER_RECEIVED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer
        , ObjectMapper objectMapper
        , ModifyOrderService modifyOrderService
        , @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate
        , @Value("${topics.order.modify-order-received.topic-name:ModifyOrderReceived}") String topicName) {

        super(consumer, objectMapper, producerTemplate, topicName);
        this.objectMapper = objectMapper;
        this.modifyOrderService = modifyOrderService;

    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ModifyOrderReceived",
        description = "Partner Modify Order received Events", // Optional
        payloadType = ModifyOrderReceivedDTO.class
    ))
    @KafkaAsyncOperationBinding
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {
            var message = objectMapper.readValue(event.value(), ModifyOrderReceivedDTO.class);
            return modifyOrderService.processModifyOrderEvent(message)
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
                "with the following json: %s ", CancelOrderReceivedDTO.class.getSimpleName(), event), e);
            sendToDlq(event.value(), e.getMessage());
            return Mono.error(new RuntimeException(e));
        }
    }

}
