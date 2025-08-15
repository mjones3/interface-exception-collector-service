-- Create audit triggers for automatic timestamp updates
-- This ensures updated_at is automatically set when records are modified

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for interface_exceptions table
CREATE TRIGGER update_interface_exceptions_updated_at 
    BEFORE UPDATE ON interface_exceptions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON FUNCTION update_updated_at_column() IS 'Function to automatically update the updated_at timestamp';
COMMENT ON TRIGGER update_interface_exceptions_updated_at ON interface_exceptions IS 'Trigger to automatically update updated_at on record modification';