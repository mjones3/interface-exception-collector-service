package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.application.service.DashboardMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Service for managing dashboard metrics subscriptions.
 * Publishes real-time dashboard updates to WebSocket subscribers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardSubscriptionService {

    private final DashboardMetricsService dashboardMetricsService;

    // Sink for broadcasting dashboard updates
    private final Sinks.Many<DashboardSummary> dashboardSink = Sinks.many().multicast()
            .onBackpressureBuffer();

    /**
     * Get dashboard metrics stream for subscriptions.
     */
    public Flux<DashboardSummary> getDashboardStream() {
        log.info("📊 New dashboard subscription started");
        
        return dashboardSink.asFlux()
                .doOnSubscribe(subscription -> {
                    log.info("📡 Dashboard subscriber connected");
                    // Send initial metrics immediately
                    publishDashboardUpdate();
                })
                .doOnCancel(() -> {
                    log.info("📡 Dashboard subscriber disconnected");
                })
                .onErrorResume(throwable -> {
                    log.warn("Dashboard subscription error: {}", throwable.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Publish dashboard update to all subscribers.
     */
    public void publishDashboardUpdate() {
        try {
            DashboardMetricsService.DashboardMetrics metrics = dashboardMetricsService.calculateMetrics();
            
            DashboardSummary summary = DashboardSummary.builder()
                .activeExceptions(metrics.getActiveExceptions())
                .todayExceptions(metrics.getTodayExceptions())
                .failedRetries(metrics.getFailedRetries())
                .successfulRetries(metrics.getSuccessfulRetries())
                .totalRetries(metrics.getTotalRetries())
                .retrySuccessRate(Math.round(metrics.getRetrySuccessRate() * 100.0) / 100.0)
                .apiSuccessRate(Math.round(metrics.getApiSuccessRate() * 100.0) / 100.0)
                .totalApiCallsToday(metrics.getTotalApiCallsToday())
                .lastUpdated(metrics.getLastUpdated())
                .build();

            Sinks.EmitResult result = dashboardSink.tryEmitNext(summary);
            
            if (result.isFailure()) {
                log.warn("Failed to emit dashboard update: {}", result);
            } else {
                log.debug("📊 Dashboard update published: {} active exceptions, {}% API success rate", 
                    summary.getActiveExceptions(), summary.getApiSuccessRate());
            }
            
        } catch (Exception e) {
            log.error("Error publishing dashboard update", e);
        }
    }

    /**
     * Scheduled task to publish dashboard updates every 30 seconds.
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void scheduledDashboardUpdate() {
        log.debug("🕐 Scheduled dashboard update triggered");
        publishDashboardUpdate();
    }

    /**
     * Trigger immediate dashboard update (called by event handlers).
     */
    public void triggerUpdate() {
        log.debug("🔄 Manual dashboard update triggered");
        publishDashboardUpdate();
    }

    /**
     * Dashboard summary data for GraphQL subscriptions.
     */
    @lombok.Builder
    @lombok.Data
    public static class DashboardSummary {
        private final long activeExceptions;
        private final long todayExceptions;
        private final long failedRetries;
        private final long successfulRetries;
        private final long totalRetries;
        private final double retrySuccessRate;
        private final double apiSuccessRate;
        private final long totalApiCallsToday;
        private final OffsetDateTime lastUpdated;
    }
}