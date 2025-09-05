#!/usr/bin/env pwsh
# Direct update of interface type using application endpoint

Write-Host "=== Updating Interface Type Directly ===" -ForegroundColor Green

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODA4NDYsImV4cCI6MTc1NzA4NDQ0Nn0.7hQgoI8QEZynrMGanP2Yqd_WRtfrmP44LTiBLqEzfiI"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 1: Get the current exception details
Write-Host "Step 1: Getting current exception details..." -ForegroundColor Yellow
$transactionId = "137ed65a-ce10-4cac-84d2-4e6e08bbed40"

try {
    $exception = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$transactionId" -Method GET -Headers $headers
    Write-Host "Current exception details:" -ForegroundColor Cyan
    Write-Host "  Transaction ID: $($exception.transactionId)" -ForegroundColor Gray
    Write-Host "  Interface Type: $($exception.interfaceType)" -ForegroundColor Gray
    Write-Host "  External ID: $($exception.externalId)" -ForegroundColor Gray
    Write-Host "  Status: $($exception.status)" -ForegroundColor Gray
} catch {
    Write-Host "Could not retrieve exception details: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 2: Update the exception to use PARTNER_ORDER interface type
Write-Host "`nStep 2: Updating interface type to PARTNER_ORDER..." -ForegroundColor Yellow

$updateData = @{
    interfaceType = "PARTNER_ORDER"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$transactionId" -Method PATCH -Headers $headers -Body $updateData
    Write-Host "SUCCESS: Updated interface type to PARTNER_ORDER" -ForegroundColor Green
    Write-Host "Updated exception:" -ForegroundColor Cyan
    Write-Host ($updateResponse | ConvertTo-Json -Depth 2)
} catch {
    Write-Host "Could not update exception: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

# Step 3: Test retry with updated interface type
Write-Host "`nStep 3: Testing retry with PARTNER_ORDER interface type..." -ForegroundColor Yellow

$retryRequest = @{
    reason = "Testing REST-based retry after interface type update"
    initiatedBy = "test-user"
} | ConvertTo-Json

try {
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$transactionId/retry" -Method POST -Headers $headers -Body $retryRequest
    Write-Host "SUCCESS: Retry submitted with PARTNER_ORDER interface type!" -ForegroundColor Green
    Write-Host "Retry Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
} catch {
    Write-Host "Retry failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

# Step 4: Check logs for PartnerOrderServiceClient usage
Write-Host "`nStep 4: Checking logs for PartnerOrderServiceClient usage..." -ForegroundColor Yellow

Start-Sleep -Seconds 5

$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($podName -and $LASTEXITCODE -eq 0) {
    $logs = kubectl logs $podName --tail=30 2>$null
    if ($logs) {
        Write-Host "Recent application logs:" -ForegroundColor Cyan
        $logs | Select-Object -Last 15 | ForEach-Object { 
            if ($_ -match "PartnerOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Green
            } elseif ($_ -match "MockRSocketOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "rsocket://") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "http://.*partner") {
                Write-Host "  $_" -ForegroundColor Green
            } else {
                Write-Host "  $_" -ForegroundColor Gray
            }
        }
        
        if ($logs -match "PartnerOrderServiceClient") {
            Write-Host "`n✅ SUCCESS: PartnerOrderServiceClient is being used!" -ForegroundColor Green
        } elseif ($logs -match "MockRSocketOrderServiceClient") {
            Write-Host "`n❌ ISSUE: Still using MockRSocketOrderServiceClient" -ForegroundColor Red
        } else {
            Write-Host "`n⚠️  No specific client usage found in recent logs" -ForegroundColor Yellow
        }
        
        if ($logs -match "rsocket://") {
            Write-Host "❌ ISSUE: Still seeing RSocket URLs in logs" -ForegroundColor Red
        } else {
            Write-Host "✅ SUCCESS: No RSocket URLs found in recent logs" -ForegroundColor Green
        }
    }
}

Write-Host "`n=== Interface Type Update Complete ===" -ForegroundColor Green