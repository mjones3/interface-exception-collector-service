package com.arcone.biopro.exception.collector.application.dto;

import lombok.Data;

/**
 * DTO for resolve responses.
 */
@Data
public class ResolveResponse {
    private String transactionId;
    private boolean success;
    private String message;
}