package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record NotificationDTO(
    int statusCode,
    String notificationType,
    String message

) implements Serializable {
}
