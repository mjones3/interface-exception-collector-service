#!/usr/bin/env powershell
# Quick fix for missing acknowledgment_notes column
Write-Host "🚀 Quick Database Column Fix" -ForegroundColor Green
Write-Host ""

Write-Host "Adding missing acknowledgment_notes column..." -ForegroundColor Yellow

# Try different ways to connect to PostgreSQL
$connectionMethods = @(
    @{
        Name = "Docker Exec (postgres container)"
        Command = "docker exec -i postgres psql -U postgres -d exception_collector_db"
    },
    @{
        Name = "Docker Exec (postgres_container)"
        Command = "docker exec -i postgres_container psql -U postgres -d exception_collector_db"
    },
    @{
        Name = "Local psql"
        Command = "psql -h localhost -p 5432 -U postgres -d exception_collector_db"
    }
)

$success = $false

foreach ($method in $connectionMethods) {
    Write-Host "Trying: $($method.Name)..." -ForegroundColor Cyan
    
    try {
        # Execute the SQL to add the missing column
        $sql = @"
DO `$`$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes TEXT;
        
        RAISE NOTICE 'Added acknowledgment_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
END
`$`$;
"@
        
        $sql | & $method.Command.Split(' ')
        Write-Host "✅ Success with $($method.Name)!" -ForegroundColor Green
        $success = $true
        break
    }
    catch {
        Write-Host "❌ Failed with $($method.Name): $($_.Exception.Message)" -ForegroundColor Red
    }
}

if (-not $success) {
    Write-Host ""
    Write-Host "⚠️  Automatic fix failed. Manual steps:" -ForegroundColor Yellow
    Write-Host "1. Find your PostgreSQL container:" -ForegroundColor Gray
    Write-Host "   docker ps | grep postgres" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Connect to PostgreSQL:" -ForegroundColor Gray
    Write-Host "   docker exec -it <container_name> psql -U postgres -d exception_collector_db" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Run this SQL:" -ForegroundColor Gray
    Write-Host "   ALTER TABLE interface_exceptions ADD COLUMN acknowledgment_notes TEXT;" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "🎯 Column added successfully!" -ForegroundColor Green
    Write-Host "Your application should now work without the schema error." -ForegroundColor White
    Write-Host ""
    Write-Host "If the app is still running, restart it to pick up the change:" -ForegroundColor Yellow
    Write-Host "   tilt down && tilt up" -ForegroundColor Cyan
}