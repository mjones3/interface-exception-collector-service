package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UseCaseNotificationDTO(
    String type,
    String message,
    Integer code,
    String action,
    String reason,
    List<String> details,
    String name
) {
}
