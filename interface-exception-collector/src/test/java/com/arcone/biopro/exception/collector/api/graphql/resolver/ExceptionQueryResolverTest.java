package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLExceptionService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExceptionQueryResolver.
 * Tests GraphQL query operations for exceptions with various filtering and
 * pagination scenarios.
 */
@ExtendWith(MockitoExtension.class)
class ExceptionQueryResolverTest {

        @Mock
        private GraphQLExceptionService graphQLExceptionService;

        @InjectMocks
        private ExceptionQueryResolver exceptionQueryResolver;

        private InterfaceException testException;
        private ExceptionConnection testConnection;

        @BeforeEach
        void setUp() {
                testException = InterfaceException.builder()
                                .id(1L)
                                .transactionId("TXN-001")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Test exception")
                                .operation("CREATE_ORDER")
                                .status(ExceptionStatus.NEW)
                                .severity(ExceptionSeverity.HIGH)
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .retryable(true)
                                .retryCount(0)
                                .build();

                ExceptionConnection.ExceptionEdge edge = ExceptionConnection.ExceptionEdge.builder()
                                .node(testException)
                                .cursor("cursor-1")
                                .build();

                ExceptionConnection.PageInfo pageInfo = ExceptionConnection.PageInfo.builder()
                                .hasNextPage(false)
                                .hasPreviousPage(false)
                                .startCursor("cursor-1")
                                .endCursor("cursor-1")
                                .build();

                testConnection = ExceptionConnection.builder()
                                .edges(List.of(edge))
                                .pageInfo(pageInfo)
                                .totalCount(1L)
                                .build();
        }

        @Test
        void exceptions_WithNoFilters_ShouldReturnAllExceptions() {
                // Given
                when(graphQLExceptionService.findExceptions(any(), any(), any()))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When
                CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, null, null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isEqualTo(testConnection);
                verify(graphQLExceptionService).findExceptions(null, null, null);
        }

        @Test
        void exceptions_WithFilters_ShouldApplyFiltersCorrectly() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .interfaceTypes(List.of(InterfaceType.ORDER))
                                .statuses(List.of(ExceptionStatus.NEW))
                                .severities(List.of(ExceptionSeverity.HIGH))
                                .build();

                when(graphQLExceptionService.findExceptions(eq(filters), any(), any()))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When
                CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(filters, null, null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isEqualTo(testConnection);
                verify(graphQLExceptionService).findExceptions(eq(filters), any(), any());
        }

        @Test
        void exceptions_WithPagination_ShouldApplyPaginationCorrectly() {
                // Given
                PaginationInput pagination = PaginationInput.builder()
                                .first(10)
                                .after("cursor-after")
                                .build();

                when(graphQLExceptionService.findExceptions(any(), eq(pagination), any()))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When
                CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, pagination,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isEqualTo(testConnection);
                verify(graphQLExceptionService).findExceptions(any(), eq(pagination), any());
        }

        @Test
        void exceptions_WithSorting_ShouldApplySortingCorrectly() {
                // Given
                SortingInput sorting = SortingInput.builder()
                                .field("timestamp")
                                .direction(SortingInput.SortDirection.DESC)
                                .build();

                when(graphQLExceptionService.findExceptions(any(), any(), eq(sorting)))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When
                CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, null, sorting);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isEqualTo(testConnection);
                verify(graphQLExceptionService).findExceptions(any(), any(), eq(sorting));
        }

        @Test
        void exceptions_WithInvalidPagination_ShouldThrowException() {
                // Given
                PaginationInput invalidPagination = PaginationInput.builder()
                                .first(10)
                                .last(5) // Both forward and backward pagination
                                .build();

                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exceptions(null, invalidPagination, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Cannot use both forward pagination");
        }

        @Test
        void exceptions_WithNegativePageSize_ShouldThrowException() {
                // Given
                PaginationInput invalidPagination = PaginationInput.builder()
                                .first(-1)
                                .build();

                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exceptions(null, invalidPagination, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("'first' parameter must be positive");
        }

        @Test
        void exceptions_WithInvalidSortField_ShouldThrowException() {
                // Given
                SortingInput invalidSorting = SortingInput.builder()
                                .field("invalidField")
                                .direction(SortingInput.SortDirection.ASC)
                                .build();

                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exceptions(null, null, invalidSorting))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Invalid sort field");
        }

        @Test
        void exception_WithValidTransactionId_ShouldReturnException() {
                // Given
                String transactionId = "TXN-001";
                when(graphQLExceptionService.findExceptionByTransactionId(transactionId))
                                .thenReturn(CompletableFuture.completedFuture(Optional.of(testException)));

                // When
                CompletableFuture<InterfaceException> result = exceptionQueryResolver.exception(transactionId);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isEqualTo(testException);
                verify(graphQLExceptionService).findExceptionByTransactionId(transactionId);
        }

        @Test
        void exception_WithNonExistentTransactionId_ShouldReturnNull() {
                // Given
                String transactionId = "NON-EXISTENT";
                when(graphQLExceptionService.findExceptionByTransactionId(transactionId))
                                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

                // When
                CompletableFuture<InterfaceException> result = exceptionQueryResolver.exception(transactionId);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                assertThat(result.join()).isNull();
                verify(graphQLExceptionService).findExceptionByTransactionId(transactionId);
        }

        @Test
        void exception_WithBlankTransactionId_ShouldThrowException() {
                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exception(""))
                                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        }

        @Test
        void exception_WithNullTransactionId_ShouldThrowException() {
                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exception(null))
                                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        }

        @Test
        void exception_WithTooLongTransactionId_ShouldThrowException() {
                // Given
                String longTransactionId = "A".repeat(256); // Exceeds 255 character limit

                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exception(longTransactionId))
                                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        }

        @Test
        void exceptions_WithValidSortFields_ShouldAcceptAllValidFields() {
                // Given
                String[] validFields = {
                                "timestamp", "processedAt", "severity", "status",
                                "interfaceType", "customerId", "retryCount", "acknowledgedAt", "resolvedAt"
                };

                when(graphQLExceptionService.findExceptions(any(), any(), any()))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When & Then
                for (String field : validFields) {
                        SortingInput sorting = SortingInput.builder()
                                        .field(field)
                                        .direction(SortingInput.SortDirection.ASC)
                                        .build();

                        // Should not throw exception
                        CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, null,
                                        sorting);
                        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                }
        }

        @Test
        void exceptions_WithMaxPageSize_ShouldAcceptValidPageSize() {
                // Given
                PaginationInput pagination = PaginationInput.builder()
                                .first(100) // Maximum allowed
                                .build();

                when(graphQLExceptionService.findExceptions(any(), eq(pagination), any()))
                                .thenReturn(CompletableFuture.completedFuture(testConnection));

                // When
                CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, pagination,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                verify(graphQLExceptionService).findExceptions(any(), eq(pagination), any());
        }

        @Test
        void exceptions_WithExcessivePageSize_ShouldThrowException() {
                // Given
                PaginationInput pagination = PaginationInput.builder()
                                .first(101) // Exceeds maximum
                                .build();

                // When & Then
                assertThatThrownBy(() -> exceptionQueryResolver.exceptions(null, pagination, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Page size cannot exceed 100 items");
        }
}