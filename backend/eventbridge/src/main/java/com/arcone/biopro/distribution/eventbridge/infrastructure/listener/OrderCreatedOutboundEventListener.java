package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCreatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCreatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderCreatedOutboundMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class OrderCreatedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, EventMessage<OrderCreatedOutboundPayload>> producerTemplate;
    private final String topicName;
    private final OrderCreatedOutboundMapper orderCreatedOutboundMapper;

    public OrderCreatedOutboundEventListener(@Qualifier(KafkaConfiguration.ORDER_CREATED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, EventMessage<OrderCreatedOutboundPayload>> producerTemplate,
                                                 @Value("${topics.order.order-created-outbound.topic-name:OrderCreatedOutbound}") String topicName,
                                                 OrderCreatedOutboundMapper orderCreatedOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.orderCreatedOutboundMapper = orderCreatedOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderCreatedOutbound",
        description = "Order Created Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCreatedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "OrderCreatedOutbound",
            title = "OrderCreatedOutbound",
            description = "Order Created Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderCreatedOutboundEvent(OrderCreatedOutboundEvent event) {
        log.debug("Order Created Outbound event trigger Event ID {}", event.getEventId());

        var message = new EventMessage<>("OrderCreatedOutbound","1.0",orderCreatedOutboundMapper.toDto(event.getPayload()));

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
                .doOnError(e-> log.error("Send failed", e))
                .doOnNext(senderResult -> log.info("Order Created Outbound Message {} (orderNumber {}). Event produced: {}",
                        event.getPayload().externalId(),
                        event.getPayload().orderNumber(),
                        senderResult.recordMetadata()))
                .subscribe();
    }
}
