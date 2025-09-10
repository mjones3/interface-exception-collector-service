package com.arcone.biopro.exception.collector.application.dto;

import lombok.Data;

/**
 * DTO for retry responses.
 */
@Data
public class RetryResponse {
    private String transactionId;
    private boolean success;
    private String message;
    private int attemptNumber;
}