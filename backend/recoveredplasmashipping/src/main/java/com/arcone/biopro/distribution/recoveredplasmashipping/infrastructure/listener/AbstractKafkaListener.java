package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.util.HashMap;


/**
 AbstractListener is a base class for processing Kafka messages.
 */

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractKafkaListener implements CommandLineRunner {


    ReactiveKafkaConsumerTemplate<String, String> consumer;
    ObjectMapper objectMapper;
    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate;
    String topicDLQ;
    Scheduler scheduler = Schedulers.newBoundedElastic(
            16,
            128,
            "schedulers"
    );
    private static final String DLQ_SUFFIX = "DLQ";

    public AbstractKafkaListener(ReactiveKafkaConsumerTemplate<String, String> consumer
            , ObjectMapper objectMapper
            , ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate
            , String topic) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.producerDLQTemplate = producerDLQTemplate;
        this.topicDLQ = topic + DLQ_SUFFIX;

    }

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

    protected void sendToDlq(String value, String errorMessage) {
        var dlqMessage = new HashMap<String, String>();
        dlqMessage.put("message", value);
        dlqMessage.put("error", errorMessage);
        try {
            var dlqMessageJson = objectMapper.writeValueAsString(dlqMessage);
            producerDLQTemplate.send(topicDLQ, dlqMessageJson)
                    .doOnError(e -> log.error("Send failed", e))
                    .subscribe();
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to DLQ. Reason: {}", e.getMessage());
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

    protected abstract Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event);

}
