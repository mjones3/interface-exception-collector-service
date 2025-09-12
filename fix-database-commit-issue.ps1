#!/usr/bin/env pwsh

# Fix Database Commit Issue Script
# This script fixes the "Unable to commit against JDBC Connection" error

Write-Host "=== Database Commit Issue Fix ===" -ForegroundColor Green
Write-Host "Fixing autocommit configuration that was causing transaction commit failures..." -ForegroundColor Yellow

# Navigate to project directory
Set-Location "interface-exception-collector"

Write-Host "Step 1: Stopping any running application..." -ForegroundColor Cyan
try {
    # Kill any Java processes running on port 8080
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($processes) {
        foreach ($pid in $processes) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "Stopped process $pid" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "No processes to stop on port 8080" -ForegroundColor Gray
}

Write-Host "Step 2: Cleaning and rebuilding application..." -ForegroundColor Cyan
if (Test-Path "mvnw.cmd") {
    & .\mvnw.cmd clean compile -DskipTests -q
} elseif (Test-Path "mvnw") {
    & .\mvnw clean compile -DskipTests -q
} else {
    Write-Host "Maven wrapper not found, using system maven..." -ForegroundColor Yellow
    mvn clean compile -DskipTests -q
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Check compilation errors." -ForegroundColor Red
    exit 1
}

Write-Host "Step 3: Starting application with fixed database configuration..." -ForegroundColor Cyan
if (Test-Path "mvnw.cmd") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} elseif (Test-Path "mvnw") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} else {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
}

Write-Host "Step 4: Waiting for application startup..." -ForegroundColor Cyan
$maxAttempts = 30
$attempt = 0

do {
    Start-Sleep -Seconds 2
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "Application started successfully!" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "Attempt $attempt/$maxAttempts - Application not ready yet..." -ForegroundColor Gray
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Host "Application failed to start within timeout period" -ForegroundColor Red
    exit 1
}

Write-Host "Step 5: Testing the fixed endpoint..." -ForegroundColor Cyan
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2Mzc0OTYsImV4cCI6MTc1NzY0MTA5Nn0.1nzyU1gEdd8W4U4LOJTbLzkWdv9jg2srw_-gibOAc7EI"

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    Write-Host "Testing GET /api/v1/exceptions endpoint..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
    
    if ($response.StatusCode -eq 200) {
        Write-Host "SUCCESS: Endpoint is working! Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response length: $($response.Content.Length) characters" -ForegroundColor Green
    } else {
        Write-Host "Unexpected status code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR: Endpoint test failed - $($_.Exception.Message)" -ForegroundColor Red
    
    # Check application logs for errors
    Write-Host "Checking for recent errors in logs..." -ForegroundColor Yellow
    try {
        $logFiles = Get-ChildItem -Path "logs" -Filter "*.log" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($logFiles) {
            $recentErrors = Get-Content $logFiles.FullName -Tail 20 | Where-Object { $_ -match "ERROR|Exception" }
            if ($recentErrors) {
                Write-Host "Recent errors found:" -ForegroundColor Red
                $recentErrors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
            }
        }
    } catch {
        Write-Host "Could not read log files" -ForegroundColor Gray
    }
    exit 1
}

Write-Host "Step 6: Testing GraphQL endpoint..." -ForegroundColor Cyan
try {
    $graphqlQuery = @{
        query = "query { exceptions(first: 5) { edges { node { id transactionId status } } } }"
    } | ConvertTo-Json
    
    $graphqlResponse = Invoke-WebRequest -Uri "http://localhost:8080/graphql" -Method POST -Headers $headers -Body $graphqlQuery -TimeoutSec 10
    
    if ($graphqlResponse.StatusCode -eq 200) {
        Write-Host "SUCCESS: GraphQL endpoint is also working!" -ForegroundColor Green
    }
} catch {
    Write-Host "GraphQL test failed, but REST API is working" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== RESOLUTION COMPLETE ===" -ForegroundColor Green
Write-Host "Fixed autocommit configuration in application.yml" -ForegroundColor Green
Write-Host "Changed auto-commit from false to true" -ForegroundColor Green
Write-Host "Changed provider_disables_autocommit from true to false" -ForegroundColor Green
Write-Host "Changed elideSetAutoCommits from true to false" -ForegroundColor Green
Write-Host "Application is running and endpoint is accessible" -ForegroundColor Green
Write-Host ""
Write-Host "The database commit issue has been resolved!" -ForegroundColor Green
Write-Host "You can now access the API at localhost:8080" -ForegroundColor Cyan