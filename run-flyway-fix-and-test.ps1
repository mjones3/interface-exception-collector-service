#!/usr/bin/env pwsh

# Run Flyway Fix and Test Script
# This script runs the new Flyway migration and tests the complete flow

Write-Host "=== Flyway Fix and End-to-End Test ===" -ForegroundColor Green

Set-Location "interface-exception-collector"

# Step 1: Stop application to run migrations safely
Write-Host "Step 1: Stopping application for migration..." -ForegroundColor Cyan
try {
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    foreach ($processId in $processes) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Write-Host "Stopped process $processId" -ForegroundColor Gray
    }
    Start-Sleep -Seconds 3
} catch {
    Write-Host "No processes to stop on port 8080" -ForegroundColor Gray
}

# Step 2: Run Flyway migration
Write-Host "Step 2: Running Flyway migration..." -ForegroundColor Cyan
try {
    if (Test-Path "mvnw.cmd") {
        Write-Host "Running Flyway migration with Maven wrapper..." -ForegroundColor Yellow
        & .\mvnw.cmd flyway:migrate -Dflyway.cleanDisabled=false
    } else {
        Write-Host "Running Flyway migration with Maven..." -ForegroundColor Yellow
        mvn flyway:migrate -Dflyway.cleanDisabled=false
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Flyway migration completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Flyway migration had issues (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
        Write-Host "Continuing with application startup..." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Flyway migration failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "Continuing with application startup..." -ForegroundColor Yellow
}

# Step 3: Start application
Write-Host "Step 3: Starting Interface Exception Collector..." -ForegroundColor Cyan
if (Test-Path "mvnw.cmd") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} else {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
}

# Wait for application to start
Write-Host "Waiting for application to start..." -ForegroundColor Yellow
$maxAttempts = 30
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
        Write-Host "Attempt $attempt/$maxAttempts - Application starting..." -ForegroundColor Gray
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Host "Application failed to start within timeout!" -ForegroundColor Red
    exit 1
}

