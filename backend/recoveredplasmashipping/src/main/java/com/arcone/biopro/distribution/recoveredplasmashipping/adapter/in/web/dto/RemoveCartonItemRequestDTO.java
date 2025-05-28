package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record RemoveCartonItemRequestDTO(
    Long cartonId,
    String employeeId,
    List<Long> cartonItemIds
) implements Serializable {
}
