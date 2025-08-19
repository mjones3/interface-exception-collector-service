package com.arcone.biopro.exception.collector.api.actuator;

import com.arcone.biopro.exception.collector.infrastructure.monitoring.DynatraceBusinessMetricsService;
import com.dynatrace.oneagent.sdk.OneAgentSDK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for exposing Dynatrace business metrics.
 * Provides detailed business metrics and monitoring status for
 * interface exception management operations.
 */
@Component
@Endpoint(id = "dynatrace")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = true)
public class DynatraceBusinessMetricsEndpoint {

    private final DynatraceBusinessMetricsService businessMetricsService;
    private final OneAgentSDK oneAgentSDK;

    /**
     * Provides comprehensive Dynatrace monitoring status and business metrics.
     *
     * @return monitoring status and business metrics
     */
    @ReadOperation
    public Map<String, Object> dynatraceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Dynatrace SDK Status
            status.put("timestamp", LocalDateTime.now());
            status.put("dynatrace_sdk_state", oneAgentSDK.getCurrentState().name());
            status.put("dynatrace_sdk_active", oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE);
            status.put("dynatrace_version", oneAgentSDK.getVersionInfo());

            // Business Metrics Summary
            DynatraceBusinessMetricsService.BusinessMetricsSummary businessMetrics = 
                    businessMetricsService.getBusinessMetricsSummary();
            status.put("business_metrics", businessMetrics);

            // Exception Management KPIs
            Map<String, Object> kpis = calculateBusinessKPIs(businessMetrics);
            status.put("business_kpis", kpis);

            // Monitoring Capabilities
            status.put("monitoring_capabilities", getMonitoringCapabilities());

            // Health Indicators
            status.put("health_indicators", getHealthIndicators(businessMetrics));

            // Performance Indicators
            status.put("performance_indicators", getPerformanceIndicators(businessMetrics));

