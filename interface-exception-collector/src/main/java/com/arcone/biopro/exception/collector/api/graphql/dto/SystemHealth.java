package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * GraphQL type for system health information.
 * Maps to the SystemHealth type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealth {

    private HealthStatus status;
    private ComponentHealth database;
    private ComponentHealth cache;
    private List<ServiceHealth> externalServices;
    private OffsetDateTime lastUpdated;

    /**
     * Enum representing overall health status.
     */
    public enum HealthStatus {
        UP,
        DOWN,
        DEGRADED
    }

    /**
     * Health information for a system component.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealth {
        private HealthStatus status;
        private Float responseTime;
        private Object details; // JSON type in GraphQL
    }

    /**
     * Health information for an external service.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String serviceName;
        private HealthStatus status;
        private Float responseTime;
        private OffsetDateTime lastChecked;
    }
}