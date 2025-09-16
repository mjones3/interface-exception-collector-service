#!/usr/bin/env pwsh

# Apply Flyway Migrations using Maven Plugin
Write-Host "=== APPLYING FLYWAY MIGRATIONS WITH MAVEN ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Navigating to project directory" "Magenta"
Set-Location "interface-exception-collector"

Write-Log "Step 2: Running Flyway migrations via Maven" "Magenta"

# Set environment variables for Flyway
$env:FLYWAY_URL = "jdbc:postgresql://localhost:5432/exception_collector_db"
$env:FLYWAY_USER = "exception_user"
$env:FLYWAY_PASSWORD = "exception_pass"

try {
    Write-Log "Executing: mvn flyway:migrate" "Cyan"
    mvn flyway:migrate
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "SUCCESS: Flyway migrations completed!" "Green"
    } else {
        Write-Log "Flyway migrations failed with exit code: $LASTEXITCODE" "Red"
        Write-Log "Trying with explicit configuration..." "Yellow"
        
        # Try with explicit configuration
        mvn flyway:migrate -Dflyway.url="jdbc:postgresql://localhost:5432/exception_collector_db" -Dflyway.user="exception_user" -Dflyway.password="exception_pass"
        
        if ($LASTEXITCODE -eq 0) {
            Write-Log "SUCCESS: Flyway migrations completed with explicit config!" "Green"
        } else {
            Write-Log "Flyway migrations still failed" "Red"
        }
    }
} catch {
    Write-Log "Error running Flyway: $_" "Red"
}

Write-Log "Step 3: Checking migration status" "Magenta"

try {
    Write-Log "Running: mvn flyway:info" "Cyan"
    mvn flyway:info
} catch {
    Write-Log "Could not get migration info: $_" "Yellow"
}

Set-Location ".."

Write-Log "Step 4: Testing application if running" "Magenta"

try {
    $healthCheck = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($healthCheck.StatusCode -eq 200) {
        Write-Log "Application is running" "Green"
        
        # Test the exceptions endpoint
        $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $apiTest = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
        if ($apiTest.StatusCode -eq 200) {
            Write-Log "SUCCESS: API test passed - no acknowledgment_notes error!" "Green"
            $content = $apiTest.Content | ConvertFrom-Json
            Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
        }
    }
} catch {
    Write-Log "Application not running or not ready: $_" "Yellow"
}

Write-Log "=== FLYWAY MIGRATION PROCESS COMPLETE ===" "Green"
Write-Log "If migrations were successful, the acknowledgment_notes column should now exist" "Cyan"

# Clean up environment variables
Remove-Item Env:FLYWAY_URL -ErrorAction SilentlyContinue
Remove-Item Env:FLYWAY_USER -ErrorAction SilentlyContinue  
Remove-Item Env:FLYWAY_PASSWORD -ErrorAction SilentlyContinue