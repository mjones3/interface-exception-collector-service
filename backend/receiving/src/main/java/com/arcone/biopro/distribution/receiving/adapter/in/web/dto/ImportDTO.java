package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ImportDTO(
        Long id,
        String temperatureCategory,
        String transitStartDateTime,
        String transitStartTimeZone,
        String transitEndDateTime,
        String transitEndTimeZone,
        String totalTransitTime,
        String transitTimeResult,
        String temperature,
        String thermometerCode,
        String temperatureResult,
        String locationCode,
        String comments,
        String status,
        String employeeId,
        String createDate,
        String modificationDate,
        boolean isQuarantined,
        int maxNumberOfProducts,
        List<ImportItemDTO> products,
        boolean canComplete
) implements Serializable {
}
