package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record UseCaseOutput<T>(
    List<UseCaseNotificationOutput> notifications,
    T data,
    Map<String, String> _links
) implements Serializable {
}
