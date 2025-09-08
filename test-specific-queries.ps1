# Test Specific GraphQL Queries
Write-Host "TESTING SPECIFIC QUERIES" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test exceptionSummary query
Write-Host "1. Testing exceptionSummary query..." -ForegroundColor Cyan
$summaryQuery = @{
    query = 'query { exceptionSummary(timeRange: { period: LAST_24_HOURS }) { totalExceptions } }'
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $summaryQuery -TimeoutSec 10
    
    if ($response.errors) {
        Write-Host "❌ exceptionSummary error: $($response.errors[0].message)" -ForegroundColor Red
    } else {
        Write-Host "✅ exceptionSummary WORKS! Response: $($response.data | ConvertTo-Json -Compress)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ exceptionSummary request failed: $_" -ForegroundColor Red
}

# Test exceptions query
Write-Host "2. Testing exceptions query..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = 'query { exceptions(pagination: { first: 5 }) { totalCount } }'
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery -TimeoutSec 10
    
    if ($response.errors) {
        Write-Host "❌ exceptions error: $($response.errors[0].message)" -ForegroundColor Red
    } else {
        Write-Host "✅ exceptions WORKS! Response: $($response.data | ConvertTo-Json -Compress)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ exceptions request failed: $_" -ForegroundColor Red
}

Write-Host "SPECIFIC QUERIES TEST COMPLETE" -ForegroundColor Blue