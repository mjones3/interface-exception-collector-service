package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final CustomerService customerService;
    private final OrderItemMapper orderItemMapper;

    public OrderDTO mapToDTO(final Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber().getOrderNumber())
            .externalId(order.getOrderExternalId().getOrderExternalId())
            .locationCode(order.getLocationCode())
            .shipmentType(order.getShipmentType().getShipmentType())
            .shippingMethod(order.getShippingMethod().getShippingMethod())
            .shippingCustomerName(order.getShippingCustomer().getName())
            .shippingCustomerCode(order.getShippingCustomer().getCode())
            .billingCustomerName(order.getBillingCustomer().getName())
            .billingCustomerCode(order.getBillingCustomer().getCode())
            .desiredShippingDate(order.getDesiredShippingDate())
            .willCallPickup(order.getWillCallPickup())
            .phoneNumber(order.getPhoneNumber())
            .productCategory(order.getProductCategory().getProductCategory())
            .comments(order.getComments())
            .status(order.getOrderStatus().getOrderStatus())
            .priority(order.getOrderPriority().getOrderPriority())
            .createEmployeeId(order.getCreateEmployeeId())
            .createDate(order.getCreateDate())
            .modificationDate(order.getModificationDate())
            .deleteDate(order.getDeleteDate())
            .orderItems(
                ofNullable(order.getOrderItems())
                    .filter(orderItems -> !orderItems.isEmpty())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(orderItemMapper::mapToDTO)
                    .toList()
            )
            .build();
    }

    public Order mapToDomain(final OrderDTO orderDTO) {
        return new Order(
            this.customerService,
            orderDTO.id(),
            orderDTO.orderNumber(),
            orderDTO.externalId(),
            orderDTO.locationCode(),
            orderDTO.shipmentType(),
            orderDTO.shippingMethod(),
            orderDTO.shippingCustomerCode(),
            orderDTO.billingCustomerCode(),
            orderDTO.desiredShippingDate(),
            orderDTO.willCallPickup(),
            orderDTO.phoneNumber(),
            orderDTO.productCategory(),
            orderDTO.comments(),
            orderDTO.status(),
            orderDTO.priority(),
            orderDTO.createEmployeeId(),
            orderDTO.createDate(),
            orderDTO.modificationDate(),
            orderDTO.deleteDate(),
            ofNullable(orderDTO.orderItems())
                .filter(orderItems -> !orderItems.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(orderItemMapper::mapToDomain)
                .toList()
        );
    }

}
