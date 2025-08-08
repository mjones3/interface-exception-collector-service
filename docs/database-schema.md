# Database Schema Documentation

## Overview

The Interface Exception Collector Service uses PostgreSQL as its primary database with Flyway for database migrations. The schema is designed to efficiently store and query exception events from BioPro interface services.

## Tables

### interface_exceptions

The main table that stores all exception events captured from interface services.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-generated unique identifier |
| transaction_id | VARCHAR(255) | NOT NULL, UNIQUE | Unique transaction identifier from source event |
| interface_type | VARCHAR(50) | NOT NULL, CHECK | Type of interface (ORDER, COLLECTION, DISTRIBUTION, RECRUITMENT) |
| exception_reason | TEXT | NOT NULL | Detailed reason for the exception |
| operation | VARCHAR(100) | NOT NULL | Operation being performed when exception occurred |
| external_id | VARCHAR(255) | | External identifier (order ID, collection ID, etc.) |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'NEW' | Current exception status |
| severity | VARCHAR(50) | NOT NULL, DEFAULT 'MEDIUM' | Exception severity level |
| category | VARCHAR(50) | NOT NULL | Exception category classification |
| retryable | BOOLEAN | NOT NULL, DEFAULT true | Whether exception can be retried |
| customer_id | VARCHAR(255) | | Associated customer identifier |
| location_code | VARCHAR(100) | | Location where exception occurred |
| timestamp | TIMESTAMP WITH TIME ZONE | NOT NULL | When exception originally occurred |
| processed_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | When exception was processed |
| acknowledged_at | TIMESTAMP WITH TIME ZONE | | When exception was acknowledged |
| acknowledged_by | VARCHAR(255) | | Who acknowledged the exception |
| resolved_at | TIMESTAMP WITH TIME ZONE | | When exception was resolved |
| resolved_by | VARCHAR(255) | | Who resolved the exception |
| retry_count | INTEGER | NOT NULL, DEFAULT 0 | Number of retry attempts |
| last_retry_at | TIMESTAMP WITH TIME ZONE | | When last retry was attempted |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record last update timestamp |

#### Status Values
- `NEW`: Exception just captured
- `ACKNOWLEDGED`: Exception reviewed by operations team
- `RETRIED_SUCCESS`: Retry operation succeeded
- `RETRIED_FAILED`: Retry operation failed
- `ESCALATED`: Exception escalated for management attention
- `RESOLVED`: Exception resolved
- `CLOSED`: Exception investigation complete

#### Severity Values
- `LOW`: Minor issues with minimal impact
- `MEDIUM`: Standard exceptions requiring attention
- `HIGH`: Important exceptions requiring prompt action
- `CRITICAL`: Critical exceptions requiring immediate action

#### Category Values
- `BUSINESS_RULE`: Business logic validation failures
- `VALIDATION`: Data validation errors
- `SYSTEM_ERROR`: System-level errors
- `TIMEOUT`: Request timeout errors
- `NETWORK`: Network connectivity issues

### retry_attempts

Tracks all retry attempts made for exceptions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-generated unique identifier |
| exception_id | BIGINT | NOT NULL, FK | Reference to interface_exceptions.id |
| attempt_number | INTEGER | NOT NULL | Sequential retry attempt number |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'PENDING' | Retry attempt status |
| initiated_by | VARCHAR(255) | NOT NULL | Who initiated the retry |
| initiated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | When retry was started |
| completed_at | TIMESTAMP WITH TIME ZONE | | When retry was completed |
| result_success | BOOLEAN | | Whether retry succeeded |
| result_message | TEXT | | Result message from retry |
| result_response_code | INTEGER | | HTTP response code from retry |
| result_error_details | JSONB | | Detailed error information |

#### Constraints
- `UNIQUE(exception_id, attempt_number)`: Ensures unique attempt numbers per exception
- `FOREIGN KEY(exception_id)`: References interface_exceptions with CASCADE delete

## Indexes

### Performance Indexes

#### Single Column Indexes
- `idx_interface_exceptions_transaction_id`: Fast lookup by transaction ID
- `idx_interface_exceptions_interface_type`: Filter by interface type
- `idx_interface_exceptions_status`: Filter by exception status
- `idx_interface_exceptions_severity`: Filter by severity level
- `idx_interface_exceptions_customer_id`: Filter by customer
- `idx_interface_exceptions_external_id`: Lookup by external ID

