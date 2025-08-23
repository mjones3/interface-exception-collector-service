package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.application.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Component to initialize metrics and handle periodic metric updates.
 * Provides metrics initialization as per requirements US-016, US-017.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsInitializer {

    private final MetricsService metricsService;

    /**
     * Initialize gauge metrics when the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeMetrics() {
        log.info("Initializing metrics and observability features");
        try {
            metricsService.initializeGaugeMetrics();
            log.info("Successfully initialized metrics and observability features");
        } catch (Exception e) {
            log.error("Failed to initialize metrics", e);
        }
    }

    /**
     * Reset daily counters at midnight.
     * Runs every day at 00:00:00.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCounters() {
        log.info("Resetting daily metric counters");
        try {
            metricsService.resetDailyCounters();
            log.info("Successfully reset daily metric counters");
        } catch (Exception e) {
            log.error("Failed to reset daily metric counters", e);
        }
    }

    /**
     * Log system health status every 5 minutes for monitoring.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logSystemHealth() {
        try {
            double activeExceptions = metricsService.getActiveExceptionsCount();
            double todayExceptions = metricsService.getTotalExceptionsToday();
            double criticalExceptions = metricsService.getCriticalExceptionsToday();
            double avgResolutionTime = metricsService.getAverageResolutionTimeHours();

            log.info("System Health Status - Active: {}, Today: {}, Critical: {}, Avg Resolution: {}h",
                    (int) activeExceptions, (int) todayExceptions, (int) criticalExceptions,
                    String.format("%.2f", avgResolutionTime));
        } catch (Exception e) {
            log.error("Failed to log system health status", e);
        }
    }
}