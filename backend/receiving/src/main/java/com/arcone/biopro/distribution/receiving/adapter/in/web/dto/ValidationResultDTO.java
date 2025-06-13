package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidationResultDTO(
    boolean valid,
    String message,
    String result,
    String resultDescription
) implements Serializable {
}
