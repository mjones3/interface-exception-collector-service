package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryNotificationDTO(
    String errorName,
    Integer errorCode,
    String errorMessage,
    String errorType,
    String action
) implements Serializable {
}
