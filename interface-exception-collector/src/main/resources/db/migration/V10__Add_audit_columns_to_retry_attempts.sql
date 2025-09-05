-- Add audit columns to retry_attempts table
-- These columns are required for JPA auditing functionality

ALTER TABLE retry_attempts 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Add comments for documentation
COMMENT ON COLUMN retry_attempts.created_at IS 'When the retry attempt record was created';
COMMENT ON COLUMN retry_attempts.updated_at IS 'When the retry attempt record was last updated';

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_retry_attempts_created_at ON retry_attempts(created_at);
CREATE INDEX IF NOT EXISTS idx_retry_attempts_updated_at ON retry_attempts(updated_at);