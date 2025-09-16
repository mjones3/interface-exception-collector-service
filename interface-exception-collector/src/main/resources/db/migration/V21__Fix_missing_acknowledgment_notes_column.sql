-- V21: Fix missing acknowledgment_notes column
-- This migration ensures the acknowledgment_notes column exists and fixes the schema issue

-- Add acknowledgment_notes column if it doesn't exist
DO $$
BEGIN
    -- Check and add acknowledgment_notes column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes TEXT;
        RAISE NOTICE 'Added acknowledgment_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists in interface_exceptions table';
    END IF;
    
    -- Check and add resolution_method column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'resolution_method column already exists in interface_exceptions table';
    END IF;
    
    -- Check and add resolution_notes column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes TEXT;
        RAISE NOTICE 'Added resolution_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'resolution_notes column already exists in interface_exceptions table';
    END IF;
END
$$;

-- Add index for resolution method if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method 
ON interface_exceptions(resolution_method);

-- Add index for acknowledgment status if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_acknowledged 
ON interface_exceptions(acknowledged_at) 
WHERE acknowledged_at IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when the exception was acknowledged by an operator';
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Detailed notes about how the exception was resolved';

-- Verify the columns exist by selecting their metadata
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name IN ('acknowledgment_notes', 'resolution_method', 'resolution_notes')
ORDER BY column_name;