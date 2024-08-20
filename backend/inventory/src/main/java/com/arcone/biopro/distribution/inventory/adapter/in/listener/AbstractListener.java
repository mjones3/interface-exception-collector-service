package com.arcone.biopro.distribution.inventory.adapter.in.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;

/**
  AbstractListener is a base class for processing Kafka messages.

  @param <T> the application layer input type.
 * @param <G> the application layer output type.
 * @param <U> the adapter layer Kafka message DTO.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractListener<T, G, U> implements CommandLineRunner {

    TypeReference<U> typeReference;
    ReactiveKafkaConsumerTemplate<String, String> consumer;
    ObjectMapper objectMapper;
    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate;
    String topicDLQ;
    Scheduler scheduler = Schedulers.newBoundedElastic(16, 128, "schedulers");

    public AbstractListener(ReactiveKafkaConsumerTemplate<String, String> consumer,
                            ObjectMapper objectMapper,
                            ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                            String topic,
                            TypeReference<U> typeReference) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.producerDLQTemplate = producerDLQTemplate;
        this.topicDLQ = topic + "DLQ";
        this.typeReference = typeReference;
    }

    @Override
    public void run(String... args) {
        handleEvent().publishOn(scheduler).subscribe();
    }

    private Flux<G> handleEvent() {
        return consumer
            .receiveAutoAck()
            .doOnNext(
                consumerRecord ->
                    log.info(
                        "received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset()
                    )
            )
            .map(ConsumerRecord::value)
            .flatMap(this::handleMessage)
            .doOnNext(message -> log.debug("successfully consumed {}={}", String.class.getSimpleName(), message))
            .doOnError(throwable -> log.error("something bad happened while consuming: {}", throwable.getMessage()));
    }

    private Mono<G> handleMessage(String value) {
        try {
            U message = objectMapper.readValue(value, typeReference);
            return processInput(fromMessageToInput(message))
                .retryWhen(Retry
                    .fixedDelay(3, Duration.ofSeconds(60))
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying due to error: {}. Attempt: {}",
                            retrySignal.failure().getMessage(),
                            retrySignal.totalRetries())))
                .doOnSuccess(product -> log.info("Processed message = {}", message))
                .onErrorResume(e -> {
                    log.error("Skipping message processing. Reason: {} Message: {}", e.getMessage(), value);
                    sendToDlq(value, e.getMessage());
                    return Mono.empty();
                });
        } catch (JsonProcessingException e) {
            log.error(String.format("Problem deserializing an instance of [%s] with the following json: %s ",  typeReference.getType(), value), e);
            sendToDlq(value, e.getMessage());
            return Mono.empty();
        }
    }

    private void sendToDlq(String value, String errorMessage) {
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

    protected abstract Mono<G> processInput(T input);

    protected abstract T fromMessageToInput(U message);
}

