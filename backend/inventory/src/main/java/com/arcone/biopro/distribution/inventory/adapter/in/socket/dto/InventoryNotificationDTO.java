package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryNotificationDTO(
    Integer errorCode,
    String errorMessage
) implements Serializable {
}
