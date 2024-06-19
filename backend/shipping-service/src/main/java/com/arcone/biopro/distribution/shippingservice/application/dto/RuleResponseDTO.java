package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Builder
public record RuleResponseDTO(
    HttpStatus ruleCode,
    Map<String, List<?>> results,
    List<NotificationDTO> notifications,
    Map<String, String> _links
) implements Serializable {
}
