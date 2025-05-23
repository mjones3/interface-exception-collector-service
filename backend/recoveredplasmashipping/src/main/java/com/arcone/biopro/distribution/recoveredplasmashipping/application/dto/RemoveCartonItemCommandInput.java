package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record RemoveCartonItemCommandInput(
    Long cartonId,
    String employeeId,
    List<Long> cartonItemIds
) implements Serializable {
}
