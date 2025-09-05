-- V18__Complete_schema_validation_and_fixes.sql
-- Comprehensive schema validation and fixes to ensure all entity fields have corresponding database columns
-- This migration ensures 100% compatibility between JPA entities and database schema

-- =====================================================
-- INTERFACE_EXCEPTIONS TABLE SCHEMA VALIDATION
-- =====================================================

-- Ensure all columns from InterfaceException entity exist
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY,
ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(255) NOT NULL,
ADD COLUMN IF NOT EXISTS interface_type VARCHAR(50) NOT NULL,
ADD COLUMN IF NOT EXISTS exception_reason TEXT NOT NULL,
ADD COLUMN IF NOT EXISTS operation VARCHAR(100) NOT NULL,
ADD COLUMN IF NOT EXISTS external_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'NEW',
ADD COLUMN IF NOT EXISTS severity VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
ADD COLUMN IF NOT EXISTS category VARCHAR(50) NOT NULL,
ADD COLUMN IF NOT EXISTS retryable BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN IF NOT EXISTS customer_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS location_code VARCHAR(100),
ADD COLUMN IF NOT EXISTS timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS acknowledged_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS resolved_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50),
ADD COLUMN IF NOT EXISTS resolution_notes VARCHAR(1000),
ADD COLUMN IF NOT EXISTS acknowledgment_notes VARCHAR(1000),
ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS max_retries INTEGER NOT NULL DEFAULT 3,
ADD COLUMN IF NOT EXISTS last_retry_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS order_received JSONB,
ADD COLUMN IF NOT EXISTS order_retrieval_attempted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS order_retrieval_error TEXT,
ADD COLUMN IF NOT EXISTS order_retrieved_at TIMESTAMP WITH TIME ZONE;

-- Ensure unique constraint on transaction_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'interface_exceptions' 
        AND constraint_name = 'interface_exceptions_transaction_id_key'
    ) THEN
        ALTER TABLE interface_exceptions ADD CONSTRAINT interface_exceptions_transaction_id_key UNIQUE (transaction_id);
    END IF;
END $$;

-- =====================================================
-- RETRY_ATTEMPTS TABLE SCHEMA VALIDATION
-- =====================================================

