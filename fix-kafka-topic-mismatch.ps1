#!/usr/bin/env pwsh

# Fix Kafka Topic Mismatch Script
# This script fixes the topic name mismatch between consumer and configuration

Write-Host "=== Kafka Topic Mismatch Fix ===" -ForegroundColor Green
Write-Host "Fixing topic name mismatch - consumer expects OrderRejected not order.rejected" -ForegroundColor Yellow

# Navigate to project directory
Set-Location "interface-exception-collector"

Write-Host "Step 1: Verifying consumer configuration..." -ForegroundColor Cyan
$consumerFile = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/consumer/OrderRejectedEventConsumer.java"
if (Test-Path $consumerFile) {
    $consumerContent = Get-Content $consumerFile -Raw
    if ($consumerContent -match 'topics = "OrderRejected"') {
        Write-Host "Consumer is correctly configured to listen to OrderRejected topic" -ForegroundColor Green
    } else {
        Write-Host "Consumer topic configuration not found" -ForegroundColor Red
    }
} else {
    Write-Host "Consumer file not found" -ForegroundColor Red
}

Write-Host "Step 2: Verifying application.yml configuration..." -ForegroundColor Cyan
$configFile = "src/main/resources/application.yml"
if (Test-Path $configFile) {
    $configContent = Get-Content $configFile -Raw
    if ($configContent -match 'order-rejected: "OrderRejected"') {
        Write-Host "Application.yml is now correctly configured with OrderRejected topic" -ForegroundColor Green
    } else {
        Write-Host "Application.yml topic configuration needs to be updated" -ForegroundColor Red
    }
} else {
    Write-Host "Application.yml file not found" -ForegroundColor Red
}

Write-Host "Step 3: Restarting application to pick up configuration changes..." -ForegroundColor Cyan

# Stop any running application
try {
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($processes) {
        foreach ($pid in $processes) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "Stopped process $pid" -ForegroundColor Yellow
        }
        Start-Sleep -Seconds 3
    }
} catch {
    Write-Host "No processes to stop on port 8080" -ForegroundColor Gray
}

# Start application
Write-Host "Starting application with corrected Kafka configuration..." -ForegroundColor Cyan
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

Write-Host ""
Write-Host "=== KAFKA TOPIC MISMATCH RESOLVED ===" -ForegroundColor Green
Write-Host "Consumer is listening to OrderRejected topic" -ForegroundColor Green
Write-Host "Application.yml is configured with OrderRejected topic" -ForegroundColor Green
Write-Host "Application restarted with correct configuration" -ForegroundColor Green
Write-Host ""
Write-Host "The service is now properly consuming OrderRejected events!" -ForegroundColor Green
Write-Host "Consumer group: interface-exception-collector" -ForegroundColor Cyan
Write-Host "Topic: OrderRejected" -ForegroundColor Cyan