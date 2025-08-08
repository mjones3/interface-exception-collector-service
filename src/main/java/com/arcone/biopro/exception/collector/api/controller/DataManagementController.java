package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.application.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative data management operations.
 * Provides endpoints for data migration, cleanup, archiving, and validation.
 */
@RestController
@RequestMapping("/api/v1/admin/data-management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Management", description = "Administrative data management operations")
public class DataManagementController {

    private final DataManagementService dataManagementService;
    private final DataValidationService dataValidationService;

    @PostMapping("/migrate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Migrate legacy exception data", description = "Migrates exception data from an external source table")
    public ResponseEntity<MigrationResult> migrateLegacyExceptions(
            @Parameter(description = "Source table name") @RequestParam String sourceTable,
            @Parameter(description = "Batch size for processing") @RequestParam(defaultValue = "1000") int batchSize,
            @Parameter(description = "Dry run mode") @RequestParam(defaultValue = "true") boolean dryRun) {

        log.info("Migration request received: sourceTable={}, batchSize={}, dryRun={}",
                sourceTable, batchSize, dryRun);

        MigrationResult result = dataManagementService.migrateLegacyExceptions(sourceTable, batchSize, dryRun);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate data integrity", description = "Performs comprehensive data integrity validation")
    public ResponseEntity<ValidationResult> validateDataIntegrity() {
        log.info("Data validation request received");

        ValidationResult result = dataManagementService.validateDataIntegrity();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/cleanup/old")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup old exceptions", description = "Removes old exception records based on retention policy")
    public ResponseEntity<CleanupResult> cleanupOldExceptions(
            @Parameter(description = "Retention period in days") @RequestParam(defaultValue = "365") int retentionDays,
            @Parameter(description = "Batch size for processing") @RequestParam(defaultValue = "1000") int batchSize,
            @Parameter(description = "Dry run mode") @RequestParam(defaultValue = "true") boolean dryRun,
            @Parameter(description = "Preserve critical exceptions") @RequestParam(defaultValue = "true") boolean preserveCritical) {

        log.info("Old exceptions cleanup request: retentionDays={}, batchSize={}, dryRun={}, preserveCritical={}",
                retentionDays, batchSize, dryRun, preserveCritical);

        CleanupResult result = dataManagementService.cleanupOldExceptions(
                retentionDays, batchSize, dryRun, preserveCritical);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/cleanup/resolved")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup resolved exceptions", description = "Removes old resolved exception records")
    public ResponseEntity<CleanupResult> cleanupResolvedExceptions(
            @Parameter(description = "Retention period for resolved exceptions in days") @RequestParam(defaultValue = "90") int resolvedRetentionDays,
            @Parameter(description = "Batch size for processing") @RequestParam(defaultValue = "1000") int batchSize,
            @Parameter(description = "Dry run mode") @RequestParam(defaultValue = "true") boolean dryRun) {

        log.info("Resolved exceptions cleanup request: retentionDays={}, batchSize={}, dryRun={}",
                resolvedRetentionDays, batchSize, dryRun);

        CleanupResult result = dataManagementService.cleanupResolvedExceptions(
                resolvedRetentionDays, batchSize, dryRun);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archive old exceptions", description = "Archives old exception records to archive tables")
    public ResponseEntity<ArchiveResult> archiveOldExceptions(
            @Parameter(description = "Archive period in days") @RequestParam(defaultValue = "730") int archiveDays,
            @Parameter(description = "Batch size for processing") @RequestParam(defaultValue = "1000") int batchSize,
            @Parameter(description = "Dry run mode") @RequestParam(defaultValue = "true") boolean dryRun,
            @Parameter(description = "Preserve critical exceptions") @RequestParam(defaultValue = "true") boolean preserveCritical) {

        log.info("Archive request: archiveDays={}, batchSize={}, dryRun={}, preserveCritical={}",
                archiveDays, batchSize, dryRun, preserveCritical);

        ArchiveResult result = dataManagementService.archiveOldExceptions(
                archiveDays, batchSize, dryRun, preserveCritical);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore archived exceptions", description = "Restores archived exception records back to main tables")
    public ResponseEntity<RestoreResult> restoreArchivedExceptions(
            @Parameter(description = "List of transaction IDs to restore") @RequestBody List<String> transactionIds) {

        log.info("Restore request for {} transaction IDs", transactionIds.size());

        RestoreResult result = dataManagementService.restoreArchivedExceptions(transactionIds);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/statistics/cleanup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Get cleanup statistics", description = "Retrieves statistics about data that can be cleaned up")
    public ResponseEntity<List<CleanupStatistic>> getCleanupStatistics() {
        log.info("Cleanup statistics request received");

        List<CleanupStatistic> statistics = dataManagementService.getCleanupStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Get archive statistics", description = "Retrieves statistics about archived vs active data")
    public ResponseEntity<List<ArchiveStatistic>> getArchiveStatistics() {
        log.info("Archive statistics request received");

        List<ArchiveStatistic> statistics = dataManagementService.getArchiveStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/validate/full")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perform full data validation", description = "Runs comprehensive data integrity validation")
    public ResponseEntity<ValidationResult> performFullValidation() {
        log.info("Full data validation request received");

        ValidationResult result = dataValidationService.performFullValidation();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/validate/specific")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Validate specific exceptions", description = "Validates specific exception records by transaction IDs")
    public ResponseEntity<ValidationResult> validateSpecificExceptions(
            @Parameter(description = "List of transaction IDs to validate") @RequestBody List<String> transactionIds) {

        log.info("Specific validation request for {} transaction IDs", transactionIds.size());

        ValidationResult result = dataValidationService.validateSpecificExceptions(transactionIds);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/validate/referential-integrity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate referential integrity", description = "Validates referential integrity between tables")
    public ResponseEntity<ValidationResult> validateReferentialIntegrity() {
        log.info("Referential integrity validation request received");

        ValidationResult result = dataValidationService.validateReferentialIntegrity();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/validate/archive-consistency")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate archive consistency", description = "Validates consistency between main and archive tables")
    public ResponseEntity<ValidationResult> validateArchiveConsistency() {
        log.info("Archive consistency validation request received");

        ValidationResult result = dataValidationService.validateArchiveConsistency();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/validate/business-rules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate business rules", description = "Validates business rule compliance")
    public ResponseEntity<ValidationResult> validateBusinessRules() {
        log.info("Business rules validation request received");

        ValidationResult result = dataValidationService.validateBusinessRules();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }
}