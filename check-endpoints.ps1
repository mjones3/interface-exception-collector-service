# Check available endpoints
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "Checking available endpoints..." -ForegroundColor Cyan

# Try actuator endpoints
try {
    $actuator = Invoke-RestMethod -Uri "http://localhost:8080/actuator" -Headers $headers
    Write-Host "Available actuator endpoints:" -ForegroundColor Green
    $actuator._links.PSObject.Properties | ForEach-Object {
        Write-Host "  $($_.Name): $($_.Value.href)" -ForegroundColor Gray
    }
} catch {
    Write-Host "Actuator failed: $_" -ForegroundColor Red
}

# Try to find test endpoints
Write-Host "`nTrying common test endpoints..." -ForegroundColor Cyan

$testEndpoints = @(
    "/api/test/kafka/order-rejected",
    "/api/exceptions",
    "/api/test/trigger-event",
    "/test/kafka/order-rejected"
)

foreach ($endpoint in $testEndpoints) {
    try {
        $testPayload = @{
            transactionId = "test-endpoint-check"
            operation = "CREATE_ORDER"
            rejectedReason = "Endpoint availability test"
        } | ConvertTo-Json
        
        Write-Host "Testing: $endpoint" -ForegroundColor Yellow
        $response = Invoke-RestMethod -Uri "http://localhost:8080$endpoint" -Method Post -Headers $headers -Body $testPayload -TimeoutSec 5
        Write-Host "  SUCCESS: $endpoint works!" -ForegroundColor Green
        Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
        break
    } catch {
        Write-Host "  FAILED: $endpoint - $($_.Exception.Message)" -ForegroundColor Red
    }
}