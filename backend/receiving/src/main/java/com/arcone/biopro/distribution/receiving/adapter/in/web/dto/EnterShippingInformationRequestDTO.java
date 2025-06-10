package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record EnterShippingInformationRequestDTO(
    String productCategory,
    String employeeId,
    String locationCode
) implements Serializable {
}
