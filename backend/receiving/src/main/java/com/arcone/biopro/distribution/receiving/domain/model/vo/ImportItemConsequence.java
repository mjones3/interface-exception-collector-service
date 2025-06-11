package com.arcone.biopro.distribution.receiving.domain.model.vo;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ImportItemConsequence(
    String consequenceType ,
    String consequenceReason
) implements Serializable {
}
