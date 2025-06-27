package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedEventDTO;
import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
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
public class OrderModifiedListener extends AbstractKafkaListener {

    private static final String ORDER_MODIFIED_SCHEMA = "schema/order-modified.json";
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final SchemaValidationService schemaValidationService;

    public OrderModifiedListener(
            @Qualifier(KafkaConfiguration.ORDER_MODIFIED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            OrderService orderService,
            @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate,
            @Value("${topics.order.order-modified.topic-name:OrderModified}") String topicName,
            SchemaValidationService schemaValidationService) {

        super(consumer, objectMapper, producerTemplate, topicName);
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.schemaValidationService = schemaValidationService;
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "OrderModified",
        description = "Order Modified received event",
        payloadType = OrderModifiedEventDTO.class
    ))
    @KafkaAsyncOperationBinding
    @Override
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        try {
            var message = objectMapper.readValue(event.value(), OrderModifiedEventDTO.class);
            return schemaValidationService.validateSchema(event.value(), ORDER_MODIFIED_SCHEMA)
                .then(Mono.defer(() -> orderService
                            .processOrderModifiedEvent(message.payload())))
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
                    "with the following json: %s ", OrderModifiedEventDTO.class.getSimpleName(), event), e);
            sendToDlq(event.value(), e.getMessage());
            return Mono.error(new RuntimeException(e));
        }
    }
}