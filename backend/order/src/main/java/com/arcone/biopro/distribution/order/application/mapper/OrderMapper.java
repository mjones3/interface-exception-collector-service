package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.NotificationDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderResponseDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final CustomerService customerService;
    private final OrderItemMapper orderItemMapper;
    private final LookupService lookupService;
    private final OrderConfigService orderConfigService;

    public OrderResponseDTO  mapToDTO(final UseCaseResponseDTO<Order> useCaseResponse) {

        return OrderResponseDTO.builder()
            .notifications(ofNullable(useCaseResponse.notifications())
                .filter(notificationDTOList -> !notificationDTOList.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(notification -> NotificationDTO.builder()
                    .notificationType(notification.notificationType().name())
                    .notificationMessage(notification.notificationMessage())
                    .build())
                .toList()
            )
            .data(mapToDTO(useCaseResponse.data()))
            .build();
    }

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
            .priority(order.getOrderPriority().getDeliveryType())
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
            .totalRemaining(order.getTotalRemaining())
            .totalShipped(order.getTotalShipped())
            .totalProducts(order.getTotalProducts())
            .build();
    }

    public Order mapToDomain(final OrderDTO orderDTO) {
        var order = new Order(
            this.customerService,
            this.lookupService,
            orderDTO.id(),
            orderDTO.orderNumber(),
            orderDTO.externalId(),
            orderDTO.locationCode(),
            orderDTO.shipmentType(),
            orderDTO.shippingMethod(),
            orderDTO.shippingCustomerCode(),
            orderDTO.billingCustomerCode(),
            Optional.of(orderDTO.desiredShippingDate().toString()).orElse(""),
            orderDTO.willCallPickup(),
            orderDTO.phoneNumber(),
            orderDTO.productCategory(),
            orderDTO.comments(),
            orderDTO.status(),
            orderDTO.priority(),
            orderDTO.createEmployeeId(),
            orderDTO.createDate(),
            orderDTO.modificationDate(),
            orderDTO.deleteDate());

        ofNullable(orderDTO.orderItems())
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .forEach(orderItemDTO -> order.addItem(orderItemDTO.id()
                    , orderItemDTO.productFamily(), orderItemDTO.bloodType()
                    , orderItemDTO.quantity(),0, orderItemDTO.comments(), orderItemDTO.createDate()
                    , orderItemDTO.modificationDate(), this.orderConfigService
                )
            );

        return order;
    }
}
