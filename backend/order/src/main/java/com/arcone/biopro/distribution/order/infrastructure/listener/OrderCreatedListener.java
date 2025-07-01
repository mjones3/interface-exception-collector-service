package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCreatedDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderItemCreatedDTO;
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
public class OrderCreatedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> producerTemplate;
    private final String topicName;

    public OrderCreatedListener(@Qualifier(KafkaConfiguration.ORDER_CREATED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> producerTemplate,
                                @Value("${topics.order-created.name:OrderCreated}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderCreated",
        description = "Order Created Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.dto.OrderCreatedDTO"
        )),
        message = @AsyncMessage(
            name = "OrderCreated",
            title = "OrderCreated",
            description = "Order Created Event Payload"
        ),payloadType = OrderCreatedDTO.class
    ))
    @KafkaAsyncOperationBinding
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
            .transactionId(event.getPayload().getTransactionId())
            .priority(event.getPayload().getOrderPriority().getDeliveryType())
            .shipmentType(event.getPayload().getShipmentType().getShipmentType())
            .productCategory(event.getPayload().getProductCategory().getProductCategory())
            .shippingMethod(event.getPayload().getShippingMethod().getShippingMethod())
            .comments(event.getPayload().getComments())
            .createEmployeeCode(event.getPayload().getCreateEmployeeId())
            .billingCustomerCode(event.getPayload().getBillingCustomer() != null ? event.getPayload().getBillingCustomer().getCode() : null)
            .shippingCustomerCode(event.getPayload().getShippingCustomer() != null ? event.getPayload().getShippingCustomer().getCode() : null)
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
