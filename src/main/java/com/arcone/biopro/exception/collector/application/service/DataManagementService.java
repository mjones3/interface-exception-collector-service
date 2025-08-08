package com.arcone.biopro.exception.collector.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for administrative data management operations including migration,
 * cleanup, archiving, and validation utilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataManagementService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Migrates legacy exception data from an external source table.
     *
     * @param sourceTable The name of the source table containing legacy data
     * @param batchSize   Number of records to process in each batch
     * @param dryRun      If true, performs validation without actual migration
     * @return Migration result containing counts and validation errors
     */
    @Transactional
    public MigrationResult migrateLegacyExceptions(String sourceTable, int batchSize, boolean dryRun) {
        log.info("Starting legacy exception migration from table: {} (batchSize: {}, dryRun: {})",
                sourceTable, batchSize, dryRun);

        try {
            // Log migration start
            Long migrationLogId = logMigrationStart("LEGACY_MIGRATION", sourceTable, "system");

            // Execute migration function
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM migrate_legacy_exceptions(?, ?, ?)",
                    sourceTable, batchSize, dryRun);

            Map<String, Object> result = results.get(0);
            int migratedCount = (Integer) result.get("migrated_count");
            int errorCount = (Integer) result.get("error_count");
            String[] validationErrors = (String[]) result.get("validation_errors");

            // Log migration completion
            logMigrationCompletion(migrationLogId, migratedCount, errorCount, "COMPLETED", null);

            log.info("Legacy exception migration completed: {} migrated, {} errors",
                    migratedCount, errorCount);

            return MigrationResult.builder()
                    .migratedCount(migratedCount)
                    .errorCount(errorCount)
                    .validationErrors(List.of(validationErrors))
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Legacy exception migration failed", e);
            return MigrationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Validates data integrity after migration or other operations.
     *
     * @return Validation result containing any issues found
     */
    public ValidationResult validateDataIntegrity() {
        log.info("Starting data integrity validation");

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM validate_migrated_data()");

            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            for (Map<String, Object> result : results) {
                String validationType = (String) result.get("validation_type");
                Integer issueCount = (Integer) result.get("issue_count");
                String[] sampleIssues = (String[]) result.get("sample_issues");

                if ("validation_passed".equals(validationType)) {
                    log.info("Data integrity validation passed");
                } else {
                    log.warn("Data integrity issue found: {} ({} issues)", validationType, issueCount);
                    builder.issue(ValidationIssue.builder()
                            .type(validationType)
                            .count(issueCount)
                            .sampleIssues(List.of(sampleIssues))
                            .build());
                }
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Data integrity validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Cleans up old exception records based on retention policy.
     *
     * @param retentionDays    Number of days to retain records
     * @param batchSize        Number of records to process in each batch
     * @param dryRun           If true, performs analysis without actual deletion
     * @param preserveCritical If true, preserves critical exceptions
     * @return Cleanup result containing deletion counts and duration
     */
    @Transactional
    public CleanupResult cleanupOldExceptions(int retentionDays, int batchSize,
            boolean dryRun, boolean preserveCritical) {
        log.info("Starting exception cleanup (retentionDays: {}, batchSize: {}, dryRun: {}, preserveCritical: {})",
                retentionDays, batchSize, dryRun, preserveCritical);

        try {
            // Log cleanup start
            Long cleanupLogId = logCleanupStart("OLD_EXCEPTIONS", retentionDays, dryRun, "system");

            // Execute cleanup function
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM cleanup_old_exceptions(?, ?, ?, ?)",
                    retentionDays, batchSize, dryRun, preserveCritical);

            Map<String, Object> result = results.get(0);
            int deletedExceptions = (Integer) result.get("deleted_exceptions");
            int deletedRetryAttempts = (Integer) result.get("deleted_retry_attempts");
            int preservedCritical = (Integer) result.get("preserved_critical");
            String duration = result.get("cleanup_duration").toString();

            // Log cleanup completion
            logCleanupCompletion(cleanupLogId, deletedExceptions, preservedCritical, "COMPLETED", null);

            log.info("Exception cleanup completed: {} exceptions, {} retry attempts deleted, {} critical preserved",
                    deletedExceptions, deletedRetryAttempts, preservedCritical);

            return CleanupResult.builder()
                    .deletedExceptions(deletedExceptions)
                    .deletedRetryAttempts(deletedRetryAttempts)
                    .preservedCritical(preservedCritical)
                    .duration(duration)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Exception cleanup failed", e);
            return CleanupResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Cleans up resolved exceptions older than specified days.
     *
     * @param resolvedRetentionDays Number of days to retain resolved exceptions
     * @param batchSize             Number of records to process in each batch
     * @param dryRun                If true, performs analysis without actual
     *                              deletion
     * @return Cleanup result for resolved exceptions
     */
    @Transactional
    public CleanupResult cleanupResolvedExceptions(int resolvedRetentionDays, int batchSize, boolean dryRun) {
        log.info("Starting resolved exception cleanup (retentionDays: {}, batchSize: {}, dryRun: {})",
                resolvedRetentionDays, batchSize, dryRun);

        try {
            // Log cleanup start
            Long cleanupLogId = logCleanupStart("RESOLVED_EXCEPTIONS", resolvedRetentionDays, dryRun, "system");

            // Execute cleanup function
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM cleanup_resolved_exceptions(?, ?, ?)",
                    resolvedRetentionDays, batchSize, dryRun);

            Map<String, Object> result = results.get(0);
            int deletedCount = (Integer) result.get("deleted_count");
            String duration = result.get("cleanup_duration").toString();

            // Log cleanup completion
            logCleanupCompletion(cleanupLogId, deletedCount, 0, "COMPLETED", null);

            log.info("Resolved exception cleanup completed: {} exceptions deleted", deletedCount);

            return CleanupResult.builder()
                    .deletedExceptions(deletedCount)
                    .duration(duration)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Resolved exception cleanup failed", e);
            return CleanupResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Archives old exceptions to archive tables.
     *
     * @param archiveDays      Number of days after which to archive records
     * @param batchSize        Number of records to process in each batch
     * @param dryRun           If true, performs analysis without actual archival
     * @param preserveCritical If true, preserves critical exceptions from archival
     * @return Archive result containing archived counts and duration
     */
    @Transactional
    public ArchiveResult archiveOldExceptions(int archiveDays, int batchSize,
            boolean dryRun, boolean preserveCritical) {
        log.info("Starting exception archival (archiveDays: {}, batchSize: {}, dryRun: {}, preserveCritical: {})",
                archiveDays, batchSize, dryRun, preserveCritical);

        try {
            // Log archive start
            Long archiveLogId = logArchiveStart("OLD_EXCEPTIONS", archiveDays, dryRun, "system");

            // Execute archive function
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM archive_old_exceptions(?, ?, ?, ?, ?)",
                    archiveDays, batchSize, dryRun, preserveCritical, "system");

            Map<String, Object> result = results.get(0);
            int archivedExceptions = (Integer) result.get("archived_exceptions");
            int archivedRetryAttempts = (Integer) result.get("archived_retry_attempts");
            int preservedCritical = (Integer) result.get("preserved_critical");
            String duration = result.get("archive_duration").toString();

            // Log archive completion
            logArchiveCompletion(archiveLogId, archivedExceptions, 0, "COMPLETED", null);

            log.info("Exception archival completed: {} exceptions, {} retry attempts archived, {} critical preserved",
                    archivedExceptions, archivedRetryAttempts, preservedCritical);

            return ArchiveResult.builder()
                    .archivedExceptions(archivedExceptions)
                    .archivedRetryAttempts(archivedRetryAttempts)
                    .preservedCritical(preservedCritical)
                    .duration(duration)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Exception archival failed", e);
            return ArchiveResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Restores archived exceptions back to main tables.
     *
     * @param transactionIds List of transaction IDs to restore
     * @return Restore result containing restored counts and any failures
     */
    @Transactional
    public RestoreResult restoreArchivedExceptions(List<String> transactionIds) {
        log.info("Starting exception restoration for {} transaction IDs", transactionIds.size());

        try {
            // Log restore start
            Long archiveLogId = logArchiveStart("RESTORE_EXCEPTIONS", 0, false, "system");

            // Convert list to PostgreSQL array format
            String[] transactionArray = transactionIds.toArray(new String[0]);

            // Execute restore function
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM restore_archived_exceptions(?, ?)",
                    transactionArray, "system");

            Map<String, Object> result = results.get(0);
            int restoredExceptions = (Integer) result.get("restored_exceptions");
            int restoredRetryAttempts = (Integer) result.get("restored_retry_attempts");
            String[] failedRestorations = (String[]) result.get("failed_restorations");

            // Log restore completion
            logArchiveCompletion(archiveLogId, 0, restoredExceptions, "COMPLETED", null);

            log.info("Exception restoration completed: {} exceptions, {} retry attempts restored",
                    restoredExceptions, restoredRetryAttempts);

            return RestoreResult.builder()
                    .restoredExceptions(restoredExceptions)
                    .restoredRetryAttempts(restoredRetryAttempts)
                    .failedRestorations(List.of(failedRestorations))
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Exception restoration failed", e);
            return RestoreResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Gets cleanup statistics and recommendations.
     *
     * @return Statistics about data that can be cleaned up
     */
    public List<CleanupStatistic> getCleanupStatistics() {
        log.info("Retrieving cleanup statistics");

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM get_cleanup_statistics()");

            return results.stream()
                    .map(result -> CleanupStatistic.builder()
                            .metricName((String) result.get("metric_name"))
                            .metricValue((Long) result.get("metric_value"))
                            .recommendation((String) result.get("recommendation"))
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("Failed to retrieve cleanup statistics", e);
            throw new RuntimeException("Failed to retrieve cleanup statistics", e);
        }
    }

    /**
     * Gets archive statistics showing distribution between main and archive tables.
     *
     * @return Statistics about archived vs active data
     */
    public List<ArchiveStatistic> getArchiveStatistics() {
        log.info("Retrieving archive statistics");

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT * FROM get_archive_statistics()");

            return results.stream()
                    .map(result -> ArchiveStatistic.builder()
                            .metricName((String) result.get("metric_name"))
                            .mainTableCount((Long) result.get("main_table_count"))
                            .archiveTableCount((Long) result.get("archive_table_count"))
                            .totalCount((Long) result.get("total_count"))
                            .archivePercentage((Double) result.get("archive_percentage"))
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("Failed to retrieve archive statistics", e);
            throw new RuntimeException("Failed to retrieve archive statistics", e);
        }
    }

    // Helper methods for logging activities

    private Long logMigrationStart(String migrationType, String sourceTable, String initiatedBy) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO data_migration_log (migration_type, source_table, initiated_by) " +
                        "VALUES (?, ?, ?) RETURNING id",
                Long.class, migrationType, sourceTable, initiatedBy);
    }

    private void logMigrationCompletion(Long logId, int migrated, int failed, String status, String errorDetails) {
        jdbcTemplate.update(
                "UPDATE data_migration_log SET records_migrated = ?, records_failed = ?, " +
                        "completed_at = NOW(), status = ?, error_details = ? WHERE id = ?",
                migrated, failed, status, errorDetails, logId);
    }

    private Long logCleanupStart(String cleanupType, int retentionDays, boolean dryRun, String initiatedBy) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO data_cleanup_log (cleanup_type, retention_days, dry_run, initiated_by) " +
                        "VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, cleanupType, retentionDays, dryRun, initiatedBy);
    }

    private void logCleanupCompletion(Long logId, int deleted, int preserved, String status, String notes) {
        jdbcTemplate.update(
                "UPDATE data_cleanup_log SET records_deleted = ?, records_preserved = ?, " +
                        "completed_at = NOW(), duration = NOW() - started_at, status = ?, notes = ? WHERE id = ?",
                deleted, preserved, status, notes, logId);
    }

    private Long logArchiveStart(String archiveType, int archiveDays, boolean dryRun, String initiatedBy) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO data_archive_log (archive_type, archive_days, dry_run, initiated_by) " +
                        "VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, archiveType, archiveDays, dryRun, initiatedBy);
    }

    private void logArchiveCompletion(Long logId, int archived, int restored, String status, String notes) {
        jdbcTemplate.update(
                "UPDATE data_archive_log SET records_archived = ?, records_restored = ?, " +
                        "completed_at = NOW(), duration = NOW() - started_at, status = ?, notes = ? WHERE id = ?",
                archived, restored, status, notes, logId);
    }
}