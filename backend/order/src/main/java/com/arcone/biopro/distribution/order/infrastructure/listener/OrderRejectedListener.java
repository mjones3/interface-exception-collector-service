package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderRejectedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("prod")
public class OrderRejectedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderRejectedDTO> producerTemplate;
    private final String topicName;

    public OrderRejectedListener(@Qualifier(KafkaConfiguration.ORDER_REJECTED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderRejectedDTO> producerTemplate,
                                 @Value("${topics.order-rejected.name:OrderRejected}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @EventListener
    public void handleOrderRejectedEvent(OrderRejectedEvent event) {
        log.info("Order Rejected event trigger Event ID {}", event.getEventId());

        var message = OrderRejectedDTO
            .builder()
            .eventId(event.getEventId().toString())
            .occurredOn(event.getOccurredOn())
            .externalId(event.getPayload().externalId())
            .rejectedReason(event.getPayload().errorMessage())
        .build();
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.eventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
