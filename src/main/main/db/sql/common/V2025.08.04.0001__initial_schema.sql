-- Initial schema for Interface Exception Collector Service

CREATE TABLE IF NOT EXISTS interface_exceptions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    interface_type VARCHAR(50) NOT NULL,
    exception_reason TEXT NOT NULL,
    operation VARCHAR(100) NOT NULL,
    external_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    severity VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    retryable BOOLEAN NOT NULL DEFAULT true,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    retry_timestamp TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_interface_exceptions_transaction_id ON interface_exceptions(transaction_id);
CREATE INDEX idx_interface_exceptions_interface_type ON interface_exceptions(interface_type);
CREATE INDEX idx_interface_exceptions_timestamp ON interface_exceptions(timestamp DESC);
CREATE INDEX idx_interface_exceptions_status ON interface_exceptions(status);

-- Comments
COMMENT ON TABLE interface_exceptions IS 'Centralized storage for all interface exceptions';
COMMENT ON COLUMN interface_exceptions.transaction_id IS 'Unique transaction identifier from source system';
COMMENT ON COLUMN interface_exceptions.interface_type IS 'Type of interface: order, collection, distribution';
COMMENT ON COLUMN interface_exceptions.exception_reason IS 'Human readable reason for the exception';
COMMENT ON COLUMN interface_exceptions.operation IS 'Operation that caused the exception: CREATE_ORDER, MODIFY_ORDER, etc.';
COMMENT ON COLUMN interface_exceptions.external_id IS 'External/customer identifier for the request';
