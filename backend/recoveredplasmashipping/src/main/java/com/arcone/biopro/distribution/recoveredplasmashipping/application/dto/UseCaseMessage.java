package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record UseCaseMessage(
    UseCaseNotificationType type,
    String message,
    Integer code,
    String action,
    String reason,
    List<String>details
) implements Serializable {

}
