-- Test database initialization script for integration tests
-- This script sets up the basic schema structure for testing

-- Create test database user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'test_user') THEN
        CREATE ROLE test_user LOGIN PASSWORD 'test_pass';
    END IF;
END
$$;

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE exception_collector_test TO test_user;
GRANT ALL ON SCHEMA public TO test_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO test_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO test_user;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create enum types for testing
DO $$
BEGIN
    -- Interface Type enum
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'interface_type') THEN
        CREATE TYPE interface_type AS ENUM (
            'ORDER',
            'COLLECTION', 
            'DISTRIBUTION'
        );
    END IF;

    -- Exception Status enum
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'exception_status') THEN
        CREATE TYPE exception_status AS ENUM (
            'NEW',
            'ACKNOWLEDGED',
            'IN_PROGRESS',
            'RESOLVED',
            'FAILED'
        );
    END IF;

    -- Exception Severity enum
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'exception_severity') THEN
        CREATE TYPE exception_severity AS ENUM (
            'LOW',
            'MEDIUM',
            'HIGH',
            'CRITICAL'
        );
    END IF;

    -- Exception Category enum
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'exception_category') THEN
        CREATE TYPE exception_category AS ENUM (
            'VALIDATION',
            'BUSINESS_RULE',
            'TECHNICAL',
            'INTEGRATION',
            'TIMEOUT',
            'AUTHENTICATION',
            'AUTHORIZATION'
        );
    END IF;

    -- Retry Status enum
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'retry_status') THEN
        CREATE TYPE retry_status AS ENUM (
            'PENDING',
            'IN_PROGRESS',
            'COMPLETED',
            'FAILED',
            'CANCELLED'
        );
    END IF;
END
$$;

-- Create sequences for testing
CREATE SEQUENCE IF NOT EXISTS interface_exceptions_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS retry_attempts_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS status_changes_seq START 1 INCREMENT 1;

-- Create basic table structure (will be managed by Flyway in actual tests)
-- This is just for reference and basic functionality

-- Interface Exceptions table
CREATE TABLE IF NOT EXISTS interface_exceptions (
    id BIGINT PRIMARY KEY DEFAULT nextval('interface_exceptions_seq'),
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    external_id VARCHAR(255),
    interface_type interface_type NOT NULL,
    exception_reason TEXT NOT NULL,
    operation VARCHAR(255) NOT NULL,
    status exception_status NOT NULL DEFAULT 'NEW',
    severity exception_severity NOT NULL,
    category exception_category NOT NULL,
    customer_id VARCHAR(255),
    location_code VARCHAR(255),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retryable BOOLEAN NOT NULL DEFAULT false,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    last_retry_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_method VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Retry Attempts table
CREATE TABLE IF NOT EXISTS retry_attempts (
    id BIGINT PRIMARY KEY DEFAULT nextval('retry_attempts_seq'),
    exception_id BIGINT NOT NULL REFERENCES interface_exceptions(id) ON DELETE CASCADE,
    attempt_number INTEGER NOT NULL,
    status retry_status NOT NULL DEFAULT 'PENDING',
    initiated_by VARCHAR(255) NOT NULL,
    initiated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    result_success BOOLEAN,
    result_message TEXT,
    result_response_code INTEGER,
    result_error_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(exception_id, attempt_number)
);

-- Status Changes table (for audit trail)
CREATE TABLE IF NOT EXISTS exception_status_changes (
    id BIGINT PRIMARY KEY DEFAULT nextval('status_changes_seq'),
    exception_id BIGINT NOT NULL REFERENCES interface_exceptions(id) ON DELETE CASCADE,
    previous_status exception_status,
    new_status exception_status NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    notes TEXT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_exceptions_transaction_id ON interface_exceptions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_exceptions_interface_type ON interface_exceptions(interface_type);
CREATE INDEX IF NOT EXISTS idx_exceptions_status ON interface_exceptions(status);
CREATE INDEX IF NOT EXISTS idx_exceptions_severity ON interface_exceptions(severity);
CREATE INDEX IF NOT EXISTS idx_exceptions_timestamp ON interface_exceptions(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_exceptions_customer_id ON interface_exceptions(customer_id);
CREATE INDEX IF NOT EXISTS idx_exceptions_location_code ON interface_exceptions(location_code);
CREATE INDEX IF NOT EXISTS idx_exceptions_composite ON interface_exceptions(interface_type, status, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_retry_attempts_exception_id ON retry_attempts(exception_id);
CREATE INDEX IF NOT EXISTS idx_retry_attempts_status ON retry_attempts(status);
CREATE INDEX IF NOT EXISTS idx_retry_attempts_initiated_at ON retry_attempts(initiated_at DESC);

CREATE INDEX IF NOT EXISTS idx_status_changes_exception_id ON exception_status_changes(exception_id);
CREATE INDEX IF NOT EXISTS idx_status_changes_changed_at ON status_changes(changed_at DESC);

-- Create full-text search index
CREATE INDEX IF NOT EXISTS idx_exceptions_search ON interface_exceptions 
USING gin(to_tsvector('english', exception_reason));

-- Create trigger for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
DROP TRIGGER IF EXISTS update_interface_exceptions_updated_at ON interface_exceptions;
CREATE TRIGGER update_interface_exceptions_updated_at 
    BEFORE UPDATE ON interface_exceptions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_retry_attempts_updated_at ON retry_attempts;
CREATE TRIGGER update_retry_attempts_updated_at 
    BEFORE UPDATE ON retry_attempts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some test data for basic functionality
INSERT INTO interface_exceptions (
    transaction_id, external_id, interface_type, exception_reason, operation,
    status, severity, category, customer_id, location_code, retryable
) VALUES 
    ('TEST-001', 'EXT-001', 'ORDER', 'Test exception 1', 'CREATE_ORDER', 'NEW', 'HIGH', 'VALIDATION', 'CUST-001', 'LOC-001', true),
    ('TEST-002', 'EXT-002', 'COLLECTION', 'Test exception 2', 'CREATE_COLLECTION', 'ACKNOWLEDGED', 'MEDIUM', 'BUSINESS_RULE', 'CUST-002', 'LOC-002', false),
    ('TEST-003', 'EXT-003', 'DISTRIBUTION', 'Test exception 3', 'CREATE_DISTRIBUTION', 'RESOLVED', 'LOW', 'TECHNICAL', 'CUST-003', 'LOC-003', true)
ON CONFLICT (transaction_id) DO NOTHING;

-- Grant permissions on new objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO test_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO test_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO test_user;