-- Create interface_exceptions table
-- This table stores all exception events captured from BioPro interface services

CREATE TABLE interface_exceptions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    interface_type VARCHAR(50) NOT NULL CHECK (interface_type IN ('ORDER', 'COLLECTION', 'DISTRIBUTION', 'RECRUITMENT')),
    exception_reason TEXT NOT NULL,
    operation VARCHAR(100) NOT NULL,
    external_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_SUCCESS', 'RETRIED_FAILED', 'ESCALATED', 'RESOLVED', 'CLOSED')),
    severity VARCHAR(50) NOT NULL DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    category VARCHAR(50) NOT NULL CHECK (category IN ('BUSINESS_RULE', 'VALIDATION', 'SYSTEM_ERROR', 'TIMEOUT', 'NETWORK')),
    retryable BOOLEAN NOT NULL DEFAULT true,
    customer_id VARCHAR(255),
    location_code VARCHAR(100),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by VARCHAR(255),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(255),
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Add comments for documentation
COMMENT ON TABLE interface_exceptions IS 'Stores exception events from BioPro interface services';
COMMENT ON COLUMN interface_exceptions.transaction_id IS 'Unique identifier for the transaction that caused the exception';
COMMENT ON COLUMN interface_exceptions.interface_type IS 'Type of interface service (ORDER, COLLECTION, DISTRIBUTION, RECRUITMENT)';
COMMENT ON COLUMN interface_exceptions.exception_reason IS 'Detailed reason for the exception';
COMMENT ON COLUMN interface_exceptions.operation IS 'Operation that was being performed when exception occurred';
COMMENT ON COLUMN interface_exceptions.external_id IS 'External identifier (order ID, collection ID, etc.)';
COMMENT ON COLUMN interface_exceptions.status IS 'Current status of the exception';
COMMENT ON COLUMN interface_exceptions.severity IS 'Severity level of the exception';
COMMENT ON COLUMN interface_exceptions.category IS 'Category classification of the exception';
COMMENT ON COLUMN interface_exceptions.retryable IS 'Whether this exception can be retried';
COMMENT ON COLUMN interface_exceptions.customer_id IS 'Customer associated with the exception';
COMMENT ON COLUMN interface_exceptions.location_code IS 'Location code where exception occurred';
COMMENT ON COLUMN interface_exceptions.timestamp IS 'When the exception originally occurred';
COMMENT ON COLUMN interface_exceptions.processed_at IS 'When the exception was processed by this service';
COMMENT ON COLUMN interface_exceptions.retry_count IS 'Number of retry attempts made';