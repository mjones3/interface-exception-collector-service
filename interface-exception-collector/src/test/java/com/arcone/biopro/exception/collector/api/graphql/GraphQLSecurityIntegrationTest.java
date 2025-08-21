package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GraphQL operations with security.
 * Tests JWT authentication and role-based authorization for GraphQL endpoints.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class GraphQLSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Create test data
        createTestException();
    }

    @Test
    void graphqlEndpoint_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Given
        String query = """
                query {
                    exceptions {
                        edges {
                            node {
                                transactionId
                            }
                        }
                    }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", query);

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_WithViewerRole_ShouldAllowQueries() {
        // Given
        String query = """
                query {
                    exceptions {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                            }
                        }
                        totalCount
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isGreaterThan(0L);
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlMutation_WithViewerRole_ShouldDenyMutations() {
        // Given
        String mutation = """
                mutation {
                    retryException(transactionId: "TXN-001", reason: "Test retry", priority: HIGH) {
                        success
                        message
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied"));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void graphqlMutation_WithOperatorRole_ShouldAllowRetryOperations() {
        // Given
        String mutation = """
                mutation {
                    retryException(transactionId: "TXN-001", reason: "Test retry", priority: HIGH) {
                        success
                        message
                        exception {
                            transactionId
                            status
                        }
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void graphqlMutation_WithOperatorRole_ShouldAllowAcknowledgeOperations() {
        // Given
        String mutation = """
                mutation {
                    acknowledgeException(transactionId: "TXN-001", reason: "Acknowledged by operator") {
                        success
                        message
                        exception {
                            transactionId
                            status
                        }
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .path("acknowledgeException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void graphqlMutation_WithOperatorRole_ShouldDenyResolveOperations() {
        // Given
        String mutation = """
                mutation {
                    resolveException(transactionId: "TXN-001", resolution: "Fixed", notes: "Test resolution") {
                        success
                        message
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void graphqlMutation_WithAdminRole_ShouldAllowAllOperations() {
        // Given
        String mutation = """
                mutation {
                    resolveException(transactionId: "TXN-001", resolution: "Fixed by admin", notes: "Admin resolution") {
                        success
                        message
                        exception {
                            transactionId
                            status
                        }
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .path("resolveException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void graphqlMutation_BulkOperations_WithAdminRole_ShouldSucceed() {
        // Given
        String mutation = """
                mutation {
                    bulkRetryExceptions(
                        transactionIds: ["TXN-001"], 
                        reason: "Bulk retry test", 
                        priority: HIGH
                    ) {
                        totalRequested
                        successCount
                        failureCount
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .path("bulkRetryExceptions.totalRequested")
                .entity(Integer.class)
                .isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_WithComplexFilters_ShouldRespectSecurityContext() {
        // Given
        String query = """
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER]
                            statuses: [NEW, ACKNOWLEDGED]
                            severities: [HIGH, CRITICAL]
                        }
                        pagination: {
                            first: 10
                        }
                        sorting: {
                            field: "timestamp"
                            direction: DESC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                                severity
                                customerId
                            }
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                        totalCount
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_ExceptionById_ShouldReturnDetails() {
        // Given
        String query = """
                query {
                    exception(transactionId: "TXN-001") {
                        transactionId
                        interfaceType
                        exceptionReason
                        status
                        severity
                        category
                        customerId
                        locationCode
                        timestamp
                        retryable
                        retryCount
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001");
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_ExceptionSummary_ShouldReturnAggregatedData() {
        // Given
        String query = """
                query {
                    exceptionSummary(
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
                        byStatus {
                            status
                            count
                            percentage
                        }
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exceptionSummary.totalExceptions")
                .entity(Long.class)
                .isGreaterThanOrEqualTo(0L);
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void graphqlMutation_WithInvalidTransactionId_ShouldReturnError() {
        // Given
        String mutation = """
                mutation {
                    retryException(transactionId: "NON-EXISTENT", reason: "Test retry", priority: HIGH) {
                        success
                        message
                    }
                }
                """;

        // When & Then
        graphQlTester.document(mutation)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_WithInvalidTransactionId_ShouldReturnNull() {
        // Given
        String query = """
                query {
                    exception(transactionId: "NON-EXISTENT") {
                        transactionId
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exception")
                .valueIsNull();
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void graphqlQuery_WithPagination_ShouldRespectLimits() {
        // Given
        String query = """
                query {
                    exceptions(
                        pagination: {
                            first: 5
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                            }
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                            startCursor
                            endCursor
                        }
                        totalCount
                    }
                }
                """;

        // When & Then
        graphQlTester.document(query)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSizeLessThanOrEqualTo(5);
    }

    private void createTestException() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception for security testing")
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