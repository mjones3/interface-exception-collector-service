package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for RSocket client settings.
 * Binds configuration values from application.yml for mock server and partner order service.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rsocket")
public class RSocketProperties {

    private MockServer mockServer = new MockServer();
    private PartnerOrderService partnerOrderService = new PartnerOrderService();

    @Data
    public static class MockServer {
        private boolean enabled = true;
        private String host = "localhost";
        private int port = 7000;
        private Duration timeout = Duration.ofSeconds(5);
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private Duration keepAliveInterval = Duration.ofSeconds(30);
        private Duration keepAliveMaxLifetime = Duration.ofSeconds(300);
        private boolean debugLogging = false;
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
        private Retry retry = new Retry();
    }

    @Data
    public static class PartnerOrderService {
        private boolean enabled = false;
        private String host = "partner-order-service";
        private int port = 8090;
        private Duration timeout = Duration.ofSeconds(30);
        private Duration connectionTimeout = Duration.ofSeconds(20);
    }

    @Data
    public static class CircuitBreaker {
        private boolean enabled = true;
        private int failureRateThreshold = 50;
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private int permittedCallsInHalfOpen = 3;
    }

    @Data
    public static class Retry {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private Duration waitDuration = Duration.ofSeconds(1);
        private double exponentialBackoffMultiplier = 2.0;
    }
}