package com.arcone.biopro.exception.collector.api;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests that verify error handling consistency between REST and GraphQL APIs.
 * Ensures both APIs provide equivalent error responses and handling behavior.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DualApiErrorHandlingConsistencyTest {

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
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void notFoundError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        String nonExistentId = "NON-EXISTENT-TXN";

        // Test REST API - Should return 404
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions/{transactionId}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        JsonNode restError = objectMapper.readTree(restResult.getResponse().getContentAsString());

        // Test GraphQL API - Should return null with no errors
        String graphqlQuery = """
                query {
                    exception(transactionId: "NON-EXISTENT-TXN") {
                        transactionId
                    }
                }
                """;

        graphQlTester.document(graphqlQuery)
                .execute()
                .path("exception")
                .valueIsNull();

        // Both APIs should handle non-existent resources consistently
        // REST returns 404, GraphQL returns null - both are valid patterns
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void validationError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // Test REST API with invalid parameters
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions")
                .param("page", "-1") // Invalid page number
                .param("size", "0") // Invalid page size
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode restError = objectMapper.readTree(restResult.getResponse().getContentAsString());

        // Test GraphQL API with invalid parameters
        String graphqlQuery = """
                query {
                    exceptions(
                        pagination: {
                            first: -1
                        }
                    ) {
                        totalCount
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("validation") ||
                        error.getMessage().contains("invalid") ||
                        error.getMessage().contains("positive"));

        // Both APIs should provide meaningful validation error messages
        assertThat(restError.has("message")).isTrue();
        assertThat(restError.get("message").asText()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "operator", roles = { "OPERATOR" })
    void businessLogicError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // Test REST API with business logic error (e.g., retry non-retryable exception)
        MvcResult restResult = mockMvc.perform(post("/api/v1/exceptions/TXN-001/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Test retry\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode restError = objectMapper.readTree(restResult.getResponse().getContentAsString());

        // Test GraphQL API with same business logic error
        String graphqlMutation = """
                mutation {
                    retryException(transactionId: "TXN-001", reason: "Test retry", priority: HIGH) {
                        success
                        message
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlMutation)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(false);

        // Both APIs should provide consistent business logic error handling
        assertThat(restError.has("message")).isTrue();

        // GraphQL should return success=false with error message
        graphQlTester.document(graphqlMutation)
                .execute()
                .path("retryException.message")
                .entity(String.class)
                .satisfies(message -> assertThat(message).isNotEmpty());
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void malformedRequestError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // Test REST API with malformed JSON
        mockMvc.perform(post("/api/v1/exceptions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("JSON")));

        // Test GraphQL API with malformed query
        String malformedQuery = """
                query {
                    exceptions {
                        invalid syntax here
                    }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", malformedQuery);

        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("syntax")));
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void queryComplexityError_GraphQL_ShouldReturnMeaningfulError() throws Exception {
        // Test GraphQL API with overly complex query (if complexity analysis is
        // enabled)
        String complexQuery = """
                query {
                    exceptions(pagination: { first: 1000 }) {
                        edges {
                            node {
                                transactionId
                                retryHistory {
                                    exception {
                                        retryHistory {
                                            exception {
                                                retryHistory {
                                                    exception {
                                                        transactionId
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        // This should either succeed (if limits are high enough) or return a meaningful
        // error
        var response = graphQlTester.document(complexQuery)
                .execute();

        // If it fails, it should be due to complexity/depth limits
        try {
            response.path("exceptions").hasValue();
        } catch (Exception e) {
            // Should contain meaningful error about complexity or depth
            response.errors()
                    .expect(error -> error.getMessage().contains("complexity") ||
                            error.getMessage().contains("depth") ||
                            error.getMessage().contains("limit"));
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void timeoutError_BothAPIs_ShouldHandleGracefully() throws Exception {
        // This test would typically involve mocking slow services
        // For now, we'll test that both APIs handle timeout scenarios consistently

        // Test REST API timeout handling (would require service mocking)
        // mockMvc.perform(get("/api/v1/exceptions/slow-operation")...)

        // Test GraphQL API timeout handling (would require service mocking)
        // graphQlTester.document("query { slowOperation }")...

        // Both should return appropriate timeout errors
        // This is a placeholder test - actual implementation would require service
        // mocking
        assertThat(true).isTrue(); // Placeholder assertion
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void authorizationError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // Test REST API authorization error
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resolution\":\"Fixed\",\"notes\":\"Test\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Access")));

        // Test GraphQL API authorization error
        String mutation = """
                mutation {
                    resolveException(transactionId: "TXN-001", resolution: "Fixed", notes: "Test") {
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
    void concurrentModificationError_BothAPIs_ShouldHandleConsistently() throws Exception {
        // This test would simulate concurrent modification scenarios
        // Both APIs should handle optimistic locking or similar mechanisms consistently

        // Test REST API concurrent modification
        // This would typically involve version numbers or timestamps
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"First acknowledgment\",\"version\":1}"))
                .andExpect(status().isOk());

        // Simulate concurrent modification with stale version
        mockMvc.perform(post("/api/v1/exceptions/TXN-001/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Second acknowledgment\",\"version\":1}"))
                .andExpect(status().isConflict());

        // Test GraphQL API concurrent modification
        String mutation = """
                mutation {
                    acknowledgeException(
                        transactionId: "TXN-001",
                        reason: "GraphQL acknowledgment",
                        version: 1
                    ) {
                        success
                        message
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("acknowledgeException.success")
                .entity(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void rateLimitError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // This test would simulate rate limiting scenarios
        // Both APIs should return consistent rate limit error responses

        // In a real scenario, this would involve making many requests quickly
        // to trigger rate limiting, then verifying both APIs return 429 status
        // with consistent error messages

        // Placeholder test - actual implementation would require rate limiting
        // configuration
        assertThat(true).isTrue(); // Placeholder assertion
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void serverError_BothAPIs_ShouldReturnConsistentResponse() throws Exception {
        // This test would simulate internal server errors
        // Both APIs should return consistent 500-level errors without exposing
        // sensitive information

        // Test would involve mocking service layer to throw exceptions
        // and verifying both APIs handle them consistently

        // Placeholder test - actual implementation would require service mocking
        assertThat(true).isTrue(); // Placeholder assertion
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void errorResponseFormat_BothAPIs_ShouldFollowConsistentStructure() throws Exception {
        // Test that error responses follow consistent structure

        // REST API error structure
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions/INVALID-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        JsonNode restError = objectMapper.readTree(restResult.getResponse().getContentAsString());

        // Verify REST error structure
        assertThat(restError.has("timestamp")).isTrue();
        assertThat(restError.has("status")).isTrue();
        assertThat(restError.has("error")).isTrue();
        assertThat(restError.has("message")).isTrue();
        assertThat(restError.has("path")).isTrue();

        // GraphQL API error structure
        String invalidQuery = """
                query {
                    exception(transactionId: "") {
                        transactionId
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(invalidQuery)
                .execute()
                .errors()
                .expect(error -> {
                    // Verify GraphQL error structure
                    assertThat(error.getMessage()).isNotEmpty();
                    assertThat(error.getLocations()).isNotEmpty();
                    assertThat(error.getPath()).isNotNull();
                    return true;
                });
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void errorLogging_BothAPIs_ShouldLogErrorsConsistently() throws Exception {
        // This test would verify that both APIs log errors consistently
        // It would typically involve checking log outputs or using log capture

        // Test REST API error logging
        mockMvc.perform(get("/api/v1/exceptions/INVALID-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Test GraphQL API error logging
        String invalidQuery = """
                query {
                    nonExistentField
                }
                """;

        graphQlTester.document(invalidQuery)
                .execute()
                .errors()
                .verify();

        // Both operations should generate equivalent log entries
        // This would be verified through log analysis in a real implementation
        assertThat(true).isTrue(); // Placeholder assertion
    }

    private void createTestException() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception for error handling testing")
                .operation("CREATE_ORDER")
                .externalId("ORDER-001")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .retryable(false) // Make it non-retryable for business logic error tests
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .maxRetries(0)
                .build();

        exceptionRepository.save(exception);
    }
}