-- Data Cleanup Utilities
-- This migration creates stored procedures for data cleanup and retention management

-- Function to cleanup old exception records based on retention policies
CREATE OR REPLACE FUNCTION cleanup_old_exceptions(
    p_retention_days INTEGER DEFAULT 365,
    p_batch_size INTEGER DEFAULT 1000,
    p_dry_run BOOLEAN DEFAULT true,
    p_preserve_critical BOOLEAN DEFAULT true
) RETURNS TABLE(
    deleted_exceptions INTEGER,
    deleted_retry_attempts INTEGER,
    preserved_critical INTEGER,
    cleanup_duration INTERVAL
) AS $$
DECLARE
    v_deleted_exceptions INTEGER := 0;
    v_deleted_retry_attempts INTEGER := 0;
    v_preserved_critical INTEGER := 0;
    v_start_time TIMESTAMP := NOW();
    v_cutoff_date TIMESTAMP;
    v_batch_count INTEGER;
    v_current_batch INTEGER := 0;
    exception_ids BIGINT[];
BEGIN
    -- Calculate cutoff date
    v_cutoff_date := NOW() - INTERVAL '1 day' * p_retention_days;
    
    RAISE NOTICE 'Starting cleanup of exceptions older than % (cutoff: %)', 
        p_retention_days || ' days', v_cutoff_date;
    
    -- Count critical exceptions that would be preserved
    IF p_preserve_critical THEN
        SELECT COUNT(*) INTO v_preserved_critical
        FROM interface_exceptions
        WHERE processed_at < v_cutoff_date
        AND severity = 'CRITICAL';
        
        RAISE NOTICE 'Preserving % critical exceptions', v_preserved_critical;
    END IF;
    
    -- Get total batches needed
    SELECT CEIL(COUNT(*)::DECIMAL / p_batch_size) INTO v_batch_count
    FROM interface_exceptions
    WHERE processed_at < v_cutoff_date
    AND (NOT p_preserve_critical OR severity != 'CRITICAL');
    
    RAISE NOTICE 'Processing % batches of % records each', v_batch_count, p_batch_size;
    
    -- Process cleanup in batches
    WHILE v_current_batch < v_batch_count LOOP
        -- Get batch of exception IDs to delete
        SELECT array_agg(id) INTO exception_ids
        FROM (
            SELECT id
            FROM interface_exceptions
            WHERE processed_at < v_cutoff_date
            AND (NOT p_preserve_critical OR severity != 'CRITICAL')
            ORDER BY id
            LIMIT p_batch_size
            OFFSET v_current_batch * p_batch_size
        ) batch_exceptions;
        
        -- Skip if no more records
        IF exception_ids IS NULL OR array_length(exception_ids, 1) = 0 THEN
            EXIT;
        END IF;
        
        IF NOT p_dry_run THEN
            -- Delete retry attempts first (due to foreign key constraint)
            DELETE FROM retry_attempts
            WHERE exception_id = ANY(exception_ids);
            
            GET DIAGNOSTICS v_deleted_retry_attempts = ROW_COUNT;
            
            -- Delete exceptions
            DELETE FROM interface_exceptions
            WHERE id = ANY(exception_ids);
            
            GET DIAGNOSTICS v_deleted_exceptions = ROW_COUNT;
        ELSE
            -- Count what would be deleted in dry run
            SELECT COUNT(*) INTO v_deleted_retry_attempts
            FROM retry_attempts
            WHERE exception_id = ANY(exception_ids);
            
            v_deleted_exceptions := array_length(exception_ids, 1);
        END IF;
        
        v_current_batch := v_current_batch + 1;
        
        -- Progress logging every 10 batches
        IF v_current_batch % 10 = 0 THEN
            RAISE NOTICE 'Processed batch % of %', v_current_batch, v_batch_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Cleanup completed: % exceptions, % retry attempts deleted (dry_run: %)', 
        v_deleted_exceptions, v_deleted_retry_attempts, p_dry_run;
    
    RETURN QUERY SELECT 
        v_deleted_exceptions,
        v_deleted_retry_attempts,
        v_preserved_critical,
        NOW() - v_start_time;
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup resolved exceptions older than specified days
CREATE OR REPLACE FUNCTION cleanup_resolved_exceptions(
    p_resolved_retention_days INTEGER DEFAULT 90,
    p_batch_size INTEGER DEFAULT 1000,
    p_dry_run BOOLEAN DEFAULT true
) RETURNS TABLE(
    deleted_count INTEGER,
    cleanup_duration INTERVAL
) AS $$
DECLARE
    v_deleted_count INTEGER := 0;
    v_start_time TIMESTAMP := NOW();
    v_cutoff_date TIMESTAMP;
    v_batch_deleted INTEGER;
