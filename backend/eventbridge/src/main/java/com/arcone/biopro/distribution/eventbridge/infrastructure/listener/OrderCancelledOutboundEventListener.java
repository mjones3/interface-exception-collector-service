package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCancelledOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCancelledOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderCancelledOutboundMapper;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
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
public class OrderCancelledOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, EventMessage<OrderCancelledOutboundPayload>> producerTemplate;
    private final String topicName;
    private final OrderCancelledOutboundMapper orderCancelledOutboundMapper;

    public OrderCancelledOutboundEventListener(@Qualifier(KafkaConfiguration.ORDER_CANCELLED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, EventMessage<OrderCancelledOutboundPayload>> producerTemplate,
                                                 @Value("${topics.order.order-cancelled-outbound.topic-name:OrderCancelledOutbound}") String topicName,
                                                 OrderCancelledOutboundMapper orderCancelledOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.orderCancelledOutboundMapper = orderCancelledOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderCancelledOutbound",
        description = "Order Cancelled Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCancelledOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "OrderCancelledOutbound",
            title = "OrderCancelledOutbound",
            description = "Order Cancelled Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderCancelledOutboundEvent(OrderCancelledOutboundEvent event) {
        log.debug("Order Cancelled Outbound event trigger Event ID {}", event.getEventId());

        var message = new EventMessage<>("OrderCancelledOutbound","1.0",orderCancelledOutboundMapper.toDto(event.getPayload()));

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
                .doOnError(e-> log.error("Send failed", e))
                .doOnNext(senderResult -> log.info("Order Cancelled Outbound Message {} (orderNumber {}). Event produced: {}",
                        event.getPayload().externalId(),
                        event.getPayload().orderNumber(),
                        senderResult.recordMetadata()))
                .subscribe();
    }
}