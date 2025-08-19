package com.arcone.biopro.exception.collector.api.graphql.exception;

import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.language.Field;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQLExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLExceptionHandlerTest {

    @Mock
    private GraphQLExceptionMapper exceptionMapper;

    @Mock
    private DataFetchingEnvironment environment;

    @Mock
    private ExecutionStepInfo executionStepInfo;

    @Mock
    private Field field;

    @InjectMocks
    private GraphQLExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // Set up test configuration
        ReflectionTestUtils.setField(exceptionHandler, "includeStackTrace", false);
        ReflectionTestUtils.setField(exceptionHandler, "includeDebugInfo", true);

        // Mock environment
        when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);
        when(environment.getField()).thenReturn(field);
        when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("testField"));
        when(field.getName()).thenReturn("testField");
        when(field.getSourceLocation()).thenReturn(null);

        // Mock operation definition
        graphql.language.OperationDefinition operationDefinition = mock(graphql.language.OperationDefinition.class);
        when(environment.getOperationDefinition()).thenReturn(operationDefinition);
        when(operationDefinition.getOperation()).thenReturn(graphql.language.OperationDefinition.Operation.QUERY);
    }

    @Test
    void shouldHandleValidationError() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.VALIDATION_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.VALIDATION_ERROR, exception))
                .thenReturn("The provided input is invalid. Please check your request and try again.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("The provided input is invalid");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.VALIDATION_ERROR);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions).containsKey("errorId");
        assertThat(extensions).containsKey("code");
        assertThat(extensions).containsKey("timestamp");
        assertThat(extensions.get("code")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldHandleConstraintViolationError() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Field is required");
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("fieldName");
        when(violation.getInvalidValue()).thenReturn("invalidValue");
        when(violation.getConstraintDescriptor())
                .thenReturn(mock(jakarta.validation.metadata.ConstraintDescriptor.class));
        when(violation.getConstraintDescriptor().getAnnotation())
                .thenReturn(mock(java.lang.annotation.Annotation.class));
        when(violation.getConstraintDescriptor().getAnnotation().annotationType())
                .thenReturn((Class) jakarta.validation.constraints.NotNull.class);

        ConstraintViolationException exception = new ConstraintViolationException("Validation failed",
                Set.of(violation));
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.VALIDATION_ERROR);

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Validation failed");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.VALIDATION_ERROR);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions).containsKey("violations");
    }

    @Test
    void shouldHandleAuthorizationError() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.AUTHORIZATION_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.AUTHORIZATION_ERROR, exception))
                .thenReturn("You don't have permission to perform this operation.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("You don't have permission");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.AUTHORIZATION_ERROR);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions.get("code")).isEqualTo("AUTHORIZATION_ERROR");
        assertThat(extensions.get("httpStatus")).isEqualTo(403);
    }

    @Test
    void shouldHandleNotFoundError() {
        // Given
        ExceptionNotFoundException exception = new ExceptionNotFoundException("Exception not found");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.NOT_FOUND);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.NOT_FOUND, exception))
                .thenReturn("The requested resource was not found.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("The requested resource was not found");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.NOT_FOUND);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions.get("code")).isEqualTo("NOT_FOUND");
        assertThat(extensions.get("httpStatus")).isEqualTo(404);
    }

    @Test
    void shouldHandleBusinessRuleError() {
        // Given
        RetryNotAllowedException exception = new RetryNotAllowedException("Retry not allowed");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.BUSINESS_RULE_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.BUSINESS_RULE_ERROR, exception))
                .thenReturn("This exception cannot be retried at this time.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("This exception cannot be retried");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.BUSINESS_RULE_ERROR);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions.get("code")).isEqualTo("BUSINESS_RULE_ERROR");
        assertThat(extensions.get("httpStatus")).isEqualTo(400);
    }

    @Test
    void shouldHandleInternalError() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.INTERNAL_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.INTERNAL_ERROR, exception))
                .thenReturn("An internal error occurred. Please try again later.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("An internal error occurred");
        assertThat(result.getErrorType()).isEqualTo(GraphQLErrorType.INTERNAL_ERROR);

        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions.get("code")).isEqualTo("INTERNAL_ERROR");
        assertThat(extensions.get("httpStatus")).isEqualTo(500);
        assertThat(extensions.get("retryable")).isEqualTo(true);
    }

    @Test
    void shouldSanitizeErrorMessages() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException(
                "Error with password=secret123 and token=abc123");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.VALIDATION_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.VALIDATION_ERROR, exception))
                .thenReturn("Error with password=secret123 and token=abc123");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        assertThat(result.getMessage()).contains("password=***");
        assertThat(result.getMessage()).contains("token=***");
        assertThat(result.getMessage()).doesNotContain("secret123");
        assertThat(result.getMessage()).doesNotContain("abc123");
    }

    @Test
    void shouldIncludeRetryInformationForRetryableErrors() {
        // Given
        RuntimeException exception = new RuntimeException("Service unavailable");
        when(exceptionMapper.mapExceptionToErrorType(exception))
                .thenReturn(GraphQLErrorType.EXTERNAL_SERVICE_ERROR);
        when(exceptionMapper.getUserFriendlyMessage(GraphQLErrorType.EXTERNAL_SERVICE_ERROR, exception))
                .thenReturn("An external service is currently unavailable. Please try again later.");

        // When
        GraphQLError result = exceptionHandler.resolveToSingleError(exception, environment);

        // Then
        Map<String, Object> extensions = result.getExtensions();
        assertThat(extensions.get("retryable")).isEqualTo(true);
        assertThat(extensions.get("retryAfter")).isEqualTo(30); // 30 seconds for external service errors
    }
}