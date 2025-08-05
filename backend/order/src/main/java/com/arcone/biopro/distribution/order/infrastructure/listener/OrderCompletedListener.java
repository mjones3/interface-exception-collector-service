package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCompletedDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderItemCompletedDTO;
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
public class OrderCompletedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderCompletedDTO> producerTemplate;
    private final String topicName;

    public OrderCompletedListener(@Qualifier(KafkaConfiguration.ORDER_COMPLETED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderCompletedDTO> producerTemplate,
                                  @Value("${topics.order-completed.name:OrderCompleted}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderCompleted",
        description = "Order Completed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.dto.OrderCompletedDTO"
        )),
        message = @AsyncMessage(
            name = "OrderCompleted",
            title = "OrderCompleted",
            description = "Order Completed Event Payload"
        ),payloadType = OrderCompletedDTO.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        log.debug("Order Completed event trigger Event ID {}", event.getEventId());

        var order = event.getPayload();
        var message = OrderCompletedDTO
            .builder()
            .eventId(event.getEventId().toString())
            .occurredOn(event.getOccurredOn())
            .orderStatus(order.getOrderStatus().getOrderStatus())
            .createDate(order.getCreateDate())
            .desiredShippingDate(order.getDesiredShippingDate())
            .orderNumber(order.getOrderNumber().getOrderNumber())
            .willPickUp(order.getWillCallPickup() == null ? Boolean.FALSE : Boolean.TRUE)
            .willPickUpPhoneNumber(order.getPhoneNumber())
            .locationCode(order.getLocationFrom().getCode())
            .externalId(order.getOrderExternalId().getOrderExternalId())
            .priority(order.getOrderPriority().getDeliveryType())
            .shipmentType(order.getShipmentType().getShipmentType())
            .totalRemaining(order.getTotalRemaining())
            .totalShipped(order.getTotalShipped())
            .totalProducts(order.getTotalProducts())
            .productCategory(order.getProductCategory().getProductCategory())
            .shippingMethod(order.getShippingMethod().getShippingMethod())
            .comments(order.getComments())
            .createEmployeeCode(order.getCreateEmployeeId())
            .billingCustomerCode(order.getBillingCustomer() == null ? null : order.getBillingCustomer().getCode())
            .shippingCustomerCode(order.getShippingCustomer().getCode())
            .completeDate(order.getCompleteDate())
            .completeEmployeeId(order.getCompleteEmployeeId())
            .completeComments(order.getCompleteComments())
            .orderItems(order.getOrderItems().stream().map(orderItem -> OrderItemCompletedDTO.builder()
                .productFamily(orderItem.getProductFamily().getProductFamily())
                .quantity(orderItem.getQuantity())
                .bloodType(orderItem.getBloodType().getBloodType())
                .comments(orderItem.getComments())
                .quantityRemaining(orderItem.getQuantityRemaining())
                .quantityShipped(orderItem.getQuantityShipped())
                .build()).toList())
            .build();
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.eventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
