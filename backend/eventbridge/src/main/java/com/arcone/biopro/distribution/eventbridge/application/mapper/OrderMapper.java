package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderOutbound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderMapper {

    public OrderOutbound toDomain(OrderPayload orderPayload) {
        return new OrderOutbound(
            orderPayload.orderNumber(),
            orderPayload.externalId(),
            orderPayload.orderStatus(),
            orderPayload.locationCode(),
            orderPayload.createDate(),
            orderPayload.createEmployeeCode(),
            orderPayload.shipmentType(),
            orderPayload.priority(),
            orderPayload.shippingMethod(),
            orderPayload.productCategory(),
            orderPayload.desiredShippingDate(),
            orderPayload.shippingCustomerCode(),
            orderPayload.billingCustomerCode(),
            orderPayload.comments(),
            orderPayload.willPickUp(),
            orderPayload.willPickUpPhoneNumber(),
            orderPayload.totalShipped(),
            orderPayload.totalRemaining(),
            orderPayload.totalProducts(),
            orderPayload.orderItems(),
            orderPayload.cancelEmployeeId(),
            orderPayload.cancelDate(),
            orderPayload.cancelReason(),
            orderPayload.modifyEmployeeId(),
            orderPayload.modifyDate(),
            orderPayload.modifyReason(),
            orderPayload.transactionId()
        );
    }
}