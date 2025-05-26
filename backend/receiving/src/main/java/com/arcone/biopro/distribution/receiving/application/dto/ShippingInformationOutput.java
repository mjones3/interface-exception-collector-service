package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ShippingInformationOutput(

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
    List<LookupOutput> transitTimeZoneList,
    List<LookupOutput> visualInspectionList

) implements Serializable {
}
