package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;

import java.util.List;

public record ValidateInventoryOutput(InventoryOutput inventoryOutput, List<NotificationMessage> notificationMessages) {
}
