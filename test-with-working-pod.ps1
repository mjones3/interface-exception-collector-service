#!/usr/bin/env pwsh
# Test with the working pod

Write-Host "=== Testing with Working Pod ===" -ForegroundColor Green

# Use the working pod
$workingPod = "interface-exception-collector-776f9b9b59-r8swj"

# Generate JWT token
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test health endpoint first
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "Service health: $($healthCheck.status)" -ForegroundColor Green
} catch {
    Write-Host "Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Create a PARTNER_ORDER exception
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$transactionId = "PARTNER-ORDER-WORKING-TEST-$timestamp"

$exceptionData = @{
    transactionId = $transactionId
    externalId = "TEST-PARTNER-ORDER-WORKING-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Test with working pod for RSocket to REST routing"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerId = "CUST-WORKING-TEST-001"
    locationCode = "LOC-WORKING-TEST-001"
    category = "PROCESSING_ERROR"
    status = "OPEN"
}

$jsonBody = $exceptionData | ConvertTo-Json -Depth 10

try {
    Write-Host "Creating PARTNER_ORDER exception..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions" -Method POST -Headers $headers -Body $jsonBody -TimeoutSec 30
    Write-Host "Created exception: $($createResponse.transactionId)" -ForegroundColor Green
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait for exception to be persisted
    Start-Sleep -Seconds 3
    
    # Test retry
    Write-Host "Testing retry..." -ForegroundColor Yellow
    
    $retryData = @{
        reason = "Test with working pod - should show RSocket vs HTTP behavior"
        initiatedBy = "test-user"
    }
    
    $retryJsonBody = $retryData | ConvertTo-Json -Depth 10
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryJsonBody -TimeoutSec 30
    
    Write-Host "Retry submitted!" -ForegroundColor Green
    Write-Host "Retry ID: $($retryResponse.retryId)" -ForegroundColor Cyan
    
    # Check logs immediately
    Write-Host "Checking logs for RSocket vs HTTP behavior..." -ForegroundColor Yellow
    Start-Sleep -Seconds 8
    
    $logs = kubectl logs $workingPod --tail=80 2>$null
    
    if ($logs) {
        Write-Host "`nAnalyzing recent logs..." -ForegroundColor Cyan
        
        $rsocketErrors = @()
        $httpCalls = @()
        $partnerOrderClientUsage = @()
        $transactionLogs = @()
        $rsocketUrls = @()
        
        foreach ($line in $logs) {
            if ($line -match "rsocket protocol is not supported") {
                $rsocketErrors += $line
            } elseif ($line -match "rsocket://") {
                $rsocketUrls += $line
            } elseif ($line -match "http://.*7001") {
                $httpCalls += $line
            } elseif ($line -match "PartnerOrderServiceClient") {
                $partnerOrderClientUsage += $line
            } elseif ($line -match $actualTransactionId) {
                $transactionLogs += $line
            }
        }
        
        Write-Host "`n=== ANALYSIS ===" -ForegroundColor Magenta
        
        if ($rsocketErrors.Count -gt 0) {
            Write-Host "RSOCKET PROTOCOL ERRORS ($($rsocketErrors.Count)):" -ForegroundColor Red
            $rsocketErrors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        } else {
            Write-Host "NO RSOCKET PROTOCOL ERRORS" -ForegroundColor Green
        }
        
        if ($rsocketUrls.Count -gt 0) {
            Write-Host "`nRSOCKET URLs DETECTED ($($rsocketUrls.Count)):" -ForegroundColor Yellow
            $rsocketUrls | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
        } else {
            Write-Host "`nNO RSOCKET URLs DETECTED" -ForegroundColor Green
        }
        
        if ($httpCalls.Count -gt 0) {
            Write-Host "`nHTTP CALLS TO PORT 7001 ($($httpCalls.Count)):" -ForegroundColor Green
            $httpCalls | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }
        } else {
            Write-Host "`nNO HTTP CALLS TO PORT 7001" -ForegroundColor Yellow
        }
        
        if ($partnerOrderClientUsage.Count -gt 0) {
            Write-Host "`nPARTNER ORDER CLIENT USAGE ($($partnerOrderClientUsage.Count)):" -ForegroundColor Green
            $partnerOrderClientUsage | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }
        } else {
            Write-Host "`nNO PARTNER ORDER CLIENT USAGE" -ForegroundColor Yellow
        }
        
        if ($transactionLogs.Count -gt 0) {
            Write-Host "`nTRANSACTION LOGS ($($transactionLogs.Count)):" -ForegroundColor Cyan
            $transactionLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Cyan }
        }
        
        # Show the problem clearly
        Write-Host "`n=== DIAGNOSIS ===" -ForegroundColor Magenta
        if ($rsocketErrors.Count -gt 0) {
            Write-Host "PROBLEM: The system is still trying to use RSocket URLs for REST calls" -ForegroundColor Red
            Write-Host "SOLUTION NEEDED: Fix the URL construction in BaseSourceServiceClient or routing logic" -ForegroundColor Red
        } elseif ($rsocketUrls.Count -gt 0 -and $httpCalls.Count -eq 0) {
            Write-Host "PROBLEM: System is using RSocket URLs but not failing (yet)" -ForegroundColor Yellow
            Write-Host "SOLUTION NEEDED: Update configuration to use HTTP URLs for PARTNER_ORDER" -ForegroundColor Yellow
        } elseif ($httpCalls.Count -gt 0) {
            Write-Host "SUCCESS: System is using HTTP URLs for REST calls" -ForegroundColor Green
        } else {
            Write-Host "UNCLEAR: Need more investigation into the routing logic" -ForegroundColor Yellow
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

Write-Host "`n=== Test Complete ===" -ForegroundColor Green