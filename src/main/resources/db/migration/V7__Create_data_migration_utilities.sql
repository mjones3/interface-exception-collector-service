-- Data Migration Utilities
-- This migration creates stored procedures and functions for data migration tasks

-- Function to migrate legacy exception data from external systems
CREATE OR REPLACE FUNCTION migrate_legacy_exceptions(
    p_source_table TEXT,
    p_batch_size INTEGER DEFAULT 1000,
    p_dry_run BOOLEAN DEFAULT true
) RETURNS TABLE(
    migrated_count INTEGER,
    error_count INTEGER,
    validation_errors TEXT[]
) AS $$
DECLARE
    v_migrated_count INTEGER := 0;
    v_error_count INTEGER := 0;
    v_validation_errors TEXT[] := ARRAY[]::TEXT[];
    v_batch_start INTEGER := 0;
    v_batch_end INTEGER;
    v_total_records INTEGER;
    v_current_batch INTEGER;
    rec RECORD;
BEGIN
    -- Get total record count for progress tracking
    EXECUTE format('SELECT COUNT(*) FROM %I', p_source_table) INTO v_total_records;
    
    RAISE NOTICE 'Starting migration of % records from % (dry_run: %)', 
        v_total_records, p_source_table, p_dry_run;
    
    -- Process in batches
    WHILE v_batch_start < v_total_records LOOP
        v_batch_end := v_batch_start + p_batch_size;
        
        -- Process current batch
        FOR rec IN EXECUTE format(
            'SELECT * FROM %I ORDER BY id LIMIT %s OFFSET %s',
            p_source_table, p_batch_size, v_batch_start
        ) LOOP
            BEGIN
                -- Validate required fields
                IF rec.transaction_id IS NULL OR rec.interface_type IS NULL THEN
                    v_validation_errors := array_append(v_validation_errors, 
                        format('Record ID %s: Missing required fields', rec.id));
                    v_error_count := v_error_count + 1;
                    CONTINUE;
                END IF;
                
                -- Validate interface_type enum
                IF rec.interface_type NOT IN ('ORDER', 'COLLECTION', 'DISTRIBUTION', 'RECRUITMENT') THEN
                    v_validation_errors := array_append(v_validation_errors, 
                        format('Record ID %s: Invalid interface_type %s', rec.id, rec.interface_type));
                    v_error_count := v_error_count + 1;
                    CONTINUE;
                END IF;
                
                -- Insert migrated record if not dry run
                IF NOT p_dry_run THEN
                    INSERT INTO interface_exceptions (
                        transaction_id,
                        interface_type,
                        exception_reason,
                        operation,
                        external_id,
                        status,
                        severity,
                        category,
                        retryable,
                        customer_id,
                        location_code,
                        timestamp,
                        processed_at,
                        retry_count
                    ) VALUES (
                        rec.transaction_id,
                        rec.interface_type,
                        COALESCE(rec.exception_reason, 'Migrated legacy exception'),
                        COALESCE(rec.operation, 'UNKNOWN'),
                        rec.external_id,
                        COALESCE(rec.status, 'NEW'),
                        COALESCE(rec.severity, 'MEDIUM'),
                        COALESCE(rec.category, 'SYSTEM_ERROR'),
                        COALESCE(rec.retryable, true),
                        rec.customer_id,
                        rec.location_code,
                        COALESCE(rec.timestamp, rec.created_at, NOW()),
                        NOW(),
                        COALESCE(rec.retry_count, 0)
                    ) ON CONFLICT (transaction_id) DO NOTHING;
                END IF;
                
                v_migrated_count := v_migrated_count + 1;
                
            EXCEPTION WHEN OTHERS THEN
                v_validation_errors := array_append(v_validation_errors, 
                    format('Record ID %s: %s', rec.id, SQLERRM));
                v_error_count := v_error_count + 1;
            END;
        END LOOP;
        
        v_batch_start := v_batch_end;
        
        -- Progress logging
        RAISE NOTICE 'Processed batch: % / % records', v_batch_end, v_total_records;
    END LOOP;
    
    RAISE NOTICE 'Migration completed: % migrated, % errors', v_migrated_count, v_error_count;
    
    RETURN QUERY SELECT v_migrated_count, v_error_count, v_validation_errors;
END;
$$ LANGUAGE plpgsql;

-- Function to validate data integrity after migration
CREATE OR REPLACE FUNCTION validate_migrated_data()
RETURNS TABLE(
    validation_type TEXT,
    issue_count INTEGER,
    sample_issues TEXT[]
) AS $$
DECLARE
    v_orphaned_retries INTEGER;
    v_invalid_statuses INTEGER;
    v_invalid_severities INTEGER;
    v_missing_timestamps INTEGER;
    v_duplicate_transactions INTEGER;
    v_sample_issues TEXT[];
