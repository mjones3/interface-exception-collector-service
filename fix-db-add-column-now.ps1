#!/usr/bin/env powershell
# Add missing acknowledgment_notes column immediately
Write-Host "🚀 Adding Missing Database Column" -ForegroundColor Green
Write-Host ""

# SQL to add the missing column
$addColumnSql = @"
-- Add acknowledgment_notes column if it doesn't exist
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS acknowledgment_notes VARCHAR(1000);

-- Add resolution fields too in case they're missing
ALTER TABLE interface_exceptions 
ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50),
ADD COLUMN IF NOT EXISTS resolution_notes VARCHAR(1000);

-- Add index for resolution method
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method ON interface_exceptions(resolution_method);

-- Add comments
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception (RETRY_SUCCESS, MANUAL_RESOLUTION, CUSTOMER_RESOLVED, AUTOMATED)';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Notes about how the exception was resolved';
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when the exception was acknowledged';

-- Verify columns exist
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name IN ('acknowledgment_notes', 'resolution_method', 'resolution_notes')
ORDER BY column_name;
"@

Write-Host "Attempting to add missing columns..." -ForegroundColor Yellow

# Try different connection methods
$connectionAttempts = @(
    "docker exec -i postgres psql -U postgres -d exception_collector_db",
    "docker exec -i postgres_container psql -U postgres -d exception_collector_db", 
    "psql -h localhost -p 5432 -U postgres -d exception_collector_db"
)

$success = $false

foreach ($cmd in $connectionAttempts) {
    Write-Host "Trying: $cmd" -ForegroundColor Cyan
    
    try {
        $addColumnSql | Invoke-Expression $cmd
        Write-Host "✅ Successfully added columns!" -ForegroundColor Green
        $success = $true
        break
    }
    catch {
        Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if (-not $success) {
    Write-Host ""
    Write-Host "⚠️  Automatic column addition failed. Manual steps:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Find PostgreSQL container:" -ForegroundColor Gray
    Write-Host "   docker ps" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Connect to database:" -ForegroundColor Gray
    Write-Host "   docker exec -it <postgres_container> psql -U postgres -d exception_collector_db" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Run this SQL:" -ForegroundColor Gray
    Write-Host "   ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS acknowledgment_notes VARCHAR(1000);" -ForegroundColor White
    Write-Host "   ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50);" -ForegroundColor White
    Write-Host "   ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_notes VARCHAR(1000);" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "🎯 Database schema fixed!" -ForegroundColor Green
    Write-Host "The acknowledgment_notes column has been added." -ForegroundColor White
    Write-Host ""
    Write-Host "Your application should now start without schema errors!" -ForegroundColor Yellow
}