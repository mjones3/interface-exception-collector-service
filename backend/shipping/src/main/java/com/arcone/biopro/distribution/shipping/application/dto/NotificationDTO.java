package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record NotificationDTO(
    String name,
    int statusCode,
    String notificationType,
    String message,
    Integer code,
    String action,
    String reason
) implements Serializable {
}
