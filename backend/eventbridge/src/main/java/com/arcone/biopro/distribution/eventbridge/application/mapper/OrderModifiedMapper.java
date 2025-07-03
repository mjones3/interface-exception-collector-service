package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedEventDTO;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderModifiedOutbound;
import org.springframework.stereotype.Component;

@Component
public class OrderModifiedMapper {

    public OrderModifiedOutbound toDomain(OrderModifiedEventDTO eventDTO) {
        var payload = eventDTO.payload();
        return OrderModifiedOutbound.builder()
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
                .map(item -> OrderModifiedOutbound.OrderItem.builder()
                    .productFamily(item.productFamily())
                    .bloodType(item.bloodType())
                    .quantity(item.quantity())
                    .quantityShipped(item.quantityShipped())
                    .quantityRemaining(item.quantityRemaining())
                    .comments(item.comments())
                    .build())
                .toList())
            .modifyEmployeeId(payload.modifyEmployeeId())
            .modifyDate(payload.modifyDate())
            .modifyReason(payload.modifyReason())
            .transactionId(payload.transactionId())
            .build();
    }
}
