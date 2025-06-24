package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderRejectedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderRejectedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderRejectedOutboundMapper;
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

import java.util.UUID;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class OrderRejectedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, EventMessage<OrderRejectedOutboundPayload>> producerTemplate;
    private final String topicName;
    private final OrderRejectedOutboundMapper orderRejectedOutboundMapper;

    public OrderRejectedOutboundEventListener(@Qualifier(KafkaConfiguration.ORDER_REJECTED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, EventMessage<OrderRejectedOutboundPayload>> producerTemplate,
                                                 @Value("${topics.order.order-rejected-outbound.topic-name:OrderRejectedOutbound}") String topicName,
                                                 OrderRejectedOutboundMapper orderRejectedOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.orderRejectedOutboundMapper = orderRejectedOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderRejectedOutbound",
        description = "Order Rejected Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.OrderRejectedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "OrderRejectedOutbound",
            title = "OrderRejectedOutbound",
            description = "Order Rejected Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderRejectedOutboundEvent(OrderRejectedOutboundEvent event) {
        var eventId = UUID.randomUUID().toString();
        log.debug("Order Rejected Outbound event trigger Event ID {}", eventId);

        var message = new EventMessage<>("OrderRejectedOutbound", "1.0", orderRejectedOutboundMapper.toDto(event.orderRejectedOutbound()));

        var producerRecord = new ProducerRecord<>(topicName, eventId, message);
        producerTemplate.send(producerRecord)
                .doOnError(e-> log.error("Send failed", e))
                .doOnNext(senderResult -> log.info("Order Rejected Outbound Message {} (operation {}). Event produced: {}",
                        event.orderRejectedOutbound().externalId(),
                        event.orderRejectedOutbound().operation(),
                        senderResult.recordMetadata()))
                .subscribe();
    }
}