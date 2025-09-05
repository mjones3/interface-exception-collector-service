#!/usr/bin/env pwsh
# Final fix for RSocket to REST routing

Write-Host "=== Final Fix for RSocket to REST Routing ===" -ForegroundColor Green

# 1. Generate a proper JWT token first
Write-Host "1. Generating JWT token..." -ForegroundColor Yellow
$tokenResult = node generate-jwt-correct-secret.js test-user ADMIN 2>&1
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"

Write-Host "Using token: $($token.Substring(0, 50))..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Wait for service to be ready
Write-Host "2. Waiting for service to be ready..." -ForegroundColor Yellow
$maxWaitTime = 60
$waitTime = 0
$serviceReady = $false

while ($waitTime -lt $maxWaitTime -and -not $serviceReady) {
    try {
        $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
        if ($healthCheck.status -eq "UP") {
            $serviceReady = $true
            Write-Host "Service is ready" -ForegroundColor Green
        }
    } catch {
        Write-Host "Waiting for service... ($waitTime/$maxWaitTime seconds)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        $waitTime += 5
    }
}

if (-not $serviceReady) {
    Write-Host "Service not ready, exiting..." -ForegroundColor Red
    exit 1
}

# 3. Create a PARTNER_ORDER exception
Write-Host "3. Creating PARTNER_ORDER exception..." -ForegroundColor Yellow

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$transactionId = "PARTNER-ORDER-FINAL-TEST-$timestamp"

$exceptionData = @{
    transactionId = $transactionId
    externalId = "TEST-PARTNER-ORDER-FINAL-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Final test for RSocket to REST routing fix"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerId = "CUST-FINAL-TEST-001"
    locationCode = "LOC-FINAL-TEST-001"
    category = "PROCESSING_ERROR"
    status = "OPEN"
}

$jsonBody = $exceptionData | ConvertTo-Json -Depth 10
Write-Host "JSON Body: $jsonBody" -ForegroundColor Gray

try {
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions" -Method POST -Headers $headers -Body $jsonBody -TimeoutSec 30
    Write-Host "Created exception: $($createResponse.transactionId)" -ForegroundColor Green
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait for exception to be persisted
    Start-Sleep -Seconds 3
    
    # 4. Test retry
    Write-Host "4. Testing retry..." -ForegroundColor Yellow
    
    $retryData = @{
        reason = "Final test - should use HTTP REST call to partner-order-service"
        initiatedBy = "test-user"
    }
    
    $retryJsonBody = $retryData | ConvertTo-Json -Depth 10
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryJsonBody -TimeoutSec 30
    
    Write-Host "Retry submitted successfully!" -ForegroundColor Green
    Write-Host "Retry ID: $($retryResponse.retryId)" -ForegroundColor Cyan
    
    # 5. Check logs for RSocket vs HTTP usage
    Write-Host "5. Checking logs..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    $podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
    if ($podName -and $LASTEXITCODE -eq 0) {
        Write-Host "Pod: $podName" -ForegroundColor Cyan
        $logs = kubectl logs $podName --tail=100 2>$null
        
        if ($logs) {
            Write-Host "`nAnalyzing logs..." -ForegroundColor Cyan
            
            $rsocketErrors = @()
            $httpCalls = @()
            $partnerOrderClientUsage = @()
            $transactionLogs = @()
            
            foreach ($line in $logs) {
                if ($line -match "rsocket protocol is not supported") {
                    $rsocketErrors += $line
                } elseif ($line -match "http://.*7001") {
                    $httpCalls += $line
                } elseif ($line -match "PartnerOrderServiceClient") {
                    $partnerOrderClientUsage += $line
                } elseif ($line -match $actualTransactionId) {
                    $transactionLogs += $line
                }
            }
            
            Write-Host "`n=== RESULTS ===" -ForegroundColor Magenta
            
            if ($rsocketErrors.Count -gt 0) {
                Write-Host "RSOCKET ERRORS FOUND ($($rsocketErrors.Count)):" -ForegroundColor Red
                $rsocketErrors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
            } else {
                Write-Host "NO RSOCKET ERRORS - SUCCESS!" -ForegroundColor Green
            }
            
            if ($httpCalls.Count -gt 0) {
                Write-Host "`nHTTP CALLS TO PORT 7001 FOUND ($($httpCalls.Count)):" -ForegroundColor Green
                $httpCalls | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }
            } else {
                Write-Host "`nNO HTTP CALLS TO PORT 7001 DETECTED" -ForegroundColor Yellow
            }
            
            if ($partnerOrderClientUsage.Count -gt 0) {
                Write-Host "`nPARTNER ORDER CLIENT USAGE FOUND ($($partnerOrderClientUsage.Count)):" -ForegroundColor Green
                $partnerOrderClientUsage | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }
            } else {
                Write-Host "`nNO PARTNER ORDER CLIENT USAGE DETECTED" -ForegroundColor Yellow
            }
            
            if ($transactionLogs.Count -gt 0) {
                Write-Host "`nTRANSACTION LOGS ($($transactionLogs.Count)):" -ForegroundColor Cyan
                $transactionLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Cyan }
            }
            
            # Final assessment
            Write-Host "`n=== FINAL ASSESSMENT ===" -ForegroundColor Magenta
            if ($rsocketErrors.Count -eq 0 -and $httpCalls.Count -gt 0) {
                Write-Host "SUCCESS: RSocket to REST routing is working!" -ForegroundColor Green
            } elseif ($rsocketErrors.Count -eq 0) {
                Write-Host "PARTIAL SUCCESS: No RSocket errors, but need to verify HTTP calls" -ForegroundColor Yellow
            } else {
                Write-Host "FAILED: Still getting RSocket protocol errors" -ForegroundColor Red
            }
        } else {
            Write-Host "Could not retrieve logs" -ForegroundColor Red
        }
    } else {
        Write-Host "Could not find pod" -ForegroundColor Red
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

Write-Host "`n=== Final Fix Complete ===" -ForegroundColor Green