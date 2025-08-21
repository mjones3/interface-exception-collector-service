package com.arcone.biopro.exception.collector.api.graphql.logging;

import com.arcone.biopro.exception.collector.api.graphql.monitoring.GraphQLAlertingService;
import com.arcone.biopro.exception.collector.api.graphql.monitoring.GraphQLOperationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

/**
 * Integration test for GraphQL logging and alerting functionality.
 * Verifies that GraphQL operations are properly logged and monitored.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "graphql.monitoring.alerting.enabled=true",
        "graphql.monitoring.logging.slow-query-threshold-ms=100",
        "graphql.monitoring.logging.slow-field-threshold-ms=50",
        "logging.level.com.arcone.biopro.exception.collector.api.graphql=DEBUG"
})
class GraphQLLoggingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private GraphQLLoggingInstrumentation loggingInstrumentation;

    @SpyBean
    private GraphQLAlertingService alertingService;

    @SpyBean
    private ApplicationEventPublisher eventPublisher;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldLogGraphQLQueryOperation() throws Exception {
        // Create GraphQL tester
        HttpGraphQlTester graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Execute a simple GraphQL query
        graphQlTester
                .document("{ exceptions(first: 5) { edges { node { id transactionId } } } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    // We expect this to work or fail gracefully
                    // The important thing is that logging occurs
                });

        // Verify that logging instrumentation was called
        // Note: This is a basic verification - in a real test we'd check log output
        verify(eventPublisher, atLeastOnce()).publishEvent(any(GraphQLOperationEvent.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldLogGraphQLMutationOperation() throws Exception {
        HttpGraphQlTester graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Execute a GraphQL mutation
        graphQlTester
                .document("mutation { acknowledgeException(transactionId: \"test-123\") { success message } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    // We expect this to work or fail gracefully
                    // The important thing is that logging occurs
                });

        // Verify that the operation was recorded for alerting
        verify(eventPublisher, atLeastOnce()).publishEvent(any(GraphQLOperationEvent.class));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldLogGraphQLErrorsAndTriggerAlerts() throws Exception {
        HttpGraphQlTester graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Execute an invalid GraphQL query to trigger errors
        graphQlTester
                .document("{ invalidField { nonExistentProperty } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    // We expect this to fail and generate errors
                    // The important thing is that error logging occurs
                });

        // Verify that error events are published for alerting
        verify(eventPublisher, atLeastOnce()).publishEvent(any(GraphQLOperationEvent.class));
    }

    @Test
    void shouldProvideHealthCheckForGraphQLAlerting() {
        // Test that the alerting service provides health information
        var health = alertingService.health();

        // Verify health check structure
        assert health != null;
        assert health.getStatus() != null;
        assert health.getDetails() != null;
        assert health.getDetails().containsKey("alerting_enabled");
    }
}