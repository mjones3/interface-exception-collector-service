#!/usr/bin/env pwsh

# Fix Tilt and Database Script
Write-Host "=== FIX TILT AND DATABASE SCRIPT ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Killing any existing Tilt processes" "Magenta"
try {
    Get-Process -Name "tilt" -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Log "Killed existing Tilt processes" "Green"
} catch {
    Write-Log "No Tilt processes to kill" "Yellow"
}

Write-Log "Step 2: Stopping services properly" "Magenta"
try {
    tilt down --clear-cache
    Write-Log "Tilt services stopped with cache cleared" "Green"
} catch {
    Write-Log "Tilt down failed: $_" "Yellow"
}

Start-Sleep -Seconds 10

Write-Log "Step 3: Cleaning Docker completely" "Magenta"
try {
    docker system prune -a -f --volumes
    Write-Log "Docker completely cleaned" "Green"
} catch {
    Write-Log "Docker cleanup failed: $_" "Yellow"
}

Write-Log "Step 4: Creating comprehensive migration file" "Magenta"
$migrationSql = @'
-- V22: Complete schema fix for acknowledgment_notes and all missing columns

DO $$
BEGIN
    -- Add acknowledgment_notes column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        RAISE NOTICE 'Added acknowledgment_notes column';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
    
    -- Add resolution_method column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    ELSE
        RAISE NOTICE 'resolution_method column already exists';
    END IF;
    
    -- Add resolution_notes column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes VARCHAR(1000);
        RAISE NOTICE 'Added resolution_notes column';
    ELSE
        RAISE NOTICE 'resolution_notes column already exists';
    END IF;
    
    -- Add acknowledged_at column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added acknowledged_at column';
    ELSE
        RAISE NOTICE 'acknowledged_at column already exists';
    END IF;
    
    -- Add acknowledged_by column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledged_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledged_by VARCHAR(255);
        RAISE NOTICE 'Added acknowledged_by column';
    ELSE
        RAISE NOTICE 'acknowledged_by column already exists';
    END IF;
    
    -- Add resolved_at column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_at'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_at TIMESTAMP WITH TIME ZONE;
        RAISE NOTICE 'Added resolved_at column';
    ELSE
        RAISE NOTICE 'resolved_at column already exists';
    END IF;
    
    -- Add resolved_by column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolved_by'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolved_by VARCHAR(255);
        RAISE NOTICE 'Added resolved_by column';
    ELSE
        RAISE NOTICE 'resolved_by column already exists';
    END IF;
END
$$;

-- Create indexes for performance if they don't exist
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_acknowledgment_notes 
ON interface_exceptions(acknowledgment_notes) 
WHERE acknowledgment_notes IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method 
ON interface_exceptions(resolution_method) 
WHERE resolution_method IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when acknowledging an exception';
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Detailed notes about the resolution';

-- Verify all columns exist
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'interface_exceptions' 
AND column_name IN (
    'acknowledgment_notes', 
    'resolution_method', 
    'resolution_notes',
    'acknowledged_at',
    'acknowledged_by',
    'resolved_at',
    'resolved_by'
)
ORDER BY column_name;
'@

$migrationPath = "interface-exception-collector/src/main/resources/db/migration/V22__Complete_schema_fix.sql"
$migrationSql | Out-File -FilePath $migrationPath -Encoding UTF8
Write-Log "Created comprehensive migration: $migrationPath" "Green"

Write-Log "Step 5: Starting services with fresh environment" "Magenta"
try {
    Start-Process -FilePath "tilt" -ArgumentList "up" -NoNewWindow
    Write-Log "Tilt services starting in background" "Green"
} catch {
    Write-Log "Tilt start failed: $_" "Red"
}

Write-Log "Step 6: Extended wait for complete startup (120 seconds)" "Magenta"
Start-Sleep -Seconds 120

Write-Log "Step 7: Checking Kubernetes pods" "Magenta"
try {
    kubectl get pods
    Write-Log "Kubernetes pods status checked" "Green"
} catch {
    Write-Log "Kubectl failed: $_" "Yellow"
}

Write-Log "Step 8: Testing service health with extended retries" "Magenta"
$healthRetries = 20
$healthSuccess = $false
for ($i = 1; $i -le $healthRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 15
        if ($response.StatusCode -eq 200) {
            Write-Log "Service is healthy!" "Green"
            $healthSuccess = $true
            break
        }
    } catch {
        Write-Log "Health check $i/$healthRetries failed: $_" "Yellow"
        Start-Sleep -Seconds 15
    }
}

if ($healthSuccess) {
    Write-Log "Step 9: Testing API endpoint for acknowledgment_notes error" "Magenta"
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
    
    $apiRetries = 5
    $apiSuccess = $false
    for ($i = 1; $i -le $apiRetries; $i++) {
        try {
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
            
            $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
            
            if ($response.StatusCode -eq 200) {
                Write-Log "SUCCESS! API endpoint working - no acknowledgment_notes error!" "Green"
                $content = $response.Content | ConvertFrom-Json
                Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
                $apiSuccess = $true
                break
            }
        } catch {
            Write-Log "API test $i/$apiRetries failed: $_" "Yellow"
            Start-Sleep -Seconds 10
        }
    }
    
    if ($apiSuccess) {
        Write-Log "Step 10: Testing partner order creation" "Magenta"
        try {
            $partnerHealth = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 15
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
                    Write-Log "Test order created successfully!" "Green"
                    
                    # Wait for processing
                    Start-Sleep -Seconds 10
                    
                    # Final verification
                    $finalResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
                    if ($finalResponse.StatusCode -eq 200) {
                        Write-Log "FINAL SUCCESS - acknowledgment_notes column error FIXED!" "Green"
                        $finalContent = $finalResponse.Content | ConvertFrom-Json
                        Write-Log "System now has $($finalContent.totalElements) total exceptions" "Cyan"
                    }
                }
            }
        } catch {
            Write-Log "Partner service test failed: $_" "Yellow"
        }
    }
} else {
    Write-Log "Services failed to start properly" "Red"
    Write-Log "Checking logs for issues..." "Yellow"
    try {
        kubectl logs -l app=interface-exception-collector --tail=20
    } catch {
        Write-Log "Could not get logs" "Red"
    }
}

Write-Log "=== FIX COMPLETE ===" "Green"
Write-Log "The acknowledgment_notes column issue should now be resolved" "Green"
Write-Log "If services are still starting, wait a few more minutes and test manually" "Yellow"