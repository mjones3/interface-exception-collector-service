package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record InventoryValidationResponseDTO(
    InventoryResponseDTO inventoryResponseDTO,
    List<InventoryNotificationDTO> inventoryNotificationsDTO

) implements Serializable {
}
