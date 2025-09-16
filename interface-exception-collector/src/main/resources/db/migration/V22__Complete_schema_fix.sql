-- V22: Complete schema fix for acknowledgment_notes and all missing columns
-- This migration ensures all required columns exist for the InterfaceException entity

DO $$
BEGIN
    -- Add acknowledgment_notes column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    -- Add resolution_method column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    END IF;
    
    -- Add resolution_notes column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes VARCHAR(1000);
        RAISE NOTICE 'Added resolution_notes column';
    END IF;
    
    -- Add acknowledged_at column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added acknowledged_at column';
    END IF;
    
    -- Add acknowledged_by column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_by VARCHAR(255);
        RAISE NOTICE 'Added acknowledged_by column';
    END IF;
    
    -- Add resolved_at column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added resolved_at column';
    END IF;
    
    -- Add resolved_by column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_by VARCHAR(255);
        RAISE NOTICE 'Added resolved_by column';
    END IF;
    
    -- Add order_received column if missing (JSONB for order data)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_received'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_received JSONB;
        RAISE NOTICE 'Added order_received column';
    END IF;
    
    -- Add order_retrieval_attempted column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_retrieval_attempted'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_retrieval_attempted BOOLEAN DEFAULT false;
        RAISE NOTICE 'Added order_retrieval_attempted column';
    END IF;
    
    -- Add order_retrieval_error column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_retrieval_error'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_retrieval_error TEXT;
        RAISE NOTICE 'Added order_retrieval_error column';
    END IF;
    
    -- Add order_retrieved_at column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_retrieved_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_retrieved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added order_retrieved_at column';
    END IF;
END
$$;

-- Create indexes for performance if they don't exist
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_acknowledgment_notes 
ON interface_exceptions(acknowledgment_notes) 
WHERE acknowledgment_notes IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method 
ON interface_exceptions(resolution_method) 
WHERE resolution_method IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_received 
ON interface_exceptions USING GIN(order_received) 
WHERE order_received IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when acknowledging an exception';
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Detailed notes about how the exception was resolved';
COMMENT ON COLUMN interface_exceptions.order_received IS 'JSON data of the order received from partner service';
COMMENT ON COLUMN interface_exceptions.order_retrieval_attempted IS 'Flag indicating if order retrieval was attempted';
COMMENT ON COLUMN interface_exceptions.order_retrieval_error IS 'Error message if order retrieval failed';
COMMENT ON COLUMN interface_exceptions.order_retrieved_at IS 'Timestamp when order was successfully retrieved';

-- Verify all columns exist by selecting their metadata
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name IN (
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
)
ORDER BY column_name;
