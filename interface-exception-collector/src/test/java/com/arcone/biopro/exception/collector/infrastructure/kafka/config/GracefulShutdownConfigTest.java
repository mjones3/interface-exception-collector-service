package com.arcone.biopro.exception.collector.infrastructure.kafka.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Unit tests for GracefulShutdownConfig.
 * Tests graceful shutdown behavior for Kafka consumers.
 */
@ExtendWith(MockitoExtension.class)
class GracefulShutdownConfigTest {

    @Mock
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Mock
    private MessageListenerContainer container1;

    @Mock
    private MessageListenerContainer container2;

    @Mock
    private ContainerProperties containerProperties;

    @Mock
    private ContextClosedEvent contextClosedEvent;

    private GracefulShutdownConfig gracefulShutdownConfig;

    @BeforeEach
    void setUp() {
        gracefulShutdownConfig = new GracefulShutdownConfig();
        gracefulShutdownConfig.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @Test
    void onApplicationEvent_WithMultipleContainers_ShouldStopAllContainers() {
        // Given
        when(kafkaListenerEndpointRegistry.getListenerContainers())
                .thenReturn(Arrays.asList(container1, container2));

        when(container1.getListenerId()).thenReturn("container-1");
        when(container2.getListenerId()).thenReturn("container-2");

        when(container1.getContainerProperties()).thenReturn(containerProperties);
        when(container2.getContainerProperties()).thenReturn(containerProperties);
        when(containerProperties.getShutdownTimeout()).thenReturn(30000L);

        when(container1.isRunning()).thenReturn(false); // Simulate quick stop
        when(container2.isRunning()).thenReturn(false); // Simulate quick stop

        // When
        gracefulShutdownConfig.onApplicationEvent(contextClosedEvent);

        // Then
        verify(container1).stop();
        verify(container2).stop();
        verify(container1, atLeastOnce()).isRunning();
        verify(container2, atLeastOnce()).isRunning();
    }

    @Test
    void onApplicationEvent_WithNoContainers_ShouldCompleteGracefully() {
        // Given
        when(kafkaListenerEndpointRegistry.getListenerContainers())
                .thenReturn(Collections.emptyList());

        // When
        gracefulShutdownConfig.onApplicationEvent(contextClosedEvent);

        // Then
        // Should complete without errors
        verifyNoInteractions(container1, container2);
    }

    @Test
    void onApplicationEvent_WhenContainerThrowsException_ShouldContinueWithOtherContainers() {
        // Given
        when(kafkaListenerEndpointRegistry.getListenerContainers())
                .thenReturn(Arrays.asList(container1, container2));

        when(container1.getListenerId()).thenReturn("container-1");
        when(container2.getListenerId()).thenReturn("container-2");

        doThrow(new RuntimeException("Stop failed")).when(container1).stop();

        when(container2.getContainerProperties()).thenReturn(containerProperties);
        when(containerProperties.getShutdownTimeout()).thenReturn(30000L);
        when(container2.isRunning()).thenReturn(false);

        // When
        gracefulShutdownConfig.onApplicationEvent(contextClosedEvent);

        // Then
        verify(container1).stop();
        verify(container2).stop();
        verify(container2, atLeastOnce()).isRunning();
    }

    @Test
    void onApplicationEvent_WhenContainerTakesTooLongToStop_ShouldTimeout() {
        // Given
        when(kafkaListenerEndpointRegistry.getListenerContainers())
                .thenReturn(Collections.singletonList(container1));

        when(container1.getListenerId()).thenReturn("container-1");
        when(container1.getContainerProperties()).thenReturn(containerProperties);
        when(containerProperties.getShutdownTimeout()).thenReturn(100L); // Short timeout for test
        when(container1.isRunning()).thenReturn(true); // Never stops

        // When
        gracefulShutdownConfig.onApplicationEvent(contextClosedEvent);

        // Then
        verify(container1).stop();
        verify(container1, atLeast(1)).isRunning();
    }
}