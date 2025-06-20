package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ValidateTransitTimeRequestDTO(
    String temperatureCategory,
    LocalDateTime startDateTime,
    String startTimeZone,
    LocalDateTime endDateTime,
    String endTimeZone
) {
}
