package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for GraphQL exception queries.
 * Tests the complete GraphQL query execution path including schema validation.
 */
@GraphQlTest
@ActiveProfiles("test")
class ExceptionQueryIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private InterfaceExceptionRepository exceptionRepository;

    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception for GraphQL")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .build();
    }

    @Test
    void exceptions_BasicQuery_ShouldReturnExceptionList() {
        // Given
        when(exceptionRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testException)));

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions {
                        edges {
                            node {
                                id
                                transactionId
                                interfaceType
                                exceptionReason
                                status
                                severity
                            }
                            cursor
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
                """)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSize(1)
                .path("exceptions.edges[0].node.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001")
                .path("exceptions.edges[0].node.interfaceType")
                .entity(String.class)
                .isEqualTo("ORDER_COLLECTION")
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isEqualTo(1L);
    }

    @Test
    void exceptions_WithFilters_ShouldApplyFiltersCorrectly() {
        // Given
        when(exceptionRepository.findByInterfaceType(eq(InterfaceType.ORDER_COLLECTION),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testException)));

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER_COLLECTION]
                            statuses: [NEW]
                            severities: [HIGH]
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                                severity
                            }
                        }
                        totalCount
                    }
                }
                """)
                .execute()
                .path("exceptions.edges[0].node.interfaceType")
                .entity(String.class)
                .isEqualTo("ORDER_COLLECTION")
                .path("exceptions.edges[0].node.status")
                .entity(String.class)
                .isEqualTo("NEW")
                .path("exceptions.edges[0].node.severity")
                .entity(String.class)
                .isEqualTo("HIGH");
    }

    @Test
    void exceptions_WithPagination_ShouldApplyPaginationCorrectly() {
        // Given
        when(exceptionRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testException)));

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        pagination: {
                            first: 10
                            after: "cursor123"
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                            }
                            cursor
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                    }
                }
                """)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSize(1)
                .path("exceptions.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void exceptions_WithSorting_ShouldApplySortingCorrectly() {
        // Given
        when(exceptionRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testException)));

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        sorting: {
                            field: "timestamp"
                            direction: DESC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                timestamp
                            }
                        }
                    }
                }
                """)
                .execute()
                .path("exceptions.edges[0].node.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001");
    }

    @Test
    void exception_WithValidTransactionId_ShouldReturnException() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.of(testException));

        // When & Then
        graphQlTester.document("""
                query {
                    exception(transactionId: "TXN-001") {
                        id
                        transactionId
                        interfaceType
                        exceptionReason
                        operation
                        status
                        severity
                        category
                        customerId
                        locationCode
                        retryable
                        retryCount
                    }
                }
                """)
                .execute()
                .path("exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001")
                .path("exception.interfaceType")
                .entity(String.class)
                .isEqualTo("ORDER_COLLECTION")
                .path("exception.exceptionReason")
                .entity(String.class)
                .isEqualTo("Test exception for GraphQL")
                .path("exception.operation")
                .entity(String.class)
                .isEqualTo("CREATE_ORDER")
                .path("exception.status")
                .entity(String.class)
                .isEqualTo("NEW")
                .path("exception.severity")
                .entity(String.class)
                .isEqualTo("HIGH")
                .path("exception.category")
                .entity(String.class)
                .isEqualTo("VALIDATION_ERROR")
                .path("exception.customerId")
                .entity(String.class)
                .isEqualTo("CUST-001")
                .path("exception.locationCode")
                .entity(String.class)
                .isEqualTo("LOC-001")
                .path("exception.retryable")
                .entity(Boolean.class)
                .isEqualTo(true)
                .path("exception.retryCount")
                .entity(Integer.class)
                .isEqualTo(0);
    }

    @Test
    void exception_WithNonExistentTransactionId_ShouldReturnNull() {
        // Given
        when(exceptionRepository.findByTransactionId("NON-EXISTENT"))
                .thenReturn(Optional.empty());

        // When & Then
        graphQlTester.document("""
                query {
                    exception(transactionId: "NON-EXISTENT") {
                        transactionId
                    }
                }
                """)
                .execute()
                .path("exception")
                .valueIsNull();
    }

    @Test
    void exceptions_WithInvalidPaginationParameters_ShouldReturnValidationError() {
        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        pagination: {
                            first: -1
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                            }
                        }
                    }
                }
                """)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("'first' parameter must be positive"));
    }

    @Test
    void exceptions_WithInvalidSortField_ShouldReturnValidationError() {
        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        sorting: {
                            field: "invalidField"
                            direction: ASC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                            }
                        }
                    }
                }
                """)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Invalid sort field"));
    }

    @Test
    void exception_WithBlankTransactionId_ShouldReturnValidationError() {
        // When & Then
        graphQlTester.document("""
                query {
                    exception(transactionId: "") {
                        transactionId
                    }
                }
                """)
                .execute()
                .errors()
                .expect(error -> error.getErrorType().toString().equals("BAD_REQUEST"));
    }

    @Test
    void exceptions_ComplexQuery_ShouldHandleAllParameters() {
        // Given
        when(exceptionRepository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testException)));

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER_COLLECTION, ORDER_DISTRIBUTION]
                            statuses: [NEW, ACKNOWLEDGED]
                            severities: [HIGH, CRITICAL]
                            customerIds: ["CUST-001", "CUST-002"]
                            locationCodes: ["LOC-001"]
                            searchTerm: "test"
                            excludeResolved: true
                            retryable: true
                        }
                        pagination: {
                            first: 20
                        }
                        sorting: {
                            field: "timestamp"
                            direction: DESC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                                severity
                                customerId
                                locationCode
                                retryable
                            }
                            cursor
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
                """)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSize(1)
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isEqualTo(1L);
    }
}