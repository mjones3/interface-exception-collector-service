package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import java.io.Serializable;
import java.util.List;

public record UseCaseResponseDTO<T>(
    List<UseCaseNotificationDTO> notifications,
    T data
) implements Serializable {
}
