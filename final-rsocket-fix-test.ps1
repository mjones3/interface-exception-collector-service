#!/usr/bin/env pwsh
# Final test for RSocket to REST routing fix

Write-Host "=== Final RSocket to REST Fix Test ===" -ForegroundColor Green

# Use port 8082 for the working pod
$baseUrl = "http://localhost:8082"

# Generate JWT token
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test health endpoint first
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "Service health: $($healthCheck.status)" -ForegroundColor Green
} catch {
    Write-Host "Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Create a PARTNER_ORDER exception
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$transactionId = "PARTNER-ORDER-FINAL-FIX-$timestamp"

$exceptionData = @{
    transactionId = $transactionId
    externalId = "TEST-PARTNER-ORDER-FINAL-FIX-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Final test to identify RSocket vs REST routing issue"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerId = "CUST-FINAL-FIX-TEST-001"
    locationCode = "LOC-FINAL-FIX-TEST-001"
    category = "PROCESSING_ERROR"
    status = "OPEN"
}

$jsonBody = $exceptionData | ConvertTo-Json -Depth 10

try {
    Write-Host "Creating PARTNER_ORDER exception..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/exceptions" -Method POST -Headers $headers -Body $jsonBody -TimeoutSec 30
    Write-Host "Created exception: $($createResponse.transactionId)" -ForegroundColor Green
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait for exception to be persisted
    Start-Sleep -Seconds 3
    
    # Test retry - this should trigger the RSocket error
    Write-Host "Testing retry (this should show the RSocket error)..." -ForegroundColor Yellow
    
    $retryData = @{
        reason = "Final test - this will show us exactly what's happening with RSocket vs REST"
        initiatedBy = "test-user"
    }
    
    $retryJsonBody = $retryData | ConvertTo-Json -Depth 10
    
    $retryResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryJsonBody -TimeoutSec 30
    
    Write-Host "Retry submitted!" -ForegroundColor Green
    Write-Host "Retry ID: $($retryResponse.retryId)" -ForegroundColor Cyan
    
    # Check logs immediately for the exact error
    Write-Host "Checking logs for the exact RSocket error..." -ForegroundColor Yellow
    Start-Sleep -Seconds 8
    
    $workingPod = "interface-exception-collector-776f9b9b59-r8swj"
    $logs = kubectl logs $workingPod --tail=100 2>$null
    
    if ($logs) {
        Write-Host "`nAnalyzing logs for RSocket issues..." -ForegroundColor Cyan
        
        $foundRSocketError = $false
        $foundRSocketUrl = $false
        $foundHttpUrl = $false
        $foundPartnerOrderClient = $false
        
        Write-Host "`n=== RELEVANT LOG ENTRIES ===" -ForegroundColor Magenta
        
        foreach ($line in $logs) {
            if ($line -match $actualTransactionId -or 
                $line -match "rsocket protocol is not supported" -or 
                $line -match "rsocket://" -or 
                $line -match "http://.*7001" -or 
                $line -match "PartnerOrderServiceClient" -or
                $line -match "MockRSocketOrderServiceClient" -or
                $line -match "Submitting retry") {
                
                if ($line -match "rsocket protocol is not supported") {
                    Write-Host "ERROR: $line" -ForegroundColor Red
                    $foundRSocketError = $true
                } elseif ($line -match "rsocket://") {
                    Write-Host "RSOCKET URL: $line" -ForegroundColor Yellow
                    $foundRSocketUrl = $true
                } elseif ($line -match "http://.*7001") {
                    Write-Host "HTTP URL: $line" -ForegroundColor Green
                    $foundHttpUrl = $true
                } elseif ($line -match "PartnerOrderServiceClient") {
                    Write-Host "PARTNER CLIENT: $line" -ForegroundColor Green
                    $foundPartnerOrderClient = $true
                } elseif ($line -match "MockRSocketOrderServiceClient") {
                    Write-Host "MOCK CLIENT: $line" -ForegroundColor Cyan
                } else {
                    Write-Host "INFO: $line" -ForegroundColor Gray
                }
            }
        }
        
        Write-Host "`n=== ROOT CAUSE ANALYSIS ===" -ForegroundColor Magenta
        
        if ($foundRSocketError) {
            Write-Host "CONFIRMED: RSocket protocol error is occurring" -ForegroundColor Red
            
            if ($foundRSocketUrl) {
                Write-Host "CAUSE: System is trying to use RSocket URLs (rsocket://) for REST calls" -ForegroundColor Red
                Write-Host "SOLUTION: Need to fix URL construction in BaseSourceServiceClient" -ForegroundColor Yellow
            }
            
            if (-not $foundPartnerOrderClient) {
                Write-Host "ISSUE: PartnerOrderServiceClient is not being used for PARTNER_ORDER interface" -ForegroundColor Red
                Write-Host "SOLUTION: Need to fix client routing in ExceptionProcessingService" -ForegroundColor Yellow
            }
        } else {
            Write-Host "No RSocket errors found in recent logs" -ForegroundColor Green
        }
        
        Write-Host "`n=== NEXT STEPS ===" -ForegroundColor Magenta
        if ($foundRSocketError -and $foundRSocketUrl) {
            Write-Host "1. Fix BaseSourceServiceClient to convert rsocket:// URLs to http:// for REST calls" -ForegroundColor Yellow
            Write-Host "2. Ensure PARTNER_ORDER interface type routes to PartnerOrderServiceClient" -ForegroundColor Yellow
            Write-Host "3. Update configuration to use HTTP URLs for partner-order-service" -ForegroundColor Yellow
        } elseif (-not $foundPartnerOrderClient) {
            Write-Host "1. Fix client routing to use PartnerOrderServiceClient for PARTNER_ORDER" -ForegroundColor Yellow
        } else {
            Write-Host "1. Configuration appears correct, may need to restart with new config" -ForegroundColor Green
        }
        
    } else {
        Write-Host "Could not retrieve logs" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== Final Test Complete ===" -ForegroundColor Green