package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteImportCommandInput(
    Long importId,
    String completeEmployeeId
) implements Serializable {
}
