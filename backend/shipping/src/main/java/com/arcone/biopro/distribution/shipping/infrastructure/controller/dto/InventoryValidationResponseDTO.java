package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record InventoryValidationResponseDTO(
    InventoryResponseDTO inventoryResponseDTO,
    List<InventoryNotificationDTO> inventoryNotificationsDTO

) implements Serializable {

    public boolean hasOnlyNotificationType(String type) {
        if(inventoryNotificationsDTO == null){
            throw new IllegalArgumentException("inventoryNotificationsDTO is null");
        }

        if(type == null ||  type.isBlank()){
            throw new IllegalArgumentException("type is null");
        }

        var countNotifications = inventoryNotificationsDTO.stream()
            .filter(notificationDTO -> notificationDTO.errorName().equals(type))
            .count();
        return countNotifications == 1 && inventoryNotificationsDTO.size() == 1;
    }
}
