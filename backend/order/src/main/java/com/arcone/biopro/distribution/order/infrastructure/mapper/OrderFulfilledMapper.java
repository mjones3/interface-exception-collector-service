package com.arcone.biopro.distribution.order.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledItemDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledItemShortDateDTO;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFulfilledMapper {

    private static final String ADDRESS_TYPE = "SHIPPING";

    public OrderFulfilledEventDTO buildOrderDetails(Order order, PickList pickList) {

        var orderFulfilledDTO = new OrderFulfilledDTO();
        orderFulfilledDTO.setOrderNumber(order.getOrderNumber().getOrderNumber());
        orderFulfilledDTO.setExternalId(order.getOrderExternalId().getOrderExternalId());
        orderFulfilledDTO.setLocationCode(order.getLocationCode());
        orderFulfilledDTO.setBillingCustomerCode(order.getBillingCustomer().getCode());
        orderFulfilledDTO.setBillingCustomerName(order.getBillingCustomer().getName());
        orderFulfilledDTO.setShippingCustomerCode(order.getBillingCustomer().getCode());
        orderFulfilledDTO.setShippingCustomerName(order.getShippingCustomer().getName());
        orderFulfilledDTO.setComments(order.getComments());
        orderFulfilledDTO.setStatus(order.getOrderStatus().getOrderStatus());
        orderFulfilledDTO.setShippingDate(order.getDesiredShippingDate());
        orderFulfilledDTO.setDeliveryType(order.getOrderPriority().getDeliveryType());
        orderFulfilledDTO.setPriority(order.getOrderPriority().getDeliveryType());
        orderFulfilledDTO.setProductCategory(order.getProductCategory().getProductCategory());
        orderFulfilledDTO.setShippingMethod(order.getShippingMethod().getShippingMethod());
        orderFulfilledDTO.setTransactionId(order.getTransactionId());

        ofNullable(order.getOrderItems())
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .forEach(orderItemEntity -> {
                    if(orderFulfilledDTO.getItems() == null){
                        orderFulfilledDTO.setItems(new ArrayList<>());
                    }
                    orderFulfilledDTO.getItems().add(OrderFulfilledItemDTO
                        .builder()
                            .bloodType(orderItemEntity.getBloodType().getBloodType())
                            .productFamily(orderItemEntity.getProductFamily().getProductFamily())
                            .orderId(orderItemEntity.getOrderId().getOrderId())
                            .comments(orderItemEntity.getComments())
                            .quantity(orderItemEntity.getQuantity())
                            .shortDateProducts(getShortDateByOrderItem(orderItemEntity,pickList))
                        .build());
                });

        return new OrderFulfilledEventDTO(orderFulfilledDTO);
    }


    public Mono<OrderFulfilledEventDTO> buildShippingCustomerDetails(Tuple2<OrderFulfilledEventDTO, CustomerDTO> tuple2) {
        var orderFulfilledEvent = tuple2.getT1();
        var customer = tuple2.getT2();
        var shippingAddress = customer.addresses().stream().filter(customerAddressDTO -> customerAddressDTO
            .addressType().equals(ADDRESS_TYPE)).findFirst().orElse(null);
        if (shippingAddress == null) {
            log.error("Not able to find shipping address for customer code {}", customer.code());
            return Mono.error(new IllegalArgumentException("Shipping address not found"));
        }

        orderFulfilledEvent.getPayload().setCustomerAddressAddressLine1(shippingAddress.addressLine1());
        orderFulfilledEvent.getPayload().setCustomerAddressAddressLine2(shippingAddress.addressLine2());
        orderFulfilledEvent.getPayload().setCustomerAddressCity(shippingAddress.city());
        orderFulfilledEvent.getPayload().setCustomerAddressState(shippingAddress.state());
        orderFulfilledEvent.getPayload().setCustomerAddressPostalCode(shippingAddress.postalCode());
        orderFulfilledEvent.getPayload().setCustomerAddressCountry(shippingAddress.countryCode());
        orderFulfilledEvent.getPayload().setCustomerAddressCountryCode(shippingAddress.countryCode());
        orderFulfilledEvent.getPayload().setCustomerAddressDistrict(shippingAddress.district());
        orderFulfilledEvent.getPayload().setDepartmentName(customer.departmentName());
        orderFulfilledEvent.getPayload().setCustomerPhoneNumber(customer.phoneNumber());
        orderFulfilledEvent.getPayload().setDepartmentCode(customer.departmentCode());


        return Mono.just(orderFulfilledEvent);
    }

    private List<OrderFulfilledItemShortDateDTO> getShortDateByOrderItem(OrderItem orderItem , PickList pickList){

        var shortDateList = new ArrayList<OrderFulfilledItemShortDateDTO>();

        ofNullable(pickList.getPickListItems())
            .filter(pickListItems -> !pickListItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream().filter(pickListItem ->
                pickListItem.getBloodType().equals(orderItem.getBloodType().getBloodType())
                    && pickListItem.getProductFamily().equals(orderItem.getProductFamily().getProductFamily()) && pickListItem.getShortDateList() != null && !pickListItem.getShortDateList().isEmpty())
            .toList().forEach(pickListItem -> {
                pickListItem.getShortDateList().forEach(shortDate -> {
                    shortDateList.add(OrderFulfilledItemShortDateDTO.builder()
                            .productCode(shortDate.getProductCode())
                            .storageLocation(shortDate.getStorageLocation())
                            .unitNumber(shortDate.getUnitNumber())
                        .build());
                });
            });

        return shortDateList;

    }
}
