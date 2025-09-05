#!/usr/bin/env pwsh
# Direct test to reproduce and fix the RSocket issue

Write-Host "=== Direct Test for RSocket Issue ===" -ForegroundColor Green

# Use the working token from context
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Use the exact same JSON structure that worked before
$exceptionData = @"
{
    "transactionId": "PARTNER-ORDER-DIRECT-TEST-$(Get-Date -Format 'yyyyMMdd-HHmmss')",
    "externalId": "TEST-PARTNER-ORDER-DIRECT-1",
    "interfaceType": "PARTNER_ORDER",
    "operation": "ORDER_PROCESSING",
    "exceptionReason": "Direct test for RSocket to REST routing issue",
    "severity": "HIGH",
    "retryable": true,
    "maxRetries": 5,
    "customerId": "CUST-DIRECT-TEST-001",
    "locationCode": "LOC-DIRECT-TEST-001",
    "category": "PROCESSING_ERROR",
    "status": "OPEN"
}
"@

Write-Host "JSON Data:" -ForegroundColor Gray
Write-Host $exceptionData -ForegroundColor Gray

try {
    Write-Host "`nCreating PARTNER_ORDER exception..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions" -Method POST -Headers $headers -Body $exceptionData -TimeoutSec 30
    
    Write-Host "SUCCESS: Created exception!" -ForegroundColor Green
    Write-Host "Transaction ID: $($createResponse.transactionId)" -ForegroundColor Cyan
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait for exception to be persisted
    Start-Sleep -Seconds 2
    
    # Now test retry - this should trigger the RSocket error
    Write-Host "`nTesting retry (this will show the RSocket error)..." -ForegroundColor Yellow
    
    $retryData = @"
{
    "reason": "Direct test - should show RSocket protocol error",
    "initiatedBy": "test-user"
}
"@
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryData -TimeoutSec 30
    
    Write-Host "SUCCESS: Retry submitted!" -ForegroundColor Green
    Write-Host "Retry Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json) -ForegroundColor Cyan
    
    # Now check the logs for the RSocket error
    Write-Host "`nChecking logs for RSocket error..." -ForegroundColor Yellow
    Start-Sleep -Seconds 8
    
    $workingPod = "interface-exception-collector-776f9b9b59-r8swj"
    $logs = kubectl logs $workingPod --tail=50 2>$null
    
    if ($logs) {
        Write-Host "`n=== RECENT LOGS ===" -ForegroundColor Magenta
        
        $relevantLogs = @()
        foreach ($line in $logs) {
            if ($line -match $actualTransactionId -or 
                $line -match "rsocket protocol is not supported" -or 
                $line -match "rsocket://" -or 
                $line -match "Submitting retry" -or
                $line -match "Failed to submit retry" -or
                $line -match "BaseSourceServiceClient") {
                $relevantLogs += $line
            }
        }
        
        if ($relevantLogs.Count -gt 0) {
            foreach ($log in $relevantLogs) {
                if ($log -match "rsocket protocol is not supported") {
                    Write-Host "🔴 RSOCKET ERROR: $log" -ForegroundColor Red
                } elseif ($log -match "rsocket://") {
                    Write-Host "🟡 RSOCKET URL: $log" -ForegroundColor Yellow
                } elseif ($log -match "Submitting retry") {
                    Write-Host "🔵 RETRY ATTEMPT: $log" -ForegroundColor Blue
                } elseif ($log -match "Failed to submit retry") {
                    Write-Host "🔴 RETRY FAILED: $log" -ForegroundColor Red
                } else {
                    Write-Host "ℹ️  INFO: $log" -ForegroundColor Gray
                }
            }
            
            # Analyze the logs
            $hasRSocketError = $relevantLogs | Where-Object { $_ -match "rsocket protocol is not supported" }
            $hasRSocketUrl = $relevantLogs | Where-Object { $_ -match "rsocket://" }
            
            Write-Host "`n=== ANALYSIS ===" -ForegroundColor Magenta
            if ($hasRSocketError) {
                Write-Host "✅ CONFIRMED: RSocket protocol error is happening" -ForegroundColor Red
                Write-Host "📋 PROBLEM: The system is trying to use RSocket URLs for REST calls" -ForegroundColor Red
                
                if ($hasRSocketUrl) {
                    Write-Host "🔍 ROOT CAUSE: Found RSocket URLs in logs - this is the source of the problem" -ForegroundColor Red
                    Write-Host "🛠️  SOLUTION: Need to fix BaseSourceServiceClient to use HTTP URLs for PARTNER_ORDER" -ForegroundColor Yellow
                }
            } else {
                Write-Host "❓ No RSocket errors found in recent logs" -ForegroundColor Yellow
            }
        } else {
            Write-Host "No relevant logs found for this transaction" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Could not retrieve logs" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== Direct Test Complete ===" -ForegroundColor Green