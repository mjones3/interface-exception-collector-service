package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record CustomerDTO (
    Long id,
    String externalId,
    String customerType,
    String name,
    String code,
    String departmentCode,
    String departmentName,
    String foreignFlag,
    String phoneNumber,
    String contactName,
    String state,
    String postalCode,
    String country,
    String countryCode,
    String city,
    String district,
    String addressLine1,
    String addressLine2,
    Boolean active,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate
) implements Serializable {


}
