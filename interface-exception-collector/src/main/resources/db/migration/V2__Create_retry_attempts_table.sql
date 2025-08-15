-- Create retry_attempts table
-- This table tracks all retry attempts made for exceptions

CREATE TABLE retry_attempts (
    id BIGSERIAL PRIMARY KEY,
    exception_id BIGINT NOT NULL REFERENCES interface_exceptions(id) ON DELETE CASCADE,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    initiated_by VARCHAR(255) NOT NULL,
    initiated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    result_success BOOLEAN,
    result_message TEXT,
    result_response_code INTEGER,
    result_error_details JSONB,
    UNIQUE(exception_id, attempt_number)
);

-- Add comments for documentation
COMMENT ON TABLE retry_attempts IS 'Tracks retry attempts made for interface exceptions';
COMMENT ON COLUMN retry_attempts.exception_id IS 'Foreign key reference to interface_exceptions table';
COMMENT ON COLUMN retry_attempts.attempt_number IS 'Sequential number of the retry attempt (1, 2, 3, etc.)';
COMMENT ON COLUMN retry_attempts.status IS 'Status of the retry attempt (PENDING, SUCCESS, FAILED)';
COMMENT ON COLUMN retry_attempts.initiated_by IS 'User or system that initiated the retry';
COMMENT ON COLUMN retry_attempts.initiated_at IS 'When the retry attempt was started';
COMMENT ON COLUMN retry_attempts.completed_at IS 'When the retry attempt was completed';
COMMENT ON COLUMN retry_attempts.result_success IS 'Whether the retry was successful';
COMMENT ON COLUMN retry_attempts.result_message IS 'Result message from the retry attempt';
COMMENT ON COLUMN retry_attempts.result_response_code IS 'HTTP response code from the retry attempt';
COMMENT ON COLUMN retry_attempts.result_error_details IS 'JSON object containing detailed error information';