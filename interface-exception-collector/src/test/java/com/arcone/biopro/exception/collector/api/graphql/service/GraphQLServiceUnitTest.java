package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionSummary;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.TimeRange;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
 * Unit tests for GraphQL service layer components.
 * Tests business logic, data transformation, and service integration.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLServiceUnitTest {

    @Mock
    private ExceptionQueryService exceptionQueryService;

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private RetryService retryService;

    @InjectMocks
    private GraphQLExceptionService graphQLExceptionService;

    @InjectMocks
    private GraphQLRetryService graphQLRetryService;

    @InjectMocks
    private GraphQLAcknowledgmentService graphQLAcknowledgmentService;

    @InjectMocks
    private SummaryService summaryService;

    private InterfaceException testException;
    private ExceptionFilters testFilters;
    private PaginationInput testPagination;
    private SortingInput testSorting;

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
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .build();

        testFilters = ExceptionFilters.builder()
                .interfaceTypes(List.of(InterfaceType.ORDER))
                .statuses(List.of(ExceptionStatus.NEW))
                .severities(List.of(ExceptionSeverity.HIGH))
                .build();

        testPagination = PaginationInput.builder()
                .first(10)
                .after("cursor-123")
                .build();

        testSorting = SortingInput.builder()
                .field("timestamp")
                .direction(SortingInput.SortDirection.DESC)
                .build();
    }

    @Test
    void findExceptions_WithValidInput_ShouldReturnConnection() throws Exception {
        // Given
        Page<InterfaceException> mockPage = new PageImpl<>(
                List.of(testException),
                PageRequest.of(0, 10),
                1L);
        when(exceptionQueryService.findExceptions(any(), any())).thenReturn(mockPage);

        // When
        CompletableFuture<ExceptionConnection> result = graphQLExceptionService.findExceptions(
                testFilters, testPagination, testSorting);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        ExceptionConnection connection = result.get();
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(1L);
        assertThat(connection.getPageInfo().getHasNextPage()).isFalse();

        verify(exceptionQueryService).findExceptions(any(), any());
    }

    @Test
    void findExceptionByTransactionId_WithExistingId_ShouldReturnException() throws Exception {
        // Given
        String transactionId = "TXN-001";
        when(exceptionQueryService.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                .findExceptionByTransactionId(transactionId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.get()).isPresent();
        assertThat(result.get().get().getTransactionId()).isEqualTo(transactionId);

        verify(exceptionQueryService).findByTransactionId(transactionId);
    }

    @Test
    void findExceptionByTransactionId_WithNonExistentId_ShouldReturnEmpty() throws Exception {
        // Given
        String transactionId = "NON-EXISTENT";
        when(exceptionQueryService.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        // When
        CompletableFuture<Optional<InterfaceException>> result = graphQLExceptionService
                .findExceptionByTransactionId(transactionId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.get()).isEmpty();

        verify(exceptionQueryService).findByTransactionId(transactionId);
    }

    @Test
    void retryException_WithValidInput_ShouldReturnSuccessResult() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Manual retry requested")
                .priority(RetryExceptionInput.RetryPriority.HIGH)
                .build();

        String userId = "test-user";

        when(retryService.retryException(eq("TXN-001"), eq("Manual retry requested"), any(), eq(userId)))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        CompletableFuture<RetryExceptionResult> result = graphQLRetryService.retryException(input, userId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        RetryExceptionResult retryResult = result.get();
        assertThat(retryResult.isSuccess()).isTrue();
        assertThat(retryResult.getTransactionId()).isEqualTo("TXN-001");

        verify(retryService).retryException(eq("TXN-001"), eq("Manual retry requested"), any(), eq(userId));
    }

    @Test
    void retryException_WithFailedRetry_ShouldReturnFailureResult() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Manual retry requested")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        String userId = "test-user";

        when(retryService.retryException(eq("TXN-001"), eq("Manual retry requested"), any(), eq(userId)))
                .thenReturn(CompletableFuture.completedFuture(false));

        // When
        CompletableFuture<RetryExceptionResult> result = graphQLRetryService.retryException(input, userId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        RetryExceptionResult retryResult = result.get();
        assertThat(retryResult.isSuccess()).isFalse();
        assertThat(retryResult.getErrorMessage()).isNotNull();
    }

    @Test
    void retryException_WithServiceException_ShouldPropagateException() {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Manual retry requested")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        String userId = "test-user";

        when(retryService.retryException(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        // When
        CompletableFuture<RetryExceptionResult> result = graphQLRetryService.retryException(input, userId);

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("Service error");
    }

    @Test
    void acknowledgeException_WithValidInput_ShouldReturnSuccessResult() throws Exception {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Acknowledged by operations team")
                .notes("Issue has been reviewed and documented")
                .build();

        String userId = "test-user";

        when(exceptionManagementService.acknowledgeException(
                eq("TXN-001"), eq("Acknowledged by operations team"), eq("Issue has been reviewed and documented"),
                eq(userId))).thenReturn(CompletableFuture.completedFuture(testException));

        // When
        CompletableFuture<AcknowledgeExceptionResult> result = graphQLAcknowledgmentService.acknowledgeException(input,
                userId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        AcknowledgeExceptionResult ackResult = result.get();
        assertThat(ackResult.isSuccess()).isTrue();
        assertThat(ackResult.getTransactionId()).isEqualTo("TXN-001");

        verify(exceptionManagementService).acknowledgeException(
                eq("TXN-001"), eq("Acknowledged by operations team"), eq("Issue has been reviewed and documented"),
                eq(userId));
    }

    @Test
    void acknowledgeException_WithServiceException_ShouldReturnFailureResult() throws Exception {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Acknowledged by operations team")
                .build();

        String userId = "test-user";

        when(exceptionManagementService.acknowledgeException(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Exception not found")));

        // When
        CompletableFuture<AcknowledgeExceptionResult> result = graphQLAcknowledgmentService.acknowledgeException(input,
                userId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        AcknowledgeExceptionResult ackResult = result.get();
        assertThat(ackResult.isSuccess()).isFalse();
        assertThat(ackResult.getErrorMessage()).contains("Exception not found");
    }

    @Test
    void generateSummary_WithValidTimeRange_ShouldReturnSummary() throws Exception {
        // Given
        TimeRange timeRange = TimeRange.builder()
                .startDate(OffsetDateTime.now().minusDays(7))
                .endDate(OffsetDateTime.now())
                .build();

        ExceptionFilters filters = ExceptionFilters.builder().build();

        // Mock summary data
        when(exceptionQueryService.countByInterfaceType(any(), any()))
                .thenReturn(java.util.Map.of(InterfaceType.ORDER, 10L, InterfaceType.COLLECTION, 5L));
        when(exceptionQueryService.countBySeverity(any(), any()))
                .thenReturn(java.util.Map.of(ExceptionSeverity.HIGH, 8L, ExceptionSeverity.MEDIUM, 7L));
        when(exceptionQueryService.countByStatus(any(), any()))
                .thenReturn(java.util.Map.of(ExceptionStatus.NEW, 12L, ExceptionStatus.ACKNOWLEDGED, 3L));

        // When
        CompletableFuture<ExceptionSummary> result = summaryService.generateSummary(timeRange, filters);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        ExceptionSummary summary = result.get();
        assertThat(summary.getTotalExceptions()).isEqualTo(15L);
        assertThat(summary.getByInterfaceType()).hasSize(2);
        assertThat(summary.getBySeverity()).hasSize(2);
        assertThat(summary.getByStatus()).hasSize(2);
    }

    @Test
    void convertToConnection_WithPageData_ShouldCreateValidConnection() {
        // Given
        Page<InterfaceException> page = new PageImpl<>(
                List.of(testException),
                PageRequest.of(0, 10),
                25L);

        // When
        ExceptionConnection connection = graphQLExceptionService.convertToConnection(page, testPagination);

        // Then
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(25L);
        assertThat(connection.getPageInfo().getHasNextPage()).isTrue();
        assertThat(connection.getPageInfo().getHasPreviousPage()).isFalse();

        ExceptionConnection.ExceptionEdge edge = connection.getEdges().get(0);
        assertThat(edge.getNode()).isEqualTo(testException);
        assertThat(edge.getCursor()).isNotNull();
    }

    @Test
    void convertToConnection_WithEmptyPage_ShouldCreateEmptyConnection() {
        // Given
        Page<InterfaceException> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0L);

        // When
        ExceptionConnection connection = graphQLExceptionService.convertToConnection(emptyPage, testPagination);

        // Then
        assertThat(connection.getEdges()).isEmpty();
        assertThat(connection.getTotalCount()).isEqualTo(0L);
        assertThat(connection.getPageInfo().getHasNextPage()).isFalse();
        assertThat(connection.getPageInfo().getHasPreviousPage()).isFalse();
    }

    @Test
    void buildPageable_WithValidInput_ShouldCreateCorrectPageable() {
        // When
        Pageable pageable = graphQLExceptionService.buildPageable(testPagination, testSorting);

        // Then
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().isSorted()).isTrue();
        assertThat(pageable.getSort().getOrderFor("timestamp")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("timestamp").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    void buildPageable_WithNullSorting_ShouldUseDefaultSort() {
        // When
        Pageable pageable = graphQLExceptionService.buildPageable(testPagination, null);

        // Then
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().isSorted()).isTrue();
        assertThat(pageable.getSort().getOrderFor("timestamp")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("timestamp").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    void validateRetryInput_WithInvalidInput_ShouldThrowException() {
        // Given
        RetryExceptionInput invalidInput = RetryExceptionInput.builder()
                .transactionId("") // Invalid - blank
                .reason("Manual retry")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        // When & Then
        assertThatThrownBy(() -> graphQLRetryService.validateRetryInput(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction ID cannot be blank");
    }

    @Test
    void validateAcknowledgeInput_WithInvalidInput_ShouldThrowException() {
        // Given
        AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("") // Invalid - blank
                .build();

        // When & Then
        assertThatThrownBy(() -> graphQLAcknowledgmentService.validateAcknowledgeInput(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reason cannot be blank");
    }
}