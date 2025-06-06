package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidationResultOutput(
    boolean valid,
    String message,
    String result
) implements Serializable {
}
