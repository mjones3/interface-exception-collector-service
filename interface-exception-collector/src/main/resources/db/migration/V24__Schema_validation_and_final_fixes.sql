-- V24: Schema validation and final fixes
-- This migration validates that all required columns exist and adds any final missing pieces

DO $$
DECLARE
    missing_columns TEXT[] := ARRAY[]::TEXT[];
    col_name TEXT;
BEGIN
    -- Check for all required columns and collect missing ones
    FOR col_name IN 
        SELECT unnest(ARRAY[
            'acknowledgment_notes',
            'resolution_method', 
            'resolution_notes',
            'acknowledged_at',
            'acknowledged_by',
            'resolved_at',
            'resolved_by',
            'order_received',
            'order_retrieval_attempted',
            'order_retrieval_error',
            'order_retrieved_at'
        ])
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'interface_exceptions' 
            AND column_name = col_name
        ) THEN
            missing_columns := array_append(missing_columns, col_name);
        END IF;
    END LOOP;
    
    -- Report missing columns
    IF array_length(missing_columns, 1) > 0 THEN
        RAISE WARNING 'Missing columns detected: %', array_to_string(missing_columns, ', ');
    ELSE
        RAISE NOTICE 'All required columns are present in interface_exceptions table';
    END IF;
    
    -- Add any final missing columns with proper types
    IF 'acknowledgment_notes' = ANY(missing_columns) THEN
        ALTER TABLE interface_exceptions ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    IF 'order_received' = ANY(missing_columns) THEN
        ALTER TABLE interface_exceptions ADD COLUMN order_received JSONB;
        RAISE NOTICE 'Added order_received column';
    END IF;
    
    IF 'order_retrieval_attempted' = ANY(missing_columns) THEN
        ALTER TABLE interface_exceptions ADD COLUMN order_retrieval_attempted BOOLEAN DEFAULT false;
        RAISE NOTICE 'Added order_retrieval_attempted column';
    END IF;
    
    IF 'order_retrieval_error' = ANY(missing_columns) THEN
        ALTER TABLE interface_exceptions ADD COLUMN order_retrieval_error TEXT;
        RAISE NOTICE 'Added order_retrieval_error column';
    END IF;
    
    IF 'order_retrieved_at' = ANY(missing_columns) THEN
        ALTER TABLE interface_exceptions ADD COLUMN order_retrieved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added order_retrieved_at column';
    END IF;
END
$$;

-- Final validation query
SELECT 
    'interface_exceptions' as table_name,
    COUNT(*) as total_columns,
    COUNT(CASE WHEN column_name IN (
        'acknowledgment_notes',
        'resolution_method', 
        'resolution_notes',
        'acknowledged_at',
        'acknowledged_by',
        'resolved_at',
        'resolved_by',
        'order_received',
        'order_retrieval_attempted',
        'order_retrieval_error',
        'order_retrieved_at'
    ) THEN 1 END) as required_columns_present
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions';
