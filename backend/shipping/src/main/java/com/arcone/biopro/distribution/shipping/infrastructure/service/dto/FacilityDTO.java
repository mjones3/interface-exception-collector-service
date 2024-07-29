package com.arcone.biopro.distribution.shipping.infrastructure.service.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Builder
public record FacilityDTO(
    Integer id,
    Integer partOfId,
    String externalId,
    String name,
    String description,
    String addressLine1,

    String addressLine2,
    String postalCode,
    String city,
    String state,

    List<Integer> locationTypeIds,
    List<String> licenses,
    Map<String,String> properties


) implements Serializable {
}
