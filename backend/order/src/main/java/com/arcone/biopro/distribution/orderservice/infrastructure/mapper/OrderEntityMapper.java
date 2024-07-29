package com.arcone.biopro.distribution.orderservice.infrastructure.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.OrderEntity;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class OrderEntityMapper {

    private final CustomerService customerService;
    private final LookupService lookupService;
    private final OrderConfigService orderConfigService;

    public OrderEntity mapToEntity(final Order order) {
        return OrderEntity.builder()
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
            .build();
    }

    public Mono<OrderEntity> flatMapToEntity(final Order domain) {
        return Mono.just(mapToEntity(domain));
    }

    public Order mapToDomain(final OrderEntity orderEntity, final List<OrderItemEntity> orderItemEntities) {
        var order = new Order(
            this.customerService,
            this.lookupService,
            orderEntity.getId(),
            orderEntity.getOrderNumber(),
            orderEntity.getExternalId(),
            orderEntity.getLocationCode(),
            orderEntity.getShipmentType(),
            orderEntity.getShippingMethod(),
            orderEntity.getShippingCustomerCode(),
            orderEntity.getBillingCustomerCode(),
            orderEntity.getDesiredShippingDate(),
            orderEntity.getWillCallPickup(),
            orderEntity.getPhoneNumber(),
            orderEntity.getProductCategory(),
            orderEntity.getComments(),
            orderEntity.getStatus(),
            orderEntity.getPriority(),
            orderEntity.getCreateEmployeeId(),
            orderEntity.getCreateDate(),
            orderEntity.getModificationDate(),
            orderEntity.getDeleteDate());

        ofNullable(orderItemEntities)
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .forEach(orderItemEntity -> order.addItem(orderItemEntity.getId()
                    ,orderItemEntity.getProductFamily(),orderItemEntity.getBloodType()
                    ,orderItemEntity.getQuantity(),orderItemEntity.getComments(),orderItemEntity.getCreateDate()
                    ,orderItemEntity.getModificationDate(),this.orderConfigService
                )
            );

        return order;
    }

    public Mono<Order> flatMapToDomain(final OrderEntity orderEntity, final List<OrderItemEntity> orderItemEntities) {
        return Mono.just(mapToDomain(orderEntity, orderItemEntities));
    }

}
