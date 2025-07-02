package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.vo.NotificationMessage;

import java.util.List;

public record ValidateInventoryOutput(InventoryOutput inventoryOutput, List<NotificationMessage> notificationMessages) {
}
