package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledMapper {

    public OrderCancelledOutbound toDomain(OrderCancelledPayload payload) {
        return OrderCancelledOutbound.builder()
            .orderNumber(payload.orderNumber())
            .externalId(payload.externalId())
            .orderStatus(payload.orderStatus())
            .locationCode(payload.locationCode())
            .createDate(payload.createDate())
            .createEmployeeCode(payload.createEmployeeCode())
            .shipmentType(payload.shipmentType())
            .priority(payload.priority())
            .shippingMethod(payload.shippingMethod())
            .productCategory(payload.productCategory())
            .desiredShippingDate(payload.desiredShippingDate())
            .shippingCustomerCode(payload.shippingCustomerCode())
            .billingCustomerCode(payload.billingCustomerCode())
            .comments(payload.comments())
            .willPickUp(payload.willPickUp())
            .willPickUpPhoneNumber(payload.willPickUpPhoneNumber())
            .totalShipped(payload.totalShipped())
            .totalRemaining(payload.totalRemaining())
            .totalProducts(payload.totalProducts())
            .orderItems(payload.orderItems().stream()
                .map(item -> OrderCancelledOutbound.OrderItem.builder()
                    .productFamily(item.productFamily())
                    .bloodType(item.bloodType())
                    .quantity(item.quantity())
                    .quantityShipped(item.quantityShipped())
                    .quantityRemaining(item.quantityRemaining())
                    .comments(item.comments())
                    .build())
                .toList())
            .cancelEmployeeId(payload.cancelEmployeeId())
            .cancelDate(payload.cancelDate())
            .cancelReason(payload.cancelReason())
            .transactionId(payload.transactionId())
            .build();
    }
}
