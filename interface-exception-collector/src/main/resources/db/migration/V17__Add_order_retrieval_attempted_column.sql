-- V17: Add order_retrieval_attempted column with proper handling of existing data
-- This migration safely adds the order_retrieval_attempted column to handle existing records

-- Step 1: Add the column as nullable first
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS order_retrieval_attempted BOOLEAN;

-- Step 2: Update existing records to have a default value (false)
UPDATE interface_exceptions 
SET order_retrieval_attempted = false 
WHERE order_retrieval_attempted IS NULL;

-- Step 3: Now make the column NOT NULL since all records have values
ALTER TABLE interface_exceptions 
ALTER COLUMN order_retrieval_attempted SET NOT NULL;

-- Step 4: Set default value for future inserts
ALTER TABLE interface_exceptions 
ALTER COLUMN order_retrieval_attempted SET DEFAULT false;

-- Add a comment for documentation
COMMENT ON COLUMN interface_exceptions.order_retrieval_attempted IS 'Indicates whether order data retrieval has been attempted for this exception';