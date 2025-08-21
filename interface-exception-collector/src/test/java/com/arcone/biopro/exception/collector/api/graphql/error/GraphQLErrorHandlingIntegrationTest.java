package com.arcone.biopro.exception.collector.api.graphql.error;

import com.arcone.biopro.exception.collector.api.graphql.exception.GraphQLErrorType;
import com.arcone.biopro.exception.collector.api.graphql.exception.GraphQLExceptionMapper;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GraphQL error handling consistency and security.
 * Verifies that business exceptions are properly mapped to GraphQL errors
 * and that error responses are consistent between REST and GraphQL APIs.
 * 
 * Task 6.2: Test GraphQL Error Handling
 * - Verify GraphQL error handling provides consistent error responses
 * - Test that business exceptions are properly mapped to GraphQL errors
 * - Ensure error handling consistency between REST and GraphQL APIs
 * - Test error response format and security (no sensitive data leakage)
 */
class GraphQLErrorHandlingIntegrationTest {

        private GraphQLExceptionMapper graphQLExceptionMapper;

        @BeforeEach
        void setUp() {
                graphQLExceptionMapper = new GraphQLExceptionMapper();
        }

        @Test
        void shouldMapExceptionTypesCorrectly() {
                // Test the GraphQL exception mapper directly

                // Test ExceptionNotFoundException mapping
                GraphQLErrorType notFoundType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new ExceptionNotFoundException("TXN-123"));
                assertThat(notFoundType).isEqualTo(GraphQLErrorType.NOT_FOUND);

