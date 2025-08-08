package com.arcone.biopro.exception.collector.infrastructure.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for KafkaHealthIndicator.
 * Tests Kafka health check functionality and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class KafkaHealthIndicatorTest {

    @Mock
    private KafkaAdmin kafkaAdmin;

    @Mock
    private AdminClient adminClient;

    @Mock
    private DescribeClusterResult clusterResult;

    @Mock
    private KafkaFuture<String> clusterIdFuture;

    @Mock
    private KafkaFuture<Collection<Node>> nodesFuture;

    private KafkaHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new KafkaHealthIndicator(kafkaAdmin);
    }

    @Test
    void checkHealth_WhenKafkaIsHealthy_ShouldReturnUp() throws Exception {
        // Given
        Map<String, Object> adminProps = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(adminProps);

        Collection<Node> nodes = Collections.singletonList(
                new Node(1, "localhost", 9092)
        );

        when(clusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterResult.nodes()).thenReturn(nodesFuture);
        when(clusterIdFuture.get(any(), any())).thenReturn("test-cluster-id");
        when(nodesFuture.get(any(), any())).thenReturn(nodes);

        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            adminClientMock.when(() -> AdminClient.create(adminProps)).thenReturn(adminClient);
            when(adminClient.describeCluster(any())).thenReturn(clusterResult);

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("UP");
            assertThat(health.get("kafka")).isEqualTo("cluster");
            assertThat(health.get("message")).isEqualTo("Connected");
        }
    }

    @Test
    void checkHealth_WhenKafkaConnectionFails_ShouldReturnDown() throws Exception {
        // Given
        Map<String, Object> adminProps = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(adminProps);

        when(clusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get(any(), any())).thenThrow(new ExecutionException("Connection failed", new RuntimeException()));

        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            adminClientMock.when(() -> AdminClient.create(adminProps)).thenReturn(adminClient);
            when(adminClient.describeCluster(any())).thenReturn(clusterResult);

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("kafka")).isEqualTo("cluster");
            assertThat(health).containsKey("error");
        }
    }

    @Test
    void isKafkaAvailable_WhenKafkaIsHealthy_ShouldReturnTrue() throws Exception {
        // Given
        Map<String, Object> adminProps = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(adminProps);

        Collection<Node> nodes = Collections.singletonList(
                new Node(1, "localhost", 9092)
        );

        when(clusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterResult.nodes()).thenReturn(nodesFuture);
        when(clusterIdFuture.get(any(), any())).thenReturn("test-cluster-id");
        when(nodesFuture.get(any(), any())).thenReturn(nodes);

        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            adminClientMock.when(() -> AdminClient.create(adminProps)).thenReturn(adminClient);
            when(adminClient.describeCluster(any())).thenReturn(clusterResult);

            // When
            boolean isAvailable = healthIndicator.isKafkaAvailable();

            // Then
            assertThat(isAvailable).isTrue();
        }
    }

    @Test
    void isKafkaAvailable_WhenKafkaConnectionFails_ShouldReturnFalse() {
        // Given
        Map<String, Object> adminProps = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(adminProps);

        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            adminClientMock.when(() -> AdminClient.create(adminProps))
                    .thenThrow(new RuntimeException("Failed to create admin client"));

            // When
            boolean isAvailable = healthIndicator.isKafkaAvailable();

            // Then
            assertThat(isAvailable).isFalse();
        }
    }
}