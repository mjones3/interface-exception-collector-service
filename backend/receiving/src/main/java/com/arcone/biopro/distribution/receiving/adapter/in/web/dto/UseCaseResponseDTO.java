package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record UseCaseResponseDTO<T>(
    List<UseCaseNotificationDTO> notifications,
    T data,
    Map<String, String> _links
) implements Serializable {
}

