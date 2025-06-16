package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ImportOutput(
        Long id,
        String temperatureCategory,
        LocalDateTime transitStartDateTime,
        String transitStartTimeZone,
        LocalDateTime transitEndDateTime,
        String transitEndTimeZone,
        String totalTransitTime,
        String transitTimeResult,
        BigDecimal temperature,
        String thermometerCode,
        String temperatureResult,
        String locationCode,
        String comments,
        String status,
        String employeeId,
        ZonedDateTime createDate,
        ZonedDateTime modificationDate,
        boolean isQuarantined,
        int maxNumberOfProducts,
        List<ImportItemOutput> products
) implements Serializable {
}
