package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Specialized metrics component for GraphQL subscriptions.
 * Tracks connection lifecycle, message throughput, and performance.
 */
@Slf4j
// @Component // Temporarily disabled to debug constructor issues
@ConditionalOnProperty(name = "graphql.features.metrics-enabled", havingValue = "true", matchIfMissing = false)
public class SubscriptionMetrics {

        private final MeterRegistry meterRegistry;

        // Connection metrics
        private final AtomicLong activeConnections = new AtomicLong(0);
        private final AtomicLong totalConnections = new AtomicLong(0);
        private final Counter connectionCounter;
        private final Counter disconnectionCounter;
        private final Timer connectionDurationTimer;

        // Message metrics
        private final Counter messagesSentCounter;
        private final Counter messagesFailedCounter;
        private final Timer messageDeliveryTimer;

        // Subscription-specific metrics
        private final ConcurrentHashMap<String, AtomicLong> subscriptionCounts = new ConcurrentHashMap<>();
        private final Counter subscriptionStartCounter;
        private final Counter subscriptionStopCounter;
        private final Timer subscriptionLifetimeTimer;

        // Performance metrics
        private final Timer eventProcessingTimer;
        private final Counter eventFilteredCounter;
        private final Gauge bufferUtilizationGauge;

        public SubscriptionMetrics(MeterRegistry meterRegistry) {
                this.meterRegistry = meterRegistry;

                // Initialize connection metrics
                this.connectionCounter = Counter.builder("graphql_subscription_connections_total")
                                .description("Total number of subscription connections established")
                                .register(meterRegistry);

                this.disconnectionCounter = Counter.builder("graphql_subscription_disconnections_total")
                                .description("Total number of subscription disconnections")
                                .register(meterRegistry);

                this.connectionDurationTimer = Timer.builder("graphql_subscription_connection_duration_seconds")
                                .description("Duration of subscription connections")
                                .register(meterRegistry);

                // Initialize message metrics
                this.messagesSentCounter = Counter.builder("graphql_subscription_messages_sent_total")
                                .description("Total number of subscription messages sent")
                                .register(meterRegistry);

                this.messagesFailedCounter = Counter.builder("graphql_subscription_messages_failed_total")
                                .description("Total number of failed subscription messages")
                                .register(meterRegistry);

                this.messageDeliveryTimer = Timer.builder("graphql_subscription_message_delivery_seconds")
                                .description("Time taken to deliver subscription messages")
                                .register(meterRegistry);

                // Initialize subscription metrics
                this.subscriptionStartCounter = Counter.builder("graphql_subscription_started_total")
                                .description("Total number of subscriptions started")
                                .register(meterRegistry);

                this.subscriptionStopCounter = Counter.builder("graphql_subscription_stopped_total")
                                .description("Total number of subscriptions stopped")
                                .register(meterRegistry);

                this.subscriptionLifetimeTimer = Timer.builder("graphql_subscription_lifetime_seconds")
                                .description("Lifetime duration of subscriptions")
                                .register(meterRegistry);

                // Initialize performance metrics
                this.eventProcessingTimer = Timer.builder("graphql_subscription_event_processing_seconds")
                                .description("Time taken to process subscription events")
                                .register(meterRegistry);

                this.eventFilteredCounter = Counter.builder("graphql_subscription_events_filtered_total")
                                .description("Total number of subscription events filtered out")
                                .register(meterRegistry);

                // Initialize gauges
                Gauge.builder("graphql_subscription_connections_active", activeConnections, AtomicLong::doubleValue)
                                .description("Number of active subscription connections")
                                .register(meterRegistry);

                this.bufferUtilizationGauge = Gauge
                                .builder("graphql_subscription_buffer_utilization_percent", this,
                                                SubscriptionMetrics::getBufferUtilization)
                                .description("Subscription message buffer utilization percentage")
                                .register(meterRegistry);

                log.info("Subscription metrics initialized");
        }

        /**
         * Record a new connection
         */
        public void recordConnection(String subscriptionType) {
                activeConnections.incrementAndGet();
                totalConnections.incrementAndGet();
                connectionCounter.increment();

                log.debug("Subscription connection established: type={}, active={}",
                                subscriptionType, activeConnections.get());
        }

        /**
         * Record a disconnection
         */
        public void recordDisconnection(String subscriptionType, Duration connectionDuration) {
                activeConnections.decrementAndGet();
                disconnectionCounter.increment();
                connectionDurationTimer.record(connectionDuration.toMillis(),
                                java.util.concurrent.TimeUnit.MILLISECONDS);

                log.debug("Subscription connection closed: type={}, duration={}ms, active={}",
                                subscriptionType, connectionDuration.toMillis(), activeConnections.get());
        }

