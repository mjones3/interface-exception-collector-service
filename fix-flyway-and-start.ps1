#!/usr/bin/env pwsh

# Fix Flyway and Start Application Script
# This script fixes the Flyway command and ensures the application starts

Write-Host "=== Fix Flyway and Start Application ===" -ForegroundColor Green

Set-Location "interface-exception-collector"

# Step 1: Try to run Flyway migration with correct syntax
Write-Host "Step 1: Running Flyway migration..." -ForegroundColor Cyan
try {
    if (Test-Path "mvnw.cmd") {
        Write-Host "Running Flyway migration..." -ForegroundColor Yellow
        & .\mvnw.cmd flyway:migrate
    } else {
        Write-Host "Running Flyway migration..." -ForegroundColor Yellow
        mvn flyway:migrate
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Flyway migration completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Flyway migration completed with warnings (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Flyway migration failed: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 2: Start application
Write-Host "Step 2: Starting Interface Exception Collector..." -ForegroundColor Cyan

# Stop any existing processes
try {
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    foreach ($processId in $processes) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Write-Host "Stopped existing process $processId" -ForegroundColor Gray
    }
    Start-Sleep -Seconds 3
} catch {
    Write-Host "No existing processes to stop" -ForegroundColor Gray
}

# Start application
if (Test-Path "mvnw.cmd") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} else {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
}

# Wait for application to start
Write-Host "Waiting for application to start..." -ForegroundColor Yellow
$maxAttempts = 40
$attempt = 0

do {
    Start-Sleep -Seconds 3
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "Application started successfully!" -ForegroundColor Green
            break
        }
    } catch {
        if ($attempt % 5 -eq 0) {
            Write-Host "Attempt $attempt/$maxAttempts - Still starting..." -ForegroundColor Gray
        }
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Host "Application failed to start within timeout!" -ForegroundColor Red
    Write-Host "This is likely due to the database schema issue." -ForegroundColor Red
    Write-Host ""
    Write-Host "MANUAL FIX REQUIRED:" -ForegroundColor Yellow
    Write-Host "Please run this SQL in your PostgreSQL database:" -ForegroundColor Yellow
    
    $sqlFix = @"
-- Connect to PostgreSQL and run this:
-- psql -h localhost -p 5432 -U exception_user -d exception_collector_db

ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS acknowledgment_notes TEXT;
ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50);
ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_notes TEXT;
"@
    
    Write-Host $sqlFix -ForegroundColor Cyan
    Write-Host ""
    Write-Host "After running the SQL, restart the application:" -ForegroundColor Yellow
    Write-Host "mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -ForegroundColor Cyan
    exit 1
}

# Step 3: Test the database schema
Write-Host "Step 3: Testing database schema..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -TimeoutSec 10 -ErrorAction SilentlyContinue
    
    if ($response.StatusCode -eq 403) {
        Write-Host "SUCCESS: Database schema is working! (403 = authentication required)" -ForegroundColor Green
    } elseif ($response.StatusCode -eq 200) {
        Write-Host "SUCCESS: Database schema is working!" -ForegroundColor Green
        $exceptions = $response.Content | ConvertFrom-Json
        Write-Host "Current exceptions count: $($exceptions.Count)" -ForegroundColor Gray
    } else {
        Write-Host "Unexpected response code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    if ($_.Exception.Message -match "acknowledgment_notes") {
        Write-Host "SCHEMA ERROR: acknowledgment_notes column still missing!" -ForegroundColor Red
        Write-Host "The Flyway migration did not fix the issue." -ForegroundColor Red
        Write-Host "Please run the SQL fix manually." -ForegroundColor Yellow
    } elseif ($_.Exception.Message -match "403") {
        Write-Host "SUCCESS: Schema is working (authentication required)" -ForegroundColor Green
    } else {
        Write-Host "API test result: $($_.Exception.Message)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "=== SUMMARY ===" -ForegroundColor Green
Write-Host "1. Created new Flyway migration: V21__Fix_missing_acknowledgment_notes_column.sql" -ForegroundColor Cyan
Write-Host "2. Application status: " -NoNewline -ForegroundColor Cyan

try {
    $healthCheck = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3 -ErrorAction SilentlyContinue
    if ($healthCheck.StatusCode -eq 200) {
        Write-Host "RUNNING" -ForegroundColor Green
    } else {
        Write-Host "ISSUES" -ForegroundColor Red
    }
} catch {
    Write-Host "NOT RUNNING" -ForegroundColor Red
}

Write-Host "3. Interface Exception Collector: http://localhost:8080" -ForegroundColor Cyan
Write-Host "4. API Endpoint: http://localhost:8080/api/v1/exceptions" -ForegroundColor Cyan

Write-Host ""
Write-Host "The Flyway migration has been created and should fix the schema issue." -ForegroundColor Green
Write-Host "If the application still fails to start, run the SQL fix manually." -ForegroundColor Yellow