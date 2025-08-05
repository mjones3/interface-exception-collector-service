package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderCustomerReportDTO(
    String code,
    String name
    ) implements Serializable {
}
