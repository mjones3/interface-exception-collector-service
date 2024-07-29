package com.arcone.biopro.distribution.orderservice.infrastructure.listener;

import com.arcone.biopro.distribution.orderservice.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderCreatedDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderItemCreatedDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderRejectedDTO;
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
        log.info("Order Created event trigger Event ID {}", event.getEventId());

        var message = OrderCreatedDTO
            .builder()
            .eventId(event.getEventId().toString())
            .occurredOn(event.getOccurredOn())
            .orderStatus(event.getPayload().getOrderStatus().getOrderStatus())
            .createDate(event.getPayload().getCreateDate())
            .desiredShippingDate(event.getPayload().getDesiredShippingDate())
            .orderNumber(event.getPayload().getOrderNumber().getOrderNumber())
            .willPickUp(event.getPayload().getWillCallPickup())
            .willPickUpPhoneNumber(event.getPayload().getPhoneNumber())
            .locationCode(event.getPayload().getLocationCode())
            .externalId(event.getPayload().getOrderExternalId().getOrderExternalId())
            .priority(event.getPayload().getOrderPriority().getOrderPriority())
            .shipmentType(event.getPayload().getShipmentType().getShipmentType())
            .productCategory(event.getPayload().getProductCategory().getProductCategory())
            .shippingMethod(event.getPayload().getShippingMethod().getShippingMethod())
            .comments(event.getPayload().getComments())
            .createEmployeeCode(event.getPayload().getCreateEmployeeId())
            .billingCustomerCode(event.getPayload().getBillingCustomer().getCode())
            .shippingCustomerCode(event.getPayload().getShippingCustomer().getCode())
            .orderItems(event.getPayload().getOrderItems().stream().map(orderItem -> OrderItemCreatedDTO.builder()
                .productFamily(orderItem.getProductFamily().getProductFamily())
                .quantity(orderItem.getQuantity())
                .bloodType(orderItem.getBloodType().getBloodType())
                .comments(orderItem.getComments())
                .build()).toList())
            .build();
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.eventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