#### Time-based Indexes
- `idx_interface_exceptions_timestamp`: Sort by exception occurrence time
- `idx_interface_exceptions_processed_at`: Sort by processing time
- `idx_interface_exceptions_created_at`: Sort by creation time

#### Composite Indexes
- `idx_interface_exceptions_type_status`: Filter by interface type and status
- `idx_interface_exceptions_severity_timestamp`: Filter by severity with time sorting
- `idx_interface_exceptions_status_timestamp`: Filter by status with time sorting
- `idx_interface_exceptions_customer_status`: Filter by customer and status

#### Retry-related Indexes
- `idx_interface_exceptions_retryable`: Filter retryable exceptions
- `idx_interface_exceptions_retry_count`: Filter by retry count
- `idx_interface_exceptions_last_retry_at`: Sort by last retry time

### Full-text Search Indexes

#### GIN Indexes for Text Search
- `idx_interface_exceptions_reason_fts`: Full-text search on exception_reason
- `idx_interface_exceptions_external_id_fts`: Full-text search on external_id
- `idx_interface_exceptions_combined_fts`: Combined search on reason and external_id
- `idx_interface_exceptions_operation_fts`: Full-text search on operation

These indexes enable fast text search using PostgreSQL's built-in full-text search capabilities with the `to_tsvector` function.

### Retry Attempts Indexes
- `idx_retry_attempts_exception_id`: Fast lookup of retries for an exception
- `idx_retry_attempts_status`: Filter retries by status
- `idx_retry_attempts_initiated_at`: Sort retries by initiation time
- `idx_retry_attempts_completed_at`: Sort retries by completion time
- `idx_retry_attempts_exception_attempt`: Composite index for retry history queries

## Triggers

### Audit Triggers
- `update_interface_exceptions_updated_at`: Automatically updates the `updated_at` timestamp when records are modified

## Migration Files

1. **V1__Create_interface_exceptions_table.sql**: Creates the main exceptions table
2. **V2__Create_retry_attempts_table.sql**: Creates the retry attempts table
3. **V3__Create_performance_indexes.sql**: Creates performance optimization indexes
4. **V4__Create_fulltext_search_indexes.sql**: Creates full-text search indexes
5. **V5__Create_audit_triggers.sql**: Creates audit triggers for timestamp management

## Query Performance Considerations

### Recommended Query Patterns

1. **Filter by interface type and status**:
   ```sql
   SELECT * FROM interface_exceptions 
   WHERE interface_type = 'ORDER' AND status = 'NEW'
   ORDER BY timestamp DESC;
   ```

2. **Full-text search on exception reason**:
   ```sql
   SELECT * FROM interface_exceptions 
   WHERE to_tsvector('english', exception_reason) @@ to_tsquery('english', 'order & exists');
   ```

3. **Customer-specific exceptions with status**:
   ```sql
   SELECT * FROM interface_exceptions 
   WHERE customer_id = 'CUST001' AND status IN ('NEW', 'ACKNOWLEDGED')
   ORDER BY severity DESC, timestamp DESC;
   ```

4. **Retry history for an exception**:
   ```sql
   SELECT * FROM retry_attempts 
   WHERE exception_id = 12345 
   ORDER BY attempt_number;
   ```

### Index Usage Tips

- Always include `interface_type` in WHERE clauses when possible
- Use time-based sorting with appropriate date range filters
- Leverage full-text search indexes for text-based queries
- Consider using composite indexes for multi-column filters

## Maintenance

### Regular Maintenance Tasks

1. **Analyze table statistics**: Run `ANALYZE` on tables after bulk operations
2. **Reindex**: Periodically reindex full-text search indexes
3. **Vacuum**: Regular vacuum operations to reclaim space
4. **Monitor index usage**: Check `pg_stat_user_indexes` for unused indexes

### Data Retention

Consider implementing data retention policies for:
- Old resolved exceptions (e.g., older than 1 year)
- Completed retry attempts (e.g., older than 6 months)
- Audit log data based on compliance requirements