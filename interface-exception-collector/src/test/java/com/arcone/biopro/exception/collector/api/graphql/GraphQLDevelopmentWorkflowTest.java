package com.arcone.biopro.exception.collector.api.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GraphQL development workflow and tooling.
 * Verifies GraphiQL interface, schema introspection, and development endpoints.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles({ "test", "graphql-dev-test" })
@Transactional
class GraphQLDevelopmentWorkflowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void graphiqlInterface_ShouldBeAccessible() throws Exception {
        // When & Then
        mockMvc.perform(get("/graphiql"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlSchemaIntrospection_ShouldBeEnabled() throws Exception {
        // Given
        String introspectionQuery = """
                query IntrospectionQuery {
                  __schema {
                    queryType {
                      name
                      fields {
                        name
                        type {
                          name
                          kind
                        }
                      }
                    }
                    mutationType {
                      name
                      fields {
                        name
                        type {
                          name
                          kind
                        }
                      }
                    }
                    subscriptionType {
                      name
                      fields {
            name
                        type {
                          name
                          kind
                        }
                      }
                    }
                    types {
                      name
                      kind
                      description
                    }
                  }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", introspectionQuery);

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.__schema").exists())
                .andExpect(jsonPath("$.data.__schema.queryType.name").value("Query"))
                .andExpect(jsonPath("$.data.__schema.mutationType.name").value("Mutation"))
                .andExpect(jsonPath("$.data.__schema.subscriptionType.name").value("Subscription"))
                .andExpect(jsonPath("$.data.__schema.types").isArray())
                .andExpect(jsonPath("$.data.__schema.types", hasSize(greaterThan(10))));
    }

    @Test
    void graphqlSchemaDocumentation_ShouldBeAccessible() throws Exception {
        // When & Then
        mockMvc.perform(get("/graphql/schema"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=utf-8"))
                .andExpected(content().string(containsString("type Query")))
                .andExpected(content().string(containsString("type Mutation")))
                .andExpected(content().string(containsString("type Subscription")))
                .andExpected(content().string(containsString("type InterfaceException")))
                .andExpected(content().string(containsString("enum InterfaceType")))
                .andExpected(content().string(containsString("enum ExceptionStatus")));
    }

    @Test
    void graphqlDevelopmentInfo_ShouldProvideComprehensiveDocumentation() throws Exception {
        // When & Then
        mockMvc.perform(get("/graphql/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/markdown;charset=utf-8"))
                .andExpected(content().string(containsString("# GraphQL Development Information")))
                .andExpected(content().string(containsString("## Available Endpoints")))
                .andExpected(content().string(containsString("## Available Operations")))
                .andExpected(content().string(containsString("### Queries")))
                .andExpected(content().string(containsString("### Mutations")))
                .andExpected(content().string(containsString("### Subscriptions")))
                .andExpected(content().string(containsString("## Example Queries")))
                .andExpected(content().string(containsString("## Authentication")))
                .andExpected(content().string(containsString("## Rate Limiting")));
    }

    @Test
    void graphqlExamples_ShouldProvideUsableQueryExamples() throws Exception {
        // When & Then
        mockMvc.perform(get("/graphql/examples"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=utf-8"))
                .andExpect(jsonPath("$.queries").exists())
                .andExpect(jsonPath("$.mutations").exists())
                .andExpect(jsonPath("$.subscriptions").exists())
                .andExpect(jsonPath("$.queries.listExceptions").exists())
                .andExpect(jsonPath("$.queries.listExceptions.query").isString())
                .andExpect(jsonPath("$.queries.listExceptions.variables").exists())
                .andExpect(jsonPath("$.mutations.retryException").exists())
                .andExpect(jsonPath("$.mutations.retryException.query").isString())
                .andExpect(jsonPath("$.subscriptions.exceptionUpdates").exists());
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlQueryExecution_WithDevelopmentTracing_ShouldIncludePerformanceInfo() throws Exception {
        // Given
        String query = """
                query {
                  exceptions(pagination: { first: 5 }) {
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

        Map<String, Object> requestBody = Map.of("query", query);

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exceptions").exists())
                .andExpect(jsonPath("$.extensions").exists()) // Tracing extensions should be present
                .andExpect(jsonPath("$.extensions.tracing").exists());
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlComplexQuery_ShouldAllowHighComplexityInDevelopment() throws Exception {
        // Given - A complex query that would be rejected in production
        String complexQuery = """
                query {
                  exceptions(pagination: { first: 10 }) {
                    edges {
                      node {
                        transactionId
                        interfaceType
                        exceptionReason
                        status
                        severity
                        category
                        customerId
                        locationCode
                        timestamp
                        processedAt
                        retryable
                        retryCount
                        maxRetries
                        retryHistory {
                          attemptNumber
                          status
                          initiatedBy
                          initiatedAt
                          completedAt
                          resultSuccess
                          resultMessage
                          resultResponseCode
                        }
                        originalPayload {
                          content
                          contentType
                          sourceService
                          retrievedAt
                        }
                        statusHistory {
                          previousStatus
                          newStatus
                          changedBy
                          changedAt
                          reason
                          notes
                        }
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

        Map<String, Object> requestBody = Map.of("query", complexQuery);

        // When & Then - Should succeed in development mode
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exceptions").exists())
                .andExpect(jsonPath("$.errors").doesNotExist()); // No complexity errors
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlDeepQuery_ShouldAllowHighDepthInDevelopment() throws Exception {
        // Given - A deeply nested query that would be rejected in production
        String deepQuery = """
                query {
                  exceptions(pagination: { first: 1 }) {
                    edges {
                      node {
                        retryHistory {
                          exception {
                            retryHistory {
                              exception {
                                statusHistory {
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

        Map<String, Object> requestBody = Map.of("query", deepQuery);

        // When & Then - Should succeed in development mode
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exceptions").exists())
                .andExpect(jsonPath("$.errors").doesNotExist()); // No depth errors
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlErrorHandling_ShouldProvideEnhancedErrorsInDevelopment() throws Exception {
        // Given - A query with intentional errors
        String invalidQuery = """
                query {
                  exception(transactionId: null) {
                    transactionId
                    nonExistentField
                  }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", invalidQuery);

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk()) // GraphQL returns 200 even for errors
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errors[0].message").exists())
                .andExpect(jsonPath("$.errors[0].locations").exists()) // Enhanced error info
                .andExpect(jsonPath("$.errors[0].path").exists());
    }

    @Test
    @WithMockUser(username = "developer", roles = {"ADMIN"})
    void graphqlVariableValidation_ShouldProvideDetailedValidationErrors() throws Exception {
        // Given - A query with invalid variables
        String query = """
                query($transactionId: String!) {
                  exception(transactionId: $transactionId) {
                    transactionId
                  }
                }
                """;

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("transactionId", 12345) // Wrong type - should be string
        );

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("Variable")))
                .andExpect(jsonPath("$.errors[0].extensions").exists()); // Enhanced error extensions
    }

    @Test
    void graphqlCorsConfiguration_ShouldAllowDevelopmentOrigins() throws Exception {
        // Given
        String query = """
                query {
                  __schema {
                    queryType {
                      name
                    }
                  }
                }
                """;

        Map<String, Object> requestBody = Map.of("query", query);

        // When & Then
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("Origin", "http://localhost:3000")) // Typical development origin
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    void graphqlOptionsRequest_ShouldHandlePreflightCorrectly() throws Exception {
        // When & Then
        mockMvc.perform(options("/graphql")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")));
    }
}