-- Create retry_attempts table if it doesn't exist
CREATE TABLE IF NOT EXISTS retry_attempts (
    id BIGSERIAL PRIMARY KEY,
    exception_id BIGINT NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    initiated_by VARCHAR(255) NOT NULL,
    initiated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    result_success BOOLEAN,
    result_message TEXT,
    result_response_code INTEGER,
    result_error_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_retry_attempts_exception 
        FOREIGN KEY (exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT retry_attempts_exception_id_attempt_number_key 
        UNIQUE (exception_id, attempt_number)
);

-- Ensure all columns exist in retry_attempts
ALTER TABLE retry_attempts 
ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY,
ADD COLUMN IF NOT EXISTS exception_id BIGINT NOT NULL,
ADD COLUMN IF NOT EXISTS attempt_number INTEGER NOT NULL,
ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS initiated_by VARCHAR(255) NOT NULL,
ADD COLUMN IF NOT EXISTS initiated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS result_success BOOLEAN,
ADD COLUMN IF NOT EXISTS result_message TEXT,
ADD COLUMN IF NOT EXISTS result_response_code INTEGER,
ADD COLUMN IF NOT EXISTS result_error_details JSONB,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- =====================================================
-- ORDER_ITEMS TABLE SCHEMA VALIDATION
-- =====================================================

-- Create order_items table if it doesn't exist
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    interface_exception_id BIGINT NOT NULL,
    blood_type VARCHAR(10) NOT NULL,
    product_family VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_order_items_interface_exception 
        FOREIGN KEY (interface_exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE
);

-- Ensure all columns exist in order_items
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY,
ADD COLUMN IF NOT EXISTS interface_exception_id BIGINT NOT NULL,
ADD COLUMN IF NOT EXISTS blood_type VARCHAR(10) NOT NULL,
ADD COLUMN IF NOT EXISTS product_family VARCHAR(50) NOT NULL,
ADD COLUMN IF NOT EXISTS quantity INTEGER NOT NULL,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- =====================================================
-- EXCEPTION_STATUS_CHANGES TABLE SCHEMA VALIDATION
-- =====================================================

-- Create exception_status_changes table if it doesn't exist
CREATE TABLE IF NOT EXISTS exception_status_changes (
    id BIGSERIAL PRIMARY KEY,
    exception_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_exception_status_changes_exception 
        FOREIGN KEY (exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE
);

-- Ensure all columns exist in exception_status_changes
ALTER TABLE exception_status_changes 
ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY,
ADD COLUMN IF NOT EXISTS exception_id BIGINT NOT NULL,
ADD COLUMN IF NOT EXISTS from_status VARCHAR(50),
ADD COLUMN IF NOT EXISTS to_status VARCHAR(50) NOT NULL,
ADD COLUMN IF NOT EXISTS changed_by VARCHAR(255) NOT NULL,
ADD COLUMN IF NOT EXISTS changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
ADD COLUMN IF NOT EXISTS reason VARCHAR(500),
ADD COLUMN IF NOT EXISTS notes VARCHAR(1000),
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Interface exceptions indexes
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_transaction_id ON interface_exceptions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_interface_type ON interface_exceptions(interface_type);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_status ON interface_exceptions(status);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_severity ON interface_exceptions(severity);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_customer_id ON interface_exceptions(customer_id);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_timestamp ON interface_exceptions(timestamp);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_processed_at ON interface_exceptions(processed_at);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_type_status ON interface_exceptions(interface_type, status);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_severity_timestamp ON interface_exceptions(severity, timestamp);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method ON interface_exceptions(resolution_method);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_received ON interface_exceptions USING gin(order_received);
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_order_retrieval ON interface_exceptions(order_retrieval_attempted, order_retrieved_at);

-- Retry attempts indexes
CREATE INDEX IF NOT EXISTS idx_retry_attempts_exception_id ON retry_attempts(exception_id);
CREATE INDEX IF NOT EXISTS idx_retry_attempts_status ON retry_attempts(status);
CREATE INDEX IF NOT EXISTS idx_retry_attempts_initiated_at ON retry_attempts(initiated_at);

-- Order items indexes
CREATE INDEX IF NOT EXISTS idx_order_items_interface_exception_id ON order_items(interface_exception_id);
CREATE INDEX IF NOT EXISTS idx_order_items_blood_type ON order_items(blood_type);
CREATE INDEX IF NOT EXISTS idx_order_items_product_family ON order_items(product_family);

-- Status changes indexes
CREATE INDEX IF NOT EXISTS idx_status_changes_exception_id ON exception_status_changes(exception_id);
CREATE INDEX IF NOT EXISTS idx_status_changes_changed_at ON exception_status_changes(changed_at);
CREATE INDEX IF NOT EXISTS idx_status_changes_changed_by ON exception_status_changes(changed_by);
CREATE INDEX IF NOT EXISTS idx_status_changes_from_to_status ON exception_status_changes(from_status, to_status);

-- =====================================================
-- CONSTRAINTS AND CHECK CONSTRAINTS
-- =====================================================

-- Interface type check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'interface_exceptions_interface_type_check'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD CONSTRAINT interface_exceptions_interface_type_check 
        CHECK (interface_type IN ('ORDER', 'COLLECTION', 'DISTRIBUTION', 'RECRUITMENT', 'PARTNER_ORDER', 'INVENTORY'));
    END IF;
END $$;

-- Status check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'interface_exceptions_status_check'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD CONSTRAINT interface_exceptions_status_check 
        CHECK (status IN ('NEW', 'ACKNOWLEDGED', 'RETRIED_SUCCESS', 'RETRIED_FAILED', 'ESCALATED', 'RESOLVED', 'CLOSED'));
    END IF;
END $$;

-- Severity check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'interface_exceptions_severity_check'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD CONSTRAINT interface_exceptions_severity_check 
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));
    END IF;
END $$;

-- Category check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'interface_exceptions_category_check'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD CONSTRAINT interface_exceptions_category_check 
        CHECK (category IN ('BUSINESS_RULE', 'VALIDATION', 'SYSTEM_ERROR', 'TIMEOUT', 'NETWORK'));
    END IF;
END $$;

-- Resolution method check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'interface_exceptions_resolution_method_check'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD CONSTRAINT interface_exceptions_resolution_method_check 
        CHECK (resolution_method IN ('RETRY_SUCCESS', 'MANUAL_RESOLUTION', 'CUSTOMER_RESOLVED', 'AUTOMATED'));
    END IF;
END $$;

-- Retry status check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'retry_attempts_status_check'
    ) THEN
        ALTER TABLE retry_attempts 
        ADD CONSTRAINT retry_attempts_status_check 
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));
    END IF;
END $$;

-- Order items quantity check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'order_items_quantity_check'
    ) THEN
        ALTER TABLE order_items 
        ADD CONSTRAINT order_items_quantity_check 
        CHECK (quantity > 0);
    END IF;
END $$;

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

