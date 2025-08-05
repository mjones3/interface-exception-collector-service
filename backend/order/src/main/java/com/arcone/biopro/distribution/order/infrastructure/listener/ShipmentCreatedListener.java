package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

@Service
@Slf4j
@Profile("prod")
public class ShipmentCreatedListener implements CommandLineRunner {

    private final ReactiveKafkaConsumerTemplate<String, String> consumer;
    private final ObjectMapper objectMapper;
    private final OrderShipmentService orderShipmentService;

    public ShipmentCreatedListener(@Qualifier(KafkaConfiguration.SHIPMENT_CREATED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer
        , ObjectMapper objectMapper
        , OrderShipmentService orderShipmentService) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.orderShipmentService = orderShipmentService;
    }

    private final Scheduler scheduler = Schedulers.newBoundedElastic(
        16,
        128,
        "schedulers"
    );

    @Override
    public void run(String... args) {
        handleEvent();
    }


    private Disposable handleEvent() {
        return consumer
            .receive()
            .doOnError(throwable -> log.error("Error receiving event {}", throwable.getMessage()))
            .retryWhen(Retry.max(3).transientErrors(true))
            .repeat()
            .doOnNext(this::logReceivedMessage)
            .concatMap(receiverRecord -> handleMessage(receiverRecord)
                .doOnError(throwable -> {
                    log.error(
                        "Error while while processing event from topic={} key={}, value={}, offset={}, error={}",
                        receiverRecord.topic(),
                        receiverRecord.key(),
                        receiverRecord.value(),
                        receiverRecord.offset(),
                        throwable.toString());
                })
                .onErrorResume(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    receiverRecord.receiverOffset().acknowledge();
                    return Mono.empty();
                }))
            .doOnNext(record -> record.receiverOffset().acknowledge())
            .publishOn(scheduler)
            .subscribe();

    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "ShipmentCreated",
        description = "Shipment created Events",
        payloadType = ShipmentCreatedEventDTO.class
    ))
    @KafkaAsyncOperationBinding()
    private Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {

            var message = objectMapper.readValue(event.value(), ShipmentCreatedEventDTO.class);
            return orderShipmentService.processShipmentCreatedEvent(message.payload()).then(Mono.just(event));

        } catch (JsonProcessingException e) {
            log.error(String.format("Problem deserializing an instance of [%s] " +
                "with the following json: %s ", OrderReceivedEventPayloadDTO.class.getSimpleName(), event), e);
            return Mono.error(new RuntimeException(e));
        }
    }


    private void logReceivedMessage(ReceiverRecord<String, String> event) {
        log.info(
            "event received from topic={}, key={}, value={}, offset={}",
            event.topic(),
            event.key(),
            event.value(),
            event.offset()
        );
    }

}