            log.debug("Generated Dynatrace status report with {} business metrics", 
                    businessMetrics != null ? "valid" : "null");

        } catch (Exception e) {
            log.error("Error generating Dynatrace status", e);
            status.put("error", e.getMessage());
            status.put("status", "ERROR");
        }

        return status;
    }

    /**
     * Calculates key business KPIs for interface exception management.
     */
    private Map<String, Object> calculateBusinessKPIs(DynatraceBusinessMetricsService.BusinessMetricsSummary metrics) {
        Map<String, Object> kpis = new HashMap<>();

        if (metrics == null) {
            kpis.put("status", "No metrics available");
            return kpis;
        }

        // Exception Resolution Rate
        double totalExceptions = metrics.getPendingExceptions() + metrics.getFailedExceptions() + metrics.getResolvedExceptions();
        double resolutionRate = totalExceptions > 0 ? (metrics.getResolvedExceptions() / totalExceptions) * 100.0 : 0.0;
        kpis.put("exception_resolution_rate_percent", Math.round(resolutionRate * 100.0) / 100.0);

        // Critical Exception Ratio
        double totalSeverityExceptions = metrics.getCriticalExceptions() + metrics.getHighSeverityExceptions() + 
                                       metrics.getMediumSeverityExceptions() + metrics.getLowSeverityExceptions();
        double criticalRatio = totalSeverityExceptions > 0 ? (metrics.getCriticalExceptions() / totalSeverityExceptions) * 100.0 : 0.0;
        kpis.put("critical_exception_ratio_percent", Math.round(criticalRatio * 100.0) / 100.0);

        // Processing Efficiency
        double processingEfficiency = metrics.getTotalExceptionsReceived() > 0 ? 
                (metrics.getTotalExceptionsProcessed() / metrics.getTotalExceptionsReceived()) * 100.0 : 0.0;
        kpis.put("processing_efficiency_percent", Math.round(processingEfficiency * 100.0) / 100.0);

        // Retry Success Rate
        kpis.put("retry_success_rate_percent", Math.round(metrics.getRetrySuccessRate() * 100.0) / 100.0);

        // Exception Backlog
        kpis.put("exception_backlog_count", metrics.getPendingExceptions() + metrics.getFailedExceptions());

        // Service Availability (based on processing efficiency)
        String availabilityStatus;
        if (processingEfficiency >= 95.0) {
            availabilityStatus = "EXCELLENT";
        } else if (processingEfficiency >= 90.0) {
            availabilityStatus = "GOOD";
        } else if (processingEfficiency >= 80.0) {
            availabilityStatus = "FAIR";
        } else {
            availabilityStatus = "POOR";
        }
        kpis.put("service_availability_status", availabilityStatus);

        return kpis;
    }

    /**
     * Gets monitoring capabilities status.
     */
    private Map<String, Object> getMonitoringCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        
        boolean sdkActive = oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE;
        
        capabilities.put("distributed_tracing", sdkActive);
        capabilities.put("custom_metrics", true);
        capabilities.put("business_metrics", true);
        capabilities.put("performance_monitoring", sdkActive);
        capabilities.put("error_tracking", sdkActive);
        capabilities.put("infrastructure_monitoring", sdkActive);
        capabilities.put("log_analytics", true);
        capabilities.put("alerting", sdkActive);
        capabilities.put("real_user_monitoring", false); // Not applicable for backend service
        capabilities.put("synthetic_monitoring", false); // Not applicable for backend service

        return capabilities;
    }

    /**
     * Gets health indicators based on business metrics.
     */
    private Map<String, Object> getHealthIndicators(DynatraceBusinessMetricsService.BusinessMetricsSummary metrics) {
        Map<String, Object> indicators = new HashMap<>();

        if (metrics == null) {
            indicators.put("status", "UNKNOWN");
            indicators.put("reason", "No metrics available");
            return indicators;
        }

        // Overall health status
        String overallHealth = "HEALTHY";
        StringBuilder healthReasons = new StringBuilder();

        // Check critical exceptions
        if (metrics.getCriticalExceptions() > 10) {
            overallHealth = "WARNING";
            healthReasons.append("High critical exceptions (").append(metrics.getCriticalExceptions()).append("); ");
        }

        // Check retry success rate
        if (metrics.getRetrySuccessRate() < 70.0) {
            overallHealth = "WARNING";
            healthReasons.append("Low retry success rate (").append(String.format("%.1f%%", metrics.getRetrySuccessRate())).append("); ");
        }

        // Check pending exceptions
        if (metrics.getPendingExceptions() > 100) {
            overallHealth = "WARNING";
            healthReasons.append("High pending exceptions (").append(metrics.getPendingExceptions()).append("); ");
        }

        // Check processing efficiency
        double processingEfficiency = metrics.getTotalExceptionsReceived() > 0 ? 
                (metrics.getTotalExceptionsProcessed() / metrics.getTotalExceptionsReceived()) * 100.0 : 100.0;
        if (processingEfficiency < 80.0) {
            overallHealth = "CRITICAL";
            healthReasons.append("Low processing efficiency (").append(String.format("%.1f%%", processingEfficiency)).append("); ");
        }

        indicators.put("overall_health", overallHealth);
        indicators.put("health_reasons", healthReasons.length() > 0 ? healthReasons.toString().trim() : "All indicators normal");
        
        // Individual indicators
        indicators.put("critical_exceptions_status", metrics.getCriticalExceptions() <= 10 ? "HEALTHY" : "WARNING");
        indicators.put("retry_success_rate_status", metrics.getRetrySuccessRate() >= 70.0 ? "HEALTHY" : "WARNING");
        indicators.put("pending_exceptions_status", metrics.getPendingExceptions() <= 100 ? "HEALTHY" : "WARNING");
        indicators.put("processing_efficiency_status", processingEfficiency >= 80.0 ? "HEALTHY" : "CRITICAL");

        return indicators;
    }

    /**
     * Gets performance indicators based on business metrics.
     */
    private Map<String, Object> getPerformanceIndicators(DynatraceBusinessMetricsService.BusinessMetricsSummary metrics) {
        Map<String, Object> indicators = new HashMap<>();

        if (metrics == null) {
            indicators.put("status", "No performance data available");
            return indicators;
        }

        // Throughput indicators
        indicators.put("exceptions_received_total", metrics.getTotalExceptionsReceived());
        indicators.put("exceptions_processed_total", metrics.getTotalExceptionsProcessed());
        indicators.put("retry_attempts_total", metrics.getTotalRetryAttempts());

        // Efficiency indicators
        double processingEfficiency = metrics.getTotalExceptionsReceived() > 0 ? 
                (metrics.getTotalExceptionsProcessed() / metrics.getTotalExceptionsReceived()) * 100.0 : 100.0;
        indicators.put("processing_efficiency_percent", Math.round(processingEfficiency * 100.0) / 100.0);
        indicators.put("retry_success_rate_percent", Math.round(metrics.getRetrySuccessRate() * 100.0) / 100.0);

        // Workload indicators
        indicators.put("current_pending_exceptions", metrics.getPendingExceptions());
        indicators.put("current_failed_exceptions", metrics.getFailedExceptions());
        indicators.put("current_resolved_exceptions", metrics.getResolvedExceptions());

        // Severity distribution
        Map<String, Long> severityDistribution = new HashMap<>();
        severityDistribution.put("critical", metrics.getCriticalExceptions());
        severityDistribution.put("high", metrics.getHighSeverityExceptions());
        severityDistribution.put("medium", metrics.getMediumSeverityExceptions());
        severityDistribution.put("low", metrics.getLowSeverityExceptions());
        indicators.put("severity_distribution", severityDistribution);

        return indicators;
    }
}