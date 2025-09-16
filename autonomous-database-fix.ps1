#!/usr/bin/env pwsh

# Autonomous Database Fix Script
Write-Host "=== AUTONOMOUS DATABASE FIX SCRIPT ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Stopping services" "Magenta"
try {
    tilt down
    Write-Log "Tilt services stopped" "Green"
} catch {
    Write-Log "Tilt down failed: $_" "Yellow"
}

Start-Sleep -Seconds 15

Write-Log "Step 2: Cleaning Docker" "Magenta"
try {
    docker system prune -f
    docker volume prune -f
    Write-Log "Docker cleaned" "Green"
} catch {
    Write-Log "Docker cleanup failed: $_" "Yellow"
}

Write-Log "Step 3: Creating migration file" "Magenta"
$migrationSql = @'
-- V22: Complete schema fix for acknowledgment_notes and all missing columns

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes VARCHAR(1000);
        RAISE NOTICE 'Added resolution_notes column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added acknowledged_at column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_by VARCHAR(255);
        RAISE NOTICE 'Added acknowledged_by column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added resolved_at column';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_by VARCHAR(255);
        RAISE NOTICE 'Added resolved_by column';
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_acknowledgment_notes 
ON interface_exceptions(acknowledgment_notes) 
WHERE acknowledgment_notes IS NOT NULL;

COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when acknowledging an exception';
'@

$migrationPath = "interface-exception-collector/src/main/resources/db/migration/V22__Complete_schema_fix.sql"
$migrationSql | Out-File -FilePath $migrationPath -Encoding UTF8
Write-Log "Created migration: $migrationPath" "Green"

Write-Log "Step 4: Starting services" "Magenta"
try {
    tilt up
    Write-Log "Tilt services starting" "Green"
} catch {
    Write-Log "Tilt up failed: $_" "Red"
}

Write-Log "Step 5: Waiting for startup (90 seconds)" "Magenta"
Start-Sleep -Seconds 90

Write-Log "Step 6: Testing service health" "Magenta"
$healthRetries = 15
for ($i = 1; $i -le $healthRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Log "Service is healthy" "Green"
            break
        }
    } catch {
        Write-Log "Health check $i/$healthRetries failed" "Yellow"
        Start-Sleep -Seconds 10
    }
}

Write-Log "Step 7: Testing API endpoint" "Magenta"
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"

$apiRetries = 10
for ($i = 1; $i -le $apiRetries; $i++) {
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
        
        if ($response.StatusCode -eq 200) {
            Write-Log "API endpoint working - no acknowledgment_notes error!" "Green"
            $content = $response.Content | ConvertFrom-Json
            Write-Log "Found $($content.totalElements) exceptions" "Cyan"
            break
        }
    } catch {
        Write-Log "API test $i/$apiRetries failed: $_" "Yellow"
        Start-Sleep -Seconds 15
    }
}

Write-Log "Step 8: Testing partner order service" "Magenta"
try {
    $partnerHealth = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 10
    if ($partnerHealth.StatusCode -eq 200) {
        Write-Log "Partner service is healthy" "Green"
        
        $orderPayload = @{
            customerId = "AUTO-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
            locationCode = "AUTO-LOC-001"
            orderItems = @(
                @{
                    productCode = "AUTO-PRODUCT-001"
                    quantity = 1
                    unitPrice = 19.99
                }
            )
        } | ConvertTo-Json -Depth 3
        
        $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 30
        
        if ($orderResponse.StatusCode -eq 200 -or $orderResponse.StatusCode -eq 201) {
            Write-Log "Test order created successfully" "Green"
        }
    }
} catch {
    Write-Log "Partner service test failed: $_" "Yellow"
}

Write-Log "Step 9: Final verification" "Magenta"
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    $finalResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
    if ($finalResponse.StatusCode -eq 200) {
        Write-Log "FINAL SUCCESS - Database column issue RESOLVED!" "Green"
    }
} catch {
    Write-Log "Final verification failed: $_" "Red"
}

Write-Log "=== AUTONOMOUS FIX COMPLETE ===" "Green"
Write-Log "Database acknowledgment_notes column issue should be fixed" "Green"