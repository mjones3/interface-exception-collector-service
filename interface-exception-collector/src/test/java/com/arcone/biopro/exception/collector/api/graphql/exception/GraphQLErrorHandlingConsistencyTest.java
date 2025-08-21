package com.arcone.biopro.exception.collector.api.graphql.exception;

import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for GraphQL error handling consistency and security.
 * Verifies that business exceptions are properly mapped to GraphQL errors
 * and that error responses are consistent between REST and GraphQL APIs.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLErrorHandlingConsistencyTest {

    @Mock
    private DataFetchingEnvironment environment;

    @Mock
    private ExecutionStepInfo executionStepInfo;

    @Mock
    private Field field;

    @Mock
    private OperationDefinition operationDefinition;

    private GraphQLExceptionHandler exceptionHandler;
    private GraphQLExceptionMapper exceptionMapper;

    @BeforeEach
    void setUp() {
        // Create real instances instead of mocks to test actual behavior
        exceptionMapper = new GraphQLExceptionMapper();
        exceptionHandler = new GraphQLExceptionHandler();

        // Inject the mapper into the handler
        ReflectionTestUtils.setField(exceptionHandler, "exceptionMapper", exceptionMapper);
        ReflectionTestUtils.setField(exceptionHandler, "includeStackTrace", false);
        ReflectionTestUtils.setField(exceptionHandler, "includeDebugInfo", true);

        // Mock environment
        when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);
        when(environment.getField()).thenReturn(field);
        when(environment.getOperationDefinition()).thenReturn(operationDefinition);
        when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("testField"));
        when(field.getName()).thenReturn("testField");
        when(field.getSourceLocation()).thenReturn(null);
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.QUERY);
    }

    @Test
    void shouldMapBusinessExceptionsConsistently() {
        // Test ExceptionNotFoundException
        ExceptionNotFoundException notFoundEx = new ExceptionNotFoundException("Exception not found");
        GraphQLError notFoundError = exceptionHandler.resolveToSingleError(notFoundEx, environment);

        assertThat(notFoundError).isNotNull();
        assertThat(notFoundError.getErrorType()).isEqualTo(GraphQLErrorType.NOT_FOUND);
        assertThat(notFoundError.getMessage()).contains("requested resource was not found");
        assertThat(notFoundError.getExtensions().get("httpStatus")).isEqualTo(404);
        assertThat(notFoundError.getExtensions().get("code")).isEqualTo("NOT_FOUND");

        // Test RetryNotAllowedException
        RetryNotAllowedException retryEx = new RetryNotAllowedException("Retry not allowed");
        GraphQLError retryError = exceptionHandler.resolveToSingleError(retryEx, environment);

        assertThat(retryError).isNotNull();
        assertThat(retryError.getErrorType()).isEqualTo(GraphQLErrorType.BUSINESS_RULE_ERROR);
        assertThat(retryError.getMessage()).contains("cannot be retried");
        assertThat(retryError.getExtensions().get("httpStatus")).isEqualTo(400);
        assertThat(retryError.getExtensions().get("code")).isEqualTo("BUSINESS_RULE_ERROR");

        // Test ExceptionProcessingException
        ExceptionProcessingException processingEx = new ExceptionProcessingException("Processing failed");
        GraphQLError processingError = exceptionHandler.resolveToSingleError(processingEx, environment);

        assertThat(processingError).isNotNull();
        assertThat(processingError.getErrorType()).isEqualTo(GraphQLErrorType.BUSINESS_RULE_ERROR);
        assertThat(processingError.getMessage()).contains("business rules");
        assertThat(processingError.getExtensions().get("httpStatus")).isEqualTo(400);
    }

    @Test
    void shouldHandleValidationErrorsConsistently() {
        // Test IllegalArgumentException
        IllegalArgumentException validationEx = new IllegalArgumentException("Invalid input parameter");
        GraphQLError validationError = exceptionHandler.resolveToSingleError(validationEx, environment);

        assertThat(validationError).isNotNull();
        assertThat(validationError.getErrorType()).isEqualTo(GraphQLErrorType.VALIDATION_ERROR);
        assertThat(validationError.getMessage()).contains("provided input is invalid");
        assertThat(validationError.getExtensions().get("httpStatus")).isEqualTo(400);
        assertThat(validationError.getExtensions().get("code")).isEqualTo("VALIDATION_ERROR");

        // Test ConstraintViolationException
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Field is required");
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("fieldName");
        when(violation.getInvalidValue()).thenReturn("invalidValue");

        ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(descriptor.getAnnotation()).thenReturn(mock(jakarta.validation.constraints.NotNull.class));
        when(descriptor.getAnnotation().annotationType()).thenReturn(jakarta.validation.constraints.NotNull.class);

        ConstraintViolationException constraintEx = new ConstraintViolationException("Validation failed",
                Set.of(violation));
        GraphQLError constraintError = exceptionHandler.resolveToSingleError(constraintEx, environment);

        assertThat(constraintError).isNotNull();
        assertThat(constraintError.getErrorType()).isEqualTo(GraphQLErrorType.VALIDATION_ERROR);
        assertThat(constraintError.getMessage()).contains("Validation failed");
        assertThat(constraintError.getExtensions()).containsKey("violations");
    }

    @Test
    void shouldHandleAuthorizationErrorsSecurely() {
        AccessDeniedException authEx = new AccessDeniedException("Access denied");
        GraphQLError authError = exceptionHandler.resolveToSingleError(authEx, environment);

        assertThat(authError).isNotNull();
        assertThat(authError.getErrorType()).isEqualTo(GraphQLErrorType.AUTHORIZATION_ERROR);
        assertThat(authError.getMessage()).contains("don't have permission");
        assertThat(authError.getExtensions().get("httpStatus")).isEqualTo(403);
        assertThat(authError.getExtensions().get("code")).isEqualTo("AUTHORIZATION_ERROR");

        // Verify no sensitive information is leaked
        assertThat(authError.getMessage()).doesNotContain("token");
        assertThat(authError.getMessage()).doesNotContain("secret");
        assertThat(authError.getMessage()).doesNotContain("password");
    }

    @Test
    void shouldHandleExternalServiceErrorsWithRetryInfo() {
        ResourceAccessException serviceEx = new ResourceAccessException("Service unavailable");
        GraphQLError serviceError = exceptionHandler.resolveToSingleError(serviceEx, environment);

        assertThat(serviceError).isNotNull();
        assertThat(serviceError.getErrorType()).isEqualTo(GraphQLErrorType.EXTERNAL_SERVICE_ERROR);
        assertThat(serviceError.getMessage()).contains("external service is currently unavailable");
        assertThat(serviceError.getExtensions().get("httpStatus")).isEqualTo(500);
        assertThat(serviceError.getExtensions().get("code")).isEqualTo("EXTERNAL_SERVICE_ERROR");
        assertThat(serviceError.getExtensions().get("retryable")).isEqualTo(true);
        assertThat(serviceError.getExtensions().get("retryAfter")).isEqualTo(30);
    }

    @Test
    void shouldHandleTimeoutErrorsGracefully() {
        TimeoutException timeoutEx = new TimeoutException("Operation timed out");
        GraphQLError timeoutError = exceptionHandler.resolveToSingleError(timeoutEx, environment);

        assertThat(timeoutError).isNotNull();
        assertThat(timeoutError.getErrorType()).isEqualTo(GraphQLErrorType.TIMEOUT_ERROR);
        assertThat(timeoutError.getMessage()).contains("took too long to complete");
        assertThat(timeoutError.getExtensions().get("httpStatus")).isEqualTo(504);
        assertThat(timeoutError.getExtensions().get("code")).isEqualTo("TIMEOUT_ERROR");
        assertThat(timeoutError.getExtensions().get("retryable")).isEqualTo(true);
        assertThat(timeoutError.getExtensions().get("retryAfter")).isEqualTo(15);
    }

    @Test
    void shouldHandleDatabaseErrorsSecurely() {
        DataAccessException dbEx = new DataAccessException("Database connection failed") {
        };
        GraphQLError dbError = exceptionHandler.resolveToSingleError(dbEx, environment);

        assertThat(dbError).isNotNull();
        assertThat(dbError.getErrorType()).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);
        assertThat(dbError.getMessage()).contains("internal error occurred");
        assertThat(dbError.getExtensions().get("httpStatus")).isEqualTo(500);
        assertThat(dbError.getExtensions().get("code")).isEqualTo("INTERNAL_ERROR");

        // Verify database details are not leaked
        assertThat(dbError.getMessage()).doesNotContain("database");
        assertThat(dbError.getMessage()).doesNotContain("connection");
        assertThat(dbError.getMessage()).doesNotContain("SQL");
    }

    @Test
    void shouldSanitizeSensitiveInformationInErrorMessages() {
        IllegalArgumentException sensitiveEx = new IllegalArgumentException(
                "Authentication failed with password=secret123 and token=abc123xyz and api_key=key456");
        GraphQLError sanitizedError = exceptionHandler.resolveToSingleError(sensitiveEx, environment);

        assertThat(sanitizedError).isNotNull();
        assertThat(sanitizedError.getMessage()).contains("password=***");
        assertThat(sanitizedError.getMessage()).contains("token=***");
        assertThat(sanitizedError.getMessage()).contains("api_key=***");
        assertThat(sanitizedError.getMessage()).doesNotContain("secret123");
        assertThat(sanitizedError.getMessage()).doesNotContain("abc123xyz");
        assertThat(sanitizedError.getMessage()).doesNotContain("key456");
    }

    @Test
    void shouldProvideStructuredErrorInformation() {
        RuntimeException genericEx = new RuntimeException("Generic error");
        GraphQLError structuredError = exceptionHandler.resolveToSingleError(genericEx, environment);

        assertThat(structuredError).isNotNull();
        assertThat(structuredError.getErrorType()).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);

        Map<String, Object> extensions = structuredError.getExtensions();
        assertThat(extensions).containsKey("errorId");
        assertThat(extensions).containsKey("code");
        assertThat(extensions).containsKey("classification");
        assertThat(extensions).containsKey("timestamp");
        assertThat(extensions).containsKey("httpStatus");
        assertThat(extensions).containsKey("operation");
        assertThat(extensions).containsKey("fieldName");
        assertThat(extensions).containsKey("retryable");

        assertThat(extensions.get("code")).isEqualTo("INTERNAL_ERROR");
        assertThat(extensions.get("classification")).isEqualTo("INTERNAL_ERROR");
        assertThat(extensions.get("httpStatus")).isEqualTo(500);
        assertThat(extensions.get("operation")).isEqualTo("QUERY");
        assertThat(extensions.get("fieldName")).isEqualTo("testField");
    }

    @Test
    void shouldProvideConsistentErrorFormatAcrossErrorTypes() {
        // Test multiple error types to ensure consistent structure
        Exception[] exceptions = {
                new ExceptionNotFoundException("Not found"),
                new IllegalArgumentException("Invalid argument"),
                new AccessDeniedException("Access denied"),
                new RuntimeException("Internal error"),
                new ResourceAccessException("Service error")
        };

        for (Exception ex : exceptions) {
            GraphQLError error = exceptionHandler.resolveToSingleError(ex, environment);

            // All errors should have consistent structure
            assertThat(error).isNotNull();
            assertThat(error.getMessage()).isNotNull().isNotEmpty();
            assertThat(error.getErrorType()).isNotNull();
            assertThat(error.getPath()).isNotNull();

            Map<String, Object> extensions = error.getExtensions();
            assertThat(extensions).containsKey("errorId");
            assertThat(extensions).containsKey("code");
            assertThat(extensions).containsKey("timestamp");
            assertThat(extensions).containsKey("httpStatus");
            assertThat(extensions).containsKey("retryable");

            // Error ID should be a valid UUID format
            String errorId = (String) extensions.get("errorId");
            assertThat(errorId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

            // HTTP status should be valid
            Integer httpStatus = (Integer) extensions.get("httpStatus");
            assertThat(httpStatus).isBetween(400, 599);
        }
    }

    @Test
    void shouldNotLeakStackTracesInProduction() {
        // Set production-like configuration
        ReflectionTestUtils.setField(exceptionHandler, "includeStackTrace", false);
        ReflectionTestUtils.setField(exceptionHandler, "includeDebugInfo", false);

        RuntimeException internalEx = new RuntimeException("Internal system error");
        GraphQLError productionError = exceptionHandler.resolveToSingleError(internalEx, environment);

        assertThat(productionError).isNotNull();
        assertThat(productionError.getExtensions()).doesNotContainKey("stackTrace");
        assertThat(productionError.getExtensions()).doesNotContainKey("originalMessage");
        assertThat(productionError.getMessage()).doesNotContain("at com.arcone");
        assertThat(productionError.getMessage()).doesNotContain("Caused by");
    }

    @Test
    void shouldIncludeDebugInfoInDevelopment() {
        // Set development-like configuration
        ReflectionTestUtils.setField(exceptionHandler, "includeStackTrace", true);
        ReflectionTestUtils.setField(exceptionHandler, "includeDebugInfo", true);

        RuntimeException debugEx = new RuntimeException("Debug error");
        GraphQLError debugError = exceptionHandler.resolveToSingleError(debugEx, environment);

        assertThat(debugError).isNotNull();
        Map<String, Object> extensions = debugError.getExtensions();
        assertThat(extensions).containsKey("exceptionType");
        assertThat(extensions).containsKey("originalMessage");
        assertThat(extensions).containsKey("stackTrace");

        assertThat(extensions.get("exceptionType")).isEqualTo("RuntimeException");
    }
}