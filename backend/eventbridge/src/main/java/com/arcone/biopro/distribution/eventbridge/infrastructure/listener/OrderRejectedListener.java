package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderRejectedEventDTO;
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
public class OrderRejectedListener extends BaseOrderListener<OrderRejectedEventDTO> {

    public OrderRejectedListener(
            @Qualifier(KafkaConfiguration.ORDER_REJECTED_CONSUMER) ReactiveKafkaConsumerTemplate<String, String> consumer,
            ObjectMapper objectMapper,
            OrderService orderService,
            @Qualifier(KafkaConfiguration.DLQ_PRODUCER) ReactiveKafkaProducerTemplate<String, String> producerTemplate,
            @Value("${topics.order.order-rejected.topic-name:OrderRejected}") String topicName,
            SchemaValidationService schemaValidationService) {

        super(consumer, objectMapper, orderService, producerTemplate, topicName, schemaValidationService);
    }

    @AsyncListener(operation = @AsyncOperation(
        channelName = "OrderRejected",
        description = "Order Rejected received event",
        payloadType = OrderRejectedEventDTO.class
    ))
    @KafkaAsyncOperationBinding
    @Override
    protected Mono<ReceiverRecord<String, String>> handleMessage(ReceiverRecord<String, String> event) {
        return super.handleMessage(event);
    }

    @Override
    protected Class<OrderRejectedEventDTO> getEventDTOClass() {
        return OrderRejectedEventDTO.class;
    }

    @Override
    protected String getSchemaPath() {
        return "schema/order-rejected.json";
    }

    @Override
    protected Mono<Void> processEvent(OrderRejectedEventDTO message) {
        return orderService.processOrderRejectedEvent(message.payload());
    }
}