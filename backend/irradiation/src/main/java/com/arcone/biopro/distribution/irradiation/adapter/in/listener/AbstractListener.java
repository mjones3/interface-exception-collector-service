package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
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

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractListener<TInput, TOutput, TPayload> implements CommandLineRunner {

    private static final String JSON_DESERIALIZATION_DLQ = "LabelingJsonDeserializationDLQ";

    MessageMapper<TInput, TPayload> mapper;
    UseCase<Mono<TOutput>, TInput> useCase;
    ReactiveKafkaConsumerTemplate<String, String> consumer;
    ObjectMapper objectMapper;
    ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate;
    Scheduler scheduler = Schedulers.newBoundedElastic(16, 128, "schedulers");

    public AbstractListener(ReactiveKafkaConsumerTemplate<String, String> consumer,
                            ObjectMapper objectMapper,
                            ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                            UseCase<Mono<TOutput>, TInput> useCase,
                            MessageMapper<TInput, TPayload> mapper) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.producerDLQTemplate = producerDLQTemplate;
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) {
        handleEvent().publishOn(scheduler).subscribe();
    }

    private Flux<TOutput> handleEvent() {
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

    private Mono<TOutput> handleMessage(String value) {
        try {
            EventMessage<TPayload> message = getMessage(value);
            TInput input = toInput(message.payload());
            return processMessage(input)
                .retryWhen(Retry
                    .backoff(3, Duration.ofSeconds(5))
                    .jitter(0.5)
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying due to error: {}. Attempt: {}",
                            retrySignal.failure().getMessage(),
                            retrySignal.totalRetries())))
                .doOnSuccess(output -> log.info("Processed message = {}", output))
                .onErrorResume(e -> {
                    log.error("Skipping message processing. Reason: {} Message: {}", e.getMessage(), value);
                    sendToDlq(value, e.getMessage(), "LabelingDLQ" + message.eventType());
                    return Mono.empty();
                });
        } catch (JsonProcessingException e) {
            log.error(String.format("Problem deserializing an instance of [%s] with the following json: %s ", getMessageTypeReference().getClass().getSimpleName(), value), e);
            sendToDlq(value, e.getMessage(), JSON_DESERIALIZATION_DLQ);
            return Mono.empty();
        }
    }

    private EventMessage<TPayload> getMessage(String value) throws JsonProcessingException {
        return objectMapper.readValue(value, getMessageTypeReference());
    }

    private void sendToDlq(String value, String errorMessage, String dlqTopic) {
        var dlqMessage = new HashMap<String, String>();
        dlqMessage.put("message", value);
        dlqMessage.put("error", errorMessage);
        try {
            var dlqMessageJson = objectMapper.writeValueAsString(dlqMessage);
            producerDLQTemplate.send(dlqTopic, dlqMessageJson)
                .doOnError(e -> log.error("Send failed", e))
                .subscribe();
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to DLQ. Reason: {}", e.getMessage());
        }
    }

    private Mono<TOutput> processMessage(TInput input) {
        return this.useCase.execute(input);
    }

    private TInput toInput(TPayload message) {
        return this.mapper.toInput(message);
    }

    protected abstract TypeReference<EventMessage<TPayload>> getMessageTypeReference();

}

