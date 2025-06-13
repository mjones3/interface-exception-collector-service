package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CancelImportCommandInput(
    Long importId,
    String cancelEmployeeId
) implements Serializable {
}