-- Interface exceptions table comments
COMMENT ON TABLE interface_exceptions IS 'Stores exception events from BioPro interface services';
COMMENT ON COLUMN interface_exceptions.transaction_id IS 'Unique identifier for the transaction that caused the exception';
COMMENT ON COLUMN interface_exceptions.interface_type IS 'Type of interface service (ORDER, COLLECTION, DISTRIBUTION, RECRUITMENT, PARTNER_ORDER, INVENTORY)';
COMMENT ON COLUMN interface_exceptions.exception_reason IS 'Detailed reason for the exception';
COMMENT ON COLUMN interface_exceptions.operation IS 'Operation that was being performed when exception occurred';
COMMENT ON COLUMN interface_exceptions.external_id IS 'External identifier (order ID, collection ID, etc.)';
COMMENT ON COLUMN interface_exceptions.status IS 'Current status of the exception';
COMMENT ON COLUMN interface_exceptions.severity IS 'Severity level of the exception';
COMMENT ON COLUMN interface_exceptions.category IS 'Category classification of the exception';
COMMENT ON COLUMN interface_exceptions.retryable IS 'Whether this exception can be retried';
COMMENT ON COLUMN interface_exceptions.customer_id IS 'Customer associated with the exception';
COMMENT ON COLUMN interface_exceptions.location_code IS 'Location code where exception occurred';
COMMENT ON COLUMN interface_exceptions.timestamp IS 'When the exception originally occurred';
COMMENT ON COLUMN interface_exceptions.processed_at IS 'When the exception was processed by this service';
COMMENT ON COLUMN interface_exceptions.acknowledged_at IS 'When the exception was acknowledged';
COMMENT ON COLUMN interface_exceptions.acknowledged_by IS 'User who acknowledged the exception';
COMMENT ON COLUMN interface_exceptions.resolved_at IS 'When the exception was resolved';
COMMENT ON COLUMN interface_exceptions.resolved_by IS 'User who resolved the exception';
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Notes about how the exception was resolved';
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when the exception was acknowledged';
COMMENT ON COLUMN interface_exceptions.retry_count IS 'Number of retry attempts made';
COMMENT ON COLUMN interface_exceptions.max_retries IS 'Maximum number of retry attempts allowed';
COMMENT ON COLUMN interface_exceptions.last_retry_at IS 'When the last retry attempt was made';
COMMENT ON COLUMN interface_exceptions.created_at IS 'When the record was created';
COMMENT ON COLUMN interface_exceptions.updated_at IS 'When the record was last updated';
COMMENT ON COLUMN interface_exceptions.updated_by IS 'User who last updated the exception';
COMMENT ON COLUMN interface_exceptions.order_received IS 'Complete order data retrieved from Partner Order Service or mock server stored as JSON';
COMMENT ON COLUMN interface_exceptions.order_retrieval_attempted IS 'Whether order data retrieval was attempted during exception processing';
COMMENT ON COLUMN interface_exceptions.order_retrieval_error IS 'Error message if order retrieval failed, null if successful or not attempted';
COMMENT ON COLUMN interface_exceptions.order_retrieved_at IS 'Timestamp when order data was successfully retrieved from the source service';

-- Retry attempts table comments
COMMENT ON TABLE retry_attempts IS 'Tracks retry attempts made for interface exceptions';
COMMENT ON COLUMN retry_attempts.exception_id IS 'Foreign key reference to interface_exceptions table';
COMMENT ON COLUMN retry_attempts.attempt_number IS 'Sequential number of the retry attempt (1, 2, 3, etc.)';
COMMENT ON COLUMN retry_attempts.status IS 'Status of the retry attempt (PENDING, SUCCESS, FAILED)';
COMMENT ON COLUMN retry_attempts.initiated_by IS 'User or system that initiated the retry';
COMMENT ON COLUMN retry_attempts.initiated_at IS 'When the retry attempt was started';
COMMENT ON COLUMN retry_attempts.completed_at IS 'When the retry attempt was completed';
COMMENT ON COLUMN retry_attempts.result_success IS 'Whether the retry was successful';
COMMENT ON COLUMN retry_attempts.result_message IS 'Result message from the retry attempt';
COMMENT ON COLUMN retry_attempts.result_response_code IS 'HTTP response code from the retry attempt';
COMMENT ON COLUMN retry_attempts.result_error_details IS 'JSON object containing detailed error information';

-- Order items table comments
COMMENT ON TABLE order_items IS 'Stores order item details from OrderRejected events';
COMMENT ON COLUMN order_items.interface_exception_id IS 'Foreign key to interface_exceptions table';
COMMENT ON COLUMN order_items.blood_type IS 'Blood type for the order item (e.g., O+, A-, AB+)';
COMMENT ON COLUMN order_items.product_family IS 'Product family classification (e.g., WHOLE_BLOOD, PLATELETS)';
COMMENT ON COLUMN order_items.quantity IS 'Quantity requested for this item';

-- Status changes table comments
COMMENT ON TABLE exception_status_changes IS 'Audit trail of all status changes for interface exceptions';
COMMENT ON COLUMN exception_status_changes.exception_id IS 'Foreign key to interface_exceptions table';
COMMENT ON COLUMN exception_status_changes.from_status IS 'Previous status before the change';
COMMENT ON COLUMN exception_status_changes.to_status IS 'New status after the change';
COMMENT ON COLUMN exception_status_changes.changed_by IS 'User or system that initiated the status change';
COMMENT ON COLUMN exception_status_changes.changed_at IS 'Timestamp when the status change occurred';
COMMENT ON COLUMN exception_status_changes.reason IS 'Optional reason for the status change';
COMMENT ON COLUMN exception_status_changes.notes IS 'Optional additional notes about the status change';

-- =====================================================
-- VALIDATION SUMMARY
-- =====================================================

-- Log completion
DO $$
BEGIN
    RAISE NOTICE 'Schema validation and fixes completed successfully';
    RAISE NOTICE 'All entity fields now have corresponding database columns';
    RAISE NOTICE 'All indexes and constraints have been validated';
END $$;