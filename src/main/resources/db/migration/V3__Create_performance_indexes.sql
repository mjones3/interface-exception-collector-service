-- Create performance indexes for interface_exceptions table
-- These indexes optimize common query patterns for filtering, sorting, and searching

-- Primary lookup indexes for filtering
CREATE INDEX idx_interface_exceptions_transaction_id ON interface_exceptions(transaction_id);
CREATE INDEX idx_interface_exceptions_interface_type ON interface_exceptions(interface_type);
CREATE INDEX idx_interface_exceptions_status ON interface_exceptions(status);
CREATE INDEX idx_interface_exceptions_severity ON interface_exceptions(severity);
CREATE INDEX idx_interface_exceptions_customer_id ON interface_exceptions(customer_id);
CREATE INDEX idx_interface_exceptions_external_id ON interface_exceptions(external_id);

-- Time-based queries for sorting and date range filtering
CREATE INDEX idx_interface_exceptions_timestamp ON interface_exceptions(timestamp DESC);
CREATE INDEX idx_interface_exceptions_processed_at ON interface_exceptions(processed_at DESC);
CREATE INDEX idx_interface_exceptions_created_at ON interface_exceptions(created_at DESC);

-- Composite indexes for common filter combinations
CREATE INDEX idx_interface_exceptions_type_status ON interface_exceptions(interface_type, status);
CREATE INDEX idx_interface_exceptions_severity_timestamp ON interface_exceptions(severity, timestamp DESC);
CREATE INDEX idx_interface_exceptions_status_timestamp ON interface_exceptions(status, timestamp DESC);
CREATE INDEX idx_interface_exceptions_customer_status ON interface_exceptions(customer_id, status);

-- Indexes for retry-related queries
CREATE INDEX idx_interface_exceptions_retryable ON interface_exceptions(retryable);
CREATE INDEX idx_interface_exceptions_retry_count ON interface_exceptions(retry_count);
CREATE INDEX idx_interface_exceptions_last_retry_at ON interface_exceptions(last_retry_at DESC);

-- Indexes for acknowledgment and resolution tracking
CREATE INDEX idx_interface_exceptions_acknowledged_at ON interface_exceptions(acknowledged_at DESC);
CREATE INDEX idx_interface_exceptions_resolved_at ON interface_exceptions(resolved_at DESC);

-- Performance indexes for retry_attempts table
CREATE INDEX idx_retry_attempts_exception_id ON retry_attempts(exception_id);
CREATE INDEX idx_retry_attempts_status ON retry_attempts(status);
CREATE INDEX idx_retry_attempts_initiated_at ON retry_attempts(initiated_at DESC);
CREATE INDEX idx_retry_attempts_completed_at ON retry_attempts(completed_at DESC);

-- Composite index for retry history queries
CREATE INDEX idx_retry_attempts_exception_attempt ON retry_attempts(exception_id, attempt_number);
CREATE INDEX idx_retry_attempts_status_initiated ON retry_attempts(status, initiated_at DESC);