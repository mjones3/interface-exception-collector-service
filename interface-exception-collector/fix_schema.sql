-- Fix for missing columns in interface_exceptions table
DO $$
BEGIN
    -- Add acknowledgment_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes TEXT;
        RAISE NOTICE 'Added acknowledgment_notes column';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
    
    -- Add resolution_method column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    ELSE
        RAISE NOTICE 'resolution_method column already exists';
    END IF;
    
    -- Add resolution_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes TEXT;
        RAISE NOTICE 'Added resolution_notes column';
    ELSE
        RAISE NOTICE 'resolution_notes column already exists';
    END IF;
END
$$;

-- Verify the columns exist
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name IN ('acknowledgment_notes', 'resolution_method', 'resolution_notes')
ORDER BY column_name;
