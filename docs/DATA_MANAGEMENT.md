# Data Management Utilities

This document describes the data migration, cleanup, archiving, and validation utilities for the Interface Exception Collector Service.

## Overview

The data management utilities provide comprehensive tools for:
- Migrating legacy exception data from external systems
- Cleaning up old exception records based on retention policies
- Archiving data for long-term storage
- Validating data integrity and consistency
- Automated backup and restore procedures

## Database Functions

### Migration Functions

#### `migrate_legacy_exceptions(source_table, batch_size, dry_run)`
Migrates exception data from an external source table to the main interface_exceptions table.

**Parameters:**
- `source_table` (TEXT): Name of the source table containing legacy data
- `batch_size` (INTEGER): Number of records to process in each batch (default: 1000)
- `dry_run` (BOOLEAN): If true, performs validation without actual migration (default: true)

**Returns:**
- `migrated_count` (INTEGER): Number of records successfully migrated
- `error_count` (INTEGER): Number of records that failed validation
- `validation_errors` (TEXT[]): Array of validation error messages

**Example:**
```sql
-- Dry run to validate data
SELECT * FROM migrate_legacy_exceptions('legacy_exceptions_table', 1000, true);

-- Actual migration
SELECT * FROM migrate_legacy_exceptions('legacy_exceptions_table', 1000, false);
```

### Cleanup Functions

#### `cleanup_old_exceptions(retention_days, batch_size, dry_run, preserve_critical)`
Removes old exception records based on retention policy.

**Parameters:**
- `retention_days` (INTEGER): Number of days to retain records (default: 365)
- `batch_size` (INTEGER): Number of records to process in each batch (default: 1000)
- `dry_run` (BOOLEAN): If true, performs analysis without actual deletion (default: true)
- `preserve_critical` (BOOLEAN): If true, preserves critical exceptions (default: true)

**Returns:**
- `deleted_exceptions` (INTEGER): Number of exceptions deleted
- `deleted_retry_attempts` (INTEGER): Number of retry attempts deleted
- `preserved_critical` (INTEGER): Number of critical exceptions preserved
- `cleanup_duration` (INTERVAL): Time taken for cleanup operation

#### `cleanup_resolved_exceptions(resolved_retention_days, batch_size, dry_run)`
Removes old resolved exception records.

**Parameters:**
- `resolved_retention_days` (INTEGER): Retention period for resolved exceptions (default: 90)
- `batch_size` (INTEGER): Batch size for processing (default: 1000)
- `dry_run` (BOOLEAN): Dry run mode (default: true)

### Archival Functions

#### `archive_old_exceptions(archive_days, batch_size, dry_run, preserve_critical, archived_by)`
Archives old exceptions to archive tables.

**Parameters:**
- `archive_days` (INTEGER): Number of days after which to archive (default: 730)
- `batch_size` (INTEGER): Batch size for processing (default: 1000)
- `dry_run` (BOOLEAN): Dry run mode (default: true)
- `preserve_critical` (BOOLEAN): Preserve critical exceptions (default: true)
- `archived_by` (VARCHAR): User or system performing archival (default: 'system')

#### `restore_archived_exceptions(transaction_ids, restored_by)`
Restores archived exceptions back to main tables.

**Parameters:**
- `transaction_ids` (TEXT[]): Array of transaction IDs to restore
- `restored_by` (VARCHAR): User performing restoration (default: 'system')

### Validation Functions

#### `validate_migrated_data()`
Performs comprehensive data integrity validation.

**Returns:**
- `validation_type` (TEXT): Type of validation performed
- `issue_count` (INTEGER): Number of issues found
- `sample_issues` (TEXT[]): Sample issue descriptions

### Statistics Functions

#### `get_cleanup_statistics()`
Provides statistics and recommendations for data cleanup.

#### `get_archive_statistics()`
Shows distribution between main and archive tables.

## REST API Endpoints

### Migration Endpoints

#### `POST /api/v1/admin/data-management/migrate`
Migrates legacy exception data.

**Parameters:**
- `sourceTable` (string): Source table name
- `batchSize` (int): Batch size (default: 1000)
- `dryRun` (boolean): Dry run mode (default: true)

**Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/data-management/migrate" \
  -H "Authorization: Bearer <token>" \
  -d "sourceTable=legacy_exceptions&batchSize=1000&dryRun=true"
