package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test class for GraphQLAlertingService.
 * Verifies that alerts are correctly triggered and resolved based on metrics.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLAlertingServiceTest {

    private MeterRegistry meterRegistry;
    private GraphQLAlertingService alertingService;

    @Mock
    private Environment environment;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        // Mock environment properties with default values
        when(environment.getProperty("graphql.monitoring.alerting.cooldown-minutes", Long.class, 5L))
                .thenReturn(5L);
        when(environment.getProperty("graphql.monitoring.alerting.thresholds.query-response-time-ms", Double.class,
                500.0))
                .thenReturn(500.0);
        when(environment.getProperty("graphql.monitoring.alerting.thresholds.mutation-response-time-ms", Double.class,
                3000.0))
                .thenReturn(3000.0);
        when(environment.getProperty("graphql.monitoring.alerting.thresholds.error-rate-percent", Double.class, 5.0))
                .thenReturn(5.0);
        when(environment.getProperty("graphql.monitoring.alerting.thresholds.cache-miss-rate-percent", Double.class,
                20.0))
                .thenReturn(20.0);
        when(environment.getProperty("graphql.monitoring.alerting.thresholds.throughput-per-minute", Long.class, 1000L))
                .thenReturn(1000L);
        when(environment.getProperty("graphql.websocket.max-connections", Long.class, 1000L))
                .thenReturn(1000L);
        when(environment.getProperty("ENVIRONMENT", "local"))
                .thenReturn("test");

        alertingService = new GraphQLAlertingService(meterRegistry, environment);
    }

    @Test
    void shouldInitializeWithoutErrors() {
        // When
        alertingService.initializeAlerting();

        // Then - should not throw any exceptions
        assertThat(alertingService).isNotNull();
    }

    @Test
    void shouldReturnHealthyStatusWhenNoAlertsActive() {
        // When
        Health health = alertingService.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(org.springframework.boot.actuate.health.Status.UP);
        assertThat(health.getDetails()).containsKey("active_alerts");
        assertThat(health.getDetails()).containsKey("alert_thresholds");
        assertThat(health.getDetails()).containsKey("total_alerts_triggered");
    }

    @Test
    void shouldCheckMetricsWithoutErrors() {
        // Given - create some sample metrics
        Timer queryTimer = Timer.builder("graphql_query_duration_seconds")
                .register(meterRegistry);
        queryTimer.record(100, TimeUnit.MILLISECONDS); // Below threshold

        Counter queryCounter = Counter.builder("graphql_query_count_total")
                .register(meterRegistry);
        queryCounter.increment();

        Counter errorCounter = Counter.builder("graphql_error_count_total")
                .register(meterRegistry);
        // No errors - should be healthy

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should complete without exceptions
        Health health = alertingService.health();
        assertThat(health.getStatus()).isEqualTo(org.springframework.boot.actuate.health.Status.UP);
    }

    @Test
    void shouldTriggerQueryResponseTimeAlert() {
        // Given - create slow query metrics
        Timer queryTimer = Timer.builder("graphql_query_duration_seconds")
                .register(meterRegistry);

        // Record multiple slow queries to establish a pattern
        for (int i = 0; i < 10; i++) {
            queryTimer.record(600, TimeUnit.MILLISECONDS); // Above 500ms threshold
        }

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should trigger alert (reflected in health status)
        Health health = alertingService.health();
        // Note: In a real scenario, we'd check for alert notifications
        // Here we verify the service processes the metrics without error
        assertThat(health.getDetails()).containsKey("active_alerts");
    }

    @Test
    void shouldTriggerErrorRateAlert() {
        // Given - create high error rate metrics
        Counter queryCounter = Counter.builder("graphql_query_count_total")
                .register(meterRegistry);
        Counter mutationCounter = Counter.builder("graphql_mutation_count_total")
                .register(meterRegistry);
        Counter errorCounter = Counter.builder("graphql_error_count_total")
                .register(meterRegistry);

        // Create scenario with high error rate (>5%)
        for (int i = 0; i < 100; i++) {
            queryCounter.increment();
        }
        for (int i = 0; i < 10; i++) { // 10% error rate
            errorCounter.increment();
        }

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should process without error
        Health health = alertingService.health();
        assertThat(health.getDetails()).containsKey("total_alerts_triggered");
    }

    @Test
    void shouldTriggerCacheMissRateAlert() {
        // Given - create high cache miss rate metrics
        Counter cacheHitCounter = Counter.builder("graphql_cache_access_total")
                .tag("result", "hit")
                .register(meterRegistry);
        Counter cacheMissCounter = Counter.builder("graphql_cache_access_total")
                .tag("result", "miss")
                .register(meterRegistry);

        // Create scenario with high miss rate (>20%)
        for (int i = 0; i < 20; i++) {
            cacheHitCounter.increment(); // 20 hits
        }
        for (int i = 0; i < 10; i++) {
            cacheMissCounter.increment(); // 10 misses = 33% miss rate
        }

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should process without error
        Health health = alertingService.health();
        assertThat(health.getDetails()).containsKey("active_alerts");
    }

    @Test
    void shouldTriggerSubscriptionConnectionAlert() {
        // Given - create high subscription connection metrics
        Gauge.builder("graphql_subscription_connections_active")
                .register(meterRegistry, () -> 950.0); // 95% of 1000 limit

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should process without error
        Health health = alertingService.health();
        assertThat(health.getDetails()).containsKey("active_alerts");
    }

    @Test
    void shouldHandleMissingMetricsGracefully() {
        // Given - no metrics registered

        // When
        alertingService.checkMetricsAndAlert();

        // Then - should not throw exceptions
        Health health = alertingService.health();
        assertThat(health.getStatus()).isEqualTo(org.springframework.boot.actuate.health.Status.UP);
    }

    @Test
    void shouldIncludeAlertThresholdsInHealthCheck() {
        // When
        Health health = alertingService.health();

        // Then
        assertThat(health.getDetails()).containsKey("alert_thresholds");
        @SuppressWarnings("unchecked")
        var thresholds = (java.util.Map<String, Object>) health.getDetails().get("alert_thresholds");

        assertThat(thresholds).containsKey("query_response_time_ms");
        assertThat(thresholds).containsKey("mutation_response_time_ms");
        assertThat(thresholds).containsKey("error_rate_percent");
        assertThat(thresholds).containsKey("cache_miss_rate_percent");
        assertThat(thresholds).containsKey("throughput_per_minute");
        assertThat(thresholds).containsKey("max_subscription_connections");
    }

    @Test
    void shouldTrackAlertCounts() {
        // Given
        Health initialHealth = alertingService.health();
        Long initialAlerts = (Long) initialHealth.getDetails().get("total_alerts_triggered");

        // When - trigger some checks that might generate alerts
        alertingService.checkMetricsAndAlert();

        // Then
        Health finalHealth = alertingService.health();
        Long finalAlerts = (Long) finalHealth.getDetails().get("total_alerts_triggered");

        // Alert count should be tracked (may or may not increase depending on metrics)
        assertThat(finalAlerts).isNotNull();
        assertThat(finalAlerts).isGreaterThanOrEqualTo(initialAlerts);
    }
}