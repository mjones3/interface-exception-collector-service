-- Create full-text search indexes for interface_exceptions table
-- These indexes enable fast text search across exception_reason and external_id fields

-- Full-text search index for exception_reason field
-- This enables searching within exception messages and error descriptions
CREATE INDEX idx_interface_exceptions_reason_fts 
ON interface_exceptions 
USING gin(to_tsvector('english', exception_reason));

-- Full-text search index for external_id field
-- This enables searching for specific order IDs, collection IDs, etc.
CREATE INDEX idx_interface_exceptions_external_id_fts 
ON interface_exceptions 
USING gin(to_tsvector('english', external_id));

-- Combined full-text search index for both fields
-- This enables searching across both exception_reason and external_id simultaneously
CREATE INDEX idx_interface_exceptions_combined_fts 
ON interface_exceptions 
USING gin(to_tsvector('english', coalesce(exception_reason, '') || ' ' || coalesce(external_id, '')));

-- Full-text search index for operation field
-- This enables searching for specific operations that caused exceptions
CREATE INDEX idx_interface_exceptions_operation_fts 
ON interface_exceptions 
USING gin(to_tsvector('english', operation));

-- Add comments for documentation
COMMENT ON INDEX idx_interface_exceptions_reason_fts IS 'Full-text search index for exception_reason field';
COMMENT ON INDEX idx_interface_exceptions_external_id_fts IS 'Full-text search index for external_id field';
COMMENT ON INDEX idx_interface_exceptions_combined_fts IS 'Combined full-text search index for exception_reason and external_id';
COMMENT ON INDEX idx_interface_exceptions_operation_fts IS 'Full-text search index for operation field';