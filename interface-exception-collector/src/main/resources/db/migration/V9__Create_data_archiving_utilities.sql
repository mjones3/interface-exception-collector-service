-- Data Archiving Utilities
-- This migration creates tables and procedures for long-term data archiving

-- Create archive tables with same structure as main tables
CREATE TABLE IF NOT EXISTS interface_exceptions_archive (
    LIKE interface_exceptions INCLUDING ALL
);

CREATE TABLE IF NOT EXISTS retry_attempts_archive (
    LIKE retry_attempts INCLUDING ALL
);

-- Add archive-specific columns
ALTER TABLE interface_exceptions_archive 
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS archived_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS archive_reason VARCHAR(255);

ALTER TABLE retry_attempts_archive 
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS archived_by VARCHAR(255);

-- Create indexes for archive tables
CREATE INDEX IF NOT EXISTS idx_exceptions_archive_transaction_id 
ON interface_exceptions_archive(transaction_id);

CREATE INDEX IF NOT EXISTS idx_exceptions_archive_archived_at 
ON interface_exceptions_archive(archived_at);

CREATE INDEX IF NOT EXISTS idx_exceptions_archive_interface_type 
ON interface_exceptions_archive(interface_type);

CREATE INDEX IF NOT EXISTS idx_retry_attempts_archive_exception_id 
ON retry_attempts_archive(exception_id);

-- Function to archive old exceptions
CREATE OR REPLACE FUNCTION archive_old_exceptions(
    p_archive_days INTEGER DEFAULT 730, -- 2 years
    p_batch_size INTEGER DEFAULT 1000,
    p_dry_run BOOLEAN DEFAULT true,
    p_preserve_critical BOOLEAN DEFAULT true,
    p_archived_by VARCHAR(255) DEFAULT 'system'
) RETURNS TABLE(
    archived_exceptions INTEGER,
    archived_retry_attempts INTEGER,
    preserved_critical INTEGER,
    archive_duration INTERVAL
) AS $$
DECLARE
    v_archived_exceptions INTEGER := 0;
    v_archived_retry_attempts INTEGER := 0;
    v_preserved_critical INTEGER := 0;
    v_start_time TIMESTAMP := NOW();
    v_cutoff_date TIMESTAMP;
    v_batch_count INTEGER;
    v_current_batch INTEGER := 0;
    exception_records RECORD;
    retry_records RECORD;