```

### Cleanup Endpoints

#### `POST /api/v1/admin/data-management/cleanup/old`
Cleans up old exception records.

**Parameters:**
- `retentionDays` (int): Retention period in days (default: 365)
- `batchSize` (int): Batch size (default: 1000)
- `dryRun` (boolean): Dry run mode (default: true)
- `preserveCritical` (boolean): Preserve critical exceptions (default: true)

#### `POST /api/v1/admin/data-management/cleanup/resolved`
Cleans up resolved exception records.

### Archive Endpoints

#### `POST /api/v1/admin/data-management/archive`
Archives old exception records.

#### `POST /api/v1/admin/data-management/restore`
Restores archived exception records.

**Request Body:**
```json
["transaction-id-1", "transaction-id-2", "transaction-id-3"]
```

### Validation Endpoints

#### `POST /api/v1/admin/data-management/validate/full`
Performs comprehensive data validation.

#### `POST /api/v1/admin/data-management/validate/specific`
Validates specific exception records.

#### `POST /api/v1/admin/data-management/validate/referential-integrity`
Validates referential integrity between tables.

#### `POST /api/v1/admin/data-management/validate/archive-consistency`
Validates consistency between main and archive tables.

#### `POST /api/v1/admin/data-management/validate/business-rules`
Validates business rule compliance.

### Statistics Endpoints

#### `GET /api/v1/admin/data-management/statistics/cleanup`
Gets cleanup statistics and recommendations.

#### `GET /api/v1/admin/data-management/statistics/archive`
Gets archive statistics.

## Kubernetes CronJobs

### Database Backup CronJob

**Schedule:** Daily at 2 AM UTC
**File:** `k8s/backup-cronjob.yaml`

Features:
- Automated PostgreSQL database backups
- Upload to S3 with retention policy
- Backup verification and metadata generation
- Cleanup of old backups (30-day retention)

**Configuration:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backup-config
data:
  s3-bucket: "biopro-database-backups"
  aws-region: "us-west-2"
  retention-days: "30"
```

### Data Cleanup CronJob

**Schedule:** Weekly on Sunday at 3 AM UTC
**File:** `k8s/data-cleanup-cronjob.yaml`

Features:
- Automated data archival and cleanup
- Configurable retention policies
- Data integrity validation
- Statistics reporting

**Configuration:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: data-cleanup-config
data:
  retention-days: "730"        # 2 years
  resolved-retention-days: "90" # 90 days
  archive-days: "365"          # 1 year
  batch-size: "1000"
  preserve-critical: "true"
  dry-run: "true"              # Set to false for actual cleanup
```

## Scripts

### Database Restore Script

**File:** `scripts/restore-database.sh`

Features:
- Download backups from S3
- Restore to target database
- Multiple restore modes (full, data-only, schema-only)
- Data validation after restore

**Usage:**
```bash
# List available backups
./scripts/restore-database.sh --list-backups

# Restore full backup
./scripts/restore-database.sh \
  -f exception_collector_backup_20250805_020000.sql \
  -d exception_collector_db \
  --confirm

# Restore data only
./scripts/restore-database.sh \
  -f exception_collector_backup_20250805_020000.sql \
  -d exception_collector_db \
  -m data-only \
  --confirm
```

## Best Practices

### Migration
1. Always run migrations in dry-run mode first
2. Validate source data before migration
3. Monitor migration progress and logs
4. Verify data integrity after migration

### Cleanup
1. Start with small batch sizes for testing
2. Always preserve critical exceptions
3. Run cleanup during low-traffic periods
4. Monitor database performance during cleanup

### Archival
1. Archive data before cleanup to preserve history
2. Test restore procedures regularly
3. Monitor archive table sizes and performance
4. Consider partitioning for very large archives

### Validation
1. Run validation checks regularly
2. Address validation issues promptly
3. Monitor validation metrics and trends
4. Set up alerts for critical validation failures

## Monitoring and Alerting

### Metrics
- Data migration success/failure rates
- Cleanup operation duration and records processed
- Archive table sizes and growth rates
- Validation issue counts by type

### Alerts
- Migration failures
- Cleanup operation failures
- Critical validation issues (>100 occurrences)
- Archive consistency problems
- Backup failures

## Troubleshooting

### Common Issues

#### Migration Failures
- **Cause:** Invalid source data format
- **Solution:** Review validation errors and fix source data

#### Cleanup Timeouts
- **Cause:** Large batch sizes or database locks
- **Solution:** Reduce batch size and run during off-peak hours

#### Archive Inconsistencies
- **Cause:** Concurrent operations or failed transactions
- **Solution:** Run validation and restore procedures

#### Backup Failures
- **Cause:** S3 permissions or network issues
- **Solution:** Check AWS credentials and network connectivity

### Log Analysis
Monitor application logs for:
- Migration progress and errors
- Cleanup operation statistics
- Validation issue details
- Archive operation results

### Performance Optimization
- Use appropriate batch sizes based on system capacity
- Run operations during low-traffic periods
- Monitor database connection pool usage
- Consider table partitioning for very large datasets

## Security Considerations

### Access Control
- Admin role required for migration and cleanup operations
- Operator role can perform validation and view statistics
- Audit logging for all data management operations

### Data Protection
- Backup encryption in transit and at rest
- Secure handling of sensitive data during migration
- Proper cleanup of temporary files and logs

### Compliance
- Retention policy compliance
- Data archival requirements
- Audit trail maintenance