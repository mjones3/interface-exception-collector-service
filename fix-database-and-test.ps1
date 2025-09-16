#!/usr/bin/env pwsh

# Fix Database Schema and Test Script
# This script specifically fixes the acknowledgment_notes column issue and tests the flow

Write-Host "=== Database Schema Fix and Test ===" -ForegroundColor Green

Set-Location "interface-exception-collector"

# Step 1: Force Flyway to run migrations
Write-Host "Step 1: Running Flyway migrations..." -ForegroundColor Cyan

try {
    if (Test-Path "mvnw.cmd") {
        & .\mvnw.cmd flyway:migrate -Dflyway.cleanDisabled=false
    } else {
        mvn flyway:migrate -Dflyway.cleanDisabled=false
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Flyway migrations completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Flyway migrations had issues, continuing..." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Flyway command failed: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 2: Test database connection with authentication
Write-Host "Step 2: Testing database schema with authentication..." -ForegroundColor Cyan

# Generate a test JWT token (this is a simple test token)
$testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2Mzc0OTYsImV4cCI6MTc1NzY0MTA5Nn0.1nzyU1gEdd8W4U4LOJTbLzkWdv9jg2srw_-gibOAc7EI"

$headers = @{
    "Authorization" = "Bearer $testToken"
    "Content-Type" = "application/json"
}

try {
    Write-Host "Testing exceptions endpoint with authentication..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
    
    if ($response.StatusCode -eq 200) {
        Write-Host "SUCCESS: Database schema is working correctly!" -ForegroundColor Green
        $exceptions = $response.Content | ConvertFrom-Json
        Write-Host "Current exceptions count: $($exceptions.Count)" -ForegroundColor Gray
        
        if ($exceptions.Count -gt 0) {
            $firstException = $exceptions[0]
            Write-Host "Sample exception:" -ForegroundColor Gray
            Write-Host "  ID: $($firstException.id)" -ForegroundColor Gray
            Write-Host "  External ID: $($firstException.externalId)" -ForegroundColor Gray
            Write-Host "  Status: $($firstException.status)" -ForegroundColor Gray
        }
    } else {
        Write-Host "Unexpected response code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    if ($_.Exception.Message -match "acknowledgment_notes") {
        Write-Host "SCHEMA ISSUE DETECTED: acknowledgment_notes column missing" -ForegroundColor Red
        
        # Try to fix with direct SQL
        Write-Host "Attempting to fix database schema..." -ForegroundColor Yellow
        
        # Create a SQL fix file
        $sqlFix = @"
-- Fix for missing acknowledgment_notes column
DO `$`$
BEGIN
    -- Add acknowledgment_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    -- Add resolution_method column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    END IF;
    
    -- Add resolution_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes VARCHAR(1000);
        RAISE NOTICE 'Added resolution_notes column';
    END IF;
END
`$`$;
"@
        
        $sqlFix | Out-File -FilePath "fix_schema.sql" -Encoding UTF8
        Write-Host "Created fix_schema.sql file" -ForegroundColor Yellow
        
        # Try to apply the fix using psql if available
        try {
            $env:PGPASSWORD = "exception_pass"
            psql -h localhost -p 5432 -U exception_user -d exception_collector_db -f fix_schema.sql
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Database schema fixed successfully!" -ForegroundColor Green
                
                # Restart the application to pick up schema changes
                Write-Host "Restarting application..." -ForegroundColor Yellow
                
                # Stop application
                $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
                foreach ($pid in $processes) {
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                }
                Start-Sleep -Seconds 5
                
                # Start application
                if (Test-Path "mvnw.cmd") {
                    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
                } else {
                    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
                }
                
                # Wait for restart
                Write-Host "Waiting for application restart..." -ForegroundColor Yellow
                $attempt = 0
                do {
                    Start-Sleep -Seconds 3
                    $attempt++
                    try {
                        $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
                        if ($healthResponse.StatusCode -eq 200) {
                            Write-Host "Application restarted successfully!" -ForegroundColor Green
                            break
                        }
                    } catch {
                        Write-Host "Attempt $attempt/20 - Restarting..." -ForegroundColor Gray
                    }
                } while ($attempt -lt 20)
                
                # Test again
                try {
                    $retestResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
                    if ($retestResponse.StatusCode -eq 200) {
                        Write-Host "SUCCESS: Database schema is now working!" -ForegroundColor Green
                    }
                } catch {
                    Write-Host "Schema issue still persists: $($_.Exception.Message)" -ForegroundColor Red
                }
            } else {
                Write-Host "Could not apply database fix automatically" -ForegroundColor Red
            }
        } catch {
            Write-Host "psql not available. Please run the SQL manually:" -ForegroundColor Yellow
            Write-Host $sqlFix -ForegroundColor Cyan
        }
    } else {
        Write-Host "API test failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Step 3: Test order posting with correct format
Write-Host "Step 3: Testing order posting..." -ForegroundColor Cyan

$orderPayload = @{
    customerId = "CUST-001"
    locationCode = "LOC-001"
    externalId = "TEST-ORDER-$(Get-Date -Format 'yyyyMMddHHmmss')"
    orderItems = @(
        @{
            bloodType = "O_POSITIVE"
            productFamily = "RED_BLOOD_CELLS"
            quantity = 2
            unitOfMeasure = "UNITS"
        }
    )
    priority = "NORMAL"
    requestedDeliveryDate = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    specialInstructions = "Test order for end-to-end validation"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Posting test order to Partner Order Service..." -ForegroundColor Yellow
    $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 15
    
    if ($orderResponse.StatusCode -eq 202 -or $orderResponse.StatusCode -eq 200) {
        Write-Host "Order posted successfully!" -ForegroundColor Green
        Write-Host "Response: $($orderResponse.Content)" -ForegroundColor Gray
        
        # Wait for Kafka event processing
        Write-Host "Waiting for Kafka event processing..." -ForegroundColor Yellow
        Start-Sleep -Seconds 15
        
        # Check for new exceptions
        try {
            $exceptionsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10
            if ($exceptionsResponse.StatusCode -eq 200) {
                $exceptions = $exceptionsResponse.Content | ConvertFrom-Json
                Write-Host "Total exceptions found: $($exceptions.Count)" -ForegroundColor Green
                
                # Look for recent exceptions
                $recentExceptions = $exceptions | Where-Object { $_.externalId -like "TEST-ORDER-*" -or $_.createdAt -gt (Get-Date).AddMinutes(-5).ToString("yyyy-MM-ddTHH:mm:ss") }
                if ($recentExceptions) {
                    Write-Host "SUCCESS: Found recent exceptions!" -ForegroundColor Green
                    foreach ($ex in $recentExceptions) {
                        Write-Host "  Exception: $($ex.externalId) - Status: $($ex.status)" -ForegroundColor Gray
                    }
                } else {
                    Write-Host "No recent exceptions found. Check Kafka connectivity." -ForegroundColor Yellow
                }
            }
        } catch {
            Write-Host "Could not check exceptions: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Order posting failed with status: $($orderResponse.StatusCode)" -ForegroundColor Red
        Write-Host "Response: $($orderResponse.Content)" -ForegroundColor Red
    }
} catch {
    Write-Host "Order posting failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Message -match "400") {
        Write-Host "This might be a payload format issue. Check Partner Order Service logs." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== FINAL STATUS ===" -ForegroundColor Green
Write-Host "1. Interface Exception Collector: http://localhost:8080" -ForegroundColor Cyan
Write-Host "2. Partner Order Service: http://localhost:8090" -ForegroundColor Cyan
Write-Host "3. Test exceptions endpoint: http://localhost:8080/api/v1/exceptions" -ForegroundColor Cyan
Write-Host "4. GraphQL endpoint: http://localhost:8080/graphql" -ForegroundColor Cyan

if (Test-Path "fix_schema.sql") {
    Write-Host ""
    Write-Host "Database fix file created: fix_schema.sql" -ForegroundColor Yellow
    Write-Host "If schema issues persist, run this SQL manually in your PostgreSQL database." -ForegroundColor Yellow
}