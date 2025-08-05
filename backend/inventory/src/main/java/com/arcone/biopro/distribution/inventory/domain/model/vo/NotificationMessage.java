package com.arcone.biopro.distribution.inventory.domain.model.vo;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationMessage(String name, Integer code, String message, String type, String action, String reason, List<String> details) {
}
