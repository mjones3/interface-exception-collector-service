-- Create materialized view for pre-aggregated exception statistics
-- This view provides optimized access to summary data for dashboard queries
-- Implements requirement 4.4: Dashboard statistics with 200ms response time

-- Create materialized view for exception summary statistics
CREATE MATERIALIZED VIEW exception_summary_mv AS
SELECT 
    -- Time-based aggregations
    DATE_TRUNC('hour', timestamp) as hour_bucket,
    DATE_TRUNC('day', timestamp) as day_bucket,
    DATE_TRUNC('week', timestamp) as week_bucket,
    DATE_TRUNC('month', timestamp) as month_bucket,
    
    -- Dimension columns
    interface_type,
    status,
    severity,
    category,
    
    -- Aggregated metrics
    COUNT(*) as exception_count,
    COUNT(DISTINCT customer_id) as unique_customers,
    COUNT(CASE WHEN retryable = true THEN 1 END) as retryable_count,
    COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_count,
    COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) as resolved_count,
    COUNT(CASE WHEN acknowledged_at IS NOT NULL THEN 1 END) as acknowledged_count,
    
    -- Time-based metrics
    AVG(EXTRACT(EPOCH FROM (COALESCE(resolved_at, NOW()) - timestamp))/3600) as avg_resolution_hours,
    MIN(timestamp) as first_occurrence,
    MAX(timestamp) as last_occurrence
    
FROM interface_exceptions 
WHERE timestamp >= NOW() - INTERVAL '1 year'  -- Keep last year of data
GROUP BY 
    DATE_TRUNC('hour', timestamp),
    DATE_TRUNC('day', timestamp), 
    DATE_TRUNC('week', timestamp),
    DATE_TRUNC('month', timestamp),
    interface_type,
    status,
    severity,
    category;

-- Create indexes on the materialized view for optimal query performance
CREATE INDEX idx_exception_summary_mv_day_interface 
    ON exception_summary_mv (day_bucket, interface_type);

CREATE INDEX idx_exception_summary_mv_day_status 
    ON exception_summary_mv (day_bucket, status);

CREATE INDEX idx_exception_summary_mv_day_severity 
    ON exception_summary_mv (day_bucket, severity);

CREATE INDEX idx_exception_summary_mv_hour_bucket 
    ON exception_summary_mv (hour_bucket);

CREATE INDEX idx_exception_summary_mv_week_bucket 
    ON exception_summary_mv (week_bucket);

CREATE INDEX idx_exception_summary_mv_month_bucket 
    ON exception_summary_mv (month_bucket);

-- Create a function to refresh the materialized view
CREATE OR REPLACE FUNCTION refresh_exception_summary_mv()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY exception_summary_mv;
END;
$$ LANGUAGE plpgsql;

-- Create a scheduled job to refresh the materialized view every 5 minutes
-- Note: This requires pg_cron extension to be installed
-- If pg_cron is not available, this can be handled by application-level scheduling
DO $$
BEGIN
    -- Check if pg_cron extension exists
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_cron') THEN
        -- Schedule refresh every 5 minutes
        PERFORM cron.schedule('refresh-exception-summary', '*/5 * * * *', 'SELECT refresh_exception_summary_mv();');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- If pg_cron is not available, just log a notice
        RAISE NOTICE 'pg_cron extension not available. Materialized view refresh must be scheduled externally.';
END;
$$;

-- Create a view for easy access to current summary data
CREATE OR REPLACE VIEW current_exception_summary AS
SELECT 
    interface_type,
    status,
    severity,
    SUM(exception_count) as total_exceptions,
    SUM(unique_customers) as total_unique_customers,
    SUM(critical_count) as total_critical,
    SUM(resolved_count) as total_resolved,
    AVG(avg_resolution_hours) as avg_resolution_hours
FROM exception_summary_mv 
WHERE day_bucket >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY interface_type, status, severity;

-- Grant appropriate permissions
GRANT SELECT ON exception_summary_mv TO PUBLIC;
GRANT SELECT ON current_exception_summary TO PUBLIC;