BEGIN
    -- Calculate cutoff date
    v_cutoff_date := NOW() - INTERVAL '1 day' * p_archive_days;
    
    RAISE NOTICE 'Starting archival of exceptions older than % days (cutoff: %)', 
        p_archive_days, v_cutoff_date;
    
    -- Count critical exceptions that would be preserved
    IF p_preserve_critical THEN
        SELECT COUNT(*) INTO v_preserved_critical
        FROM interface_exceptions
        WHERE processed_at < v_cutoff_date
        AND severity = 'CRITICAL';
        
        RAISE NOTICE 'Preserving % critical exceptions from archival', v_preserved_critical;
    END IF;
    
    -- Get total batches needed
    SELECT CEIL(COUNT(*)::DECIMAL / p_batch_size) INTO v_batch_count
    FROM interface_exceptions
    WHERE processed_at < v_cutoff_date
    AND (NOT p_preserve_critical OR severity != 'CRITICAL');
    
    RAISE NOTICE 'Processing % batches of % records each', v_batch_count, p_batch_size;
    
    -- Process archival in batches
    WHILE v_current_batch < v_batch_count LOOP
        -- Archive exceptions batch
        FOR exception_records IN
            SELECT *
            FROM interface_exceptions
            WHERE processed_at < v_cutoff_date
            AND (NOT p_preserve_critical OR severity != 'CRITICAL')
            ORDER BY id
            LIMIT p_batch_size
            OFFSET v_current_batch * p_batch_size
        LOOP
            IF NOT p_dry_run THEN
                -- Insert into archive table
                INSERT INTO interface_exceptions_archive (
                    id, transaction_id, interface_type, exception_reason, operation,
                    external_id, status, severity, category, retryable, customer_id,
                    location_code, timestamp, processed_at, acknowledged_at,
                    acknowledged_by, resolved_at, resolved_by, retry_count,
                    last_retry_at, created_at, updated_at, archived_by, archive_reason
                ) VALUES (
                    exception_records.id, exception_records.transaction_id, 
                    exception_records.interface_type, exception_records.exception_reason,
                    exception_records.operation, exception_records.external_id,
                    exception_records.status, exception_records.severity,
                    exception_records.category, exception_records.retryable,
                    exception_records.customer_id, exception_records.location_code,
                    exception_records.timestamp, exception_records.processed_at,
                    exception_records.acknowledged_at, exception_records.acknowledged_by,
                    exception_records.resolved_at, exception_records.resolved_by,
                    exception_records.retry_count, exception_records.last_retry_at,
                    exception_records.created_at, exception_records.updated_at,
                    p_archived_by, 'automated_archival'
                );
                
                -- Archive related retry attempts
                FOR retry_records IN
                    SELECT * FROM retry_attempts WHERE exception_id = exception_records.id
                LOOP
                    INSERT INTO retry_attempts_archive (
                        id, exception_id, attempt_number, status, initiated_by,
                        initiated_at, completed_at, result_success, result_message,
                        result_response_code, result_error_details, archived_by
                    ) VALUES (
                        retry_records.id, retry_records.exception_id,
                        retry_records.attempt_number, retry_records.status,
                        retry_records.initiated_by, retry_records.initiated_at,
                        retry_records.completed_at, retry_records.result_success,
                        retry_records.result_message, retry_records.result_response_code,
                        retry_records.result_error_details, p_archived_by
                    );
                    
                    v_archived_retry_attempts := v_archived_retry_attempts + 1;
                END LOOP;
                
                -- Delete from main tables
                DELETE FROM retry_attempts WHERE exception_id = exception_records.id;
                DELETE FROM interface_exceptions WHERE id = exception_records.id;
            END IF;
            
            v_archived_exceptions := v_archived_exceptions + 1;
        END LOOP;
        
        v_current_batch := v_current_batch + 1;
        
        -- Progress logging every 10 batches
        IF v_current_batch % 10 = 0 THEN
            RAISE NOTICE 'Processed batch % of %', v_current_batch, v_batch_count;
        END IF;
        
        -- Exit if no more records
        IF NOT FOUND THEN
            EXIT;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Archival completed: % exceptions, % retry attempts archived (dry_run: %)', 
        v_archived_exceptions, v_archived_retry_attempts, p_dry_run;
    
    RETURN QUERY SELECT 
        v_archived_exceptions,
        v_archived_retry_attempts,
        v_preserved_critical,
        NOW() - v_start_time;
END;
$$ LANGUAGE plpgsql;

-- Function to restore archived exceptions
CREATE OR REPLACE FUNCTION restore_archived_exceptions(
    p_transaction_ids TEXT[],
    p_restored_by VARCHAR(255) DEFAULT 'system'
) RETURNS TABLE(
    restored_exceptions INTEGER,
    restored_retry_attempts INTEGER,
    failed_restorations TEXT[]
) AS $$
DECLARE
    v_restored_exceptions INTEGER := 0;
    v_restored_retry_attempts INTEGER := 0;
    v_failed_restorations TEXT[] := ARRAY[]::TEXT[];
    v_transaction_id TEXT;
    exception_record RECORD;
    retry_record RECORD;
