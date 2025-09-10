package com.arcone.biopro.exception.collector.api.graphql.security;

/**
 * Exception thrown when rate limiting is exceeded for mutation operations.
 * Used to prevent abuse of the GraphQL mutation endpoints.
 * 
 * Requirements: 5.3, 5.5
 */
public class RateLimitExceededException extends RuntimeException {

    private final String userId;
    private final String operationType;
    private final int currentCount;
    private final int maxAllowed;
    private final long resetTimeMs;

    public RateLimitExceededException(String userId, String operationType, 
                                    int currentCount, int maxAllowed, long resetTimeMs) {
        super(String.format("Rate limit exceeded for user %s on operation %s: %d/%d requests. Reset in %d ms", 
              userId, operationType, currentCount, maxAllowed, resetTimeMs));
        this.userId = userId;
        this.operationType = operationType;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.resetTimeMs = resetTimeMs;
    }

    public String getUserId() {
        return userId;
    }

    public String getOperationType() {
        return operationType;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public long getResetTimeMs() {
        return resetTimeMs;
    }
}