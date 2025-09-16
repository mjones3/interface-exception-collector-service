#!/usr/bin/env pwsh

# Database Schema Verification Script
Write-Host "=== Database Schema Verification ===" -ForegroundColor Green

# Function to check database schema
function Test-DatabaseSchema {
    Write-Host "Checking database schema..." -ForegroundColor Yellow
    
    # Create Python script to check schema
    $schemaCheck = @"
import psycopg2
import json

try:
    # Connect to database
    conn = psycopg2.connect(
        host="localhost",
        port="5432", 
        database="interface_exceptions",
        user="postgres",
        password="postgres"
    )
    
    cursor = conn.cursor()
    
    # Get all columns for interface_exceptions table
    cursor.execute("""
        SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
        FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions'
        ORDER BY ordinal_position
    """)
    
    columns = cursor.fetchall()
    
    print("Current interface_exceptions table schema:")
    print("-" * 80)
    for col in columns:
        print(f"{col[0]:<30} {col[1]:<20} {col[2] or 'N/A':<10} {col[3]:<10} {col[4] or 'N/A'}")
    
    # Check for specific required columns
    required_columns = [
        'acknowledgment_notes',
        'resolution_method', 
        'resolution_notes',
        'acknowledged_at',
        'acknowledged_by',
        'resolved_at',
        'resolved_by'
    ]
    
    print("\nRequired columns check:")
    print("-" * 40)
    
    existing_columns = [col[0] for col in columns]
    missing_columns = []
    
    for req_col in required_columns:
        if req_col in existing_columns:
            print(f"✓ {req_col}")
        else:
            print(f"✗ {req_col} - MISSING")
            missing_columns.append(req_col)
    
    if missing_columns:
        print(f"\nMissing columns: {', '.join(missing_columns)}")
        exit(1)
    else:
        print("\n✓ All required columns are present!")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"Database connection failed: {e}")
    exit(1)
"@

    # Save and run schema check
    $schemaCheck | Out-File -FilePath "check_schema.py" -Encoding UTF8
    
    try {
        python check_schema.py
        $schemaOk = $LASTEXITCODE -eq 0
        Remove-Item "check_schema.py" -ErrorAction SilentlyContinue
        return $schemaOk
    }
    catch {
        Write-Host "Failed to run schema check: $_" -ForegroundColor Red
        Remove-Item "check_schema.py" -ErrorAction SilentlyContinue
        return $false
    }
}

# Function to add missing columns
function Add-MissingColumns {
    Write-Host "Adding missing columns to database..." -ForegroundColor Yellow
    
    $addColumnsSql = @"
-- Add missing columns if they don't exist
DO `$`$
BEGIN
    -- Add acknowledgment_notes column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    -- Add resolution_method column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    END IF;
    
    -- Add resolution_notes column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes VARCHAR(1000);
        RAISE NOTICE 'Added resolution_notes column';
    END IF;
    
    -- Add acknowledged_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added acknowledged_at column';
    END IF;
    
    -- Add acknowledged_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_by VARCHAR(255);
        RAISE NOTICE 'Added acknowledged_by column';
    END IF;
    
    -- Add resolved_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added resolved_at column';
    END IF;
    
    -- Add resolved_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_by VARCHAR(255);
        RAISE NOTICE 'Added resolved_by column';
    END IF;
END
`$`$;
"@

    # Save SQL to file
    $addColumnsSql | Out-File -FilePath "add_missing_columns.sql" -Encoding UTF8
    
    # Execute SQL
    try {
        $env:PGPASSWORD = "postgres"
        psql -h localhost -p 5432 -U postgres -d interface_exceptions -f add_missing_columns.sql
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Successfully added missing columns" -ForegroundColor Green
            Remove-Item "add_missing_columns.sql" -ErrorAction SilentlyContinue
            return $true
        } else {
            Write-Host "✗ Failed to add columns" -ForegroundColor Red
            Remove-Item "add_missing_columns.sql" -ErrorAction SilentlyContinue
            return $false
        }
    }
    catch {
        Write-Host "✗ Error executing SQL: $_" -ForegroundColor Red
        Remove-Item "add_missing_columns.sql" -ErrorAction SilentlyContinue
        return $false
    }
}

# Main execution
Write-Host "Step 1: Checking current database schema..." -ForegroundColor Cyan
if (Test-DatabaseSchema) {
    Write-Host "✓ Database schema is correct!" -ForegroundColor Green
} else {
    Write-Host "✗ Database schema has missing columns" -ForegroundColor Red
    Write-Host "Step 2: Adding missing columns..." -ForegroundColor Cyan
    
    if (Add-MissingColumns) {
        Write-Host "Step 3: Re-verifying schema..." -ForegroundColor Cyan
        if (Test-DatabaseSchema) {
            Write-Host "✓ Database schema is now correct!" -ForegroundColor Green
        } else {
            Write-Host "✗ Schema verification still failing" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✗ Failed to add missing columns" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n=== Schema Verification Complete ===" -ForegroundColor Green