-- Ensure acknowledgment_notes column exists (hotfix migration)
-- This migration ensures the acknowledgment_notes column exists even if previous migrations didn't run

-- Add acknowledgment_notes column if it doesn't exist
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS acknowledgment_notes VARCHAR(1000);

-- Add resolution fields too in case they're missing
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50),
ADD COLUMN IF NOT EXISTS resolution_notes VARCHAR(1000);

-- Add index for resolution method if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method ON interface_exceptions(resolution_method);

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Notes about how the exception was resolved';
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when the exception was acknowledged';