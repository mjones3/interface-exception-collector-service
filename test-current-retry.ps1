#!/usr/bin/env pwsh
# Test current retry to see exactly what's happening

Write-Host "=== Testing Current Retry Behavior ===" -ForegroundColor Green

# Generate a new JWT token (valid for 1 hour)
$header = @{
    alg = "HS256"
    typ = "JWT"
} | ConvertTo-Json -Compress

$payload = @{
    sub = "test-user"
    roles = @("ADMIN")
    iat = [int][double]::Parse((Get-Date -UFormat %s))
    exp = [int][double]::Parse((Get-Date -UFormat %s)) + 3600
} | ConvertTo-Json -Compress

# Base64 encode
$headerEncoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($header)).TrimEnd('=').Replace('+', '-').Replace('/', '_')
$payloadEncoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($payload)).TrimEnd('=').Replace('+', '-').Replace('/', '_')

# Create signature (using the same secret as in the app)
$secret = "mySecretKey"
$stringToSign = "$headerEncoded.$payloadEncoded"
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$signature = [Convert]::ToBase64String($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($stringToSign))).TrimEnd('=').Replace('+', '-').Replace('/', '_')

$token = "$headerEncoded.$payloadEncoded.$signature"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Generated new JWT token" -ForegroundColor Cyan

# Test the retry
$transactionId = "137ed65a-ce10-4cac-84d2-4e6e08bbed40"
$retryRequest = @{
    reason = "Testing current retry behavior to see client routing"
    initiatedBy = "test-user"
} | ConvertTo-Json

Write-Host "Testing retry for transaction: $transactionId" -ForegroundColor Yellow

try {
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$transactionId/retry" -Method POST -Headers $headers -Body $retryRequest -TimeoutSec 30
    
    Write-Host "SUCCESS: Retry submitted!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
    
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
}

# Wait and check logs
Write-Host "`nWaiting for retry processing..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($podName -and $LASTEXITCODE -eq 0) {
    Write-Host "Checking logs in pod: $podName" -ForegroundColor Cyan
    $logs = kubectl logs $podName --tail=50 2>$null
    
    if ($logs) {
        Write-Host "`nRecent application logs:" -ForegroundColor Cyan
        $logs | Select-Object -Last 25 | ForEach-Object { 
            if ($_ -match "PartnerOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Green
            } elseif ($_ -match "MockRSocketOrderServiceClient") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "rsocket://") {
                Write-Host "  $_" -ForegroundColor Red
            } elseif ($_ -match "http://.*partner") {
                Write-Host "  $_" -ForegroundColor Green
            } elseif ($_ -match "supports.*interface") {
                Write-Host "  $_" -ForegroundColor Cyan
            } elseif ($_ -match "getClient.*ORDER") {
                Write-Host "  $_" -ForegroundColor Cyan
            } elseif ($_ -match "Found client.*for interface") {
                Write-Host "  $_" -ForegroundColor Cyan
            } else {
                Write-Host "  $_" -ForegroundColor Gray
            }
        }
        
        Write-Host "`nAnalysis:" -ForegroundColor Yellow
        if ($logs -match "PartnerOrderServiceClient") {
            Write-Host "✅ PartnerOrderServiceClient is being used" -ForegroundColor Green
        } elseif ($logs -match "MockRSocketOrderServiceClient") {
            Write-Host "❌ MockRSocketOrderServiceClient is being used" -ForegroundColor Red
        }
        
        if ($logs -match "rsocket://") {
            Write-Host "❌ RSocket URLs still being used" -ForegroundColor Red
        } else {
            Write-Host "✅ No RSocket URLs in recent logs" -ForegroundColor Green
        }
        
        # Look for client selection logs
        $clientLogs = $logs | Where-Object { $_ -match "Found client|supports|getClient" }
        if ($clientLogs) {
            Write-Host "`nClient Selection Logs:" -ForegroundColor Yellow
            $clientLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Cyan }
        }
    }
}

Write-Host "`n=== Current Retry Test Complete ===" -ForegroundColor Green