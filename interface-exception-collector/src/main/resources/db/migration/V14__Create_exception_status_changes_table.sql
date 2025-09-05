-- Create exception status changes table for audit trail
-- This table tracks all status transitions for interface exceptions

CREATE TABLE IF NOT EXISTS exception_status_changes (
    id BIGSERIAL PRIMARY KEY,
    exception_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    reason VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_exception_status_changes_exception 
        FOREIGN KEY (exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_exception_status_changes_exception_id ON exception_status_changes(exception_id);
CREATE INDEX IF NOT EXISTS idx_exception_status_changes_changed_at ON exception_status_changes(changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_exception_status_changes_status_transition ON exception_status_changes(from_status, to_status);

-- Add comments for documentation
COMMENT ON TABLE exception_status_changes IS 'Audit trail of all status changes for interface exceptions';
COMMENT ON COLUMN exception_status_changes.exception_id IS 'Foreign key to interface_exceptions table';
COMMENT ON COLUMN exception_status_changes.from_status IS 'Previous status before the change';
COMMENT ON COLUMN exception_status_changes.to_status IS 'New status after the change';
COMMENT ON COLUMN exception_status_changes.changed_by IS 'User or system that initiated the status change';
COMMENT ON COLUMN exception_status_changes.changed_at IS 'Timestamp when the status change occurred';
COMMENT ON COLUMN exception_status_changes.reason IS 'Optional reason for the status change';
COMMENT ON COLUMN exception_status_changes.notes IS 'Optional additional notes about the status change';

-- Create a trigger to automatically create status change records when exception status is updated
CREATE OR REPLACE FUNCTION create_status_change_audit()
RETURNS TRIGGER AS $$
BEGIN
    -- Only create audit record if status actually changed
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO exception_status_changes (
            exception_id,
            from_status,
            to_status,
            changed_by,
            changed_at,
            reason
        ) VALUES (
            NEW.id,
            OLD.status,
            NEW.status,
            COALESCE(NEW.updated_by, 'SYSTEM'),
            NEW.updated_at,
            'Automatic status change audit'
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger on interface_exceptions table
CREATE TRIGGER trigger_status_change_audit
    AFTER UPDATE ON interface_exceptions
    FOR EACH ROW
    EXECUTE FUNCTION create_status_change_audit();

-- Add updated_by column to interface_exceptions if it doesn't exist
-- This will be used to track who made the status change
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN updated_by VARCHAR(255);
        
        COMMENT ON COLUMN interface_exceptions.updated_by IS 'User who last updated the exception';
    END IF;
END $$;