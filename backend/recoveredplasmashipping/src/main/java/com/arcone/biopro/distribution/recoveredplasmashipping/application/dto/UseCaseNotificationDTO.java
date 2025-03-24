package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

@Builder
public record UseCaseNotificationDTO(
    UseCaseMessageType useCaseMessageType
) {
}
