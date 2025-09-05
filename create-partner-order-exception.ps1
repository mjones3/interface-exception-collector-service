#!/usr/bin/env pwsh
# Create a new exception with PARTNER_ORDER interface type for testing

Write-Host "=== Creating PARTNER_ORDER Exception for Testing ===" -ForegroundColor Green

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODA4NDYsImV4cCI6MTc1NzA4NDQ0Nn0.7hQgoI8QEZynrMGanP2Yqd_WRtfrmP44LTiBLqEzfiI"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Wait for application to be ready
Write-Host "Waiting for application to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Test application health first
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "Application health: $($health.status)" -ForegroundColor Cyan
} catch {
    Write-Host "Application not ready yet, waiting longer..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
}

# Generate a unique transaction ID
$newTransactionId = "PARTNER-ORDER-TEST-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

# Create exception data
$exceptionData = @{
    transactionId = $newTransactionId
    externalId = "TEST-PARTNER-ORDER-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Test exception for PARTNER_ORDER REST client routing"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerId = "CUST-TEST-001"
    locationCode = "LOC-TEST-001"
    category = "PROCESSING_ERROR"
    status = "OPEN"
} | ConvertTo-Json

Write-Host "Creating new PARTNER_ORDER exception..." -ForegroundColor Yellow
Write-Host "Transaction ID: $newTransactionId" -ForegroundColor Cyan
Write-Host "Exception Data:" -ForegroundColor Gray
Write-Host $exceptionData

try {
    # Try to create the exception via API
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions" -Method POST -Headers $headers -Body $exceptionData -TimeoutSec 30
    
    Write-Host "SUCCESS: Created PARTNER_ORDER exception!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($createResponse | ConvertTo-Json -Depth 3)
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait a moment for the exception to be persisted
    Start-Sleep -Seconds 3
    
    # Now test retry with this new exception
    Write-Host "`nTesting retry with PARTNER_ORDER exception..." -ForegroundColor Yellow
    
    $retryRequest = @{
        reason = "Testing REST-based retry for PARTNER_ORDER interface type"
        initiatedBy = "test-user"
    } | ConvertTo-Json
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryRequest -TimeoutSec 30
    
    Write-Host "SUCCESS: Retry submitted for PARTNER_ORDER exception!" -ForegroundColor Green
    Write-Host "Retry Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
    
    # Update the test script to use this new transaction ID
    $testScriptPath = "test-partner-order-retry.ps1"
    if (Test-Path $testScriptPath) {
        $testContent = Get-Content $testScriptPath -Raw
        $updatedContent = $testContent -replace "137ed65a-ce10-4cac-84d2-4e6e08bbed40", $actualTransactionId
        Set-Content -Path "test-partner-order-retry-updated.ps1" -Value $updatedContent
        Write-Host "`nCreated updated test script: test-partner-order-retry-updated.ps1" -ForegroundColor Green
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
    
    # Alternative approach - try to use the existing transaction but update its interface type
    Write-Host "`nTrying alternative approach - testing with existing transaction..." -ForegroundColor Yellow
    
    try {
        $existingTransactionId = "137ed65a-ce10-4cac-84d2-4e6e08bbed40"
        $retryRequest = @{
            reason = "Testing retry - hoping for PARTNER_ORDER routing"
            initiatedBy = "test-user"
        } | ConvertTo-Json
        
        $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$existingTransactionId/retry" -Method POST -Headers $headers -Body $retryRequest -TimeoutSec 30
        
        Write-Host "Retry submitted for existing transaction" -ForegroundColor Yellow
        Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
        
    } catch {
        Write-Host "Alternative approach also failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Check logs regardless of success/failure
Write-Host "`nChecking application logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($podName -and $LASTEXITCODE -eq 0) {
    $logs = kubectl logs $podName --tail=40 2>$null
    if ($logs) {
        Write-Host "Recent application logs:" -ForegroundColor Cyan
        $logs | Select-Object -Last 20 | ForEach-Object { 
            if ($_ -match "PartnerOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Green
            } elseif ($_ -match "MockRSocketOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "rsocket://") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "http://.*partner") {
                Write-Host "  $_" -ForegroundColor Green
            } elseif ($_ -match "PARTNER_ORDER") {
                Write-Host "  $_" -ForegroundColor Cyan
            } else {
                Write-Host "  $_" -ForegroundColor Gray
            }
        }
        
        Write-Host "`nLog Analysis:" -ForegroundColor Yellow
        if ($logs -match "PartnerOrderServiceClient") {
            Write-Host "✅ SUCCESS: PartnerOrderServiceClient is being used!" -ForegroundColor Green
        } elseif ($logs -match "MockRSocketOrderServiceClient") {
            Write-Host "❌ ISSUE: Still using MockRSocketOrderServiceClient" -ForegroundColor Red
        } else {
            Write-Host "⚠️  No specific client usage found in recent logs" -ForegroundColor Yellow
        }
        
        if ($logs -match "rsocket://") {
            Write-Host "❌ ISSUE: Still seeing RSocket URLs in logs" -ForegroundColor Red
        } else {
            Write-Host "✅ SUCCESS: No RSocket URLs found in recent logs" -ForegroundColor Green
        }
    }
}

Write-Host "`n=== PARTNER_ORDER Exception Creation Complete ===" -ForegroundColor Green