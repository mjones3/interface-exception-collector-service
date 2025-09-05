#!/usr/bin/env pwsh
# Simple test to reproduce the RSocket issue

Write-Host "=== Simple RSocket Issue Test ===" -ForegroundColor Green

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$exceptionData = @"
{
    "transactionId": "PARTNER-ORDER-SIMPLE-TEST-$timestamp",
    "externalId": "TEST-PARTNER-ORDER-SIMPLE-1",
    "interfaceType": "PARTNER_ORDER",
    "operation": "ORDER_PROCESSING",
    "exceptionReason": "Simple test for RSocket issue",
    "severity": "HIGH",
    "retryable": true,
    "maxRetries": 5,
    "customerId": "CUST-SIMPLE-001",
    "locationCode": "LOC-SIMPLE-001",
    "category": "PROCESSING_ERROR",
    "status": "OPEN"
}
"@

try {
    Write-Host "Creating exception..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions" -Method POST -Headers $headers -Body $exceptionData -TimeoutSec 30
    
    Write-Host "Created: $($createResponse.transactionId)" -ForegroundColor Green
    $actualTransactionId = $createResponse.transactionId
    
    Start-Sleep -Seconds 2
    
    Write-Host "Testing retry..." -ForegroundColor Yellow
    $retryData = @"
{
    "reason": "Simple test retry",
    "initiatedBy": "test-user"
}
"@
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryData -TimeoutSec 30
    
    Write-Host "Retry submitted: $($retryResponse.retryId)" -ForegroundColor Green
    
    Start-Sleep -Seconds 8
    
    Write-Host "Checking logs..." -ForegroundColor Yellow
    $workingPod = "interface-exception-collector-776f9b9b59-r8swj"
    $logs = kubectl logs $workingPod --tail=30 2>$null
    
    if ($logs) {
        Write-Host "Recent logs:" -ForegroundColor Cyan
        foreach ($line in $logs) {
            if ($line -match $actualTransactionId -or $line -match "rsocket protocol is not supported" -or $line -match "rsocket://") {
                if ($line -match "rsocket protocol is not supported") {
                    Write-Host "ERROR: $line" -ForegroundColor Red
                } elseif ($line -match "rsocket://") {
                    Write-Host "RSOCKET URL: $line" -ForegroundColor Yellow
                } else {
                    Write-Host "INFO: $line" -ForegroundColor Gray
                }
            }
        }
    }
    
} catch {
    Write-Host "Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Test complete" -ForegroundColor Green