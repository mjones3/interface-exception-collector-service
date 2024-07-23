package com.arcone.biopro.distribution.orderservice.infrastructure.listener;

import com.arcone.biopro.distribution.orderservice.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderRejectedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
        log.info("Order Rejected event trigger {}", event);
    }
}
