# Test Retry Mutation Only
Write-Host "🔄 TESTING RETRY MUTATION ONLY" -ForegroundColor Blue
Write-Host "==============================" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "✅ JWT token generated" -ForegroundColor Green

# Get an exception to retry
Write-Host "1. Getting an exception to retry..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId status retryable retryCount } } } }"
} | ConvertTo-Json

$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$exception = $exceptionsResponse.data.exceptions.edges[0].node
$transactionId = $exception.transactionId

Write-Host "Found exception: $transactionId (Status: $($exception.status), Retryable: $($exception.retryable))" -ForegroundColor Gray

# Test different retry mutation formats
Write-Host "2. Testing retry mutation formats..." -ForegroundColor Cyan

# Format 1: Direct input object
Write-Host "Testing format 1: Direct input object" -ForegroundColor Yellow
$retryMutation1 = @{
    query = @"
mutation {
  retryException(input: {
    transactionId: "$transactionId"
    reason: "Testing retry mutation format 1"
    priority: NORMAL
  }) {
    success
    exception {
      transactionId
      retryCount
    }
    errors {
      message
      code
    }
  }
}
"@
} | ConvertTo-Json

Write-Host "Mutation query:" -ForegroundColor Gray
Write-Host $retryMutation1 -ForegroundColor White

try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $retryMutation1 -TimeoutSec 10
    
    Write-Host "RAW RESPONSE:" -ForegroundColor Yellow
    Write-Host ($response1 | ConvertTo-Json -Depth 5) -ForegroundColor White
    
    if ($response1.errors) {
        Write-Host "❌ GraphQL errors:" -ForegroundColor Red
        foreach ($error in $response1.errors) {
            Write-Host "  - $($error.message)" -ForegroundColor Red
        }
    } elseif ($response1.data.retryException.errors -and $response1.data.retryException.errors.Count -gt 0) {
        Write-Host "❌ Business errors:" -ForegroundColor Red
        foreach ($error in $response1.data.retryException.errors) {
            Write-Host "  - $($error.message) (Code: $($error.code))" -ForegroundColor Red
        }
    } else {
        Write-Host "✅ Retry mutation successful!" -ForegroundColor Green
        Write-Host "  Success: $($response1.data.retryException.success)" -ForegroundColor Gray
        if ($response1.data.retryException.exception) {
            Write-Host "  New retry count: $($response1.data.retryException.exception.retryCount)" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "❌ Request failed: $_" -ForegroundColor Red
}

Write-Host "🔄 RETRY MUTATION TEST COMPLETE" -ForegroundColor Blue