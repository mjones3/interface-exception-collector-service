-- Create mutation audit log table for comprehensive audit logging of all mutations
-- This table stores operation history for retry, acknowledge, resolve, and cancel mutations
-- Requirements: 5.3, 5.5, 6.4

CREATE TABLE IF NOT EXISTS mutation_audit_log (
    id BIGSERIAL PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    input_data TEXT,
    result_status VARCHAR(20) NOT NULL,
    error_details TEXT,
    execution_time_ms INTEGER,
    operation_id VARCHAR(100),
    correlation_id VARCHAR(100),
    client_ip VARCHAR(45),
    user_agent TEXT,
    
    CONSTRAINT chk_operation_type CHECK (operation_type IN ('RETRY', 'ACKNOWLEDGE', 'RESOLVE', 'CANCEL_RETRY', 'BULK_RETRY', 'BULK_ACKNOWLEDGE')),
    CONSTRAINT chk_result_status CHECK (result_status IN ('SUCCESS', 'FAILURE', 'PARTIAL_SUCCESS'))
);

-- Create indexes for efficient querying
CREATE INDEX idx_mutation_audit_transaction_id ON mutation_audit_log (transaction_id);
CREATE INDEX idx_mutation_audit_performed_by ON mutation_audit_log (performed_by);
CREATE INDEX idx_mutation_audit_performed_at ON mutation_audit_log (performed_at);
CREATE INDEX idx_mutation_audit_operation_type ON mutation_audit_log (operation_type);
CREATE INDEX idx_mutation_audit_result_status ON mutation_audit_log (result_status);
CREATE INDEX idx_mutation_audit_operation_id ON mutation_audit_log (operation_id);

-- Create composite index for common query patterns
CREATE INDEX idx_mutation_audit_composite ON mutation_audit_log (operation_type, performed_at, result_status);

-- Add comment for documentation
COMMENT ON TABLE mutation_audit_log IS 'Comprehensive audit log for all GraphQL mutation operations including retry, acknowledge, resolve, and cancel operations';
COMMENT ON COLUMN mutation_audit_log.operation_type IS 'Type of mutation operation performed';
COMMENT ON COLUMN mutation_audit_log.transaction_id IS 'Transaction ID of the exception being operated on';
COMMENT ON COLUMN mutation_audit_log.performed_by IS 'User who performed the operation';
COMMENT ON COLUMN mutation_audit_log.input_data IS 'JSON string representation of the mutation input parameters';
COMMENT ON COLUMN mutation_audit_log.result_status IS 'Overall result status of the operation';
COMMENT ON COLUMN mutation_audit_log.error_details IS 'JSON string representation of any errors that occurred';
COMMENT ON COLUMN mutation_audit_log.execution_time_ms IS 'Time taken to execute the operation in milliseconds';
COMMENT ON COLUMN mutation_audit_log.operation_id IS 'Unique identifier for the specific operation instance';
COMMENT ON COLUMN mutation_audit_log.correlation_id IS 'Correlation ID for tracing across systems';