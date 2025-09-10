package com.arcone.biopro.exception.collector.application.dto;

import lombok.Data;

/**
 * DTO for cancel retry responses.
 */
@Data
public class CancelRetryResponse {
    private String transactionId;
    private boolean success;
    private String message;
}