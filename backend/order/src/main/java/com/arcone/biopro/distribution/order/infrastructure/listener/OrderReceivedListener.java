package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Profile("prod")
public class OrderReceivedListener implements CommandLineRunner {


    private final ReactiveKafkaConsumerTemplate<String, String> consumer;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

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

    private Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {

            var message = objectMapper.readValue(event.value(), OrderReceivedEventDTO.class);
            return orderService.processOrder(message.payload()).then(Mono.just(event));

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
