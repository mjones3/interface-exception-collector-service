package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderModifiedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderOutboundMapper;
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
public class OrderModifiedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, EventMessage<OrderOutboundPayload>> producerTemplate;
    private final String topicName;
    private final OrderOutboundMapper orderOutboundMapper;

    public OrderModifiedOutboundEventListener(@Qualifier(KafkaConfiguration.ORDER_MODIFIED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, EventMessage<OrderOutboundPayload>> producerTemplate,
                                                 @Value("${topics.order.order-modified-outbound.topic-name:OrderModifiedOutbound}") String topicName,
                                                 OrderOutboundMapper orderOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.orderOutboundMapper = orderOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderModifiedOutbound",
        description = "Order Modified Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.OrderModifiedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "OrderModifiedOutbound",
            title = "OrderModifiedOutbound",
            description = "Order Modified Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderModifiedOutboundEvent(OrderModifiedOutboundEvent event) {
        log.debug("Order Modified Outbound event trigger Event ID {}", event.getEventId());

        var message = new EventMessage<>("OrderModifiedOutbound", "1.0", orderOutboundMapper.toDto(event.getPayload()));

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
                .doOnError(e-> log.error("Send failed", e))
                .doOnNext(senderResult -> log.info("Order Modified Outbound Message {}-{} (orderStatus {}). Event produced: {}",
                        event.getPayload().getOrderNumber(),
                        event.getPayload().getExternalId(),
                        event.getPayload().getOrderStatus(),
                        senderResult.recordMetadata()))
                .subscribe();
    }
}