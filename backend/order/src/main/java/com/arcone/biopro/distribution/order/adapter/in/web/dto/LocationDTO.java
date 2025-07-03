package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import java.io.Serializable;
import java.util.Map;

public record LocationDTO(
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
