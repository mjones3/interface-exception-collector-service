package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.Map;

@Builder
public record LocationOutput(
    Long id,
    String name,
    String code,
    String externalId,
    String addressLine1,
    String addressLine2,
    String postalCode,
    String city,
    String state,
    Map<String,String> properties

) implements Serializable {
}
