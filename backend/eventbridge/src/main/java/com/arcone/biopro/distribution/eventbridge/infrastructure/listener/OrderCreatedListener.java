package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCreatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Service
@Profile("prod")
public class OrderCreatedListener extends BaseOrderListener<OrderCreatedEventDTO> {

    public OrderCreatedListener(
            @Qualifier(KafkaConfiguration.ORDER_CREATED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            OrderService orderService,
            @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate,
            @Value("${topics.order.order-created.topic-name:OrderCreated}") String topicName,
            SchemaValidationService schemaValidationService) {

        super(consumer, objectMapper, orderService, producerTemplate, topicName, schemaValidationService);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "OrderCreated",
        description = "Order Created received event",
        payloadType = OrderCreatedEventDTO.class
    ))
    @KafkaAsyncOperationBinding
    @Override
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        return super.handleMessage(event);
    }

    @Override
    protected Class<OrderCreatedEventDTO> getEventDTOClass() {
        return OrderCreatedEventDTO.class;
    }

    @Override
    protected String getSchemaPath() {
        return "schema/order-created.json";
    }

    @Override
    protected Mono<Void> processEvent(OrderCreatedEventDTO message) {
        return orderService.processOrderCreatedEvent(message);
    }
}
