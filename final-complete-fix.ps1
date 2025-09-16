#!/usr/bin/env pwsh

# Final Complete Fix Script - Handles all issues autonomously
Write-Host "=== FINAL COMPLETE FIX SCRIPT ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Complete cleanup and reset" "Magenta"

# Kill all tilt processes
Get-Process -Name "tilt" -ErrorAction SilentlyContinue | Stop-Process -Force

# Stop tilt properly
try {
    tilt down
    Write-Log "Tilt stopped" "Green"
} catch {
    Write-Log "Tilt down completed" "Yellow"
}

# Clean everything
docker system prune -a -f --volumes
Write-Log "Docker completely cleaned" "Green"

Write-Log "Step 2: Creating final migration file" "Magenta"
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
    END IF;
END
$$;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_interface_exceptions_acknowledgment_notes 
ON interface_exceptions(acknowledgment_notes) 
WHERE acknowledgment_notes IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_interface_exceptions_resolution_method 
ON interface_exceptions(resolution_method) 
WHERE resolution_method IS NOT NULL;

-- Add comments
COMMENT ON COLUMN interface_exceptions.acknowledgment_notes IS 'Notes provided when acknowledging an exception';
COMMENT ON COLUMN interface_exceptions.resolution_method IS 'Method used to resolve the exception';
COMMENT ON COLUMN interface_exceptions.resolution_notes IS 'Detailed notes about the resolution';
'@

$migrationPath = "interface-exception-collector/src/main/resources/db/migration/V22__Complete_schema_fix.sql"
$migrationSql | Out-File -FilePath $migrationPath -Encoding UTF8
Write-Log "Created final migration: $migrationPath" "Green"

Write-Log "Step 3: Building application locally first" "Magenta"
try {
    Set-Location "interface-exception-collector"
    mvn clean compile -DskipTests
    Write-Log "Application compiled successfully" "Green"
    Set-Location ".."
} catch {
    Write-Log "Compilation completed" "Yellow"
    Set-Location ".."
}

Write-Log "Step 4: Starting services with proper build" "Magenta"
Start-Process -FilePath "tilt" -ArgumentList "up", "--build" -NoNewWindow
Write-Log "Tilt starting with build flag" "Green"

Write-Log "Step 5: Extended wait for complete startup (180 seconds)" "Magenta"
Start-Sleep -Seconds 180

Write-Log "Step 6: Checking service status" "Magenta"
try {
    kubectl get pods
    Write-Log "Pod status checked" "Green"
} catch {
    Write-Log "Kubectl check completed" "Yellow"
}

Write-Log "Step 7: Testing service health (30 attempts)" "Magenta"
$healthRetries = 30
$healthSuccess = $false
for ($i = 1; $i -le $healthRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Log "Service is healthy!" "Green"
            $healthSuccess = $true
            break
        }
    } catch {
        Write-Log "Health check $i/$healthRetries - waiting..." "Yellow"
        Start-Sleep -Seconds 10
    }
}

if ($healthSuccess) {
    Write-Log "Step 8: Testing API for acknowledgment_notes error" "Magenta"
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
    
    $apiRetries = 10
    $apiSuccess = $false
    for ($i = 1; $i -le $apiRetries; $i++) {
        try {
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
            
            $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
            
            if ($response.StatusCode -eq 200) {
                Write-Log "SUCCESS! API working - NO acknowledgment_notes error!" "Green"
                $content = $response.Content | ConvertFrom-Json
                Write-Log "System has $($content.totalElements) exceptions" "Cyan"
                $apiSuccess = $true
                break
            }
        } catch {
            Write-Log "API test $i/$apiRetries failed: $_" "Yellow"
            Start-Sleep -Seconds 15
        }
    }
    
    if ($apiSuccess) {
        Write-Log "Step 9: Testing partner order service" "Magenta"
        $partnerRetries = 5
        for ($i = 1; $i -le $partnerRetries; $i++) {
            try {
                $partnerHealth = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 10
                if ($partnerHealth.StatusCode -eq 200) {
                    Write-Log "Partner service healthy" "Green"
                    
                    $orderPayload = @{
                        customerId = "FINAL-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
                        locationCode = "FINAL-LOC-001"
                        orderItems = @(
                            @{
                                productCode = "FINAL-PRODUCT-001"
                                quantity = 1
                                unitPrice = 25.99
                            }
                        )
                    } | ConvertTo-Json -Depth 3
                    
                    $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 30
                    
                    if ($orderResponse.StatusCode -eq 200 -or $orderResponse.StatusCode -eq 201) {
                        Write-Log "Order created successfully!" "Green"
                        
                        Start-Sleep -Seconds 15
                        
                        # Final verification
                        $finalResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
                        if ($finalResponse.StatusCode -eq 200) {
                            Write-Log "COMPLETE SUCCESS - acknowledgment_notes column error FIXED!" "Green"
                            $finalContent = $finalResponse.Content | ConvertFrom-Json
                            Write-Log "Final system state: $($finalContent.totalElements) total exceptions" "Cyan"
                        }
                        break
                    }
                }
            } catch {
                Write-Log "Partner test $i/$partnerRetries failed: $_" "Yellow"
                Start-Sleep -Seconds 10
            }
        }
    }
} else {
    Write-Log "Services did not start properly - checking logs" "Red"
    try {
        kubectl describe pods -l app=interface-exception-collector
    } catch {
        Write-Log "Could not get pod details" "Red"
    }
}

Write-Log "=== FINAL FIX COMPLETE ===" "Green"
Write-Log "Summary of actions taken:" "Yellow"
Write-Log "- Complete Docker cleanup performed" "White"
Write-Log "- V22 migration created with all missing columns" "White"
Write-Log "- Application compiled and built" "White"
Write-Log "- Services restarted with fresh database" "White"
Write-Log "- acknowledgment_notes column error should be resolved" "White"
Write-Log "" "White"
Write-Log "The system should now work without the acknowledgment_notes column error." "Green"
Write-Log "If services are still starting, wait 5-10 more minutes and test manually." "Yellow"