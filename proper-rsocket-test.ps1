#!/usr/bin/env pwsh
# Proper test to reproduce and fix the RSocket issue

Write-Host "=== Proper RSocket Issue Test ===" -ForegroundColor Green

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 1: Create an exception using the Kafka endpoint (correct JSON structure)
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$kafkaExceptionData = @"
{
    "externalId": "TEST-PARTNER-ORDER-PROPER-$timestamp",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Test for RSocket to REST routing issue",
    "customerId": "CUST-PROPER-001",
    "locationCode": "LOC-PROPER-001",
    "orderItems": [
        {
            "bloodType": "O-",
            "productFamily": "RED_BLOOD_CELLS",
            "quantity": 2
        }
    ]
}
"@

try {
    Write-Host "Step 1: Creating exception via Kafka endpoint..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions" -Method POST -Headers $headers -Body $kafkaExceptionData -TimeoutSec 30
    
    Write-Host "SUCCESS: Created Kafka event!" -ForegroundColor Green
    Write-Host "Transaction ID: $($createResponse.transactionId)" -ForegroundColor Cyan
    Write-Host "Event ID: $($createResponse.eventId)" -ForegroundColor Cyan
    
    $transactionId = $createResponse.transactionId
    
    # Wait for Kafka event to be consumed and database record to be created
    Write-Host "Waiting for Kafka event to be consumed..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    # Step 2: Now test retry - this should trigger the RSocket error
    Write-Host "Step 2: Testing retry (this should show the RSocket error)..." -ForegroundColor Yellow
    
    $retryData = @"
{
    "reason": "Proper test - should show RSocket protocol error for PARTNER_ORDER interface",
    "initiatedBy": "test-user"
}
"@
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/exceptions/$transactionId/retry" -Method POST -Headers $headers -Body $retryData -TimeoutSec 30
    
    Write-Host "SUCCESS: Retry submitted!" -ForegroundColor Green
    Write-Host "Retry ID: $($retryResponse.retryId)" -ForegroundColor Cyan
    Write-Host "Status: $($retryResponse.status)" -ForegroundColor Cyan
    
    # Step 3: Check logs for the RSocket error
    Write-Host "Step 3: Checking logs for RSocket error..." -ForegroundColor Yellow
    Start-Sleep -Seconds 8
    
    $workingPod = "interface-exception-collector-776f9b9b59-r8swj"
    $logs = kubectl logs $workingPod --tail=50 2>$null
    
    if ($logs) {
        Write-Host "`n=== LOG ANALYSIS ===" -ForegroundColor Magenta
        
        $rsocketErrors = @()
        $rsocketUrls = @()
        $httpUrls = @()
        $retryLogs = @()
        
        foreach ($line in $logs) {
            if ($line -match $transactionId -or 
                $line -match "rsocket protocol is not supported" -or 
                $line -match "rsocket://" -or 
                $line -match "http://.*7001" -or
                $line -match "Submitting retry" -or
                $line -match "Failed to submit retry") {
                
                if ($line -match "rsocket protocol is not supported") {
                    $rsocketErrors += $line
                    Write-Host "🔴 RSOCKET ERROR: $line" -ForegroundColor Red
                } elseif ($line -match "rsocket://") {
                    $rsocketUrls += $line
                    Write-Host "🟡 RSOCKET URL: $line" -ForegroundColor Yellow
                } elseif ($line -match "http://.*7001") {
                    $httpUrls += $line
                    Write-Host "🟢 HTTP URL: $line" -ForegroundColor Green
                } elseif ($line -match "Submitting retry") {
                    $retryLogs += $line
                    Write-Host "🔵 RETRY: $line" -ForegroundColor Blue
                } elseif ($line -match "Failed to submit retry") {
                    Write-Host "🔴 RETRY FAILED: $line" -ForegroundColor Red
                } else {
                    Write-Host "ℹ️  INFO: $line" -ForegroundColor Gray
                }
            }
        }
        
        Write-Host "`n=== DIAGNOSIS ===" -ForegroundColor Magenta
        
        if ($rsocketErrors.Count -gt 0) {
            Write-Host "✅ CONFIRMED: RSocket protocol error is occurring!" -ForegroundColor Red
            Write-Host "📊 Found $($rsocketErrors.Count) RSocket error(s)" -ForegroundColor Red
            
            if ($rsocketUrls.Count -gt 0) {
                Write-Host "🔍 ROOT CAUSE: Found $($rsocketUrls.Count) RSocket URL(s) being used for REST calls" -ForegroundColor Red
                Write-Host "🛠️  SOLUTION NEEDED: Fix BaseSourceServiceClient to use HTTP URLs for PARTNER_ORDER" -ForegroundColor Yellow
            }
            
            if ($httpUrls.Count -eq 0) {
                Write-Host "❌ PROBLEM: No HTTP URLs found - system is not using REST properly" -ForegroundColor Red
            }
        } else {
            Write-Host "❓ No RSocket errors found in recent logs" -ForegroundColor Yellow
            Write-Host "💡 This could mean the error occurred earlier or the fix is working" -ForegroundColor Yellow
        }
        
        Write-Host "`n=== NEXT ACTIONS ===" -ForegroundColor Magenta
        if ($rsocketErrors.Count -gt 0) {
            Write-Host "1. ✏️  Fix BaseSourceServiceClient URL construction" -ForegroundColor Yellow
            Write-Host "2. 🔧 Update configuration to use HTTP for PARTNER_ORDER" -ForegroundColor Yellow
            Write-Host "3. 🔄 Ensure proper client routing for interface types" -ForegroundColor Yellow
            
            Write-Host "`nI will now implement these fixes..." -ForegroundColor Green
        } else {
            Write-Host "1. ✅ Monitor for any delayed errors" -ForegroundColor Green
            Write-Host "2. 🔍 Check if the issue was already resolved" -ForegroundColor Green
        }
        
    } else {
        Write-Host "❌ Could not retrieve logs" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Test failed: $($_.Exception.Message)" -ForegroundColor Red
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