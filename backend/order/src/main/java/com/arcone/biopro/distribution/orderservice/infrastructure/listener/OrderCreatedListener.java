package com.arcone.biopro.distribution.orderservice.infrastructure.listener;

import com.arcone.biopro.distribution.orderservice.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreatedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> producerTemplate;
    private final String topicName;

    public OrderCreatedListener(@Qualifier(KafkaConfiguration.ORDER_CREATED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> producerTemplate,
                                @Value("${topics.order-created.name:OrderCreated}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Order Created event trigger {}", event);
    }
}
