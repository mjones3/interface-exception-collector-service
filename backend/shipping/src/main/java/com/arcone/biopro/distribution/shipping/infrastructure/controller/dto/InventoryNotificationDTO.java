package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryNotificationDTO(
    String errorName,
    Integer errorCode,
    String errorMessage,
    String errorType,
    String action,
    String reason
) implements Serializable {
}
