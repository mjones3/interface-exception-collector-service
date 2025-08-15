package com.arcone.biopro.exception.collector.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataManagementService.
 */
@ExtendWith(MockitoExtension.class)
class DataManagementServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DataManagementService dataManagementService;

    @BeforeEach
    void setUp() {
        // Setup common mock behaviors
    }

    @Test
    void migrateLegacyExceptions_Success() {
        // Given
        String sourceTable = "legacy_exceptions";
        int batchSize = 1000;
        boolean dryRun = true;

        Map<String, Object> mockResult = Map.of(
                "migrated_count", 500,
                "error_count", 5,
                "validation_errors", new String[] { "Sample error 1", "Sample error 2" });

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(anyString(), eq(sourceTable), eq(batchSize), eq(dryRun)))
                .thenReturn(List.of(mockResult));
        when(jdbcTemplate.update(anyString(), anyInt(), anyInt(), anyString(), isNull(), eq(1L)))
                .thenReturn(1);

        // When
        MigrationResult result = dataManagementService.migrateLegacyExceptions(sourceTable, batchSize, dryRun);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMigratedCount()).isEqualTo(500);
        assertThat(result.getErrorCount()).isEqualTo(5);
        assertThat(result.getValidationErrors()).hasSize(2);

        verify(jdbcTemplate).queryForList(anyString(), eq(sourceTable), eq(batchSize), eq(dryRun));
    }

    @Test
    void migrateLegacyExceptions_Failure() {
        // Given
        String sourceTable = "legacy_exceptions";
        int batchSize = 1000;
        boolean dryRun = true;

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        MigrationResult result = dataManagementService.migrateLegacyExceptions(sourceTable, batchSize, dryRun);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Database connection failed");
    }

    @Test
    void validateDataIntegrity_Success() {
        // Given
        Map<String, Object> mockResult = Map.of(
                "validation_type", "validation_passed",
                "issue_count", 0,
                "sample_issues", new String[] { "No data integrity issues found" });

        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(mockResult));

        // When
        ValidationResult result = dataManagementService.validateDataIntegrity();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getIssues()).isEmpty();

        verify(jdbcTemplate).queryForList("SELECT * FROM validate_migrated_data()");
    }

    @Test
    void validateDataIntegrity_WithIssues() {
        // Given
        Map<String, Object> mockResult = Map.of(
                "validation_type", "orphaned_retry_attempts",
                "issue_count", 3,
                "sample_issues", new String[] { "Retry ID: 1", "Retry ID: 2", "Retry ID: 3" });

        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(mockResult));

        // When
        ValidationResult result = dataManagementService.validateDataIntegrity();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getIssues()).hasSize(1);
        assertThat(result.getIssues().get(0).getType()).isEqualTo("orphaned_retry_attempts");
        assertThat(result.getIssues().get(0).getCount()).isEqualTo(3);
    }

    @Test
    void cleanupOldExceptions_Success() {
        // Given
        int retentionDays = 365;
        int batchSize = 1000;
        boolean dryRun = true;
        boolean preserveCritical = true;

        Map<String, Object> mockResult = Map.of(
                "deleted_exceptions", 1000,
                "deleted_retry_attempts", 500,
                "preserved_critical", 50,
                "cleanup_duration", "00:05:30");

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyInt(), eq(dryRun), anyString()))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(anyString(), eq(retentionDays), eq(batchSize), eq(dryRun), eq(preserveCritical)))
                .thenReturn(List.of(mockResult));
        when(jdbcTemplate.update(anyString(), anyInt(), anyInt(), anyString(), isNull(), eq(1L)))
                .thenReturn(1);

        // When
        CleanupResult result = dataManagementService.cleanupOldExceptions(
                retentionDays, batchSize, dryRun, preserveCritical);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDeletedExceptions()).isEqualTo(1000);
        assertThat(result.getDeletedRetryAttempts()).isEqualTo(500);
        assertThat(result.getPreservedCritical()).isEqualTo(50);
        assertThat(result.getDuration()).isEqualTo("00:05:30");
    }

    @Test
    void cleanupResolvedExceptions_Success() {
        // Given
        int resolvedRetentionDays = 90;
        int batchSize = 1000;
        boolean dryRun = true;

        Map<String, Object> mockResult = Map.of(
                "deleted_count", 250,
                "cleanup_duration", "00:02:15");

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyInt(), eq(dryRun), anyString()))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(anyString(), eq(resolvedRetentionDays), eq(batchSize), eq(dryRun)))
                .thenReturn(List.of(mockResult));
        when(jdbcTemplate.update(anyString(), anyInt(), anyInt(), anyString(), isNull(), eq(1L)))
                .thenReturn(1);

        // When
        CleanupResult result = dataManagementService.cleanupResolvedExceptions(
                resolvedRetentionDays, batchSize, dryRun);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDeletedExceptions()).isEqualTo(250);
        assertThat(result.getDuration()).isEqualTo("00:02:15");
    }

    @Test
    void archiveOldExceptions_Success() {
        // Given
        int archiveDays = 730;
        int batchSize = 1000;
        boolean dryRun = true;
        boolean preserveCritical = true;

        Map<String, Object> mockResult = Map.of(
                "archived_exceptions", 2000,
                "archived_retry_attempts", 800,
                "preserved_critical", 100,
                "archive_duration", "00:10:45");

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyInt(), eq(dryRun), anyString()))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(anyString(), eq(archiveDays), eq(batchSize), eq(dryRun), eq(preserveCritical),
                eq("system")))
                .thenReturn(List.of(mockResult));
        when(jdbcTemplate.update(anyString(), anyInt(), anyInt(), anyString(), isNull(), eq(1L)))
                .thenReturn(1);

        // When
        ArchiveResult result = dataManagementService.archiveOldExceptions(
                archiveDays, batchSize, dryRun, preserveCritical);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getArchivedExceptions()).isEqualTo(2000);
        assertThat(result.getArchivedRetryAttempts()).isEqualTo(800);
        assertThat(result.getPreservedCritical()).isEqualTo(100);
        assertThat(result.getDuration()).isEqualTo("00:10:45");
    }

    @Test
    void restoreArchivedExceptions_Success() {
        // Given
        List<String> transactionIds = List.of("txn-001", "txn-002", "txn-003");

        Map<String, Object> mockResult = Map.of(
                "restored_exceptions", 3,
                "restored_retry_attempts", 5,
                "failed_restorations", new String[] {});

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString(), anyInt(), eq(false), anyString()))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(anyString(), any(String[].class), eq("system")))
                .thenReturn(List.of(mockResult));
        when(jdbcTemplate.update(anyString(), anyInt(), anyInt(), anyString(), isNull(), eq(1L)))
                .thenReturn(1);

        // When
        RestoreResult result = dataManagementService.restoreArchivedExceptions(transactionIds);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRestoredExceptions()).isEqualTo(3);
        assertThat(result.getRestoredRetryAttempts()).isEqualTo(5);
        assertThat(result.getFailedRestorations()).isEmpty();
    }

    @Test
    void getCleanupStatistics_Success() {
        // Given
        List<Map<String, Object>> mockResults = List.of(
                Map.of(
                        "metric_name", "total_exceptions",
                        "metric_value", 50000L,
                        "recommendation", "Exception count is manageable"),
                Map.of(
                        "metric_name", "old_exceptions_1year",
                        "metric_value", 15000L,
                        "recommendation", "Recommend cleanup of old exceptions"));

        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(mockResults);

        // When
        List<CleanupStatistic> statistics = dataManagementService.getCleanupStatistics();

        // Then
        assertThat(statistics).hasSize(2);
        assertThat(statistics.get(0).getMetricName()).isEqualTo("total_exceptions");
        assertThat(statistics.get(0).getMetricValue()).isEqualTo(50000L);
        assertThat(statistics.get(1).getMetricName()).isEqualTo("old_exceptions_1year");
        assertThat(statistics.get(1).getRecommendation()).contains("cleanup");
    }

    @Test
    void getArchiveStatistics_Success() {
        // Given
        List<Map<String, Object>> mockResults = List.of(
                Map.of(
                        "metric_name", "exceptions",
                        "main_table_count", 30000L,
                        "archive_table_count", 20000L,
                        "total_count", 50000L,
                        "archive_percentage", 40.0),
                Map.of(
                        "metric_name", "retry_attempts",
                        "main_table_count", 15000L,
                        "archive_table_count", 10000L,
                        "total_count", 25000L,
                        "archive_percentage", 40.0));

        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(mockResults);

        // When
        List<ArchiveStatistic> statistics = dataManagementService.getArchiveStatistics();

        // Then
        assertThat(statistics).hasSize(2);
        assertThat(statistics.get(0).getMetricName()).isEqualTo("exceptions");
        assertThat(statistics.get(0).getMainTableCount()).isEqualTo(30000L);
        assertThat(statistics.get(0).getArchiveTableCount()).isEqualTo(20000L);
        assertThat(statistics.get(0).getArchivePercentage()).isEqualTo(40.0);
    }
}