        /**
         * Record subscription start
         */
        public void recordSubscriptionStart(String operationName, String subscriptionType) {
                subscriptionStartCounter.increment();

                // Track subscription counts by operation
                subscriptionCounts.computeIfAbsent(operationName, k -> new AtomicLong(0)).incrementAndGet();

                log.debug("Subscription started: operation={}, type={}", operationName, subscriptionType);
        }

        /**
         * Record subscription stop
         */
        public void recordSubscriptionStop(String operationName, String subscriptionType, Duration lifetime) {
                subscriptionStopCounter.increment();

                subscriptionLifetimeTimer.record(lifetime.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

                // Decrement subscription count
                AtomicLong count = subscriptionCounts.get(operationName);
                if (count != null) {
                        count.decrementAndGet();
                }

                log.debug("Subscription stopped: operation={}, type={}, lifetime={}ms",
                                operationName, subscriptionType, lifetime.toMillis());
        }

        /**
         * Record message sent
         */
        public void recordMessageSent(String operationName, Duration deliveryTime) {
                messagesSentCounter.increment();
                messageDeliveryTimer.record(deliveryTime.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

                log.debug("Subscription message sent: operation={}, deliveryTime={}ms",
                                operationName, deliveryTime.toMillis());
        }

        /**
         * Record message failure
         */
        public void recordMessageFailed(String operationName, String errorType) {
                messagesFailedCounter.increment();

                log.warn("Subscription message failed: operation={}, errorType={}", operationName, errorType);
        }

        /**
         * Record event processing time
         */
        public void recordEventProcessing(String eventType, Duration processingTime) {
                eventProcessingTimer.record(processingTime.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

                // Log slow event processing
                if (processingTime.toMillis() > 100) {
                        log.warn("Slow subscription event processing: eventType={}, time={}ms",
                                        eventType, processingTime.toMillis());
                }
        }

        /**
         * Record filtered event
         */
        public void recordEventFiltered(String eventType, String reason) {
                eventFilteredCounter.increment();

                log.debug("Subscription event filtered: eventType={}, reason={}", eventType, reason);
        }

        /**
         * Get active connection count
         */
        public long getActiveConnections() {
                return activeConnections.get();
        }

        /**
         * Get total connection count
         */
        public long getTotalConnections() {
                return totalConnections.get();
        }

        /**
         * Get subscription count for a specific operation
         */
        public long getSubscriptionCount(String operationName) {
                AtomicLong count = subscriptionCounts.get(operationName);
                return count != null ? count.get() : 0;
        }

        /**
         * Get buffer utilization percentage (simplified implementation)
         */
        private double getBufferUtilization() {
                // This is a simplified implementation
                // In a real scenario, you'd calculate based on actual buffer usage
                long active = activeConnections.get();
                long maxConnections = Long.parseLong(
                                System.getProperty("graphql.websocket.max-connections", "1000"));

                return maxConnections > 0 ? (active * 100.0 / maxConnections) : 0.0;
        }

        /**
         * Record subscription latency (time from event to delivery)
         */
        public void recordSubscriptionLatency(String operationName, Duration latency) {
                Timer.builder("graphql_subscription_latency_seconds")
                                .description("Latency from event generation to subscription delivery")
                                .tags("operation", operationName)
                                .register(meterRegistry)
                                .record(latency.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

                // Alert on high latency
                long latencyThreshold = Long.parseLong(
                                System.getProperty("graphql.websocket.subscription.latency-threshold-ms", "2000"));
                if (latency.toMillis() > latencyThreshold) {
                        log.warn("High subscription latency detected: operation={}, latency={}ms",
                                        operationName, latency.toMillis());
                }
        }

        /**
         * Get metrics summary for health checks
         */
        public SubscriptionMetricsSummary getMetricsSummary() {
                return new SubscriptionMetricsSummary(
                                activeConnections.get(),
                                totalConnections.get(),
                                subscriptionCounts.size(),
                                getBufferUtilization());
        }

        /**
         * Metrics summary for health checks and monitoring
         */
        public static class SubscriptionMetricsSummary {
                private final long activeConnections;
                private final long totalConnections;
                private final int uniqueSubscriptions;
                private final double bufferUtilization;

                public SubscriptionMetricsSummary(long activeConnections, long totalConnections,
                                int uniqueSubscriptions, double bufferUtilization) {
                        this.activeConnections = activeConnections;
                        this.totalConnections = totalConnections;
                        this.uniqueSubscriptions = uniqueSubscriptions;
                        this.bufferUtilization = bufferUtilization;
                }

                public long getActiveConnections() {
                        return activeConnections;
                }

                public long getTotalConnections() {
                        return totalConnections;
                }

                public int getUniqueSubscriptions() {
                        return uniqueSubscriptions;
                }

                public double getBufferUtilization() {
                        return bufferUtilization;
                }
        }
}