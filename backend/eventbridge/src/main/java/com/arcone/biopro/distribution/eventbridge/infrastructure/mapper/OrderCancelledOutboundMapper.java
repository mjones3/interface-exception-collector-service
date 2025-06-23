package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCancelledOutboundPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCancelledOutboundMapper {

    public OrderCancelledOutboundPayload toDto(OrderCancelledOutbound orderCancelledOutbound) {
        return OrderCancelledOutboundPayload
            .builder()
            .orderNumber(orderCancelledOutbound.getOrderNumber())
            .externalId(orderCancelledOutbound.getExternalId())
            .orderStatus(orderCancelledOutbound.getOrderStatus())
            .locationCode(orderCancelledOutbound.getLocationCode())
            .createDate(orderCancelledOutbound.getCreateDate())
            .createEmployeeCode(orderCancelledOutbound.getCreateEmployeeCode())
            .shipmentType(orderCancelledOutbound.getShipmentType())
            .priority(orderCancelledOutbound.getPriority())
            .shippingMethod(orderCancelledOutbound.getShippingMethod())
            .productCategory(orderCancelledOutbound.getProductCategory())
            .desiredShippingDate(orderCancelledOutbound.getDesiredShippingDate())
            .shippingCustomerCode(orderCancelledOutbound.getShippingCustomerCode())
            .billingCustomerCode(orderCancelledOutbound.getBillingCustomerCode())
            .comments(orderCancelledOutbound.getComments())
            .willPickUp(orderCancelledOutbound.getWillPickUp())
            .willPickUpPhoneNumber(orderCancelledOutbound.getWillPickUpPhoneNumber())
            .totalShipped(orderCancelledOutbound.getTotalShipped())
            .totalRemaining(orderCancelledOutbound.getTotalRemaining())
            .totalProducts(orderCancelledOutbound.getTotalProducts())
            .orderItems(orderCancelledOutbound.getOrderItems())
            .cancelEmployeeId(orderCancelledOutbound.getCancelEmployeeId())
            .cancelDate(orderCancelledOutbound.getCancelDate())
            .cancelReason(orderCancelledOutbound.getCancelReason())
            .transactionId(orderCancelledOutbound.getTransactionId())
            .build();
    }
}