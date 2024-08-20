package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListCustomerDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListItemDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListItemShortDateDTO;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItem;
import com.arcone.biopro.distribution.order.domain.model.ShortDateProduct;
import com.arcone.biopro.distribution.order.domain.model.vo.PickListCustomer;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledItemDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
public class PickListMapper {

    public PickListDTO mapToDTO(PickList pickList) {
        return PickListDTO
            .builder()
            .orderNumber(pickList.getOrderNumber())
            .customer(PickListCustomerDTO.builder()
                .code(pickList.getCustomer().getCode())
                .name(pickList.getCustomer().getName())
                .build())
            .pickListItems(
                ofNullable(pickList.getPickListItems())
                    .filter(pickListItems -> !pickListItems.isEmpty())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(pickListItem -> PickListItemDTO
                        .builder()
                        .bloodType(pickListItem.getBloodType())
                        .productFamily(pickListItem.getProductFamily())
                        .quantity(pickListItem.getQuantity())
                        .comments(pickListItem.getComments())
                        .shortDateList(ofNullable(pickListItem.getShortDateList())
                            .filter(shortDateList -> !shortDateList.isEmpty())
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .map(pickListItemShortDate -> PickListItemShortDateDTO
                                .builder()
                                .productCode(pickListItemShortDate.getProductCode())
                                .storageLocation(pickListItemShortDate.getStorageLocation())
                                .unitNumber(pickListItemShortDate.getUnitNumber())
                                .build())
                            .toList()
                        )
                        .build())
                    .toList()
            )
            .build();
    }

    public PickList mapToDomain(Order order){

        var pickList = new PickList(order.getOrderNumber().getOrderNumber() , order.getLocationCode() , order.getOrderStatus().getOrderStatus()
            , new PickListCustomer(order.getShippingCustomer().getCode() , order.getShippingCustomer().getName()));

        if(order.getOrderItems() != null){
            order.getOrderItems().forEach(orderItem -> pickList.addPickListItem(new PickListItem(orderItem.getProductFamily().getProductFamily()
                , orderItem.getBloodType().getBloodType() , orderItem.getQuantity() , orderItem.getComments() )));
        }

        return pickList;
    }
}
