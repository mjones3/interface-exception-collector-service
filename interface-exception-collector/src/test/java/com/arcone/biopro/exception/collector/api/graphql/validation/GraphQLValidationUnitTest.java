package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.TimeRange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQL validation logic including input validation,
 * business rule validation, and custom validators.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLValidationUnitTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private GraphQLValidationService validationService;

    @InjectMocks
    private PaginationValidator paginationValidator;

    @InjectMocks
    private DateRangeValidator dateRangeValidator;

    @InjectMocks
    private RetryOperationValidator retryOperationValidator;

    @InjectMocks
    private TransactionIdValidator transactionIdValidator;

    @BeforeEach
    void setUp() {
        // Mock validator to return no violations by default
        when(validator.validate(any())).thenReturn(Set.of());
    }

    @Test
    void validatePaginationInput_WithValidForwardPagination_ShouldPass() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(10)
                .after("cursor-123")
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isTrue();
    }

    @Test
    void validatePaginationInput_WithValidBackwardPagination_ShouldPass() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .last(10)
                .before("cursor-123")
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isTrue();
    }

    @Test
    void validatePaginationInput_WithBothDirections_ShouldFail() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(10)
                .last(5)
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isFalse();
    }

    @Test
    void validatePaginationInput_WithNegativePageSize_ShouldFail() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(-1)
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isFalse();
    }

    @Test
    void validatePaginationInput_WithExcessivePageSize_ShouldFail() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(101) // Exceeds maximum of 100
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isFalse();
    }

    @Test
    void validatePaginationInput_WithZeroPageSize_ShouldFail() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(0)
                .build();

        // When & Then
        assertThat(paginationValidator.isValid(pagination, null)).isFalse();
    }

    @Test
    void validateDateRange_WithValidRange_ShouldPass() {
        // Given
        TimeRange timeRange = TimeRange.builder()
                .startDate(OffsetDateTime.now().minusDays(7))
                .endDate(OffsetDateTime.now())
                .build();

        // When & Then
        assertThat(dateRangeValidator.isValid(timeRange, null)).isTrue();
    }

    @Test
    void validateDateRange_WithStartAfterEnd_ShouldFail() {
        // Given
        TimeRange timeRange = TimeRange.builder()
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().minusDays(1))
                .build();

        // When & Then
        assertThat(dateRangeValidator.isValid(timeRange, null)).isFalse();
    }

    @Test
    void validateDateRange_WithExcessiveRange_ShouldFail() {
        // Given
        TimeRange timeRange = TimeRange.builder()
                .startDate(OffsetDateTime.now().minusDays(400)) // Exceeds 365 day limit
                .endDate(OffsetDateTime.now())
                .build();

        // When & Then
        assertThat(dateRangeValidator.isValid(timeRange, null)).isFalse();
    }

    @Test
    void validateDateRange_WithFutureStartDate_ShouldFail() {
        // Given
        TimeRange timeRange = TimeRange.builder()
                .startDate(OffsetDateTime.now().plusDays(1))
                .endDate(OffsetDateTime.now().plusDays(2))
                .build();

        // When & Then
        assertThat(dateRangeValidator.isValid(timeRange, null)).isFalse();
    }

    @Test
    void validateDateRange_WithNullDates_ShouldPass() {
        // Given
        TimeRange timeRange = TimeRange.builder().build();

        // When & Then
        assertThat(dateRangeValidator.isValid(timeRange, null)).isTrue();
    }

    @Test
    void validateTransactionId_WithValidId_ShouldPass() {
        // Given
        String transactionId = "TXN-12345";

        // When & Then
        assertThat(transactionIdValidator.isValid(transactionId, null)).isTrue();
    }

    @Test
    void validateTransactionId_WithBlankId_ShouldFail() {
        // Given
        String transactionId = "";

        // When & Then
        assertThat(transactionIdValidator.isValid(transactionId, null)).isFalse();
    }

    @Test
    void validateTransactionId_WithNullId_ShouldFail() {
        // Given
        String transactionId = null;

        // When & Then
        assertThat(transactionIdValidator.isValid(transactionId, null)).isFalse();
    }

    @Test
    void validateTransactionId_WithTooLongId_ShouldFail() {
        // Given
        String transactionId = "A".repeat(256); // Exceeds 255 character limit

        // When & Then
        assertThat(transactionIdValidator.isValid(transactionId, null)).isFalse();
    }

    @Test
    void validateTransactionId_WithInvalidCharacters_ShouldFail() {
        // Given
        String transactionId = "TXN-123<script>";

        // When & Then
        assertThat(transactionIdValidator.isValid(transactionId, null)).isFalse();
    }

    @Test
    void validateRetryOperation_WithValidInput_ShouldPass() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("Manual retry requested")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        // When & Then
        assertThat(retryOperationValidator.isValid(input, null)).isTrue();
    }

    @Test
    void validateRetryOperation_WithBlankReason_ShouldFail() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        // When & Then
        assertThat(retryOperationValidator.isValid(input, null)).isFalse();
    }

    @Test
    void validateRetryOperation_WithTooLongReason_ShouldFail() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("A".repeat(1001)) // Exceeds 1000 character limit
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        // When & Then
        assertThat(retryOperationValidator.isValid(input, null)).isFalse();
    }

    @Test
    void validateRetryOperation_WithNullPriority_ShouldFail() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("Manual retry requested")
                .priority(null)
                .build();

        // When & Then
        assertThat(retryOperationValidator.isValid(input, null)).isFalse();
    }

    @Test
    void validateSortingInput_WithValidField_ShouldPass() {
        // Given
        SortingInput sorting = SortingInput.builder()
                .field("timestamp")
                .direction(SortingInput.SortDirection.DESC)
                .build();

        // When
        boolean result = validationService.isValidSortField(sorting.getField());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateSortingInput_WithInvalidField_ShouldFail() {
        // Given
        SortingInput sorting = SortingInput.builder()
                .field("invalidField")
                .direction(SortingInput.SortDirection.DESC)
                .build();

        // When
        boolean result = validationService.isValidSortField(sorting.getField());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateSortingInput_WithAllValidFields_ShouldPass() {
        // Given
        String[] validFields = {
                "timestamp", "processedAt", "severity", "status",
                "interfaceType", "customerId", "retryCount", "acknowledgedAt"
        };

        // When & Then
        for (String field : validFields) {
            assertThat(validationService.isValidSortField(field))
                    .as("Field %s should be valid", field)
                    .isTrue();
        }
    }

    @Test
    void validateExceptionFilters_WithValidFilters_ShouldPass() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
                .interfaceTypes(List.of(InterfaceType.ORDER, InterfaceType.COLLECTION))
                .statuses(List.of(ExceptionStatus.NEW, ExceptionStatus.ACKNOWLEDGED))
                .severities(List.of(ExceptionSeverity.HIGH, ExceptionSeverity.MEDIUM))
                .customerIds(List.of("CUST-001", "CUST-002"))
                .locationCodes(List.of("LOC-001", "LOC-002"))
                .searchTerm("order failed")
                .excludeResolved(true)
                .retryable(true)
                .build();

        // When
        Set<ConstraintViolation<ExceptionFilters>> violations = validator.validate(filters);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validateExceptionFilters_WithTooManyFilters_ShouldFail() {
        // Given
        List<String> tooManyCustomers = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> "CUST-" + String.format("%03d", i))
                .toList();

        ExceptionFilters filters = ExceptionFilters.builder()
                .customerIds(tooManyCustomers) // Exceeds limit of 100
                .build();

        // When
        boolean result = validationService.validateFilterLimits(filters);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateExceptionFilters_WithInvalidSearchTerm_ShouldFail() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
                .searchTerm("<script>alert('xss')</script>") // Contains invalid characters
                .build();

        // When
        boolean result = validationService.isValidSearchTerm(filters.getSearchTerm());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateExceptionFilters_WithValidSearchTerm_ShouldPass() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
                .searchTerm("order processing failed customer ABC-123")
                .build();

        // When
        boolean result = validationService.isValidSearchTerm(filters.getSearchTerm());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateInput_WithConstraintViolations_ShouldThrowException() {
        // Given
        ConstraintViolation<Object> violation = createMockViolation("transactionId", "cannot be blank");
        when(validator.validate(any())).thenReturn(Set.of(violation));

        PaginationInput invalidInput = PaginationInput.builder()
                .first(-1)
                .build();

        // When & Then
        assertThatThrownBy(() -> validationService.validateInput(invalidInput))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    void validateInput_WithValidInput_ShouldNotThrow() {
        // Given
        PaginationInput validInput = PaginationInput.builder()
                .first(10)
                .build();

        // When & Then - Should not throw exception
        validationService.validateInput(validInput);
    }

    @Test
    void validateBusinessRules_WithValidRetryRequest_ShouldPass() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("Manual retry after fixing data issue")
                .priority(RetryExceptionInput.RetryPriority.HIGH)
                .build();

        // When
        boolean result = validationService.validateRetryBusinessRules(input);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateBusinessRules_WithSuspiciousReason_ShouldFail() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-12345")
                .reason("test") // Too short/generic
                .priority(RetryExceptionInput.RetryPriority.HIGH)
                .build();

        // When
        boolean result = validationService.validateRetryBusinessRules(input);

        // Then
        assertThat(result).isFalse();
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Object> createMockViolation(String propertyPath, String message) {
        ConstraintViolation<Object> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(org.mockito.Mockito.mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}