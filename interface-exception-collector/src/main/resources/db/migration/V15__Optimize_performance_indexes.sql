-- Performance optimization indexes for GraphQL API
-- These indexes are specifically designed to optimize GraphQL query patterns

-- Composite indexes for GraphQL dashboard queries
-- Optimizes the most common filter combinations used by the dashboard
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_dashboard_filters 
ON interface_exceptions(interface_type, status, severity, timestamp DESC);

-- Optimizes pagination queries with cursor-based pagination
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_cursor_pagination 
ON interface_exceptions(timestamp DESC, transaction_id);

-- Optimizes customer and location filtering
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_customer_location_time 
ON interface_exceptions(customer_id, location_code, timestamp DESC);

-- Optimizes retryable exception queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_retryable_status_time 
ON interface_exceptions(retryable, status, timestamp DESC) 
WHERE retryable = true;

-- Optimizes unresolved exception queries (common dashboard filter)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_unresolved 
ON interface_exceptions(status, timestamp DESC) 
WHERE status != 'RESOLVED';

-- Optimizes recent exceptions queries (last 24 hours, 7 days, etc.)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_recent_activity 
ON interface_exceptions(timestamp DESC, status, interface_type) 
WHERE timestamp > NOW() - INTERVAL '7 days';

-- Optimizes summary statistics queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_summary_stats 
ON interface_exceptions(interface_type, status, severity, DATE(timestamp));

-- Optimizes search queries with filters
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_search_with_filters 
ON interface_exceptions(interface_type, status, timestamp DESC) 
WHERE exception_reason IS NOT NULL;

-- Partial index for acknowledged exceptions
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_acknowledged 
ON interface_exceptions(acknowledged_at DESC, acknowledged_by) 
WHERE acknowledged_at IS NOT NULL;

-- Partial index for exceptions with retry history
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_with_retries 
ON interface_exceptions(retry_count, last_retry_at DESC) 
WHERE retry_count > 0;

-- Optimize retry_attempts table for GraphQL queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_retry_attempts_graphql 
ON retry_attempts(exception_id, attempt_number DESC, initiated_at DESC);

-- Optimize status changes for audit trail queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_status_changes_graphql 
ON exception_status_changes(exception_id, changed_at DESC, new_status);

-- Create covering index for exception list queries (includes commonly selected fields)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_list_covering 
ON interface_exceptions(timestamp DESC, transaction_id) 
INCLUDE (interface_type, status, severity, exception_reason, customer_id, location_code, retry_count);

-- Optimize materialized view refresh performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exceptions_mv_refresh 
ON interface_exceptions(DATE(timestamp), interface_type, status, severity);

-- Add statistics collection for better query planning
ANALYZE interface_exceptions;
ANALYZE retry_attempts;
ANALYZE exception_status_changes;

-- Update table statistics more frequently for better query planning
ALTER TABLE interface_exceptions SET (autovacuum_analyze_scale_factor = 0.05);
ALTER TABLE retry_attempts SET (autovacuum_analyze_scale_factor = 0.1);
ALTER TABLE exception_status_changes SET (autovacuum_analyze_scale_factor = 0.1);

-- Comments for documentation
COMMENT ON INDEX idx_exceptions_dashboard_filters IS 'Optimizes GraphQL dashboard filter queries';
COMMENT ON INDEX idx_exceptions_cursor_pagination IS 'Optimizes cursor-based pagination for GraphQL';
COMMENT ON INDEX idx_exceptions_customer_location_time IS 'Optimizes customer and location filtering';
COMMENT ON INDEX idx_exceptions_retryable_status_time IS 'Optimizes retryable exception queries';
COMMENT ON INDEX idx_exceptions_unresolved IS 'Optimizes unresolved exception queries';
COMMENT ON INDEX idx_exceptions_recent_activity IS 'Optimizes recent activity queries';
COMMENT ON INDEX idx_exceptions_summary_stats IS 'Optimizes summary statistics queries';
COMMENT ON INDEX idx_exceptions_list_covering IS 'Covering index for exception list queries';