package com.arcone.biopro.distribution.partnerorderprovider.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record ValidationResponseDTO(
    UUID id,
    String status,
    String errorMessage,
    ZonedDateTime timestamp

) implements Serializable {

}
