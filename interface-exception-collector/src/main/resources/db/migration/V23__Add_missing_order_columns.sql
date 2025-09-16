-- V23: Add missing order-related columns
-- This migration adds order data fields that were missing from the schema

DO $$
BEGIN
    -- Ensure order_received column exists with proper type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_received'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_received JSONB;
        RAISE NOTICE 'Added order_received column';
    END IF;
    
    -- Ensure order_retrieval_attempted column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_retrieval_attempted'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_retrieval_attempted BOOLEAN DEFAULT false;
        RAISE NOTICE 'Added order_retrieval_attempted column';
    END IF;
    
    -- Ensure order_retrieval_error column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'order_retrieval_error'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN order_retrieval_error TEXT;
        RAISE NOTICE 'Added order_retrieval_error column';
    END IF;
    
    -- Ensure order_retrieved_at column exists
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

-- Create additional indexes for order-related queries
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_retrieval_attempted 
ON interface_exceptions(order_retrieval_attempted) 
WHERE order_retrieval_attempted = true;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_retrieved_at 
ON interface_exceptions(order_retrieved_at) 
WHERE order_retrieved_at IS NOT NULL;

-- Add comments
COMMENT ON COLUMN interface_exceptions.order_received IS 'JSON data containing the complete order information from partner service';
COMMENT ON COLUMN interface_exceptions.order_retrieval_attempted IS 'Boolean flag indicating whether order retrieval was attempted';
COMMENT ON COLUMN interface_exceptions.order_retrieval_error IS 'Error message if order retrieval failed';
COMMENT ON COLUMN interface_exceptions.order_retrieved_at IS 'Timestamp when order data was successfully retrieved';
