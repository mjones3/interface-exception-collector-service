package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderRejectedDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderRejectedOutputEvent;
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
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class OrderRejectedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderRejectedOutputEvent> producerTemplate;
    private final String topicName;

    public OrderRejectedListener(@Qualifier(KafkaConfiguration.ORDER_REJECTED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderRejectedOutputEvent> producerTemplate,
                                 @Value("${topics.order-cancelled.name:OrderRejected}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderRejected",
        description = "Order Rejected Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.OrderRejectedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "OrderRejected",
            title = "OrderRejected",
            description = "Order Rejected Event"
        ),payloadType = OrderRejectedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderRejectedEvent(OrderRejectedEvent event) {
        log.debug("Order Rejected event trigger Event ID {}", event.getEventId());

        var message = new OrderRejectedOutputEvent(OrderRejectedDTO
            .builder()
            .externalId(event.getPayload().externalId())
            .rejectedReason(event.getPayload().errorMessage())
            .build());
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
