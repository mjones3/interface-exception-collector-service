package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record InventoryNotificationDTO(
    String errorName,
    Integer errorCode,
    String errorMessage,
    String errorType,
    String action,
    String reason,
    List<String> details
) implements Serializable {
}
