#!/usr/bin/env pwsh
# Test retry with correct JWT token

Write-Host "=== Testing Retry with Correct JWT Token ===" -ForegroundColor Green

# Use the correct JWT secret from application.yml
$secret = "dev-secret-key-1234567890123456789012345678901234567890"

# Create JWT header
$header = @{
    alg = "HS256"
    typ = "JWT"
} | ConvertTo-Json -Compress

# Create JWT payload with proper expiration
$currentTime = [int][double]::Parse((Get-Date -UFormat %s))
$payload = @{
    sub = "test-user"
    roles = @("ADMIN")
    iat = $currentTime
    exp = $currentTime + 3600  # 1 hour from now
    iss = "interface-exception-collector"
    aud = "biopro-services"
} | ConvertTo-Json -Compress

Write-Host "JWT Payload:" -ForegroundColor Cyan
Write-Host $payload

# Base64 URL encode (without padding)
$headerEncoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($header)).TrimEnd('=').Replace('+', '-').Replace('/', '_')
$payloadEncoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($payload)).TrimEnd('=').Replace('+', '-').Replace('/', '_')

# Create signature using HMAC-SHA256
$stringToSign = "$headerEncoded.$payloadEncoded"
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$signatureBytes = $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($stringToSign))
$signature = [Convert]::ToBase64String($signatureBytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

$token = "$headerEncoded.$payloadEncoded.$signature"

Write-Host "Generated JWT token (first 50 chars): $($token.Substring(0, 50))..." -ForegroundColor Cyan
Write-Host "Token length: $($token.Length)" -ForegroundColor Gray

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test the retry
$transactionId = "137ed65a-ce10-4cac-84d2-4e6e08bbed40"
$retryRequest = @{
    reason = "Testing retry with correct JWT token - should route to PartnerOrderServiceClient"
    initiatedBy = "test-user"
} | ConvertTo-Json

Write-Host "`nTesting retry for transaction: $transactionId" -ForegroundColor Yellow
Write-Host "Retry request:" -ForegroundColor Gray
Write-Host $retryRequest

try {
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$transactionId/retry" -Method POST -Headers $headers -Body $retryRequest -TimeoutSec 30
    
    Write-Host "`nSUCCESS: Retry submitted!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
    
} catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
            Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

# Wait and check logs for client routing
Write-Host "`nWaiting for retry processing..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($podName -and $LASTEXITCODE -eq 0) {
    Write-Host "Checking logs in pod: $podName" -ForegroundColor Cyan
    $logs = kubectl logs $podName --tail=50 2>$null
    
    if ($logs) {
        Write-Host "`nRecent application logs:" -ForegroundColor Cyan
        $logs | Select-Object -Last 30 | ForEach-Object { 
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
            } elseif ($_ -match "JWT.*validation") {
                Write-Host "  $_" -ForegroundColor Yellow
            } elseif ($_ -match "retry.*submit") {
                Write-Host "  $_" -ForegroundColor Magenta
            } else {
                Write-Host "  $_" -ForegroundColor Gray
            }
        }
        
        Write-Host "`nAnalysis:" -ForegroundColor Yellow
        if ($logs -match "PartnerOrderServiceClient") {
            Write-Host "✅ SUCCESS: PartnerOrderServiceClient is being used!" -ForegroundColor Green
        } elseif ($logs -match "MockRSocketOrderServiceClient") {
            Write-Host "❌ ISSUE: MockRSocketOrderServiceClient is still being used" -ForegroundColor Red
        } else {
            Write-Host "⚠️  No specific client usage found in recent logs" -ForegroundColor Yellow
        }
        
        if ($logs -match "rsocket://") {
            Write-Host "❌ ISSUE: RSocket URLs still being used" -ForegroundColor Red
        } else {
            Write-Host "✅ SUCCESS: No RSocket URLs in recent logs" -ForegroundColor Green
        }
        
        if ($logs -match "JWT.*validation.*successful") {
            Write-Host "✅ SUCCESS: JWT validation successful" -ForegroundColor Green
        } elseif ($logs -match "JWT.*validation.*failed") {
            Write-Host "❌ ISSUE: JWT validation failed" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== JWT Retry Test Complete ===" -ForegroundColor Green