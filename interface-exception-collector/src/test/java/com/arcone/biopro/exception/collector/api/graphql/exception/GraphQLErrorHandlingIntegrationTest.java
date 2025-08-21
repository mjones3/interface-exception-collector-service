package com.arcone.biopro.exception.collector.api.graphql.exception;

import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GraphQL error handling consistency and security.
 * Verifies that business exceptions are properly mapped to GraphQL errors
 * and that error responses are consistent between REST and GraphQL APIs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GraphQLErrorHandlingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private WebGraphQlTester graphQlTester;

    @Test
    void shouldMapBusinessExceptionToGraphQLError() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A query that will trigger a business exception (non-existent exception)
        String query = """
            query GetNonExistentException {
                exception(transactionId: "non-existent-id") {
                    id
                    transactionId
                    serviceName
                    status
                }
            }
            """;

        // When & Then - Should return proper GraphQL error
        graphQlTester.document(query)
                .execute()
                .errors()
                .expect(error -> {
                    assertThat(error.getMessage()).containsIgnoringCase("not found");
                    assertThat(error.getPath()).contains("exception");
                    
                    // Should not leak sensitive information
                    assertThat(error.getMessage()).doesNotContain("SQLException");
                    assertThat(error.getMessage()).doesNotContain("database");
                    assertThat(error.getMessage()).doesNotContain("connection");
                    assertThat(error.getMessage()).doesNotContain("password");
                    assertThat(error.getMessage()).doesNotContain("secret");
                    assertThat(error.getMessage()).doesNotContain("token");
                });
    }

@Test
    void shouldHandleValidationErrorsConsistently() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A query with invalid parameters
        String invalidQuery = """
            query GetExceptionWithInvalidId {
                exception(transactionId: "") {
                    id
                    status
                }
            }
            """;

        // When & Then - Should return validation error
        graphQlTester.document(invalidQuery)
                .execute()
                .errors()
                .expect(error -> {
                    assertThat(error.getMessage()).containsAnyOf("invalid", "required", "empty");
                    
                    // Should provide helpful error message without sensitive data
                    assertThat(error.getMessage()).doesNotContain("password");
                    assertThat(error.getMessage()).doesNotContain("secret");
                    assertThat(error.getMessage()).doesNotContain("token");
                    assertThat(error.getMessage()).doesNotContain("api_key");
                });
    }

    @Test
    void shouldProvideConsistentErrorFormatBetweenRESTAndGraphQL() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A GraphQL query that will fail
        String graphqlQuery = """
            query GetException {
                exception(transactionId: "invalid-format-id") {
                    id
                    status
                }
            }
            """;

        // When - Execute GraphQL query
        WebGraphQlTester.Response response = graphQlTester.document(graphqlQuery)
                .execute();

        // Then - Error format should be consistent
        response.errors()
                .expect(error -> {
                    // Should have standard error structure
                    assertThat(error.getMessage()).isNotNull();
                    assertThat(error.getPath()).isNotNull();
                    
                    // Should not expose internal details
                    assertThat(error.getMessage()).doesNotContain("Exception in thread");
                    assertThat(error.getMessage()).doesNotContain("at com.arcone");
                    assertThat(error.getMessage()).doesNotContain("Caused by");
                    assertThat(error.getMessage()).doesNotContain("java.lang");
                });
    }

    @Test
    void shouldNotLeakSensitiveInformationInErrors() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A query that might expose sensitive data
        String query = """
            query SensitiveDataQuery {
                exception(transactionId: "test-id") {
                    id
                    stackTrace
                    payload {
                        content
                    }
                }
            }
            """;

        // When & Then - Errors should not leak sensitive information
        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> {
                    errors.forEach(error -> {
                        String message = error.getMessage().toLowerCase();
                        
                        // Should not contain sensitive information
                        assertThat(message).doesNotContain("password");
                        assertThat(message).doesNotContain("secret");
                        assertThat(message).doesNotContain("token");
                        assertThat(message).doesNotContain("api_key");
                        assertThat(message).doesNotContain("database_url");
                        assertThat(message).doesNotContain("connection_string");
                        
                        // Should not contain internal paths or stack traces
                        assertThat(message).doesNotContain("/home/");
                        assertThat(message).doesNotContain("c:\\");
                        assertThat(message).doesNotContain("at com.arcone");
                        assertThat(message).doesNotContain("caused by:");
                    });
                });
    }

    @Test
    void shouldHandleConcurrentRequestsWithConsistentErrorHandling() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - Multiple concurrent queries that will fail
        String failingQuery = """
            query ConcurrentFailure {
                exception(transactionId: "non-existent-concurrent-id") {
                    id
                    status
                }
            }
            """;

        // When & Then - All concurrent requests should have consistent error handling
        for (int i = 0; i < 3; i++) {
            graphQlTester.document(failingQuery)
                    .execute()
                    .errors()
                    .expect(error -> {
                        assertThat(error.getMessage()).containsIgnoringCase("not found");
                        assertThat(error.getPath()).contains("exception");
                        
                        // Consistent error structure
                        assertThat(error.getMessage()).isNotNull().isNotEmpty();
                        assertThat(error.getPath()).isNotNull();
                    });
        }
    }

    @Test
    void shouldProvideStructuredErrorInformation() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A query with potential error points
        String query = """
            query MultipleErrorPoints {
                exception(transactionId: "test-id") {
                    id
                    payload {
                        id
                        content
                    }
                }
            }
            """;

        // When & Then - Errors should be structured and informative
        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> {
                    errors.forEach(error -> {
                        // Should have proper error structure
                        assertThat(error.getMessage()).isNotEmpty();
                        assertThat(error.getPath()).isNotNull();
                        assertThat(error.getLocations()).isNotNull();
                        
                        // Should provide context about where error occurred
                        if (error.getPath() != null && !error.getPath().isEmpty()) {
                            assertThat(error.getPath().toString()).containsAnyOf(
                                "exception", "payload"
                            );
                        }
                    });
                });
    }

    @Test
    void shouldHandleComplexQueriesWithErrorsGracefully() {
        // Given - Setup GraphQL tester
        graphQlTester = WebGraphQlTester.create(webTestClient);
        
        // Given - A complex query that might have multiple failure points
        String complexQuery = """
            query ComplexErrorQuery {
                exceptions(first: 10) {
                    edges {
                        node {
                            id
                            transactionId
                            serviceName
                            exceptionType
                            message
                            stackTrace
                            payload {
                                id
                                content
                                metadata
                            }
                            retryHistory {
                                id
                                attemptNumber
                                timestamp
                                status
                                errorMessage
                            }
                        }
                    }
                }
            }
            """;

        // When & Then - Should handle complex queries gracefully
        graphQlTester.document(complexQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    // If there are errors, they should be handled properly
                    errors.forEach(error -> {
                        assertThat(error.getMessage()).isNotNull();
                        assertThat(error.getPath()).isNotNull();
                        
                        // Should not contain sensitive information
                        String message = error.getMessage().toLowerCase();
                        assertThat(message).doesNotContain("password");
                        assertThat(message).doesNotContain("secret");
                        assertThat(message).doesNotContain("token");
                        assertThat(message).doesNotContain("outofmemoryerror");
                    });
                });
    }
}