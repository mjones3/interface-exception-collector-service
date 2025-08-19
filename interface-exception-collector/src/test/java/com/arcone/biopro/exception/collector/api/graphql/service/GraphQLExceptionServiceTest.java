package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQLExceptionService.
 * Tests the service layer logic for GraphQL exception queries.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLExceptionServiceTest {

        @Mock
        private InterfaceExceptionRepository exceptionRepository;

        @InjectMocks
        private GraphQLExceptionService graphQLExceptionService;

        private InterfaceException testException;
        private Page<InterfaceException> testPage;

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

                testPage = new PageImpl<>(List.of(testException), PageRequest.of(0, 20), 1);
        }

        @Test
        void findExceptions_WithNoFilters_ShouldReturnAllExceptions() {
                // Given
                when(exceptionRepository.findAll(any(Pageable.class))).thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(null, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);
                assertThat(connection.getEdges().get(0).getNode()).isEqualTo(testException);
                assertThat(connection.getTotalCount()).isEqualTo(1L);
                assertThat(connection.getPageInfo().getHasNextPage()).isFalse();
                assertThat(connection.getPageInfo().getHasPreviousPage()).isFalse();

                verify(exceptionRepository).findAll(any(Pageable.class));
        }

        @Test
        void findExceptions_WithInterfaceTypeFilter_ShouldUseInterfaceTypeQuery() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .interfaceTypes(List.of(InterfaceType.ORDER))
                                .build();

                when(exceptionRepository.findByInterfaceType(eq(InterfaceType.ORDER), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);
                assertThat(connection.getEdges().get(0).getNode().getInterfaceType())
                                .isEqualTo(InterfaceType.ORDER);

                verify(exceptionRepository).findByInterfaceType(eq(InterfaceType.ORDER),
                                any(Pageable.class));
        }

        @Test
        void findExceptions_WithStatusFilter_ShouldUseStatusQuery() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .statuses(List.of(ExceptionStatus.NEW))
                                .build();

                when(exceptionRepository.findByStatus(eq(ExceptionStatus.NEW), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);
                assertThat(connection.getEdges().get(0).getNode().getStatus()).isEqualTo(ExceptionStatus.NEW);

                verify(exceptionRepository).findByStatus(eq(ExceptionStatus.NEW), any(Pageable.class));
        }

        @Test
        void findExceptions_WithSeverityFilter_ShouldUseSeverityQuery() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .severities(List.of(ExceptionSeverity.HIGH))
                                .build();

                when(exceptionRepository.findBySeverity(eq(ExceptionSeverity.HIGH), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);
                assertThat(connection.getEdges().get(0).getNode().getSeverity()).isEqualTo(ExceptionSeverity.HIGH);

                verify(exceptionRepository).findBySeverity(eq(ExceptionSeverity.HIGH), any(Pageable.class));
        }

        @Test
        void findExceptions_WithCustomerIdFilter_ShouldUseCustomerIdQuery() {
                // Given
                String customerId = "CUST-001";
                ExceptionFilters filters = ExceptionFilters.builder()
                                .customerIds(List.of(customerId))
                                .build();

                when(exceptionRepository.findByCustomerId(eq(customerId), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findByCustomerId(eq(customerId), any(Pageable.class));
        }

        @Test
        void findExceptions_WithDateRangeFilter_ShouldUseDateRangeQuery() {
                // Given
                OffsetDateTime fromDate = OffsetDateTime.now().minusDays(7);
                OffsetDateTime toDate = OffsetDateTime.now();

                ExceptionFilters filters = ExceptionFilters.builder()
                                .dateRange(ExceptionFilters.DateRangeInput.builder()
                                                .from(fromDate)
                                                .to(toDate)
                                                .build())
                                .build();

                when(exceptionRepository.findByTimestampBetween(eq(fromDate), eq(toDate), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findByTimestampBetween(eq(fromDate), eq(toDate), any(Pageable.class));
        }

        @Test
        void findExceptions_WithSearchTermFilter_ShouldUseSearchQuery() {
                // Given
                String searchTerm = "test exception";
                ExceptionFilters filters = ExceptionFilters.builder()
                                .searchTerm(searchTerm)
                                .build();

                when(exceptionRepository.searchByExceptionReason(eq(searchTerm), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).searchByExceptionReason(eq(searchTerm), any(Pageable.class));
        }

        @Test
        void findExceptions_WithComplexFilters_ShouldUseComplexQuery() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .interfaceTypes(List.of(InterfaceType.ORDER,
                                                InterfaceType.DISTRIBUTION))
                                .statuses(List.of(ExceptionStatus.NEW, ExceptionStatus.ACKNOWLEDGED))
                                .severities(List.of(ExceptionSeverity.HIGH))
                                .build();

                when(exceptionRepository.findWithFilters(
                                eq(InterfaceType.ORDER), eq(ExceptionStatus.NEW), eq(ExceptionSeverity.HIGH),
                                any(), any(), any(), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findWithFilters(
                                eq(InterfaceType.ORDER), eq(ExceptionStatus.NEW), eq(ExceptionSeverity.HIGH),
                                any(), any(), any(), any(Pageable.class));
        }

        @Test
        void findExceptions_WithPagination_ShouldApplyPaginationCorrectly() {
                // Given
                PaginationInput pagination = PaginationInput.builder()
                                .first(10)
                                .build();

                when(exceptionRepository.findAll(any(Pageable.class))).thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(null, pagination,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findAll(any(Pageable.class));
        }

        @Test
        void findExceptions_WithSorting_ShouldApplySortingCorrectly() {
                // Given
                SortingInput sorting = SortingInput.builder()
                                .field("severity")
                                .direction(SortingInput.SortDirection.ASC)
                                .build();

                when(exceptionRepository.findAll(any(Pageable.class))).thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(null, null,
                                sorting);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findAll(any(Pageable.class));
        }

        @Test
        void findExceptionByTransactionId_WithValidId_ShouldReturnException() {
                // Given
                String transactionId = "TXN-001";
                when(exceptionRepository.findByTransactionId(transactionId))
                                .thenReturn(Optional.of(testException));

                // When
                CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                                .findExceptionByTransactionId(transactionId);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                Optional<InterfaceException> exception = result.join();

                assertThat(exception).isPresent();
                assertThat(exception.get()).isEqualTo(testException);

                verify(exceptionRepository).findByTransactionId(transactionId);
        }

        @Test
        void findExceptionByTransactionId_WithNonExistentId_ShouldReturnEmpty() {
                // Given
                String transactionId = "NON-EXISTENT";
                when(exceptionRepository.findByTransactionId(transactionId))
                                .thenReturn(Optional.empty());

                // When
                CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                                .findExceptionByTransactionId(transactionId);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                Optional<InterfaceException> exception = result.join();

                assertThat(exception).isEmpty();

                verify(exceptionRepository).findByTransactionId(transactionId);
        }

        @Test
        void findExceptionByTransactionId_WithNullId_ShouldReturnEmpty() {
                // When
                CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                                .findExceptionByTransactionId(null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                Optional<InterfaceException> exception = result.join();

                assertThat(exception).isEmpty();
        }

        @Test
        void findExceptionByTransactionId_WithBlankId_ShouldReturnEmpty() {
                // When
                CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                                .findExceptionByTransactionId("   ");

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                Optional<InterfaceException> exception = result.join();

                assertThat(exception).isEmpty();
        }

        @Test
        void findExceptions_WithExcludeResolvedFilter_ShouldFilterResolvedExceptions() {
                // Given
                ExceptionFilters filters = ExceptionFilters.builder()
                                .excludeResolved(true)
                                .build();

                when(exceptionRepository.findWithFilters(
                                any(), eq(ExceptionStatus.NEW), any(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(testPage);

                // When
                CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(filters, null,
                                null);

                // Then
                assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
                ExceptionConnection connection = result.join();

                assertThat(connection.getEdges()).hasSize(1);

                verify(exceptionRepository).findWithFilters(
                                any(), eq(ExceptionStatus.NEW), any(), any(), any(), any(), any(Pageable.class));
        }
}