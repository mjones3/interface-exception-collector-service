package com.arcone.biopro.partner.order.infrastructure.kafka;

import com.arcone.biopro.partner.order.domain.event.InvalidOrderEvent;
import com.arcone.biopro.partner.order.domain.event.OrderReceivedEvent;
import com.arcone.biopro.partner.order.domain.event.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing events to Kafka topics.
 * Handles OrderReceived, OrderRejected, and InvalidOrderEvent publishing
 * with proper error handling and retry logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublishingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-received:OrderRecieved}")
    private String orderReceivedTopic;

    @Value("${app.kafka.topics.order-rejected:OrderRejected}")
    private String orderRejectedTopic;

    @Value("${app.kafka.topics.invalid-order:InvalidOrderEvent}")
    private String invalidOrderTopic;

    /**
     * Publishes an OrderReceived event to the OrderRecieved topic.
     * This event indicates successful order processing.
     *
     * @param event the OrderReceived event to publish
     * @return CompletableFuture that completes when the event is published
     */
    public CompletableFuture<SendResult<String, Object>> publishOrderReceived(OrderReceivedEvent event) {
        log.info("Publishing OrderReceived event - transactionId: {}, externalId: {}",
                event.getTransactionId(), event.getPayload().getExternalId());

        return publishEvent(orderReceivedTopic, event.getTransactionId().toString(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish OrderReceived event - transactionId: {}, error: {}",
                                event.getTransactionId(), throwable.getMessage(), throwable);
                    } else {
                        log.info(
                                "Successfully published OrderReceived event - transactionId: {}, partition: {}, offset: {}",
                                event.getTransactionId(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Publishes an OrderRejected event to the OrderRejected topic.
     * This event is used for testing the Interface Exception Collector
     * functionality.
     *
     * @param event the OrderRejected event to publish
     * @return CompletableFuture that completes when the event is published
     */
    public CompletableFuture<SendResult<String, Object>> publishOrderRejected(OrderRejectedEvent event) {
        log.info("Publishing OrderRejected event - transactionId: {}, externalId: {}, reason: {}",
                event.getTransactionId(), event.getPayload().getExternalId(), event.getPayload().getRejectedReason());

        return publishEvent(orderRejectedTopic, event.getTransactionId().toString(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish OrderRejected event - transactionId: {}, error: {}",
                                event.getTransactionId(), throwable.getMessage(), throwable);
                    } else {
                        log.info(
                                "Successfully published OrderRejected event - transactionId: {}, partition: {}, offset: {}",
                                event.getTransactionId(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Publishes an InvalidOrderEvent to the InvalidOrderEvent topic.
     * This event indicates validation failures in order processing.
     *
     * @param event the InvalidOrderEvent to publish
     * @return CompletableFuture that completes when the event is published
     */
    public CompletableFuture<SendResult<String, Object>> publishInvalidOrder(InvalidOrderEvent event) {
        log.info("Publishing InvalidOrderEvent - transactionId: {}, validationErrors: {}",
                event.getTransactionId(), event.getPayload().getValidationErrors().size());

        return publishEvent(invalidOrderTopic, event.getTransactionId().toString(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish InvalidOrderEvent - transactionId: {}, error: {}",
                                event.getTransactionId(), throwable.getMessage(), throwable);
                    } else {
                        log.info(
                                "Successfully published InvalidOrderEvent - transactionId: {}, partition: {}, offset: {}",
                                event.getTransactionId(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Generic method to publish events to Kafka topics with consistent error
     * handling.
     *
     * @param topic the Kafka topic to publish to
     * @param key   the message key (usually transaction ID)
     * @param event the event payload
     * @return CompletableFuture that completes when the event is published
     */
    private CompletableFuture<SendResult<String, Object>> publishEvent(String topic, String key, Object event) {
        try {
            log.debug("Publishing event to topic: {}, key: {}, eventType: {}",
                    topic, key, event.getClass().getSimpleName());

            return kafkaTemplate.send(topic, key, event);

        } catch (Exception e) {
            log.error("Error publishing event to topic: {}, key: {}, error: {}",
                    topic, key, e.getMessage(), e);

            // Return a failed future
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    /**
     * Publishes multiple events in sequence with proper error handling.
     * Used when multiple events need to be published for a single order.
     *
     * @param events array of events to publish
     * @return CompletableFuture that completes when all events are published
     */
    public CompletableFuture<Void> publishEvents(Object... events) {
        CompletableFuture<?>[] futures = new CompletableFuture[events.length];

        for (int i = 0; i < events.length; i++) {
            Object event = events[i];
            if (event instanceof OrderReceivedEvent) {
                futures[i] = publishOrderReceived((OrderReceivedEvent) event);
            } else if (event instanceof OrderRejectedEvent) {
                futures[i] = publishOrderRejected((OrderRejectedEvent) event);
            } else if (event instanceof InvalidOrderEvent) {
                futures[i] = publishInvalidOrder((InvalidOrderEvent) event);
            } else {
                log.warn("Unknown event type: {}", event.getClass().getSimpleName());
                CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                        new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName()));
                futures[i] = failedFuture;
            }
        }

        return CompletableFuture.allOf(futures);
    }
}