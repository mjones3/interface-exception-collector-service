#!/usr/bin/env pwsh

Write-Host "=== Fixing acknowledgment_notes Column Issue ===" -ForegroundColor Green

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "Docker is running" -ForegroundColor Green
} catch {
    Write-Host "Docker is not running. Please start Docker first." -ForegroundColor Red
    exit 1
}

# Check if PostgreSQL container is running
Write-Host "Checking PostgreSQL container..." -ForegroundColor Yellow
$postgresContainer = docker ps --filter "name=postgres" --format "table {{.Names}}\t{{.Status}}"
if ($postgresContainer -match "postgres.*Up") {
    Write-Host "PostgreSQL container is running" -ForegroundColor Green
} else {
    Write-Host "PostgreSQL container is not running. Starting it..." -ForegroundColor Yellow
    docker-compose up -d postgres
    Start-Sleep -Seconds 10
}

# Execute the SQL fix
Write-Host "Adding missing acknowledgment_notes column..." -ForegroundColor Yellow
try {
    # Use docker exec to run the SQL directly
    $sqlCommand = @"
DO `$`$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        
        RAISE NOTICE 'Added acknowledgment_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
END
`$`$;
"@

    docker exec -i postgres psql -U postgres -d exception_collector_db -c $sqlCommand
    
    Write-Host "Successfully executed database fix" -ForegroundColor Green
    
    # Verify the column was added
    Write-Host "Verifying column was added..." -ForegroundColor Yellow
    $verifyCommand = "SELECT column_name, data_type, character_maximum_length FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';"
    docker exec -i postgres psql -U postgres -d exception_collector_db -c $verifyCommand
    
} catch {
    Write-Host "Failed to execute database fix: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=== Database Fix Complete ===" -ForegroundColor Green
Write-Host "The acknowledgment_notes column has been added to the interface_exceptions table." -ForegroundColor Cyan
Write-Host "You can now restart your application." -ForegroundColor Cyan