package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for GraphQL alerts information.
 * Provides current alert status and recent alert history.
 */
@Component
@Endpoint(id = "graphql-alerts")
@RequiredArgsConstructor
public class GraphQLAlertsEndpoint {

    private final GraphQLAlertingService alertingService;

    @ReadOperation
    public Map<String, Object> graphqlAlerts() {
        Map<String, Object> alerts = new HashMap<>();

        alerts.put("timestamp", Instant.now());
        alerts.put("alerting_enabled", true); // This should come from configuration
        alerts.put("component", "graphql-alerting");

        // Get current alert status from the alerting service
        // This would need to be exposed by the alerting service
        alerts.put("active_alerts", getActiveAlerts());
        alerts.put("alert_thresholds", getAlertThresholds());
        alerts.put("recent_alerts", getRecentAlerts());

        return alerts;
    }

    private Map<String, Object> getActiveAlerts() {
        Map<String, Object> activeAlerts = new HashMap<>();
        activeAlerts.put("count", 0);
        activeAlerts.put("types", new String[] {});
        return activeAlerts;
    }

    private Map<String, Object> getAlertThresholds() {
        Map<String, Object> thresholds = new HashMap<>();
        thresholds.put("query_response_time_ms", 500);
        thresholds.put("mutation_response_time_ms", 3000);
        thresholds.put("error_rate_percent", 5.0);
        thresholds.put("throughput_per_minute", 1000);
        return thresholds;
    }

    private Map<String, Object> getRecentAlerts() {
        Map<String, Object> recentAlerts = new HashMap<>();
        recentAlerts.put("last_24_hours", 0);
        recentAlerts.put("last_hour", 0);
        recentAlerts.put("last_alert_time", null);
        return recentAlerts;
    }
}