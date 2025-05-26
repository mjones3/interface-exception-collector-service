package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ShippingInformationDTO(

    String productCategory,
    LocalDate startTransitDate,
    LocalDate endTransitDate,
    LocalTime startTransitTime,
    LocalTime endTransitTime,
    String startTransitTimeZone,
    String endTransitTimeZone,
    Integer temperature,
    String temperatureUnit,
    String thermometerCode,
    String comments,
    boolean displayTransitInformation,
    boolean displayTemperature,
    List<LookupDTO> transitTimeZoneList,
    List<LookupDTO> visualInspectionList

) implements Serializable {
}
