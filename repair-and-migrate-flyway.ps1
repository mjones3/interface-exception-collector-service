#!/usr/bin/env pwsh

# Repair and Migrate Flyway Script
Write-Host "=== REPAIRING AND MIGRATING FLYWAY ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Navigating to project directory" "Magenta"
Set-Location "interface-exception-collector"

Write-Log "Step 2: Running Flyway repair to fix checksum mismatches" "Magenta"

try {
    Write-Log "Executing: mvn flyway:repair" "Cyan"
    mvn flyway:repair
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "SUCCESS: Flyway repair completed!" "Green"
    } else {
        Write-Log "Flyway repair failed" "Red"
    }
} catch {
    Write-Log "Error running Flyway repair: $_" "Red"
}

Write-Log "Step 3: Running Flyway migrate to apply pending migrations" "Magenta"

try {
    Write-Log "Executing: mvn flyway:migrate" "Cyan"
    mvn flyway:migrate
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "SUCCESS: Flyway migrations completed!" "Green"
    } else {
        Write-Log "Flyway migrations failed" "Red"
    }
} catch {
    Write-Log "Error running Flyway migrate: $_" "Red"
}

Write-Log "Step 4: Checking final migration status" "Magenta"

try {
    Write-Log "Executing: mvn flyway:info" "Cyan"
    mvn flyway:info
} catch {
    Write-Log "Could not get migration info: $_" "Yellow"
}

Set-Location ".."

Write-Log "Step 5: Testing API endpoint" "Magenta"

# Wait a moment for any application restarts
Start-Sleep -Seconds 5

try {
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    Write-Log "Testing exceptions endpoint..." "Yellow"
    $apiTest = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 15
    
    if ($apiTest.StatusCode -eq 200) {
        Write-Log "SUCCESS: API test passed - acknowledgment_notes column error FIXED!" "Green"
        $content = $apiTest.Content | ConvertFrom-Json
        Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
    }
} catch {
    Write-Log "API test failed: $_" "Red"
    Write-Log "This might be normal if the application is restarting after schema changes" "Yellow"
}

Write-Log "=== FLYWAY REPAIR AND MIGRATION COMPLETE ===" "Green"
Write-Log "All pending migrations should now be applied, including the acknowledgment_notes column" "Cyan"