BEGIN
    -- Calculate cutoff date for resolved exceptions
    v_cutoff_date := NOW() - INTERVAL '1 day' * p_resolved_retention_days;
    
    RAISE NOTICE 'Cleaning up resolved exceptions older than % days (cutoff: %)', 
        p_resolved_retention_days, v_cutoff_date;
    
    -- Process in batches
    LOOP
        IF NOT p_dry_run THEN
            -- Delete retry attempts first
            DELETE FROM retry_attempts
            WHERE exception_id IN (
                SELECT id FROM interface_exceptions
                WHERE status IN ('RESOLVED', 'CLOSED')
                AND resolved_at < v_cutoff_date
                LIMIT p_batch_size
            );
            
            -- Delete resolved exceptions
            DELETE FROM interface_exceptions
            WHERE id IN (
                SELECT id FROM interface_exceptions
                WHERE status IN ('RESOLVED', 'CLOSED')
                AND resolved_at < v_cutoff_date
                LIMIT p_batch_size
            );
            
            GET DIAGNOSTICS v_batch_deleted = ROW_COUNT;
        ELSE
            -- Count what would be deleted in dry run
            SELECT COUNT(*) INTO v_batch_deleted
            FROM interface_exceptions
            WHERE status IN ('RESOLVED', 'CLOSED')
            AND resolved_at < v_cutoff_date
            LIMIT p_batch_size;
        END IF;
        
        v_deleted_count := v_deleted_count + v_batch_deleted;
        
        -- Exit if no more records to process
        EXIT WHEN v_batch_deleted = 0;
        
        -- Progress logging
        IF v_deleted_count % (p_batch_size * 10) = 0 THEN
            RAISE NOTICE 'Deleted % resolved exceptions so far', v_deleted_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Resolved exceptions cleanup completed: % deleted (dry_run: %)', 
        v_deleted_count, p_dry_run;
    
    RETURN QUERY SELECT v_deleted_count, NOW() - v_start_time;
END;
$$ LANGUAGE plpgsql;

-- Function to get cleanup statistics and recommendations
CREATE OR REPLACE FUNCTION get_cleanup_statistics()
RETURNS TABLE(
    metric_name TEXT,
    metric_value BIGINT,
    recommendation TEXT
) AS $$
DECLARE
    v_total_exceptions BIGINT;
    v_old_exceptions BIGINT;
    v_resolved_exceptions BIGINT;
    v_critical_exceptions BIGINT;
    v_avg_age_days INTEGER;
    v_db_size_mb BIGINT;
BEGIN
    -- Get total exception count
    SELECT COUNT(*) INTO v_total_exceptions FROM interface_exceptions;
    
    -- Get old exceptions (> 1 year)
    SELECT COUNT(*) INTO v_old_exceptions
    FROM interface_exceptions
    WHERE processed_at < NOW() - INTERVAL '365 days';
    
    -- Get resolved exceptions (> 90 days)
    SELECT COUNT(*) INTO v_resolved_exceptions
    FROM interface_exceptions
    WHERE status IN ('RESOLVED', 'CLOSED')
    AND resolved_at < NOW() - INTERVAL '90 days';
    
    -- Get critical exceptions
    SELECT COUNT(*) INTO v_critical_exceptions
    FROM interface_exceptions
    WHERE severity = 'CRITICAL';
    
    -- Get average age in days
    SELECT EXTRACT(DAYS FROM AVG(NOW() - processed_at))::INTEGER INTO v_avg_age_days
    FROM interface_exceptions;
    
    -- Get database size (approximate)
    SELECT pg_total_relation_size('interface_exceptions') / (1024 * 1024) INTO v_db_size_mb;
    
    -- Return statistics with recommendations
    RETURN QUERY SELECT 'total_exceptions'::TEXT, v_total_exceptions, 
        CASE WHEN v_total_exceptions > 1000000 THEN 'Consider implementing automated cleanup'
             ELSE 'Exception count is manageable' END;
    
    RETURN QUERY SELECT 'old_exceptions_1year'::TEXT, v_old_exceptions,
        CASE WHEN v_old_exceptions > 100000 THEN 'Recommend cleanup of old exceptions'
             ELSE 'Old exception count is acceptable' END;
    
    RETURN QUERY SELECT 'resolved_exceptions_90days'::TEXT, v_resolved_exceptions,
        CASE WHEN v_resolved_exceptions > 50000 THEN 'Recommend cleanup of old resolved exceptions'
             ELSE 'Resolved exception count is acceptable' END;
    
    RETURN QUERY SELECT 'critical_exceptions'::TEXT, v_critical_exceptions,
        CASE WHEN v_critical_exceptions > 1000 THEN 'High number of critical exceptions - investigate'
             ELSE 'Critical exception count is normal' END;
    
    RETURN QUERY SELECT 'average_age_days'::TEXT, v_avg_age_days::BIGINT,
        CASE WHEN v_avg_age_days > 180 THEN 'Data is aging - consider cleanup policies'
             ELSE 'Data age is reasonable' END;
    
    RETURN QUERY SELECT 'database_size_mb'::TEXT, v_db_size_mb,
        CASE WHEN v_db_size_mb > 10240 THEN 'Large database size - consider archiving'
             ELSE 'Database size is manageable' END;
END;
$$ LANGUAGE plpgsql;

-- Create cleanup log table to track cleanup activities
CREATE TABLE IF NOT EXISTS data_cleanup_log (
    id BIGSERIAL PRIMARY KEY,
    cleanup_type VARCHAR(100) NOT NULL,
    retention_days INTEGER,
    records_deleted INTEGER NOT NULL DEFAULT 0,
    records_preserved INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    duration INTERVAL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),
    dry_run BOOLEAN NOT NULL DEFAULT true,
    initiated_by VARCHAR(255) NOT NULL,
    notes TEXT
);

COMMENT ON TABLE data_cleanup_log IS 'Tracks data cleanup activities and their outcomes';