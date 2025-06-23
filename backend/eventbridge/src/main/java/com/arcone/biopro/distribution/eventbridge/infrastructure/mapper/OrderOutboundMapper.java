package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderOutboundPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderOutboundMapper {

    public OrderOutboundPayload toDto(OrderOutbound orderOutbound) {
        return OrderOutboundPayload
            .builder()
            .orderNumber(orderOutbound.getOrderNumber())
            .externalId(orderOutbound.getExternalId())
            .orderStatus(orderOutbound.getOrderStatus())
            .locationCode(orderOutbound.getLocationCode())
            .createDate(orderOutbound.getCreateDate())
            .createEmployeeCode(orderOutbound.getCreateEmployeeCode())
            .shipmentType(orderOutbound.getShipmentType())
            .priority(orderOutbound.getPriority())
            .shippingMethod(orderOutbound.getShippingMethod())
            .productCategory(orderOutbound.getProductCategory())
            .desiredShippingDate(orderOutbound.getDesiredShippingDate())
            .shippingCustomerCode(orderOutbound.getShippingCustomerCode())
            .billingCustomerCode(orderOutbound.getBillingCustomerCode())
            .comments(orderOutbound.getComments())
            .willPickUp(orderOutbound.getWillPickUp())
            .willPickUpPhoneNumber(orderOutbound.getWillPickUpPhoneNumber())
            .totalShipped(orderOutbound.getTotalShipped())
            .totalRemaining(orderOutbound.getTotalRemaining())
            .totalProducts(orderOutbound.getTotalProducts())
            .orderItems(orderOutbound.getOrderItems())
            .cancelEmployeeId(orderOutbound.getCancelEmployeeId())
            .cancelDate(orderOutbound.getCancelDate())
            .cancelReason(orderOutbound.getCancelReason())
            .modifyEmployeeId(orderOutbound.getModifyEmployeeId())
            .modifyDate(orderOutbound.getModifyDate())
            .modifyReason(orderOutbound.getModifyReason())
            .transactionId(orderOutbound.getTransactionId())
            .build();
    }
}