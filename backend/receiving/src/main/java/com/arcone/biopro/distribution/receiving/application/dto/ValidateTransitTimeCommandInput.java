package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ValidateTransitTimeCommandInput(
    String temperatureCategory,
    LocalDateTime startDateTime,
    String startTimeZone,
    LocalDateTime endDateTime,
    String endTimeZone
) {
}
