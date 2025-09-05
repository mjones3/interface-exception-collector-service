-- Add missing acknowledgment_notes column to interface_exceptions table
-- This is a quick fix for the schema mismatch

-- Connect to the database
\c exception_collector_db;

-- Add the missing column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes TEXT;
        
        RAISE NOTICE 'Added acknowledgment_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
END
$$;

-- Verify the column was added
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name = 'acknowledgment_notes';