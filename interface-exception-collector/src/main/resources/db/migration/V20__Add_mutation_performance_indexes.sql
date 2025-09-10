-- Mutation Performance Optimization Indexes
-- These indexes are specifically designed to optimize GraphQL mutation operations

-- Primary mutation lookup index - optimized for transaction_id queries
-- This is the most critical index for all mutation operations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_transaction_id_optimized 
ON interface_exceptions(transaction_id) 
INCLUDE (status, retryable, retry_count, max_retries);

-- Retry mutation validation index - optimized for retry eligibility checks
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_retry_validation 
ON interface_exceptions(transaction_id, retryable, status) 
WHERE retryable = true;

-- Acknowledge mutation validation index - optimized for acknowledgment eligibility
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_acknowledge_validation 
ON interface_exceptions(transaction_id, status) 
WHERE status IN ('NEW', 'RETRIED_FAILED', 'ESCALATED');

-- Resolve mutation validation index - optimized for resolution eligibility
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_resolve_validation 
ON interface_exceptions(transaction_id, status) 
WHERE status IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_FAILED', 'ESCALATED');

-- Cancel retry validation index - optimized for cancel retry operations
-- Covers both exception lookup and active retry checking
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_cancel_retry_validation 
ON retry_attempts(exception_id, status) 
WHERE status IN ('PENDING', 'IN_PROGRESS');

-- Retry limits checking index - optimized for retry count validation
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_retry_limits 
ON interface_exceptions(transaction_id) 
INCLUDE (retry_count, max_retries, retryable);

-- Pending retries count index - optimized for concurrent retry prevention
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_pending_retries 
ON retry_attempts(exception_id, status, initiated_at DESC) 
WHERE status = 'PENDING';

-- Mutation audit and tracking index - optimized for mutation history queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_audit_tracking 
ON interface_exceptions(transaction_id, updated_at DESC) 
INCLUDE (status, acknowledged_at, resolved_at, retry_count);

-- Batch validation index - optimized for bulk mutation validation operations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_batch_validation 
ON interface_exceptions(transaction_id, status, retryable, retry_count);

-- Optimistic locking index - optimized for concurrent mutation handling
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_optimistic_locking 
ON interface_exceptions(transaction_id, updated_at);

-- Status transition index - optimized for status change validation
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_status_transitions 
ON interface_exceptions(status, transaction_id) 
INCLUDE (acknowledged_at, resolved_at, retryable);

-- Active retry operations index - optimized for cancel retry validation
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_active_retries 
ON retry_attempts(exception_id, status, attempt_number DESC) 
WHERE status IN ('PENDING', 'IN_PROGRESS', 'RETRYING');

-- Mutation performance monitoring index - for tracking mutation operation performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_performance_monitoring 
ON interface_exceptions(updated_at DESC, transaction_id) 
WHERE updated_at > NOW() - INTERVAL '1 hour';

-- Partial index for recently modified exceptions (hot data)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_recent_modifications 
ON interface_exceptions(transaction_id, status, updated_at DESC) 
WHERE updated_at > NOW() - INTERVAL '24 hours';

-- Covering index for mutation validation queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mutations_validation_covering 
ON interface_exceptions(transaction_id) 
INCLUDE (id, status, retryable, retry_count, max_retries, acknowledged_at, resolved_at);

-- Retry attempt mutation index - optimized for retry attempt queries in mutations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_retry_attempts_mutation_ops 
ON retry_attempts(exception_id, status, attempt_number DESC, initiated_at DESC);

-- Exception status changes mutation index - for audit trail in mutations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_status_changes_mutation_audit 
ON exception_status_changes(exception_id, changed_at DESC, new_status, changed_by);

-- Add query timeout settings for mutation operations
-- Set statement timeout for mutation queries to prevent long-running operations
ALTER DATABASE exception_collector_db SET statement_timeout = '30s';

-- Set lock timeout for optimistic locking in mutations
ALTER DATABASE exception_collector_db SET lock_timeout = '10s';

-- Set idle in transaction timeout to prevent hanging connections
ALTER DATABASE exception_collector_db SET idle_in_transaction_session_timeout = '60s';

-- Optimize autovacuum for mutation-heavy tables
ALTER TABLE interface_exceptions SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05,
    autovacuum_vacuum_cost_delay = 10,
    autovacuum_vacuum_cost_limit = 1000
);

ALTER TABLE retry_attempts SET (
    autovacuum_vacuum_scale_factor = 0.2,
    autovacuum_analyze_scale_factor = 0.1,
    autovacuum_vacuum_cost_delay = 10,
    autovacuum_vacuum_cost_limit = 500
);

-- Update table statistics for better query planning
ANALYZE interface_exceptions;
ANALYZE retry_attempts;
ANALYZE exception_status_changes;

-- Add comments for documentation
COMMENT ON INDEX idx_mutations_transaction_id_optimized IS 'Primary index for mutation transaction ID lookups with covering columns';
COMMENT ON INDEX idx_mutations_retry_validation IS 'Optimized index for retry mutation validation';
COMMENT ON INDEX idx_mutations_acknowledge_validation IS 'Optimized index for acknowledge mutation validation';
COMMENT ON INDEX idx_mutations_resolve_validation IS 'Optimized index for resolve mutation validation';
COMMENT ON INDEX idx_mutations_cancel_retry_validation IS 'Optimized index for cancel retry mutation validation';
COMMENT ON INDEX idx_mutations_retry_limits IS 'Optimized index for retry limits checking';
COMMENT ON INDEX idx_mutations_pending_retries IS 'Optimized index for pending retries count';
COMMENT ON INDEX idx_mutations_batch_validation IS 'Optimized index for batch mutation validation';
COMMENT ON INDEX idx_mutations_validation_covering IS 'Covering index for comprehensive mutation validation';

-- Create function to refresh mutation-related statistics
CREATE OR REPLACE FUNCTION refresh_mutation_statistics()
RETURNS void AS $$
BEGIN
    -- Refresh statistics for mutation-critical tables
    ANALYZE interface_exceptions;
    ANALYZE retry_attempts;
    ANALYZE exception_status_changes;
    
    -- Log the refresh
    RAISE NOTICE 'Mutation statistics refreshed at %', NOW();
END;
$$ LANGUAGE plpgsql;

-- Create function to monitor mutation performance
CREATE OR REPLACE FUNCTION get_mutation_performance_stats()
RETURNS TABLE(
    table_name text,
    index_name text,
    index_size text,
    index_scans bigint,
    tuples_read bigint,
    tuples_fetched bigint
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        schemaname || '.' || tablename as table_name,
        indexname as index_name,
        pg_size_pretty(pg_relation_size(indexrelid)) as index_size,
        idx_scan as index_scans,
        idx_tup_read as tuples_read,
        idx_tup_fetch as tuples_fetched
    FROM pg_stat_user_indexes 
    WHERE indexname LIKE 'idx_mutations_%' 
       OR indexname LIKE 'idx_retry_attempts_mutation_%'
    ORDER BY idx_scan DESC;
END;
$$ LANGUAGE plpgsql;

-- Schedule statistics refresh (if pg_cron is available)
-- This would need to be run manually if pg_cron is not installed
-- SELECT cron.schedule('refresh-mutation-stats', '*/15 * * * *', 'SELECT refresh_mutation_statistics();');