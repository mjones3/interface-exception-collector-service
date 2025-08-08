-- Add acknowledgment and resolution fields to interface_exceptions table
-- Supports requirements US-013 and US-014 for exception acknowledgment and resolution

ALTER TABLE interface_exceptions 
ADD COLUMN resolution_method VARCHAR(50),
ADD COLUMN resolution_notes VARCHAR(1000),
ADD COLUMN acknowledgment_notes VARCHAR(1000);

-- Add index for resolution method for reporting queries
CREATE INDEX idx_interface_exceptions_resolution_method ON interface_exceptions(resolution_method);

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Notes about how the exception was resolved';
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when the exception was acknowledged';