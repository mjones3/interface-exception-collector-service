package com.arcone.biopro.exception.collector.application.dto;

import lombok.Data;

/**
 * DTO for acknowledge responses.
 */
@Data
public class AcknowledgeResponse {
    private String transactionId;
    private boolean success;
    private String message;
}