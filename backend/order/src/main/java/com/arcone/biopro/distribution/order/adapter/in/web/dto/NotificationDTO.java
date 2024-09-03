package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record NotificationDTO(
    String name,
    String notificationType,
    String notificationMessage
) implements Serializable {

}
