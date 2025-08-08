package com.arcone.biopro.exception.collector.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for data validation and integrity checking operations.
 * Provides comprehensive validation of exception data and relationships.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataValidationService {

    private final JdbcTemplate jdbcTemplate;
    private final MetricsService metricsService;

    /**
     * Performs comprehensive data integrity validation.
     *
     * @return Validation result with any issues found
     */
    public ValidationResult performFullValidation() {
        log.info("Starting comprehensive data integrity validation");

        try {
            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            // Run database function validation
            List<Map<String, Object>> dbResults = jdbcTemplate.queryForList(
                    "SELECT * FROM validate_migrated_data()");

            for (Map<String, Object> result : dbResults) {
                String validationType = (String) result.get("validation_type");
                Integer issueCount = (Integer) result.get("issue_count");
                String[] sampleIssues = (String[]) result.get("sample_issues");

                if (!"validation_passed".equals(validationType)) {
                    log.warn("Validation issue found: {} ({} issues)", validationType, issueCount);
                    builder.issue(ValidationIssue.builder()
                            .type(validationType)
                            .count(issueCount)
                            .sampleIssues(List.of(sampleIssues))
                            .build());
                }
            }

            // Additional custom validations
            performCustomValidations(builder);

            ValidationResult result = builder.build();

            // Record metrics
            metricsService.recordDataValidationMetrics(result);

            if (result.getIssues().isEmpty()) {
                log.info("Data integrity validation passed - no issues found");
            } else {
                log.warn("Data integrity validation found {} issue types", result.getIssues().size());
            }

            return result;

        } catch (Exception e) {
            log.error("Data integrity validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Validates specific exception records by transaction IDs.
     *
     * @param transactionIds List of transaction IDs to validate
     * @return Validation result for specific records
     */
    public ValidationResult validateSpecificExceptions(List<String> transactionIds) {
        log.info("Validating {} specific exception records", transactionIds.size());

        try {
            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            for (String transactionId : transactionIds) {
                validateSingleException(transactionId, builder);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Specific exception validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Validates referential integrity between tables.
     *
     * @return Validation result for referential integrity
     */
    public ValidationResult validateReferentialIntegrity() {
        log.info("Validating referential integrity");

        try {
            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            // Check for orphaned retry attempts
            List<Map<String, Object>> orphanedRetries = jdbcTemplate.queryForList(
                    "SELECT ra.id, ra.exception_id FROM retry_attempts ra " +
                            "LEFT JOIN interface_exceptions ie ON ra.exception_id = ie.id " +
                            "WHERE ie.id IS NULL LIMIT 10");

            if (!orphanedRetries.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("orphaned_retry_attempts")
                        .count(orphanedRetries.size())
                        .sampleIssues(orphanedRetries.stream()
                                .map(row -> String.format("Retry ID: %s, Exception ID: %s",
                                        row.get("id"), row.get("exception_id")))
                                .toList())
                        .build());
            }

            // Check for inconsistent retry counts
            List<Map<String, Object>> inconsistentCounts = jdbcTemplate.queryForList(
                    "SELECT ie.id, ie.retry_count, COUNT(ra.id) as actual_count " +
                            "FROM interface_exceptions ie " +
                            "LEFT JOIN retry_attempts ra ON ie.id = ra.exception_id " +
                            "GROUP BY ie.id, ie.retry_count " +
                            "HAVING ie.retry_count != COUNT(ra.id) LIMIT 10");

            if (!inconsistentCounts.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("inconsistent_retry_counts")
                        .count(inconsistentCounts.size())
                        .sampleIssues(inconsistentCounts.stream()
                                .map(row -> String.format("Exception ID: %s, Recorded: %s, Actual: %s",
                                        row.get("id"), row.get("retry_count"), row.get("actual_count")))
                                .toList())
                        .build());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Referential integrity validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Validates data consistency across archive and main tables.
     *
     * @return Validation result for archive consistency
     */
    public ValidationResult validateArchiveConsistency() {
        log.info("Validating archive consistency");

        try {
            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            // Check for duplicates between main and archive tables
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(
                    "SELECT ie.transaction_id FROM interface_exceptions ie " +
                            "INNER JOIN interface_exceptions_archive iea ON ie.transaction_id = iea.transaction_id " +
                            "LIMIT 10");

            if (!duplicates.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("duplicate_main_archive_records")
                        .count(duplicates.size())
                        .sampleIssues(duplicates.stream()
                                .map(row -> String.format("Transaction ID: %s", row.get("transaction_id")))
                                .toList())
                        .build());
            }

            // Check for archived retry attempts without archived exceptions
            List<Map<String, Object>> orphanedArchiveRetries = jdbcTemplate.queryForList(
                    "SELECT ra.id, ra.exception_id FROM retry_attempts_archive ra " +
                            "LEFT JOIN interface_exceptions_archive iea ON ra.exception_id = iea.id " +
                            "WHERE iea.id IS NULL LIMIT 10");

            if (!orphanedArchiveRetries.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("orphaned_archive_retry_attempts")
                        .count(orphanedArchiveRetries.size())
                        .sampleIssues(orphanedArchiveRetries.stream()
                                .map(row -> String.format("Archive Retry ID: %s, Exception ID: %s",
                                        row.get("id"), row.get("exception_id")))
                                .toList())
                        .build());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Archive consistency validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Validates business rule compliance.
     *
     * @return Validation result for business rules
     */
    public ValidationResult validateBusinessRules() {
        log.info("Validating business rule compliance");

        try {
            ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                    .success(true);

            // Check for resolved exceptions without resolution timestamp
            List<Map<String, Object>> missingResolutionTime = jdbcTemplate.queryForList(
                    "SELECT id, transaction_id, status FROM interface_exceptions " +
                            "WHERE status IN ('RESOLVED', 'CLOSED') AND resolved_at IS NULL LIMIT 10");

            if (!missingResolutionTime.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("missing_resolution_timestamp")
                        .count(missingResolutionTime.size())
                        .sampleIssues(missingResolutionTime.stream()
                                .map(row -> String.format("ID: %s, Transaction: %s, Status: %s",
                                        row.get("id"), row.get("transaction_id"), row.get("status")))
                                .toList())
                        .build());
            }

            // Check for acknowledged exceptions without acknowledgment timestamp
            List<Map<String, Object>> missingAckTime = jdbcTemplate.queryForList(
                    "SELECT id, transaction_id, status FROM interface_exceptions " +
                            "WHERE status = 'ACKNOWLEDGED' AND acknowledged_at IS NULL LIMIT 10");

            if (!missingAckTime.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("missing_acknowledgment_timestamp")
                        .count(missingAckTime.size())
                        .sampleIssues(missingAckTime.stream()
                                .map(row -> String.format("ID: %s, Transaction: %s",
                                        row.get("id"), row.get("transaction_id")))
                                .toList())
                        .build());
            }

            // Check for retry attempts on non-retryable exceptions
            List<Map<String, Object>> invalidRetries = jdbcTemplate.queryForList(
                    "SELECT ie.id, ie.transaction_id, ie.retryable, COUNT(ra.id) as retry_count " +
                            "FROM interface_exceptions ie " +
                            "INNER JOIN retry_attempts ra ON ie.id = ra.exception_id " +
                            "WHERE ie.retryable = false " +
                            "GROUP BY ie.id, ie.transaction_id, ie.retryable LIMIT 10");

            if (!invalidRetries.isEmpty()) {
                builder.issue(ValidationIssue.builder()
                        .type("retries_on_non_retryable_exceptions")
                        .count(invalidRetries.size())
                        .sampleIssues(invalidRetries.stream()
                                .map(row -> String.format("ID: %s, Transaction: %s, Retries: %s",
                                        row.get("id"), row.get("transaction_id"), row.get("retry_count")))
                                .toList())
                        .build());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Business rule validation failed", e);
            return ValidationResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Scheduled validation that runs daily to check data integrity.
     */
    @Scheduled(cron = "0 0 4 * * *") // Daily at 4 AM
    public void scheduledValidation() {
        log.info("Starting scheduled data integrity validation");

        try {
            ValidationResult result = performFullValidation();

            if (!result.isSuccess() || !result.getIssues().isEmpty()) {
                log.warn("Scheduled validation found issues: {}", result.getIssues().size());

                // Could send alerts here for critical issues
                for (ValidationIssue issue : result.getIssues()) {
                    if (issue.getCount() > 100) { // Threshold for critical issues
                        log.error("Critical validation issue: {} with {} occurrences",
                                issue.getType(), issue.getCount());
                    }
                }
            } else {
                log.info("Scheduled validation completed successfully - no issues found");
            }

        } catch (Exception e) {
            log.error("Scheduled validation failed", e);
        }
    }

    // Private helper methods

    private void performCustomValidations(ValidationResult.ValidationResultBuilder builder) {
        // Validate timestamp consistency
        validateTimestampConsistency(builder);

        // Validate enum values
        validateEnumValues(builder);

        // Validate required fields
        validateRequiredFields(builder);
    }

    private void validateSingleException(String transactionId, ValidationResult.ValidationResultBuilder builder) {
        // Check if exception exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM interface_exceptions WHERE transaction_id = ?",
                Integer.class, transactionId);

        if (count == 0) {
            builder.issue(ValidationIssue.builder()
                    .type("exception_not_found")
                    .count(1)
                    .sampleIssues(List.of("Transaction ID: " + transactionId))
                    .build());
            return;
        }

        // Validate specific exception data
        Map<String, Object> exception = jdbcTemplate.queryForMap(
                "SELECT * FROM interface_exceptions WHERE transaction_id = ?",
                transactionId);

        // Check required fields
        if (exception.get("exception_reason") == null ||
                ((String) exception.get("exception_reason")).trim().isEmpty()) {
            builder.issue(ValidationIssue.builder()
                    .type("missing_exception_reason")
                    .count(1)
                    .sampleIssues(List.of("Transaction ID: " + transactionId))
                    .build());
        }
    }

    private void validateTimestampConsistency(ValidationResult.ValidationResultBuilder builder) {
        // Check for processed_at before timestamp
        List<Map<String, Object>> inconsistentTimestamps = jdbcTemplate.queryForList(
                "SELECT id, transaction_id FROM interface_exceptions " +
                        "WHERE processed_at < timestamp LIMIT 10");

        if (!inconsistentTimestamps.isEmpty()) {
            builder.issue(ValidationIssue.builder()
                    .type("inconsistent_timestamps")
                    .count(inconsistentTimestamps.size())
                    .sampleIssues(inconsistentTimestamps.stream()
                            .map(row -> String.format("ID: %s, Transaction: %s",
                                    row.get("id"), row.get("transaction_id")))
                            .toList())
                    .build());
        }
    }

    private void validateEnumValues(ValidationResult.ValidationResultBuilder builder) {
        // This is handled by database constraints, but we can add additional checks
        // Check for any enum values that might have been inserted incorrectly

        List<Map<String, Object>> invalidStatuses = jdbcTemplate.queryForList(
                "SELECT id, status FROM interface_exceptions " +
                        "WHERE status NOT IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_SUCCESS', 'RETRIED_FAILED', 'ESCALATED', 'RESOLVED', 'CLOSED') "
                        +
                        "LIMIT 10");

        if (!invalidStatuses.isEmpty()) {
            builder.issue(ValidationIssue.builder()
                    .type("invalid_status_values")
                    .count(invalidStatuses.size())
                    .sampleIssues(invalidStatuses.stream()
                            .map(row -> String.format("ID: %s, Status: %s",
                                    row.get("id"), row.get("status")))
                            .toList())
                    .build());
        }
    }

    private void validateRequiredFields(ValidationResult.ValidationResultBuilder builder) {
        // Check for null or empty required fields
        List<Map<String, Object>> missingFields = jdbcTemplate.queryForList(
                "SELECT id, transaction_id FROM interface_exceptions " +
                        "WHERE transaction_id IS NULL OR interface_type IS NULL OR exception_reason IS NULL " +
                        "OR TRIM(exception_reason) = '' LIMIT 10");

        if (!missingFields.isEmpty()) {
            builder.issue(ValidationIssue.builder()
                    .type("missing_required_fields")
                    .count(missingFields.size())
                    .sampleIssues(missingFields.stream()
                            .map(row -> String.format("ID: %s, Transaction: %s",
                                    row.get("id"), row.get("transaction_id")))
                            .toList())
                    .build());
        }
    }
}