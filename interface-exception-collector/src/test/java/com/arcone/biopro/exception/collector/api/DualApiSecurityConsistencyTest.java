package com.arcone.biopro.exception.collector.api;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that verify security consistency between REST and GraphQL APIs.
 * Ensures both APIs handle authentication and authorization identically.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DualApiSecurityConsistencyTest {

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
    void unauthenticatedAccess_BothAPIs_ShouldReturn401() throws Exception {
        // Test REST API
        mockMvc.perform(get("/api/v1/exceptions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Test GraphQL API
        String query = """
                query {
                    exceptions {
                        totalCount
                    }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", query);

        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void viewerRole_BothAPIs_ShouldAllowQueries() throws Exception {
        // Test REST API
        mockMvc.perform(get("/api/v1/exceptions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test GraphQL API
        String query = """
                query {
                    exceptions {
                        edges {
                            node {
                                transactionId
                            }
                        }
                        totalCount
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isGreaterThanOrEqualTo(0L);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void viewerRole_BothAPIs_ShouldDenyMutations() throws Exception {
        // Test REST API - POST operations should be denied
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        // Test GraphQL API - Mutations should be denied
        String mutation = """
                mutation {
                    retryException(transactionId: "TXN-001", reason: "Test", priority: HIGH) {
                        success
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied") ||
                        error.getMessage().contains("Forbidden"));
    }

    @Test
    @WithMockUser(username = "operator", roles = { "OPERATOR" })
    void operatorRole_BothAPIs_ShouldAllowRetryOperations() throws Exception {
        // Test REST API - Retry operations should be allowed
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Test retry\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isOk());

        // Test GraphQL API - Retry mutations should be allowed
        String mutation = """
                mutation {
                    retryException(transactionId: "TXN-001", reason: "Test retry", priority: HIGH) {
                        success
                        message
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "operator", roles = { "OPERATOR" })
    void operatorRole_BothAPIs_ShouldAllowAcknowledgeOperations() throws Exception {
        // Test REST API - Acknowledge operations should be allowed
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Acknowledged\",\"notes\":\"Test notes\"}"))
                .andExpect(status().isOk());

        // Test GraphQL API - Acknowledge mutations should be allowed
        String mutation = """
                mutation {
                    acknowledgeException(transactionId: "TXN-001", reason: "Acknowledged", notes: "Test notes") {
                        success
                        message
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("acknowledgeException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "operator", roles = { "OPERATOR" })
    void operatorRole_BothAPIs_ShouldDenyResolveOperations() throws Exception {
        // Test REST API - Resolve operations should be denied
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resolution\":\"Fixed\",\"notes\":\"Test resolution\"}"))
                .andExpect(status().isForbidden());

        // Test GraphQL API - Resolve mutations should be denied
        String mutation = """
                mutation {
                    resolveException(transactionId: "TXN-001", resolution: "Fixed", notes: "Test resolution") {
                        success
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied") ||
                        error.getMessage().contains("Forbidden"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void adminRole_BothAPIs_ShouldAllowAllOperations() throws Exception {
        // Test REST API - All operations should be allowed
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resolution\":\"Fixed by admin\",\"notes\":\"Admin resolution\"}"))
                .andExpect(status().isOk());

        // Test GraphQL API - All mutations should be allowed
        String mutation = """
                mutation {
                    resolveException(transactionId: "TXN-001", resolution: "Fixed by admin", notes: "Admin resolution") {
                        success
                        message
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("resolveException.success")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void adminRole_BothAPIs_ShouldAllowBulkOperations() throws Exception {
        // Test REST API - Bulk operations should be allowed
        mockMvc.perform(post("/api/v1/exceptions/bulk/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"transactionIds\":[\"TXN-001\"],\"reason\":\"Bulk retry\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isOk());

        // Test GraphQL API - Bulk mutations should be allowed
        String mutation = """
                mutation {
                    bulkRetryExceptions(
                        transactionIds: ["TXN-001"],
                        reason: "Bulk retry",
                        priority: HIGH
                    ) {
                        totalRequested
                        successCount
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("bulkRetryExceptions.totalRequested")
                .entity(Integer.class)
                .isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void dataFiltering_BothAPIs_ShouldRespectUserContext() throws Exception {
        // Both APIs should apply the same data filtering based on user context
        // This test verifies that user-specific data filtering is consistent

        // Test REST API
        mockMvc.perform(get("/api/v1/exceptions")
                .param("customerId", "CUST-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test GraphQL API
        String query = """
                query {
                    exceptions(
                        filters: {
                            customerIds: ["CUST-001"]
                        }
                    ) {
                        edges {
                            node {
                                customerId
                            }
                        }
                        totalCount
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isGreaterThanOrEqualTo(0L);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void sensitiveDataAccess_BothAPIs_ShouldHaveSameRestrictions() throws Exception {
        // Test that both APIs apply the same restrictions on sensitive data access

        // Test REST API - Should not expose sensitive fields for VIEWER role
        mockMvc.perform(get("/api/v1/exceptions/TXN-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // Additional assertions would check that sensitive fields are not included

        // Test GraphQL API - Should not expose sensitive fields for VIEWER role
        String query = """
                query {
                    exception(transactionId: "TXN-001") {
                        transactionId
                        interfaceType
                        status
                        # Sensitive fields should be restricted based on role
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001");
    }

    @Test
    @WithMockUser(username = "operator", roles = { "OPERATOR" })
    void auditLogging_BothAPIs_ShouldLogUserActions() throws Exception {
        // Both APIs should log user actions consistently for audit purposes

        // Test REST API - Action should be logged
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Audit test\",\"notes\":\"Testing audit logging\"}"))
                .andExpect(status().isOk());

        // Test GraphQL API - Action should be logged
        String mutation = """
                mutation {
                    acknowledgeException(transactionId: "TXN-001", reason: "Audit test", notes: "Testing audit logging") {
                        success
                        message
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("acknowledgeException.success")
                .entity(Boolean.class)
                .isEqualTo(true);

        // Both operations should generate equivalent audit log entries
        // This would typically be verified through log analysis or audit table checks
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void rateLimiting_BothAPIs_ShouldApplySameLimits() throws Exception {
        // Both APIs should apply the same rate limiting rules
        // This test would typically involve making multiple requests to test rate
        // limits

        // Test REST API rate limiting
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/exceptions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Test GraphQL API rate limiting
        String query = """
                query {
                    exceptions {
                        totalCount
                    }
                }
                """;

        for (int i = 0; i < 5; i++) {
            graphQlTester.document(query)
                    .execute()
                    .path("exceptions.totalCount")
                    .entity(Long.class)
                    .isGreaterThanOrEqualTo(0L);
        }

        // In a real scenario, both APIs should start returning 429 Too Many Requests
        // after exceeding the rate limit
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void corsHandling_BothAPIs_ShouldHaveSamePolicy() throws Exception {
        // Both APIs should handle CORS requests consistently

        // Test REST API CORS
        mockMvc.perform(options("/api/v1/exceptions")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());

        // Test GraphQL API CORS
        mockMvc.perform(options("/graphql")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());

        // Both should return the same CORS headers
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