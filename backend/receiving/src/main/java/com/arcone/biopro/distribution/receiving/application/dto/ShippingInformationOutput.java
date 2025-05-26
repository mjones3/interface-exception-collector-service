package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShippingInformationOutput(

    String productCategory,
    String temperatureUnit,
    boolean displayTransitInformation,
    boolean displayTemperature,
    List<LookupOutput> transitTimeZoneList,
    List<LookupOutput> visualInspectionList

) implements Serializable {
}
