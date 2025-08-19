package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQLValidationService.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLValidationServiceTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private GraphQLValidationService validationService;

    @BeforeEach
    void setUp() {
        // Default to no violations for most tests
        when(validator.validate(any())).thenReturn(Collections.emptySet());
    }

    @Test
    void shouldValidateValidExceptionFilters() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
                .interfaceTypes(List.of(InterfaceType.ORDER))
                .statuses(List.of(ExceptionStatus.OPEN))
                .searchTerm("test")
                .build();

        // When & Then
        validationService.validateExceptionFilters(filters); // Should not throw
    }

    @Test
    void shouldRejectInvalidExceptionFilters() {
        // Given
        ConstraintViolation<ExceptionFilters> violation = createMockViolation("Search term too long");
        when(validator.validate(any(ExceptionFilters.class))).thenReturn(Set.of(violation));

        ExceptionFilters filters = ExceptionFilters.builder()
                .searchTerm("a".repeat(101)) // Too long
                .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validateExceptionFilters(filters))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Exception filters validation failed");
    }

    @Test
    void shouldValidateValidPaginationInput() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(20)
                .build();

        // When & Then
        validationService.validatePaginationInput(pagination); // Should not throw
}

@Test
    void shouldRejectInvalidPaginationInput() {
        // Given
        ConstraintViolation<PaginationInput> violation = createMockViolation("Page size too large");
        when(validator.validate(any(PaginationInput.class))).thenReturn(Set.of(violation));

        PaginationInput pagination = PaginationInput.builder()
            .first(1000) // Too large
            .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validatePaginationInput(pagination))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("Pagination validation failed");
    }

    @Test
    void shouldValidateValidRetryExceptionInput() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("valid-transaction-id")
            .reason("Valid retry reason with sufficient detail")
            .priority(RetryExceptionInput.RetryPriority.NORMAL)
            .build();

        // When & Then
        validationService.validateRetryExceptionInput(input); // Should not throw
    }

    @Test
    void shouldRejectNullRetryExceptionInput() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateRetryExceptionInput(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Retry input cannot be null");
    }

    @Test
    void shouldRejectInvalidRetryExceptionInput() {
        // Given
        ConstraintViolation<RetryExceptionInput> violation = createMockViolation("Transaction ID required");
        when(validator.validate(any(RetryExceptionInput.class))).thenReturn(Set.of(violation));

        RetryExceptionInput input = RetryExceptionInput.builder()
            .reason("Valid reason")
            .priority(RetryExceptionInput.RetryPriority.NORMAL)
            .build(); // Missing transaction ID

        // When & Then
        assertThatThrownBy(() -> validationService.validateRetryExceptionInput(input))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("Retry input validation failed");
    }

    @Test
    void shouldRejectShortRetryReason() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("valid-transaction-id")
            .reason("abc") // Too short
            .priority(RetryExceptionInput.RetryPriority.NORMAL)
            .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validateRetryExceptionInput(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Retry reason must be at least 5 characters long");
    }

    @Test
    void shouldRejectRepeatedCharacterReason() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("valid-transaction-id")
            .reason("aaaaaaaaaa") // Repeated characters
            .priority(RetryExceptionInput.RetryPriority.NORMAL)
            .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validateRetryExceptionInput(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Retry reason must contain meaningful content");
    }

    @Test
    void shouldValidateValidTransactionId() {
        // When & Then
        validationService.validateTransactionId("valid-transaction-id-123"); // Should not throw
    }

    @Test
    void shouldRejectNullTransactionId() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateTransactionId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transaction ID is required");
    }

    @Test
    void shouldRejectEmptyTransactionId() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateTransactionId("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transaction ID is required");
    }

    @Test
    void shouldRejectTooShortTransactionId() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateTransactionId("abc123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transaction ID must be between 8 and 64 characters");
    }

    @Test
    void shouldRejectTooLongTransactionId() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateTransactionId("a".repeat(65)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transaction ID must be between 8 and 64 characters");
    }

    @Test
    void shouldRejectTransactionIdWithInvalidCharacters() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateTransactionId("invalid@transaction#id"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transaction ID contains invalid characters");
    }

    @Test
    void shouldValidateValidSearchTerm() {
        // When & Then
        validationService.validateSearchTerm("valid search term"); // Should not throw
    }

    @Test
    void shouldAllowEmptySearchTerm() {
        // When & Then
        validationService.validateSearchTerm(""); // Should not throw
        validationService.validateSearchTerm(null); // Should not throw
    }

    @Test
    void shouldRejectTooLongSearchTerm() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchTerm("a".repeat(101)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Search term cannot exceed 100 characters");
    }

    @Test
    void shouldRejectSearchTermWithSqlInjection() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchTerm("'; DROP TABLE users; --"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Search term contains invalid patterns");
    }

    @Test
    void shouldRejectSearchTermWithTooManyWildcards() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchTerm("*****search******"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Search term contains too many wildcards");
    }

    @Test
    void shouldRejectComplexFilterCombinations() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
            .dateRange(ExceptionFilters.DateRangeInput.builder()
                .from(OffsetDateTime.now().minusDays(100)) // Too long range
                .to(OffsetDateTime.now())
                .build())
            .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validateExceptionFilters(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Date range cannot exceed 90 days");
    }

    @SuppressWarnings("unchecked")
    private <T> ConstraintViolation<T> createMockViolation(String message) {
        ConstraintViolation<T> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}