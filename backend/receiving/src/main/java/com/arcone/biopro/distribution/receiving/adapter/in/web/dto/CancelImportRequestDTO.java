package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CancelImportRequestDTO(
    Long importId,
    String cancelEmployeeId
) implements Serializable {
}
