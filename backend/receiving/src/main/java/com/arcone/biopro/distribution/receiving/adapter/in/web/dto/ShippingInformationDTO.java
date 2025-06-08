package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShippingInformationDTO(

    String productCategory,
    String temperatureUnit,
    boolean displayTransitInformation,
    boolean displayTemperature,
    List<LookupDTO> transitTimeZoneList,
    List<LookupDTO> visualInspectionList,
    String defaultTimeZone

) implements Serializable {
}
