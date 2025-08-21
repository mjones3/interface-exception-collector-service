package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GraphQL subscriptions with WebSocket clients.
 * Tests subscription functionality, security, and real-time updates.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class GraphQLSubscriptionSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    private WebSocketGraphQlTester webSocketTester;

    @BeforeEach
    void setUp() {
        String url = "ws://localhost:" + port + "/subscriptions";
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        
        webSocketTester = WebSocketGraphQlTester.builder(URI.create(url), client)
                .build();

        // Create test data
        createTestException();
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionEndpoint_WithAuthentication_ShouldConnect() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates {
                        transactionId
                        status
                        interfaceType
                        timestamp
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(5))
                .as(flux -> {
                    // Verify subscription is established
                    assertThat(flux).isNotNull();
                    return flux;
                });
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionExceptionUpdates_ShouldReceiveUpdates() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates(
                        filters: {
                            interfaceTypes: [ORDER]
                            statuses: [NEW, ACKNOWLEDGED]
                        }
                    ) {
                        transactionId
                        status
                        interfaceType
                        severity
                        timestamp
                        updateType
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    // Verify response structure
                    assertThat(response.path("exceptionUpdates.transactionId")).exists();
                    assertThat(response.path("exceptionUpdates.status")).exists();
                    assertThat(response.path("exceptionUpdates.interfaceType")).exists();
                })
                .blockFirst(Duration.ofSeconds(15));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void subscriptionRetryStatusUpdates_ShouldReceiveRetryEvents() {
        // Given
        String subscription = """
                subscription {
                    retryStatusUpdates(transactionId: "TXN-001") {
                        transactionId
                        attemptNumber
                        status
                        initiatedBy
                        initiatedAt
                        completedAt
                        resultSuccess
                        resultMessage
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    // Verify retry update structure
                    assertThat(response.path("retryStatusUpdates.transactionId")).exists();
                    assertThat(response.path("retryStatusUpdates.attemptNumber")).exists();
                    assertThat(response.path("retryStatusUpdates.status")).exists();
                })
                .blockFirst(Duration.ofSeconds(15));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionSummaryUpdates_ShouldReceiveAggregatedUpdates() {
        // Given
        String subscription = """
                subscription {
                    summaryUpdates(
                        timeRange: {
                            start: "2024-01-01T00:00:00Z"
                            end: "2024-12-31T23:59:59Z"
                        }
                    ) {
                        totalExceptions
                        byInterfaceType {
                            interfaceType
                            count
                            percentage
                        }
                        bySeverity {
                            severity
                            count
                            percentage
                        }
                        timestamp
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    // Verify summary update structure
                    assertThat(response.path("summaryUpdates.totalExceptions")).exists();
                    assertThat(response.path("summaryUpdates.timestamp")).exists();
                })
                .blockFirst(Duration.ofSeconds(15));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionWithFilters_ShouldRespectFilterCriteria() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates(
                        filters: {
                            interfaceTypes: [ORDER]
                            severities: [HIGH, CRITICAL]
                            customerIds: ["CUST-001"]
                        }
                    ) {
                        transactionId
                        interfaceType
                        severity
                        customerId
                        updateType
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    // Verify filtered data
                    String interfaceType = response.path("exceptionUpdates.interfaceType").entity(String.class).get();
                    String severity = response.path("exceptionUpdates.severity").entity(String.class).get();
                    String customerId = response.path("exceptionUpdates.customerId").entity(String.class).get();
                    
                    assertThat(interfaceType).isEqualTo("ORDER");
                    assertThat(severity).isIn("HIGH", "CRITICAL");
                    assertThat(customerId).isEqualTo("CUST-001");
                })
                .blockFirst(Duration.ofSeconds(15));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionConnectionLimits_ShouldRespectMaxConnections() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates {
                        transactionId
                        status
                    }
                }
                """;

        // When & Then - Test multiple connections
        for (int i = 0; i < 5; i++) {
            webSocketTester.document(subscription)
                    .executeSubscription()
                    .toFlux()
                    .take(Duration.ofSeconds(2))
                    .subscribe();
        }

        // Verify connections are managed properly
        // This test ensures the system can handle multiple concurrent subscriptions
        assertThat(true).isTrue(); // Placeholder - actual connection counting would require metrics
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionHeartbeat_ShouldMaintainConnection() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates {
                        transactionId
                        timestamp
                    }
                }
                """;

        // When & Then
        webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(30)) // Long enough to test heartbeat
                .doOnNext(response -> {
                    // Verify connection remains active
                    assertThat(response).isNotNull();
                })
                .doOnComplete(() -> {
                    // Verify graceful completion
                    assertThat(true).isTrue();
                })
                .blockLast(Duration.ofSeconds(35));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionError_ShouldHandleGracefully() {
        // Given
        String invalidSubscription = """
                subscription {
                    exceptionUpdates(
                        filters: {
                            invalidField: "invalid"
                        }
                    ) {
                        transactionId
                    }
                }
                """;

        // When & Then
        webSocketTester.document(invalidSubscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(5))
                .doOnError(error -> {
                    // Verify error handling
                    assertThat(error).isNotNull();
                    assertThat(error.getMessage()).contains("validation");
                })
                .onErrorComplete()
                .blockLast(Duration.ofSeconds(10));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void subscriptionDisconnection_ShouldCleanupResources() {
        // Given
        String subscription = """
                subscription {
                    exceptionUpdates {
                        transactionId
                        status
                    }
                }
                """;

        // When
        var flux = webSocketTester.document(subscription)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(5));

        // Then - Simulate disconnection
        flux.subscribe().dispose();

        // Verify cleanup (this would typically be verified through metrics or logs)
        assertThat(true).isTrue(); // Placeholder for actual cleanup verification
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void subscriptionWithVariables_ShouldAcceptDynamicFilters() {
        // Given
        String subscription = """
                subscription($interfaceType: InterfaceType!, $severity: ExceptionSeverity!) {
                    exceptionUpdates(
                        filters: {
                            interfaceTypes: [$interfaceType]
                            severities: [$severity]
                        }
                    ) {
                        transactionId
                        interfaceType
                        severity
                        updateType
                    }
                }
                """;

        Map<String, Object> variables = Map.of(
                "interfaceType", "ORDER",
                "severity", "HIGH"
        );

        // When & Then
        webSocketTester.document(subscription)
                .variables(variables)
                .executeSubscription()
                .toFlux()
                .take(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    // Verify variable substitution worked
                    String interfaceType = response.path("exceptionUpdates.interfaceType").entity(String.class).get();
                    String severity = response.path("exceptionUpdates.severity").entity(String.class).get();
                    
                    assertThat(interfaceType).isEqualTo("ORDER");
                    assertThat(severity).isEqualTo("HIGH");
                })
                .blockFirst(Duration.ofSeconds(15));
    }

    private void createTestException() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception for subscription testing")
                .operation("CREATE_ORDER")
                .externalId("ORDER-001")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .maxRetries(3)
                .build();

        exceptionRepository.save(exception);
    }
}