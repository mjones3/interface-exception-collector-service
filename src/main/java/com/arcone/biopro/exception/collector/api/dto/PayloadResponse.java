package com.arcone.biopro.exception.collector.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for original payload retrieval from source services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadResponse {

    private String transactionId;
    private String interfaceType;
    private Object payload;
    private String sourceService;
    private boolean retrieved;
    private String errorMessage;
}