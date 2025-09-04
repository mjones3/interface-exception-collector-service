-- V16__Add_order_data_fields.sql
-- Add order data storage fields to interface_exceptions table for mock RSocket server integration

-- Add new columns for order data storage (using IF NOT EXISTS for safety)
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS order_received JSONB,
ADD COLUMN IF NOT EXISTS order_retrieval_attempted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS order_retrieval_error TEXT,
ADD COLUMN IF NOT EXISTS order_retrieved_at TIMESTAMP WITH TIME ZONE;

-- Index for order data queries using GIN index for JSONB
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_received 
ON interface_exceptions USING gin(order_received);

-- Index for retrieval status queries
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_retrieval 
ON interface_exceptions(order_retrieval_attempted, order_retrieved_at);

-- Index for order retrieval errors
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_retrieval_error 
ON interface_exceptions(order_retrieval_error) 
WHERE order_retrieval_error IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.order_received IS 'Complete order data retrieved from Partner Order Service or mock server stored as JSON';
COMMENT ON COLUMN interface_exceptions.order_retrieval_attempted IS 'Whether order data retrieval was attempted during exception processing';
COMMENT ON COLUMN interface_exceptions.order_retrieval_error IS 'Error message if order retrieval failed, null if successful or not attempted';
COMMENT ON COLUMN interface_exceptions.order_retrieved_at IS 'Timestamp when order data was successfully retrieved from the source service';