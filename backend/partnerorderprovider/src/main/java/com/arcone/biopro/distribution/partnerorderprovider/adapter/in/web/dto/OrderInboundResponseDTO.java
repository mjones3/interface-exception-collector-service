package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record OrderInboundResponseDTO(
    String id,
    String status,
    String errorMessage,
    ZonedDateTime timestamp

) implements Serializable {
}
