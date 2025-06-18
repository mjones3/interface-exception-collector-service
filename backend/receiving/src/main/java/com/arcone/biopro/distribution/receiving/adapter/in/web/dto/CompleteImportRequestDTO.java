package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteImportRequestDTO(
    Long importId,
    String completeEmployeeId
) implements Serializable {
}
