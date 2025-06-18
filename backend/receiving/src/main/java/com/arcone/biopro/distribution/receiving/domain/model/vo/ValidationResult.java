package com.arcone.biopro.distribution.receiving.domain.model.vo;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidationResult(
        boolean valid,
        String message,
        String result,
        String resultDescription
) implements Serializable {
}
