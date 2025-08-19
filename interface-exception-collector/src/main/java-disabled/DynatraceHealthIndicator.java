package com.arcone.biopro.exception.collector.infrastructure.health;

import com.arcone.biopro.exception.collector.infrastructure.monitoring.DynatraceBusinessMetricsService;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Dynatrace monitoring integration.
 * Provides health status and business metrics summary for monitoring
 * dashboards.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = false)
public class DynatraceHealthIndicator implements HealthIndicator {

    private final OneAgentSDK oneAgentSDK;
    private final DynatraceBusinessMetricsService businessMetricsService;

    @Override
    public Health health() {
        try {
            Health.Builder healthBuilder = new Health.Builder();

            // Check OneAgent SDK status
            OneAgentSDK.State sdkState = oneAgentSDK.getCurrentState();
            boolean dynatraceActive = sdkState == OneAgentSDK.State.ACTIVE;

            if (dynatraceActive) {
                healthBuilder.up();
            } else {
                healthBuilder.down()
                        .withDetail("reason", "Dynatrace OneAgent SDK not active")
                        .withDetail("sdk_state", sdkState.name());
            }

            // Add SDK details
            healthBuilder.withDetail("dynatrace_sdk_state", sdkState.name());
            healthBuilder.withDetail("dynatrace_sdk_version", oneAgentSDK.getVersionInfo());

            // Add business metrics summary
            try {
                DynatraceBusinessMetricsService.BusinessMetricsSummary summary = businessMetricsService
                        .getBusinessMetricsSummary();

                healthBuilder.withDetail("business_metrics", summary);

                // Add health indicators based on business metrics
                addBusinessHealthIndicators(healthBuilder, summary);

            } catch (Exception e) {
                log.warn("Failed to retrieve business metrics summary", e);
                healthBuilder.withDetail("business_metrics_error", e.getMessage());
            }

            // Add monitoring capabilities
            healthBuilder.withDetail("capabilities", getMonitoringCapabilities());

            return healthBuilder.build();

        } catch (Exception e) {
            log.error("Error checking Dynatrace health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("dynatrace_available", false)
                    .build();
        }
    }

    /**
     * Adds business-specific health indicators based on metrics.
     */
    private void addBusinessHealthIndicators(Health.Builder healthBuilder,
            DynatraceBusinessMetricsService.BusinessMetricsSummary summary) {

        // Critical exception threshold check
        if (summary.getCriticalExceptions() > 10) {
            healthBuilder.withDetail("critical_exceptions_warning",
                    "High number of critical exceptions: " + summary.getCriticalExceptions());
        }

        // Retry success rate check
        if (summary.getRetrySuccessRate() < 70.0) {
            healthBuilder.withDetail("retry_success_rate_warning",
                    "Low retry success rate: " + String.format("%.2f%%", summary.getRetrySuccessRate()));
        }

        // Pending exceptions check
        if (summary.getPendingExceptions() > 100) {
            healthBuilder.withDetail("pending_exceptions_warning",
                    "High number of pending exceptions: " + summary.getPendingExceptions());
        }

        // Processing efficiency check
        double processingEfficiency = calculateProcessingEfficiency(summary);
        if (processingEfficiency < 80.0) {
            healthBuilder.withDetail("processing_efficiency_warning",
                    "Low processing efficiency: " + String.format("%.2f%%", processingEfficiency));
        }

        healthBuilder.withDetail("processing_efficiency_percent", processingEfficiency);
    }

    /**
     * Calculates processing efficiency based on received vs processed exceptions.
     */
    private double calculateProcessingEfficiency(DynatraceBusinessMetricsService.BusinessMetricsSummary summary) {
        if (summary.getTotalExceptionsReceived() == 0) {
            return 100.0;
        }
        return (summary.getTotalExceptionsProcessed() / summary.getTotalExceptionsReceived()) * 100.0;
    }

    /**
     * Gets the monitoring capabilities provided by Dynatrace integration.
     */
    private MonitoringCapabilities getMonitoringCapabilities() {
        return MonitoringCapabilities.builder()
                .distributedTracing(oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE)
                .customMetrics(true)
                .businessMetrics(true)
                .performanceMonitoring(true)
                .errorTracking(true)
                .userExperienceMonitoring(false) // Not applicable for backend service
                .infrastructureMonitoring(oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE)
                .logAnalytics(true)
                .alerting(true)
                .build();
    }

    /**
     * Monitoring capabilities data structure.
     */
    @lombok.Builder
    @lombok.Data
    public static class MonitoringCapabilities {
        private boolean distributedTracing;
        private boolean customMetrics;
        private boolean businessMetrics;
        private boolean performanceMonitoring;
        private boolean errorTracking;
        private boolean userExperienceMonitoring;
        private boolean infrastructureMonitoring;
        private boolean logAnalytics;
        private boolean alerting;
    }
}