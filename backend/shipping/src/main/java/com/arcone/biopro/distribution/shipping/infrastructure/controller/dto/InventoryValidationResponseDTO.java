package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    public boolean hasNotificationType(String type) {
        if(inventoryNotificationsDTO == null){
            throw new IllegalArgumentException("inventoryNotificationsDTO is null");
        }

        if(type == null ||  type.isBlank()){
            throw new IllegalArgumentException("type is null");
        }

        return inventoryNotificationsDTO.stream().map(InventoryNotificationDTO::errorName)
            .distinct().anyMatch(notificationName -> notificationName.equals(type));
    }

    public boolean hasOnlyNotificationTypes(final List<String> types) {
        if (inventoryNotificationsDTO == null) {
            throw new IllegalArgumentException("inventoryNotificationsDTO is null");
        }

        if (types == null) {
            throw new IllegalArgumentException("types is null");
        }

        var typesSet = new TreeSet<>(types);
        var notificationTypesSet = inventoryNotificationsDTO.stream()
            .map(InventoryNotificationDTO::errorName)
            .collect(Collectors.toCollection(TreeSet::new));

        return typesSet.equals(notificationTypesSet);
    }

}