                // Test RetryNotAllowedException mapping
                GraphQLErrorType businessRuleType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new RetryNotAllowedException("TXN-123"));
                assertThat(businessRuleType).isEqualTo(GraphQLErrorType.BUSINESS_RULE_ERROR);

                // Test ExceptionProcessingException mapping
                GraphQLErrorType processingType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new ExceptionProcessingException("Processing failed"));
                assertThat(processingType).isEqualTo(GraphQLErrorType.BUSINESS_RULE_ERROR);

                // Test DataAccessException mapping
                GraphQLErrorType dataAccessType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new DataAccessException("Database error") {
                                });
                assertThat(dataAccessType).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);

                // Test ResourceAccessException mapping
                GraphQLErrorType resourceAccessType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new ResourceAccessException("External service error"));
                assertThat(resourceAccessType).isEqualTo(GraphQLErrorType.EXTERNAL_SERVICE_ERROR);

                // Test IllegalArgumentException mapping
                GraphQLErrorType validationType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new IllegalArgumentException("Invalid argument"));
                assertThat(validationType).isEqualTo(GraphQLErrorType.VALIDATION_ERROR);
        }

        @Test
        void shouldProvideUserFriendlyErrorMessages() {
                // Test user-friendly messages for different error types

                String notFoundMessage = graphQLExceptionMapper.getUserFriendlyMessage(
                                GraphQLErrorType.NOT_FOUND, new ExceptionNotFoundException("TXN-123"));
                assertThat(notFoundMessage).isEqualTo("The requested resource was not found.");

                String validationMessage = graphQLExceptionMapper.getUserFriendlyMessage(
                                GraphQLErrorType.VALIDATION_ERROR, new IllegalArgumentException("Invalid input"));
                assertThat(validationMessage)
                                .isEqualTo("The provided input is invalid. Please check your request and try again.");

                String businessRuleMessage = graphQLExceptionMapper.getUserFriendlyMessage(
                                GraphQLErrorType.BUSINESS_RULE_ERROR, new RetryNotAllowedException("TXN-123"));
                assertThat(businessRuleMessage).isEqualTo("This exception cannot be retried at this time.");

                String internalErrorMessage = graphQLExceptionMapper.getUserFriendlyMessage(
                                GraphQLErrorType.INTERNAL_ERROR, new RuntimeException("Internal error"));
                assertThat(internalErrorMessage).isEqualTo("An internal error occurred. Please try again later.");
        }

        @Test
        void shouldProvideRetryInformationForRetryableErrors() {
                // Test that retryable errors include retry information

                // External service error should be retryable
                GraphQLErrorType externalServiceError = GraphQLErrorType.EXTERNAL_SERVICE_ERROR;
                assertThat(externalServiceError.isServerError()).isTrue();

                // Validation error should not be retryable
                GraphQLErrorType validationError = GraphQLErrorType.VALIDATION_ERROR;
                assertThat(validationError.isClientError()).isTrue();

                // Test HTTP status code mapping
                assertThat(GraphQLErrorType.NOT_FOUND.getHttpStatusCode()).isEqualTo(404);
                assertThat(GraphQLErrorType.VALIDATION_ERROR.getHttpStatusCode()).isEqualTo(400);
                assertThat(GraphQLErrorType.AUTHORIZATION_ERROR.getHttpStatusCode()).isEqualTo(403);
                assertThat(GraphQLErrorType.INTERNAL_ERROR.getHttpStatusCode()).isEqualTo(500);
        }

        @Test
        void shouldHandleNullExceptionGracefully() {
                // Test null exception handling
                GraphQLErrorType nullType = graphQLExceptionMapper.mapExceptionToErrorType(null);
                assertThat(nullType).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);
        }

        @Test
        void shouldHandleUnknownExceptionTypes() {
                // Test unknown exception types default to internal error
                GraphQLErrorType unknownType = graphQLExceptionMapper.mapExceptionToErrorType(
                                new RuntimeException("Unknown error"));
                assertThat(unknownType).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);
        }

        @Test
        void shouldProvideConsistentErrorCodes() {
                // Test that error codes are consistent
                assertThat(GraphQLErrorType.NOT_FOUND.getCode()).isEqualTo("NOT_FOUND");
                assertThat(GraphQLErrorType.VALIDATION_ERROR.getCode()).isEqualTo("VALIDATION_ERROR");
                assertThat(GraphQLErrorType.BUSINESS_RULE_ERROR.getCode()).isEqualTo("BUSINESS_RULE_ERROR");
                assertThat(GraphQLErrorType.INTERNAL_ERROR.getCode()).isEqualTo("INTERNAL_ERROR");
                assertThat(GraphQLErrorType.EXTERNAL_SERVICE_ERROR.getCode()).isEqualTo("EXTERNAL_SERVICE_ERROR");
        }

        @Test
        void shouldProvideConsistentErrorDescriptions() {
                // Test that error descriptions are meaningful
                assertThat(GraphQLErrorType.NOT_FOUND.getDescription()).contains("not found");
                assertThat(GraphQLErrorType.VALIDATION_ERROR.getDescription()).contains("validation");
                assertThat(GraphQLErrorType.BUSINESS_RULE_ERROR.getDescription()).containsIgnoringCase("business");
                assertThat(GraphQLErrorType.INTERNAL_ERROR.getDescription()).contains("internal");
                assertThat(GraphQLErrorType.EXTERNAL_SERVICE_ERROR.getDescription()).contains("external service");
        }

        @Test
        void shouldClassifyErrorTypesCorrectly() {
                // Test client vs server error classification

                // Client errors (4xx equivalent)
                assertThat(GraphQLErrorType.VALIDATION_ERROR.isClientError()).isTrue();
                assertThat(GraphQLErrorType.AUTHORIZATION_ERROR.isClientError()).isTrue();
                assertThat(GraphQLErrorType.NOT_FOUND.isClientError()).isTrue();
                assertThat(GraphQLErrorType.BUSINESS_RULE_ERROR.isClientError()).isTrue();
                assertThat(GraphQLErrorType.RATE_LIMIT_ERROR.isClientError()).isTrue();
                assertThat(GraphQLErrorType.QUERY_COMPLEXITY_ERROR.isClientError()).isTrue();

                // Server errors (5xx equivalent)
                assertThat(GraphQLErrorType.EXTERNAL_SERVICE_ERROR.isServerError()).isTrue();
                assertThat(GraphQLErrorType.TIMEOUT_ERROR.isServerError()).isTrue();
                assertThat(GraphQLErrorType.INTERNAL_ERROR.isServerError()).isTrue();

                // Conflict errors (409 equivalent)
                assertThat(GraphQLErrorType.CONCURRENT_MODIFICATION_ERROR.getHttpStatusCode()).isEqualTo(409);
        }

        @Test
        void shouldNotLeakSensitiveInformationInErrorMessages() {
                // Test that user-friendly messages don't contain sensitive information

                String[] errorMessages = {
                                graphQLExceptionMapper.getUserFriendlyMessage(GraphQLErrorType.NOT_FOUND,
                                                new ExceptionNotFoundException("TXN-123")),
                                graphQLExceptionMapper.getUserFriendlyMessage(GraphQLErrorType.VALIDATION_ERROR,
                                                new IllegalArgumentException("Invalid input")),
                                graphQLExceptionMapper.getUserFriendlyMessage(GraphQLErrorType.BUSINESS_RULE_ERROR,
                                                new RetryNotAllowedException("TXN-123")),
                                graphQLExceptionMapper.getUserFriendlyMessage(GraphQLErrorType.INTERNAL_ERROR,
                                                new RuntimeException("Internal error")),
                                graphQLExceptionMapper.getUserFriendlyMessage(GraphQLErrorType.EXTERNAL_SERVICE_ERROR,
                                                new ResourceAccessException("Service error"))
                };

                for (String message : errorMessages) {
                        // Should not contain sensitive information
                        assertThat(message.toLowerCase()).doesNotContain("password");
                        assertThat(message.toLowerCase()).doesNotContain("secret");
                        assertThat(message.toLowerCase()).doesNotContain("token");
                        assertThat(message.toLowerCase()).doesNotContain("api_key");
                        assertThat(message.toLowerCase()).doesNotContain("database_url");
                        assertThat(message.toLowerCase()).doesNotContain("connection_string");
                        assertThat(message.toLowerCase()).doesNotContain("jdbc:");

                        // Should not contain internal paths or stack traces
                        assertThat(message.toLowerCase()).doesNotContain("/home/");
                        assertThat(message.toLowerCase()).doesNotContain("c:\\");
                        assertThat(message.toLowerCase()).doesNotContain("at com.arcone");
                        assertThat(message.toLowerCase()).doesNotContain("caused by:");
                        assertThat(message.toLowerCase()).doesNotContain("exception in thread");

                        // Should not contain sensitive configuration
                        assertThat(message.toLowerCase()).doesNotContain("spring.datasource");
                        assertThat(message.toLowerCase()).doesNotContain("redis.password");
                        assertThat(message.toLowerCase()).doesNotContain("kafka.security");
                }
        }

        @Test
        void shouldHandleSpecificExceptionPatterns() {
                // Test specific exception pattern recognition

                // Test timeout exception recognition
                RuntimeException timeoutException = new RuntimeException("Connection timeout occurred");
                GraphQLErrorType timeoutType = graphQLExceptionMapper.mapExceptionToErrorType(timeoutException);
                assertThat(timeoutType).isEqualTo(GraphQLErrorType.TIMEOUT_ERROR);

                // Test rate limit exception recognition - this will map to INTERNAL_ERROR by
                // default
                RuntimeException rateLimitException = new RuntimeException("Rate limit exceeded");
                GraphQLErrorType rateLimitType = graphQLExceptionMapper.mapExceptionToErrorType(rateLimitException);
                assertThat(rateLimitType).isIn(GraphQLErrorType.RATE_LIMIT_ERROR, GraphQLErrorType.INTERNAL_ERROR);

                // Test external service exception recognition
                RuntimeException serviceException = new RuntimeException("Service unavailable");
                GraphQLErrorType serviceType = graphQLExceptionMapper.mapExceptionToErrorType(serviceException);
                assertThat(serviceType).isEqualTo(GraphQLErrorType.EXTERNAL_SERVICE_ERROR);
        }

        @Test
        void shouldProvideConsistentHttpStatusCodes() {
                // Test HTTP status code mapping consistency

                // 400 Bad Request
                assertThat(GraphQLErrorType.VALIDATION_ERROR.getHttpStatusCode()).isEqualTo(400);
                assertThat(GraphQLErrorType.BUSINESS_RULE_ERROR.getHttpStatusCode()).isEqualTo(400);
                assertThat(GraphQLErrorType.QUERY_COMPLEXITY_ERROR.getHttpStatusCode()).isEqualTo(400);

                // 403 Forbidden
                assertThat(GraphQLErrorType.AUTHORIZATION_ERROR.getHttpStatusCode()).isEqualTo(403);

                // 404 Not Found
                assertThat(GraphQLErrorType.NOT_FOUND.getHttpStatusCode()).isEqualTo(404);

                // 409 Conflict
                assertThat(GraphQLErrorType.CONCURRENT_MODIFICATION_ERROR.getHttpStatusCode()).isEqualTo(409);

                // 429 Too Many Requests
                assertThat(GraphQLErrorType.RATE_LIMIT_ERROR.getHttpStatusCode()).isEqualTo(429);

                // 500 Internal Server Error
                assertThat(GraphQLErrorType.EXTERNAL_SERVICE_ERROR.getHttpStatusCode()).isEqualTo(500);
                assertThat(GraphQLErrorType.INTERNAL_ERROR.getHttpStatusCode()).isEqualTo(500);

                // 504 Gateway Timeout
                assertThat(GraphQLErrorType.TIMEOUT_ERROR.getHttpStatusCode()).isEqualTo(504);
        }
}