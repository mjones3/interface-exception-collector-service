package com.arcone.biopro.exception.collector.application.dto;

import lombok.Data;

/**
 * DTO for retry requests.
 */
@Data
public class RetryRequest {
    private String transactionId;
    private String reason;
    private int maxRetries;
    private long delayMillis;
}