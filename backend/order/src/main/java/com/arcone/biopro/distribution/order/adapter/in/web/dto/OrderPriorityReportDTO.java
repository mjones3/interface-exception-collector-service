package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderPriorityReportDTO(
    String priority,
    String priorityColor

) implements Serializable {
}
