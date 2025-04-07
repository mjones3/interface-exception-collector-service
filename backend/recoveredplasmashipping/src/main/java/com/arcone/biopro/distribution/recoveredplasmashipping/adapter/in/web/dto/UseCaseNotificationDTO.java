package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

public record UseCaseNotificationDTO(
    String type,
    String message,
    Integer code
) {
}