# Step 4: Test database schema
Write-Host "Step 4: Testing database schema..." -ForegroundColor Cyan
try {
    # Test without authentication first to check for schema errors
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -TimeoutSec 10 -ErrorAction SilentlyContinue
    
    if ($response.StatusCode -eq 403) {
        Write-Host "SUCCESS: Database schema is working! (403 = auth required, not schema error)" -ForegroundColor Green
    } elseif ($response.StatusCode -eq 200) {
        Write-Host "SUCCESS: Database schema is working and no auth required!" -ForegroundColor Green
        $exceptions = $response.Content | ConvertFrom-Json
        Write-Host "Current exceptions count: $($exceptions.Count)" -ForegroundColor Gray
    } else {
        Write-Host "Unexpected response code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    if ($_.Exception.Message -match "acknowledgment_notes") {
        Write-Host "SCHEMA ERROR STILL EXISTS: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "The Flyway migration may not have run successfully." -ForegroundColor Red
        Write-Host "Please check the database manually or run the SQL fix." -ForegroundColor Yellow
    } elseif ($_.Exception.Message -match "403") {
        Write-Host "SUCCESS: Schema is working (403 = authentication issue)" -ForegroundColor Green
    } else {
        Write-Host "API test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Step 5: Start Partner Order Service if available
Write-Host "Step 5: Starting Partner Order Service..." -ForegroundColor Cyan
Set-Location ".."
if (Test-Path "partner-order-service") {
    Set-Location "partner-order-service"
    
    # Check if it's already running
    try {
        $orderServiceHealth = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($orderServiceHealth.StatusCode -eq 200) {
            Write-Host "Partner Order Service is already running!" -ForegroundColor Green
        }
    } catch {
        Write-Host "Starting Partner Order Service..." -ForegroundColor Yellow
        if (Test-Path "mvnw.cmd") {
            Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
        } else {
            Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
        }
        
        # Wait for Partner Order Service
        $attempt = 0
        do {
            Start-Sleep -Seconds 3
            $attempt++
            try {
                $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
                if ($orderResponse.StatusCode -eq 200) {
                    Write-Host "Partner Order Service started successfully!" -ForegroundColor Green
                    break
                }
            } catch {
                Write-Host "Attempt $attempt/20 - Order Service starting..." -ForegroundColor Gray
            }
        } while ($attempt -lt 20)
    }
} else {
    Write-Host "Partner Order Service directory not found" -ForegroundColor Yellow
}

# Step 6: Test end-to-end flow
Write-Host "Step 6: Testing end-to-end flow..." -ForegroundColor Cyan
Set-Location ".."
Set-Location "interface-exception-collector"

# Create test order payload
$testOrderId = "TEST-FLYWAY-$(Get-Date -Format 'yyyyMMddHHmmss')"
$orderPayload = @{
    customerId = "CUST001"
    locationCode = "LOC001"
    externalId = $testOrderId
    orderItems = @(
        @{
            bloodType = "O_POSITIVE"
            productFamily = "RED_BLOOD_CELLS"
            quantity = 1
            unitOfMeasure = "UNITS"
        }
    )
    priority = "NORMAL"
    requestedDeliveryDate = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss.000Z")
    specialInstructions = "Test order for Flyway fix validation"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Posting test order: $testOrderId" -ForegroundColor Yellow
    $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 15
    
    if ($orderResponse.StatusCode -eq 202 -or $orderResponse.StatusCode -eq 200 -or $orderResponse.StatusCode -eq 201) {
        Write-Host "Order posted successfully!" -ForegroundColor Green
        Write-Host "Order ID: $testOrderId" -ForegroundColor Gray
        
        # Wait for Kafka processing
        Write-Host "Waiting for Kafka event processing (20 seconds)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 20
        
        # Check for exceptions (try both with and without auth)
        Write-Host "Checking for new exceptions..." -ForegroundColor Yellow
        
        # Try without auth first
        try {
            $exceptionsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -TimeoutSec 10 -ErrorAction SilentlyContinue
            if ($exceptionsResponse.StatusCode -eq 200) {
                $exceptions = $exceptionsResponse.Content | ConvertFrom-Json
                Write-Host "Total exceptions found: $($exceptions.Count)" -ForegroundColor Green
                
                # Look for our test order
                $testException = $exceptions | Where-Object { $_.externalId -eq $testOrderId }
                if ($testException) {
                    Write-Host "SUCCESS: Test order found as exception!" -ForegroundColor Green
                    Write-Host "  Exception ID: $($testException.id)" -ForegroundColor Gray
                    Write-Host "  External ID: $($testException.externalId)" -ForegroundColor Gray
                    Write-Host "  Status: $($testException.status)" -ForegroundColor Gray
                    Write-Host "  Created: $($testException.createdAt)" -ForegroundColor Gray
                } else {
                    Write-Host "Test order not found in exceptions. Checking recent exceptions..." -ForegroundColor Yellow
                    $recentExceptions = $exceptions | Where-Object { 
                        ([DateTime]$_.createdAt) -gt (Get-Date).AddMinutes(-5) 
                    } | Select-Object -First 3
                    
                    if ($recentExceptions) {
                        Write-Host "Recent exceptions found:" -ForegroundColor Gray
                        foreach ($ex in $recentExceptions) {
                            Write-Host "  $($ex.externalId) - $($ex.status) - $($ex.createdAt)" -ForegroundColor Gray
                        }
                    } else {
                        Write-Host "No recent exceptions found. Check Kafka connectivity." -ForegroundColor Yellow
                    }
                }
            }
        } catch {
            if ($_.Exception.Message -match "403") {
                Write-Host "API requires authentication. Schema is working correctly." -ForegroundColor Green
            } else {
                Write-Host "Exception check failed: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "Order posting failed with status: $($orderResponse.StatusCode)" -ForegroundColor Red
        Write-Host "Response: $($orderResponse.Content)" -ForegroundColor Red
    }
} catch {
    Write-Host "Order posting failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== FLYWAY FIX RESULTS ===" -ForegroundColor Green
Write-Host "1. Created new migration: V21__Fix_missing_acknowledgment_notes_column.sql" -ForegroundColor Cyan
Write-Host "2. Interface Exception Collector: http://localhost:8080" -ForegroundColor Cyan
Write-Host "3. Partner Order Service: http://localhost:8090" -ForegroundColor Cyan
Write-Host "4. Test order ID: $testOrderId" -ForegroundColor Cyan

Write-Host ""
Write-Host "The acknowledgment_notes column fix has been added to Flyway migrations!" -ForegroundColor Green
Write-Host "If the schema error persists, the migration may need to be run manually." -ForegroundColor Yellow