BEGIN
    RAISE NOTICE 'Starting restoration of % archived exceptions', array_length(p_transaction_ids, 1);
    
    -- Process each transaction ID
    FOREACH v_transaction_id IN ARRAY p_transaction_ids LOOP
        BEGIN
            -- Find archived exception
            SELECT * INTO exception_record
            FROM interface_exceptions_archive
            WHERE transaction_id = v_transaction_id;
            
            IF NOT FOUND THEN
                v_failed_restorations := array_append(v_failed_restorations, 
                    format('Transaction %s not found in archive', v_transaction_id));
                CONTINUE;
            END IF;
            
            -- Check if already exists in main table
            IF EXISTS (SELECT 1 FROM interface_exceptions WHERE transaction_id = v_transaction_id) THEN
                v_failed_restorations := array_append(v_failed_restorations, 
                    format('Transaction %s already exists in main table', v_transaction_id));
                CONTINUE;
            END IF;
            
            -- Restore exception to main table
            INSERT INTO interface_exceptions (
                id, transaction_id, interface_type, exception_reason, operation,
                external_id, status, severity, category, retryable, customer_id,
                location_code, timestamp, processed_at, acknowledged_at,
                acknowledged_by, resolved_at, resolved_by, retry_count,
                last_retry_at, created_at, updated_at
            ) VALUES (
                exception_record.id, exception_record.transaction_id,
                exception_record.interface_type, exception_record.exception_reason,
                exception_record.operation, exception_record.external_id,
                exception_record.status, exception_record.severity,
                exception_record.category, exception_record.retryable,
                exception_record.customer_id, exception_record.location_code,
                exception_record.timestamp, exception_record.processed_at,
                exception_record.acknowledged_at, exception_record.acknowledged_by,
                exception_record.resolved_at, exception_record.resolved_by,
                exception_record.retry_count, exception_record.last_retry_at,
                exception_record.created_at, NOW()
            );
            
            -- Restore retry attempts
            FOR retry_record IN
                SELECT * FROM retry_attempts_archive WHERE exception_id = exception_record.id
            LOOP
                INSERT INTO retry_attempts (
                    id, exception_id, attempt_number, status, initiated_by,
                    initiated_at, completed_at, result_success, result_message,
                    result_response_code, result_error_details
                ) VALUES (
                    retry_record.id, retry_record.exception_id,
                    retry_record.attempt_number, retry_record.status,
                    retry_record.initiated_by, retry_record.initiated_at,
                    retry_record.completed_at, retry_record.result_success,
                    retry_record.result_message, retry_record.result_response_code,
                    retry_record.result_error_details
                );
                
                v_restored_retry_attempts := v_restored_retry_attempts + 1;
            END LOOP;
            
            v_restored_exceptions := v_restored_exceptions + 1;
            
        EXCEPTION WHEN OTHERS THEN
            v_failed_restorations := array_append(v_failed_restorations, 
                format('Transaction %s: %s', v_transaction_id, SQLERRM));
        END;
    END LOOP;
    
    RAISE NOTICE 'Restoration completed: % exceptions, % retry attempts restored', 
        v_restored_exceptions, v_restored_retry_attempts;
    
    RETURN QUERY SELECT v_restored_exceptions, v_restored_retry_attempts, v_failed_restorations;
END;
$$ LANGUAGE plpgsql;

-- Function to get archive statistics
CREATE OR REPLACE FUNCTION get_archive_statistics()
RETURNS TABLE(
    metric_name TEXT,
    main_table_count BIGINT,
    archive_table_count BIGINT,
    total_count BIGINT,
    archive_percentage DECIMAL
) AS $$
DECLARE
    v_main_exceptions BIGINT;
    v_archive_exceptions BIGINT;
    v_main_retries BIGINT;
    v_archive_retries BIGINT;
BEGIN
    -- Get exception counts
    SELECT COUNT(*) INTO v_main_exceptions FROM interface_exceptions;
    SELECT COUNT(*) INTO v_archive_exceptions FROM interface_exceptions_archive;
    
    -- Get retry attempt counts
    SELECT COUNT(*) INTO v_main_retries FROM retry_attempts;
    SELECT COUNT(*) INTO v_archive_retries FROM retry_attempts_archive;
    
    -- Return statistics
    RETURN QUERY SELECT 
        'exceptions'::TEXT,
        v_main_exceptions,
        v_archive_exceptions,
        v_main_exceptions + v_archive_exceptions,
        CASE WHEN v_main_exceptions + v_archive_exceptions > 0 
             THEN ROUND((v_archive_exceptions::DECIMAL / (v_main_exceptions + v_archive_exceptions)) * 100, 2)
             ELSE 0 END;
    
    RETURN QUERY SELECT 
        'retry_attempts'::TEXT,
        v_main_retries,
        v_archive_retries,
        v_main_retries + v_archive_retries,
        CASE WHEN v_main_retries + v_archive_retries > 0 
             THEN ROUND((v_archive_retries::DECIMAL / (v_main_retries + v_archive_retries)) * 100, 2)
             ELSE 0 END;
END;
$$ LANGUAGE plpgsql;

-- Create archive log table to track archival activities
CREATE TABLE IF NOT EXISTS data_archive_log (
    id BIGSERIAL PRIMARY KEY,
    archive_type VARCHAR(100) NOT NULL,
    archive_days INTEGER,
    records_archived INTEGER NOT NULL DEFAULT 0,
    records_restored INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    duration INTERVAL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),
    dry_run BOOLEAN NOT NULL DEFAULT true,
    initiated_by VARCHAR(255) NOT NULL,
    notes TEXT
);

COMMENT ON TABLE data_archive_log IS 'Tracks data archival and restoration activities';
COMMENT ON TABLE interface_exceptions_archive IS 'Archive table for old interface exceptions';
COMMENT ON TABLE retry_attempts_archive IS 'Archive table for retry attempts of archived exceptions';