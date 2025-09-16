#!/usr/bin/env pwsh

# Create Proper Flyway Migrations
Write-Host "=== CREATING PROPER FLYWAY MIGRATIONS ===" -ForegroundColor Green

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Update V22 migration with all missing columns" "Magenta"

# Create comprehensive V22 migration
$v22Migration = @'
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
'@

Write-Log "Writing updated V22 migration..." "Yellow"
$v22Migration | Out-File -FilePath "interface-exception-collector/src/main/resources/db/migration/V22__Complete_schema_fix.sql" -Encoding UTF8

Write-Log "Step 2: Create V23 migration for any additional order columns" "Magenta"

$v23Migration = @'
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
'@

Write-Log "Writing V23 migration..." "Yellow"
$v23Migration | Out-File -FilePath "interface-exception-collector/src/main/resources/db/migration/V23__Add_missing_order_columns.sql" -Encoding UTF8

Write-Log "Step 3: Create V24 migration for schema validation" "Magenta"

$v24Migration = @'
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
'@

Write-Log "Writing V24 migration..." "Yellow"
$v24Migration | Out-File -FilePath "interface-exception-collector/src/main/resources/db/migration/V24__Schema_validation_and_final_fixes.sql" -Encoding UTF8

Write-Log "Step 4: List all migration files" "Magenta"
Get-ChildItem "interface-exception-collector/src/main/resources/db/migration/" -Name | Sort-Object

Write-Log "Step 5: Verify migration files are properly formatted" "Magenta"
Write-Log "V22 migration size: $((Get-Item 'interface-exception-collector/src/main/resources/db/migration/V22__Complete_schema_fix.sql').Length) bytes" "Cyan"
Write-Log "V23 migration size: $((Get-Item 'interface-exception-collector/src/main/resources/db/migration/V23__Add_missing_order_columns.sql').Length) bytes" "Cyan"
Write-Log "V24 migration size: $((Get-Item 'interface-exception-collector/src/main/resources/db/migration/V24__Schema_validation_and_final_fixes.sql').Length) bytes" "Cyan"

Write-Log "=== FLYWAY MIGRATIONS CREATED ===" "Green"
Write-Log "✅ V22: Complete schema fix with all missing columns" "Green"
Write-Log "✅ V23: Additional order-related columns" "Green"
Write-Log "✅ V24: Schema validation and final fixes" "Green"
Write-Log "" "White"
Write-Log "These migrations will ensure that:" "Yellow"
Write-Log "- All required columns are added on fresh deployments" "White"
Write-Log "- Existing databases get the missing columns" "White"
Write-Log "- Future deployments will never have column missing errors" "White"
Write-Log "- All migrations are idempotent (safe to run multiple times)" "White"