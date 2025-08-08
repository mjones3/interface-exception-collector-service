package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InterfaceExceptionRepository using TestContainers for
 * PostgreSQL integration.
 * Tests all custom query methods, pagination, sorting, and full-text search
 * capabilities.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.kafka.enabled=false"
})
class InterfaceExceptionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_exception_collector")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private InterfaceExceptionRepository repository;

    private InterfaceException orderException;
    private InterfaceException collectionException;
    private InterfaceException distributionException;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        baseTime = OffsetDateTime.now().minusDays(1);

        // Create test data
        orderException = InterfaceException.builder()
                .transactionId("TXN-ORDER-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Order validation failed: Invalid customer ID")
                .operation("CREATE_ORDER")
                .externalId("ORDER-12345")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(baseTime)
                .processedAt(baseTime.plusMinutes(1))
                .retryCount(0)
                .build();

        collectionException = InterfaceException.builder()
                .transactionId("TXN-COLLECTION-001")
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Collection processing error: Donor not found")
                .operation("CREATE_COLLECTION")
                .externalId("COLLECTION-67890")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.CRITICAL)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(false)
                .customerId("CUST-002")
                .locationCode("LOC-002")
                .timestamp(baseTime.plusHours(1))
                .processedAt(baseTime.plusHours(1).plusMinutes(1))
                .retryCount(2)
                .build();

        distributionException = InterfaceException.builder()
                .transactionId("TXN-DISTRIBUTION-001")
                .interfaceType(InterfaceType.DISTRIBUTION)
                .exceptionReason("Distribution failed: Network timeout")
                .operation("CREATE_DISTRIBUTION")
                .externalId("DIST-11111")
                .status(ExceptionStatus.RETRIED_FAILED)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.SYSTEM_ERROR)
                .retryable(true)
                .customerId("CUST-001")
                .locationCode("LOC-003")
                .timestamp(baseTime.plusHours(2))
                .processedAt(baseTime.plusHours(2).plusMinutes(1))
                .retryCount(1)
                .build();

        repository.saveAll(List.of(orderException, collectionException, distributionException));
    }

    @Test
    void findByTransactionId_ShouldReturnException_WhenExists() {
        // When
        Optional<InterfaceException> result = repository.findByTransactionId("TXN-ORDER-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-ORDER-001");
        assertThat(result.get().getInterfaceType()).isEqualTo(InterfaceType.ORDER);
    }

    @Test
    void findByTransactionId_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<InterfaceException> result = repository.findByTransactionId("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByTransactionId_ShouldReturnTrue_WhenExists() {
        // When
        boolean exists = repository.existsByTransactionId("TXN-ORDER-001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTransactionId_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean exists = repository.existsByTransactionId("NON-EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_ShouldReturnPagedResults_WithSorting() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "timestamp"));

        // When
        Page<InterfaceException> result = repository.findAll(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getTransactionId()).isEqualTo("TXN-DISTRIBUTION-001");
        assertThat(result.getContent().get(1).getTransactionId()).isEqualTo("TXN-COLLECTION-001");
    }

    @Test
    void findByInterfaceType_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findByInterfaceType(InterfaceType.ORDER, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getInterfaceType()).isEqualTo(InterfaceType.ORDER);
    }

    @Test
    void findByStatus_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findByStatus(ExceptionStatus.NEW, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ExceptionStatus.NEW);
    }

    @Test
    void findBySeverity_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findBySeverity(ExceptionSeverity.CRITICAL, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSeverity()).isEqualTo(ExceptionSeverity.CRITICAL);
    }

    @Test
    void findByCustomerId_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findByCustomerId("CUST-001", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(ex -> "CUST-001".equals(ex.getCustomerId()));
    }

    @Test
    void findByTimestampBetween_ShouldReturnFilteredResults() {
        // Given
        OffsetDateTime fromDate = baseTime.minusHours(1);
        OffsetDateTime toDate = baseTime.plusMinutes(30);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findByTimestampBetween(fromDate, toDate, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionId()).isEqualTo("TXN-ORDER-001");
    }

    @Test
    void findWithFilters_ShouldReturnFilteredResults_WithMultipleFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findWithFilters(
                InterfaceType.ORDER,
                ExceptionStatus.NEW,
                ExceptionSeverity.HIGH,
                "CUST-001",
                null,
                null,
                pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionId()).isEqualTo("TXN-ORDER-001");
    }

    @Test
    void findWithFilters_ShouldReturnAllResults_WhenNoFiltersProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findWithFilters(
                null, null, null, null, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void searchByExceptionReason_ShouldReturnMatchingResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.searchByExceptionReason("validation", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExceptionReason()).containsIgnoringCase("validation");
    }

    @Test
    void searchByExternalId_ShouldReturnMatchingResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.searchByExternalId("ORDER", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExternalId()).containsIgnoringCase("ORDER");
    }

    @Test
    void searchByOperation_ShouldReturnMatchingResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.searchByOperation("CREATE", pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(ex -> ex.getOperation().contains("CREATE"));
    }

    @Test
    void searchInFields_ShouldReturnMatchingResults_FromSpecifiedFields() {
        // Given
        List<String> searchFields = List.of("exceptionReason", "externalId");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.searchInFields("ORDER", searchFields, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExternalId()).containsIgnoringCase("ORDER");
    }

    @Test
    void countByInterfaceType_ShouldReturnCorrectCount() {
        // When
        long count = repository.countByInterfaceType(InterfaceType.ORDER);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // When
        long count = repository.countByStatus(ExceptionStatus.NEW);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countBySeverity_ShouldReturnCorrectCount() {
        // When
        long count = repository.countBySeverity(ExceptionSeverity.HIGH);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByTimestampBetween_ShouldReturnCorrectCount() {
        // Given
        OffsetDateTime fromDate = baseTime.minusHours(1);
        OffsetDateTime toDate = baseTime.plusHours(3);

        // When
        long count = repository.countByTimestampBetween(fromDate, toDate);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void getDailyCounts_ShouldReturnDailyStatistics() {
        // Given
        OffsetDateTime fromDate = baseTime.minusDays(1);
        OffsetDateTime toDate = baseTime.plusDays(1);

        // When
        List<Object[]> result = repository.getDailyCounts(fromDate, toDate);

        // Then
        assertThat(result).isNotEmpty();
        // Note: Exact assertions depend on the date grouping behavior
    }

    @Test
    void findRelatedExceptionsByCustomer_ShouldReturnRelatedExceptions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findRelatedExceptionsByCustomer(
                "CUST-001", "TXN-ORDER-001", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionId()).isEqualTo("TXN-DISTRIBUTION-001");
        assertThat(result.getContent().get(0).getCustomerId()).isEqualTo("CUST-001");
    }

    @Test
    void findCriticalExceptions_ShouldReturnCriticalAndHighRetryExceptions() {
        // When
        List<InterfaceException> result = repository.findCriticalExceptions(
                ExceptionSeverity.CRITICAL, 1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(ex -> ex.getSeverity() == ExceptionSeverity.CRITICAL);
        assertThat(result).anyMatch(ex -> ex.getRetryCount() > 1);
    }

    @Test
    void findRetryableExceptions_ShouldReturnOnlyRetryableUnresolvedExceptions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InterfaceException> result = repository.findRetryableExceptions(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(InterfaceException::getRetryable);
        assertThat(result.getContent()).noneMatch(
                ex -> ex.getStatus() == ExceptionStatus.RESOLVED || ex.getStatus() == ExceptionStatus.CLOSED);
    }
}