BEGIN
    -- Check for orphaned retry attempts
    SELECT COUNT(*) INTO v_orphaned_retries
    FROM retry_attempts ra
    LEFT JOIN interface_exceptions ie ON ra.exception_id = ie.id
    WHERE ie.id IS NULL;
    
    IF v_orphaned_retries > 0 THEN
        SELECT array_agg(ra.id::TEXT) INTO v_sample_issues
        FROM retry_attempts ra
        LEFT JOIN interface_exceptions ie ON ra.exception_id = ie.id
        WHERE ie.id IS NULL
        LIMIT 5;
        
        RETURN QUERY SELECT 'orphaned_retry_attempts'::TEXT, v_orphaned_retries, v_sample_issues;
    END IF;
    
    -- Check for invalid status values
    SELECT COUNT(*) INTO v_invalid_statuses
    FROM interface_exceptions
    WHERE status NOT IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_SUCCESS', 'RETRIED_FAILED', 'ESCALATED', 'RESOLVED', 'CLOSED');
    
    IF v_invalid_statuses > 0 THEN
        SELECT array_agg(format('ID: %s, Status: %s', id, status)) INTO v_sample_issues
        FROM interface_exceptions
        WHERE status NOT IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_SUCCESS', 'RETRIED_FAILED', 'ESCALATED', 'RESOLVED', 'CLOSED')
        LIMIT 5;
        
        RETURN QUERY SELECT 'invalid_status_values'::TEXT, v_invalid_statuses, v_sample_issues;
    END IF;
    
    -- Check for invalid severity values
    SELECT COUNT(*) INTO v_invalid_severities
    FROM interface_exceptions
    WHERE severity NOT IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
    
    IF v_invalid_severities > 0 THEN
        SELECT array_agg(format('ID: %s, Severity: %s', id, severity)) INTO v_sample_issues
        FROM interface_exceptions
        WHERE severity NOT IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
        LIMIT 5;
        
        RETURN QUERY SELECT 'invalid_severity_values'::TEXT, v_invalid_severities, v_sample_issues;
    END IF;
    
    -- Check for missing timestamps
    SELECT COUNT(*) INTO v_missing_timestamps
    FROM interface_exceptions
    WHERE timestamp IS NULL OR processed_at IS NULL;
    
    IF v_missing_timestamps > 0 THEN
        SELECT array_agg(format('ID: %s', id)) INTO v_sample_issues
        FROM interface_exceptions
        WHERE timestamp IS NULL OR processed_at IS NULL
        LIMIT 5;
        
        RETURN QUERY SELECT 'missing_timestamps'::TEXT, v_missing_timestamps, v_sample_issues;
    END IF;
    
    -- Check for duplicate transaction IDs
    SELECT COUNT(*) INTO v_duplicate_transactions
    FROM (
        SELECT transaction_id, COUNT(*) as cnt
        FROM interface_exceptions
        GROUP BY transaction_id
        HAVING COUNT(*) > 1
    ) duplicates;
    
    IF v_duplicate_transactions > 0 THEN
        SELECT array_agg(transaction_id) INTO v_sample_issues
        FROM (
            SELECT transaction_id
            FROM interface_exceptions
            GROUP BY transaction_id
            HAVING COUNT(*) > 1
            LIMIT 5
        ) duplicates;
        
        RETURN QUERY SELECT 'duplicate_transaction_ids'::TEXT, v_duplicate_transactions, v_sample_issues;
    END IF;
    
    -- If no issues found
    IF v_orphaned_retries = 0 AND v_invalid_statuses = 0 AND v_invalid_severities = 0 
       AND v_missing_timestamps = 0 AND v_duplicate_transactions = 0 THEN
        RETURN QUERY SELECT 'validation_passed'::TEXT, 0, ARRAY['No data integrity issues found']::TEXT[];
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create migration log table to track migration activities
CREATE TABLE IF NOT EXISTS data_migration_log (
    id BIGSERIAL PRIMARY KEY,
    migration_type VARCHAR(100) NOT NULL,
    source_table VARCHAR(255),
    records_processed INTEGER NOT NULL DEFAULT 0,
    records_migrated INTEGER NOT NULL DEFAULT 0,
    records_failed INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),
    error_details TEXT,
    initiated_by VARCHAR(255) NOT NULL
);

COMMENT ON TABLE data_migration_log IS 'Tracks data migration activities and their outcomes';