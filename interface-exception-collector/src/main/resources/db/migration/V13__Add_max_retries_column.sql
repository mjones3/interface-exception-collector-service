-- Add max_retries column to interface_exceptions table
-- This column defines the maximum number of retry attempts allowed for each exception

ALTER TABLE interface_exceptions 
ADD COLUMN max_retries INTEGER NOT NULL DEFAULT 3;

-- Add comment to document the column purpose
COMMENT ON COLUMN interface_exceptions.max_retries IS 'Maximum number of retry attempts allowed for this exception';

-- Create index for performance when querying by retry limits
CREATE INDEX idx_interface_exceptions_retry_limits 
ON interface_exceptions (retry_count, max_retries) 
WHERE retryable = true;