package com.arcone.biopro.exceptioncollector.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceException {
    private Long id;
    private String transactionId;
    private String interfaceType;
    private String exceptionReason;
    private String operation;
    private String externalId;
    private ExceptionStatus status;
    private ExceptionSeverity severity;
    private boolean retryable;
    private Instant timestamp;
    private Instant retryTimestamp;
    private String originalPayload;
    
    public enum ExceptionStatus {
        NEW, ACKNOWLEDGED, RETRIED_SUCCESS, RETRIED_FAILED, RESOLVED
    }
    
    public enum ExceptionSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
