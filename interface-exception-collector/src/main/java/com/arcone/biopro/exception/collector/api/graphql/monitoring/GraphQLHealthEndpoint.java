package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for GraphQL health and alerting status.
 * Provides detailed information about GraphQL performance and alert status.
 */
@Component("graphQLAlertingEndpoint")
@Endpoint(id = "graphql-alerting")
@RequiredArgsConstructor
public class GraphQLHealthEndpoint {

    private final GraphQLAlertingService alertingService;

    @ReadOperation
    public Map<String, Object> graphqlHealth() {
        Map<String, Object> health = new HashMap<>();

        // Get health from alerting service
        Health alertingHealth = alertingService.health();
        health.put("status", alertingHealth.getStatus().getCode());
        health.put("details", alertingHealth.getDetails());

        // Add additional GraphQL-specific health information
        health.put("component", "graphql-api");
        health.put("description", "GraphQL API health and alerting status");

        return health;
    }
}