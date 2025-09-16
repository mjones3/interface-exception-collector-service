#!/usr/bin/env pwsh

# Run Flyway Migrations Script
Write-Host "=== RUNNING FLYWAY MIGRATIONS ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

# Database connection details
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "exception_collector_db"
$DB_USER = "exception_user"
$DB_PASSWORD = "exception_pass"

Write-Log "Step 1: Testing database connection" "Magenta"

$env:PGPASSWORD = $DB_PASSWORD

try {
    $connectionTest = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Database connection successful" "Green"
    } else {
        Write-Log "Database connection failed, waiting..." "Yellow"
        Start-Sleep -Seconds 30
    }
} catch {
    Write-Log "Error testing connection: $_" "Red"
}

Write-Log "Step 2: Running Flyway migrations" "Magenta"

Set-Location "interface-exception-collector"

try {
    Write-Log "Executing Maven Flyway migrate..." "Cyan"
    mvn flyway:migrate -Dflyway.url="jdbc:postgresql://$DB_HOST`:$DB_PORT/$DB_NAME" -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD -Dflyway.locations="classpath:db/migration" -Dflyway.schemas="public" -Dflyway.table="flyway_schema_history" -Dflyway.baselineOnMigrate=true
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Flyway migrations completed successfully" "Green"
    } else {
        Write-Log "Flyway migrations failed" "Red"
    }
} catch {
    Write-Log "Error running migrations: $_" "Red"
}

Set-Location ".."

Write-Log "Step 3: Verifying acknowledgment_notes column" "Magenta"

try {
    $columnCheck = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';" -t 2>&1
    
    if ($columnCheck -match "acknowledgment_notes") {
        Write-Log "SUCCESS: acknowledgment_notes column exists!" "Green"
    } else {
        Write-Log "WARNING: acknowledgment_notes column not found" "Yellow"
    }
} catch {
    Write-Log "Error checking column: $_" "Red"
}

Write-Log "Step 4: Testing API endpoint (if running)" "Magenta"

try {
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $apiTest = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
    if ($apiTest.StatusCode -eq 200) {
        Write-Log "API test successful - no column errors!" "Green"
    }
} catch {
    Write-Log "API not ready yet: $_" "Yellow"
}

Write-Log "=== FLYWAY MIGRATIONS COMPLETE ===" "Green"

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue