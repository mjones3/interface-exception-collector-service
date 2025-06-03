package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

@Builder
public record UseCaseNotificationOutput(
    UseCaseMessage useCaseMessage
) {
}
