package com.arcone.biopro.exception.collector.infrastructure.health;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockRSocketServerHealthIndicatorTest {

    @Mock
    private MockRSocketOrderServiceClient mockRSocketClient;

    private MockRSocketServerHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new MockRSocketServerHealthIndicator(mockRSocketClient);
    }

    @Test
    void shouldReturnUpWhenMockServerIsHealthy() {
        // Given
        PayloadResponse successResponse = PayloadResponse.builder()
            .retrieved(true)
            .build();
        
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.completedFuture(successResponse));

        // When
        Map<String, Object> health = healthIndicator.checkHealth();

        // Then
        assertThat(health.get("status")).isEqualTo("UP");
        assertThat(health.get("mock-server")).isEqualTo("Available");
        assertThat(health.get("service")).isEqualTo("mock-rsocket-server");
    }

    @Test
    void shouldReturnDownWhenMockServerCallFails() {
        // Given
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection failed")));

        // When
        Map<String, Object> health = healthIndicator.checkHealth();

        // Then
        assertThat(health.get("status")).isEqualTo("DOWN");
        assertThat(health.get("mock-server")).isEqualTo("Unavailable");
        assertThat(health.get("error")).isEqualTo("Connection failed");
        assertThat(health.get("error-type")).isEqualTo("RuntimeException");
    }

    @Test
    void shouldReturnDownWhenMockServerCallTimesOut() {
        // Given
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.failedFuture(new TimeoutException("Health check timeout")));

        // When
        Map<String, Object> health = healthIndicator.checkHealth();

        // Then
        assertThat(health.get("status")).isEqualTo("DOWN");
        assertThat(health.get("mock-server")).isEqualTo("Unavailable");
        assertThat(health.get("error")).isEqualTo("Health check timeout");
        assertThat(health.get("error-type")).isEqualTo("TimeoutException");
    }

    @Test
    void shouldReturnDownWhenMockServerReturnsNoResponse() {
        // Given
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // When
        Map<String, Object> health = healthIndicator.checkHealth();

        // Then
        assertThat(health.get("status")).isEqualTo("DOWN");
        assertThat(health.get("mock-server")).isEqualTo("Unavailable - No response");
        assertThat(health.get("service")).isEqualTo("mock-rsocket-server");
    }

    @Test
    void shouldReturnTrueWhenHealthy() {
        // Given
        PayloadResponse successResponse = PayloadResponse.builder()
            .retrieved(true)
            .build();
        
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.completedFuture(successResponse));

        // When
        boolean isHealthy = healthIndicator.isHealthy();

        // Then
        assertThat(isHealthy).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUnhealthy() {
        // Given
        when(mockRSocketClient.getOriginalPayload(any()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection failed")));

        // When
        boolean isHealthy = healthIndicator.isHealthy();

        // Then
        assertThat(isHealthy).isFalse();
    }
}