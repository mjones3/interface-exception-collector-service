# Test Retry Events Subscription
Write-Host "🔄 TESTING RETRY EVENTS SUBSCRIPTION" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue

# Check application status
Write-Host "1. Checking application status..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application running: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Application not running" -ForegroundColor Red
    exit 1
}

# Generate JWT
Write-Host "2. Generating JWT token..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
Write-Host "✅ JWT token generated" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test retry subscription field availability
Write-Host "3. Testing retry subscription field..." -ForegroundColor Cyan
$retrySubQuery = @{ query = "subscription { retryStatusUpdated { eventType } }" } | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $retrySubQuery -TimeoutSec 5
    
    if ($response.errors) {
        $errorMsg = $response.errors[0].message
        if ($errorMsg -like "*WebSocket*" -or $errorMsg -like "*subscription*") {
            Write-Host "✅ retryStatusUpdated field exists, requires WebSocket" -ForegroundColor Green
        } else {
            Write-Host "❌ retryStatusUpdated field error: $errorMsg" -ForegroundColor Red
        }
    } else {
        Write-Host "✅ retryStatusUpdated field accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Retry subscription test failed: $_" -ForegroundColor Red
}

# Test retry mutations to trigger events
Write-Host "4. Testing retry mutations to trigger events..." -ForegroundColor Cyan

# First, get an existing exception to retry
$exceptionsQuery = @{ query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId status retryable } } } }" } | ConvertTo-Json
try {
    $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
    
    if ($exceptionsResponse.data.exceptions.edges.Count -gt 0) {
        $exception = $exceptionsResponse.data.exceptions.edges[0].node
        $transactionId = $exception.transactionId
        Write-Host "Found exception to retry: $transactionId" -ForegroundColor Gray
        
        # Try to trigger a retry
        $retryMutation = @{
            query = "mutation { retryException(input: { transactionId: `"$transactionId`", reason: `"Testing retry subscription`" }) { success } }"
        } | ConvertTo-Json
        
        try {
            $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $retryMutation -TimeoutSec 10
            
            if ($retryResponse.errors) {
                Write-Host "⚠️ Retry mutation error: $($retryResponse.errors[0].message)" -ForegroundColor Yellow
            } else {
                Write-Host "✅ Retry mutation executed successfully" -ForegroundColor Green
            }
        } catch {
            Write-Host "❌ Retry mutation failed: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️ No exceptions found to retry" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Failed to get exceptions: $_" -ForegroundColor Red
}

# Start WebSocket retry subscription test
Write-Host "5. Starting WebSocket retry subscription test..." -ForegroundColor Cyan
Write-Host "RAW OUTPUT FROM RETRY SUBSCRIPTION:" -ForegroundColor Yellow
Write-Host "====================================" -ForegroundColor Yellow

# Run the retry subscription test and capture all output
node test-retry-subscription.js $token

Write-Host "====================================" -ForegroundColor Yellow
Write-Host "END OF RAW OUTPUT" -ForegroundColor Yellow

Write-Host "🔄 RETRY SUBSCRIPTION TEST COMPLETE" -ForegroundColor Blue