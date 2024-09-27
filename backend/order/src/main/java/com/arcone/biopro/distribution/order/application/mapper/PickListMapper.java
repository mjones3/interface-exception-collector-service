package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.NotificationDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListCustomerDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListItemDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListItemShortDateDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListResponseDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
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

    public PickListResponseDTO mapToDTO(UseCaseResponseDTO<PickList> useCaseResponseDTO) {
        return PickListResponseDTO.builder()
            .notifications(ofNullable(useCaseResponseDTO.notifications())
                .filter(notificationDTOList -> !notificationDTOList.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(notification -> NotificationDTO.builder()
                    .name(notification.useCaseMessageType().name())
                    .notificationType(notification.useCaseMessageType().getType().name())
                    .notificationMessage(notification.useCaseMessageType().getMessage())
                    .build())
                .toList()
            )
            .data(ofNullable(useCaseResponseDTO.data())
                .map(pickList ->
                    PickListDTO
                        .builder()
                        .orderNumber(useCaseResponseDTO.data().getOrderNumber())
                        .orderComments(useCaseResponseDTO.data().getOrderComments())
                        .customer(PickListCustomerDTO.builder()
                            .code(useCaseResponseDTO.data().getCustomer().getCode())
                            .name(useCaseResponseDTO.data().getCustomer().getName())
                            .build())
                        .pickListItems(
                            ofNullable(useCaseResponseDTO.data().getPickListItems())
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
                                            .aboRh(pickListItemShortDate.getAboRh())
                                            .unitNumber(pickListItemShortDate.getUnitNumber())
                                            .build())
                                        .toList()
                                    )
                                    .build())
                                .toList()
                        )
                        .build()
                )
                    .orElse(null))
            .build();
    }

    public UseCaseResponseDTO<PickList> mapToUseCaseResponse(Order order){

        var pickList = new PickList(order.getOrderNumber().getOrderNumber() , order.getLocationCode() , order.getOrderStatus().getOrderStatus()
            , new PickListCustomer(order.getShippingCustomer().getCode() , order.getShippingCustomer().getName()), order.getComments());

        if(order.getOrderItems() != null){
            order.getOrderItems().forEach(orderItem -> pickList.addPickListItem(new PickListItem(orderItem.getProductFamily().getProductFamily()
                , orderItem.getBloodType().getBloodType() , orderItem.getQuantity() , orderItem.getComments() )));
        }

        return new UseCaseResponseDTO<>(null,pickList);

    }
}
