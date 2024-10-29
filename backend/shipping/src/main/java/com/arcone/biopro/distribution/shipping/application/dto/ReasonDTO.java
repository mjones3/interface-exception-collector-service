package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ReasonDTO(
    Long id,
    String type,
    String reasonKey,
    boolean requireComments,
    Integer orderNumber,
    boolean active
) implements Serializable {
}
