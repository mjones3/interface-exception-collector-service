package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RemoveCartonCommandInput(
    Long cartonId,
    String employeeId
) implements Serializable {
}
