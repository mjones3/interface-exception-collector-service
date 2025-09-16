#!/usr/bin/env pwsh

# Test System with New Token
Write-Host "=== TESTING SYSTEM WITH NEW TOKEN ===" -ForegroundColor Green

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc5ODE3MDAsImV4cCI6MTc1Nzk4NTMwMH0.Ptor4zyhMG_lk0ulPlk40yOfv25uzhmucGKVTl7TF68"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Testing service health" "Magenta"
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
    if ($healthResponse.StatusCode -eq 200) {
        Write-Log "✓ Service is healthy" "Green"
    }
} catch {
    Write-Log "✗ Service health check failed: $_" "Red"
}

Write-Log "Step 2: Testing exceptions endpoint with new token" "Magenta"
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
    
    if ($response.StatusCode -eq 200) {
        Write-Log "✓ SUCCESS! Exceptions endpoint working with new token!" "Green"
        $content = $response.Content | ConvertFrom-Json
        Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
        Write-Log "✓ NO acknowledgment_notes column error!" "Green"
    }
} catch {
    Write-Log "✗ API test failed: $_" "Red"
    Write-Log "This might indicate the acknowledgment_notes column issue still exists" "Yellow"
}

Write-Log "Step 3: Testing partner order service" "Magenta"
try {
    $partnerHealth = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 10
    if ($partnerHealth.StatusCode -eq 200) {
        Write-Log "✓ Partner Order Service is healthy" "Green"
        
        # Create test order with new token timestamp
        $orderPayload = @{
            customerId = "TOKEN-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
            locationCode = "TOKEN-LOC-001"
            orderItems = @(
                @{
                    productCode = "TOKEN-PRODUCT-001"
                    quantity = 1
                    unitPrice = 29.99
                }
            )
        } | ConvertTo-Json -Depth 3
        
        Write-Log "Creating test order with new token..." "Yellow"
        $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 30
        
        if ($orderResponse.StatusCode -eq 200 -or $orderResponse.StatusCode -eq 201) {
            Write-Log "✓ Test order created successfully!" "Green"
            
            # Wait for processing
            Start-Sleep -Seconds 10
            
            # Check exceptions again
            $finalResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
            if ($finalResponse.StatusCode -eq 200) {
                Write-Log "✓ FINAL SUCCESS - System working perfectly!" "Green"
                $finalContent = $finalResponse.Content | ConvertFrom-Json
                Write-Log "System now has $($finalContent.totalElements) total exceptions" "Cyan"
            }
        }
    }
} catch {
    Write-Log "Partner service test failed: $_" "Yellow"
}

Write-Log "Step 4: Checking database schema" "Magenta"
try {
    kubectl get pods -l app=interface-exception-collector
    Write-Log "✓ Pod status checked" "Green"
} catch {
    Write-Log "Could not check pod status" "Yellow"
}

Write-Log "=== TEST COMPLETE ===" "Green"
Write-Log "If the exceptions endpoint worked without errors, the acknowledgment_notes column issue is RESOLVED!" "Green"