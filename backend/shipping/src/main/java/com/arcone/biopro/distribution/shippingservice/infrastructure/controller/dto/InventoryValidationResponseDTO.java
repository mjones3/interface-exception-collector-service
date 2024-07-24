package com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryValidationResponseDTO(
    InventoryResponseDTO inventoryResponseDTO,
    InventoryNotificationDTO inventoryNotificationDTO

) implements Serializable {
}
