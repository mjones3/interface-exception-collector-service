package com.arcone.biopro.distribution.order.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledItemDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledItemShortDateDTO;
import com.arcone.biopro.distribution.order.infrastructure.persistence.LocationEntity;
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
        orderFulfilledDTO.setLocationCode(order.getLocationFrom().getCode());
        orderFulfilledDTO.setBillingCustomerCode(order.getBillingCustomer() != null  ? order.getBillingCustomer().getCode() : null);
        orderFulfilledDTO.setBillingCustomerName(order.getBillingCustomer() != null  ? order.getBillingCustomer().getName() : null);
        orderFulfilledDTO.setShippingCustomerCode(order.getShippingCustomer().getCode());
        orderFulfilledDTO.setShippingCustomerName(order.getShippingCustomer().getName());
        orderFulfilledDTO.setComments(order.getComments());
        orderFulfilledDTO.setStatus(order.getOrderStatus().getOrderStatus());
        orderFulfilledDTO.setShippingDate(order.getDesiredShippingDate());
        orderFulfilledDTO.setDeliveryType(order.getOrderPriority().getDeliveryType());
        orderFulfilledDTO.setPriority(order.getOrderPriority().getDeliveryType());
        orderFulfilledDTO.setProductCategory(order.getProductCategory().getProductCategory());
        orderFulfilledDTO.setShippingMethod(order.getShippingMethod().getShippingMethod());
        orderFulfilledDTO.setShipmentType(order.getShipmentType().getShipmentType());
        orderFulfilledDTO.setQuarantinedProducts(order.getQuarantinedProducts());
        orderFulfilledDTO.setLabelStatus(order.getLabelStatus() != null ? order.getLabelStatus().value() : null);

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


    public Mono<OrderFulfilledEventDTO> buildShippingCustomerDetails(OrderFulfilledEventDTO orderFulfilledEventDTO, CustomerDTO customerDTO) {
        var shippingAddress = customerDTO.addresses().stream().filter(customerAddressDTO -> customerAddressDTO
            .addressType().equals(ADDRESS_TYPE)).findFirst().orElse(null);
        if (shippingAddress == null) {
            log.error("Not able to find shipping address for customer code {}", customerDTO.code());
            return Mono.error(new IllegalArgumentException("Shipping address not found"));
        }

        orderFulfilledEventDTO.getPayload().setCustomerAddressAddressLine1(shippingAddress.addressLine1());
        orderFulfilledEventDTO.getPayload().setCustomerAddressAddressLine2(shippingAddress.addressLine2());
        orderFulfilledEventDTO.getPayload().setCustomerAddressCity(shippingAddress.city());
        orderFulfilledEventDTO.getPayload().setCustomerAddressState(shippingAddress.state());
        orderFulfilledEventDTO.getPayload().setCustomerAddressPostalCode(shippingAddress.postalCode());
        orderFulfilledEventDTO.getPayload().setCustomerAddressCountry(shippingAddress.countryCode());
        orderFulfilledEventDTO.getPayload().setCustomerAddressCountryCode(shippingAddress.countryCode());
        orderFulfilledEventDTO.getPayload().setCustomerAddressDistrict(shippingAddress.district());
        orderFulfilledEventDTO.getPayload().setDepartmentName(customerDTO.departmentName());
        orderFulfilledEventDTO.getPayload().setCustomerPhoneNumber(customerDTO.phoneNumber());
        orderFulfilledEventDTO.getPayload().setDepartmentCode(customerDTO.departmentCode());


        return Mono.just(orderFulfilledEventDTO);
    }

    public Mono<OrderFulfilledEventDTO> buildShippingCustomerDetailsFromLocation(OrderFulfilledEventDTO orderFulfilledEventDTO, LocationEntity locationEntity) {

        orderFulfilledEventDTO.getPayload().setCustomerAddressAddressLine1(locationEntity.getAddressLine1());
        orderFulfilledEventDTO.getPayload().setCustomerAddressAddressLine2(locationEntity.getAddressLine2());
        orderFulfilledEventDTO.getPayload().setCustomerAddressCity(locationEntity.getCity());
        orderFulfilledEventDTO.getPayload().setCustomerAddressState(locationEntity.getState());
        orderFulfilledEventDTO.getPayload().setCustomerAddressPostalCode(locationEntity.getPostalCode());
        orderFulfilledEventDTO.getPayload().setCustomerAddressCountry("US");
        orderFulfilledEventDTO.getPayload().setCustomerAddressCountryCode("US");
        return Mono.just(orderFulfilledEventDTO);
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
