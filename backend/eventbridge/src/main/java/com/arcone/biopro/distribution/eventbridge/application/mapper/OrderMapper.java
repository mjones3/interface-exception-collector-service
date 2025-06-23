package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderMapper {

    public OrderCancelledOutbound toDomain(OrderCancelledPayload orderCancelledPayload) {
        return new OrderCancelledOutbound(
            orderCancelledPayload.orderNumber(),
            orderCancelledPayload.externalId(),
            orderCancelledPayload.orderStatus(),
            orderCancelledPayload.locationCode(),
            orderCancelledPayload.createDate(),
            orderCancelledPayload.createEmployeeCode(),
            orderCancelledPayload.shipmentType(),
            orderCancelledPayload.priority(),
            orderCancelledPayload.shippingMethod(),
            orderCancelledPayload.productCategory(),
            orderCancelledPayload.desiredShippingDate(),
            orderCancelledPayload.shippingCustomerCode(),
            orderCancelledPayload.billingCustomerCode(),
            orderCancelledPayload.comments(),
            orderCancelledPayload.willPickUp(),
            orderCancelledPayload.willPickUpPhoneNumber(),
            orderCancelledPayload.totalShipped(),
            orderCancelledPayload.totalRemaining(),
            orderCancelledPayload.totalProducts(),
            orderCancelledPayload.orderItems(),
            orderCancelledPayload.cancelEmployeeId(),
            orderCancelledPayload.cancelDate(),
            orderCancelledPayload.cancelReason(),
            orderCancelledPayload.transactionId()
        );
    }
}