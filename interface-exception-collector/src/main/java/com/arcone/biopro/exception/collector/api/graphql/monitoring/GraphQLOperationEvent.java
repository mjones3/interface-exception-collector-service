package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event published when a GraphQL operation completes.
 * Contains all relevant information for monitoring, logging, and alerting.
 */
@Data
public class GraphQLOperationEvent extends ApplicationEvent {

    private final String operationType;
    private final String operationName;
    private final String queryHash;
    private final long durationMs;
    private final boolean success;
    private final List<ErrorInfo> errors;
    private final String userId;
    private final String clientIp;

    public GraphQLOperationEvent(Object source, String operationType, String operationName,
            String queryHash, long durationMs, boolean success,
            List<ErrorInfo> errors, String userId, String clientIp) {
        super(source);
        this.operationType = operationType;
        this.operationName = operationName;
        this.queryHash = queryHash;
        this.durationMs = durationMs;
        this.success = success;
        this.errors = errors;
        this.userId = userId;
        this.clientIp = clientIp;
    }

    /**
     * Information about GraphQL errors
     */
    @Data
    @Builder
    public static class ErrorInfo {
        private final String type;
        private final String message;
        private final String path;
        private final String classification;
    }
}