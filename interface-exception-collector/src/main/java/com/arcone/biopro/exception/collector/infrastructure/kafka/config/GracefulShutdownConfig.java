package com.arcone.biopro.exception.collector.infrastructure.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Graceful shutdown handler for Kafka consumers.
 * Ensures proper cleanup of Kafka consumers during application shutdown as per
 * requirement US-018.
 */
@Component
@Slf4j
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Starting graceful shutdown of Kafka consumers...");

        try {
            // Stop all Kafka listener containers gracefully
            kafkaListenerEndpointRegistry.getListenerContainers().forEach(container -> {
                String listenerId = container.getListenerId();
                log.info("Stopping Kafka listener container: {}", listenerId);

                try {
                    // Stop the container gracefully
                    container.stop();

                    // Wait for the container to stop completely
                    long shutdownTimeout = getShutdownTimeout(container);
                    boolean stopped = awaitTermination(container, shutdownTimeout);

                    if (stopped) {
                        log.info("Successfully stopped Kafka listener container: {}", listenerId);
                    } else {
                        log.warn("Kafka listener container {} did not stop within timeout of {}ms",
                                listenerId, shutdownTimeout);
                    }
                } catch (Exception e) {
                    log.error("Error stopping Kafka listener container {}: {}", listenerId, e.getMessage(), e);
                }
            });

            log.info("Completed graceful shutdown of Kafka consumers");

        } catch (Exception e) {
            log.error("Error during Kafka consumer graceful shutdown", e);
        }
    }

    /**
     * Gets the shutdown timeout for a container.
     * 
     * @param container the message listener container
     * @return shutdown timeout in milliseconds
     */
    private long getShutdownTimeout(MessageListenerContainer container) {
        try {
            // Try to get the configured shutdown timeout
            return container.getContainerProperties().getShutdownTimeout();
        } catch (Exception e) {
            // Default to 30 seconds if unable to get configured timeout
            log.debug("Using default shutdown timeout for container {}", container.getListenerId());
            return Duration.ofSeconds(30).toMillis();
        }
    }

    /**
     * Waits for a container to terminate within the specified timeout.
     * 
     * @param container the message listener container
     * @param timeoutMs timeout in milliseconds
     * @return true if container stopped within timeout, false otherwise
     */
    private boolean awaitTermination(MessageListenerContainer container, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMs;

        while (System.currentTimeMillis() < endTime) {
            if (!container.isRunning()) {
                return true;
            }

            try {
                Thread.sleep(100); // Check every 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for container {} to stop", container.getListenerId());
                return false;
            }
        }

        return false;
    }
}