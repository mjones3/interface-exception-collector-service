package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedEventDTO;
import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
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
public class OrderModifiedListener extends BaseOrderListener<OrderModifiedEventDTO> {

    public OrderModifiedListener(
            @Qualifier(KafkaConfiguration.ORDER_MODIFIED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            OrderService orderService,
            @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate,
            @Value("${topics.order.order-modified.topic-name:OrderModified}") String topicName,
            SchemaValidationService schemaValidationService) {

        super(consumer, objectMapper, orderService, producerTemplate, topicName, schemaValidationService);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "OrderModified",
        description = "Order Modified received event",
        payloadType = OrderModifiedEventDTO.class
    ))
    @KafkaAsyncOperationBinding
    @Override
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        return super.handleMessage(event);
    }

    @Override
    protected Class<OrderModifiedEventDTO> getEventDTOClass() {
        return OrderModifiedEventDTO.class;
    }

    @Override
    protected String getSchemaPath() {
        return "schema/order-modified.json";
    }

    @Override
    protected Mono<Void> processEvent(OrderModifiedEventDTO message) {
        return orderService.processOrderModifiedEvent(message);
    }
}
