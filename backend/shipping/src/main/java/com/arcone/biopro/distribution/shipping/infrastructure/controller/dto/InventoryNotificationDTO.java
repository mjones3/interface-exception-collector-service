package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryNotificationDTO(
    Integer errorCode,
    String errorMessage
) implements Serializable {
}
