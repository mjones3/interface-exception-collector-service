package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

@Builder
public record UseCaseNotificationDTO(
    UseCaseMessageType useCaseMessageType
) {
}
