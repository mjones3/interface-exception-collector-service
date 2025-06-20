package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderModifiedEvent;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderItemCancelledDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderModifiedDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderModifiedOutputEvent;
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
public class OrderModifiedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderModifiedOutputEvent> producerTemplate;
    private final String topicName;

    public OrderModifiedListener(@Qualifier(KafkaConfiguration.ORDER_MODIFIED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderModifiedOutputEvent> producerTemplate,
                                 @Value("${topics.order-modified.name:OrderModified}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderModified",
        description = "Order Modified Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.OrderModifiedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "OrderModified",
            title = "OrderModified",
            description = "Order Modified Event Payload"
        ),payloadType = OrderModifiedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleOrderModifiedEvent(OrderModifiedEvent event) {
        log.debug("Order Modified event trigger Event ID {}", event.getEventId());

        var order = event.getPayload();
        var message = new OrderModifiedOutputEvent(
            OrderModifiedDTO
                .builder()
                .orderStatus(order.getOrderStatus().getOrderStatus())
                .createDate(order.getCreateDate())
                .desiredShippingDate(order.getDesiredShippingDate())
                .orderNumber(order.getOrderNumber().getOrderNumber())
                .willPickUp(order.getWillCallPickup() == null ? Boolean.FALSE : Boolean.TRUE)
                .willPickUpPhoneNumber(order.getPhoneNumber())
                .locationCode(order.getLocationCode())
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
                .billingCustomerCode(order.getBillingCustomer().getCode())
                .shippingCustomerCode(order.getShippingCustomer().getCode())
                .modifyDate(order.getModificationDate())
                .modifyEmployeeId(order.getModifyEmployeeId())
                .modifyReason(order.getModifyReason())
                .transactionId(order.getTransactionId())
                .orderItems(order.getOrderItems().stream().map(orderItem -> OrderItemCancelledDTO.builder()
                    .productFamily(orderItem.getProductFamily().getProductFamily())
                    .quantity(orderItem.getQuantity())
                    .bloodType(orderItem.getBloodType().getBloodType())
                    .comments(orderItem.getComments())
                    .quantityRemaining(orderItem.getQuantityRemaining())
                    .quantityShipped(orderItem.getQuantityShipped())
                    .build()).toList())
                .build());
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
