package com.arcone.biopro.distribution.inventory.domain.model.vo;

public record NotificationMessage(String name, Integer code, String message, String type, String action, String reason) {
}
