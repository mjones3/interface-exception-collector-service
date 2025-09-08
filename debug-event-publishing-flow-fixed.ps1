# Comprehensive debugging script for GraphQL event publishing flow

Write-Host "Debug GraphQL Event Publishing Flow" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue

# Generate JWT token
Write-Host "`nStep 1: Generating JWT token..." -ForegroundColor Cyan
try {
    $tokenOutput = node generate-jwt-correct-secret.js 2>$null
    $token = ($tokenOutput -split "`n")[-1].Trim()
    Write-Host "Token generated successfully" -ForegroundColor Green
} catch {
    Write-Host "Token generation failed: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Check server health
Write-Host "`nStep 2: Checking server health..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "Server is healthy" -ForegroundColor Green
} catch {
    Write-Host "Server health check failed: $_" -ForegroundColor Red
    exit 1
}

# Get current exception count
Write-Host "`nStep 3: Getting current exception count..." -ForegroundColor Cyan
$countQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { totalCount } }"
} | ConvertTo-Json

try {
    $countResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $countQuery -TimeoutSec 10
    $initialCount = $countResponse.data.exceptions.totalCount
    Write-Host "Current exception count: $initialCount" -ForegroundColor Green
} catch {
    Write-Host "Count query failed: $_" -ForegroundColor Red
    $initialCount = 0
}

# Trigger a new exception event
Write-Host "`nStep 4: Triggering new exception event..." -ForegroundColor Cyan
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$transactionId = "debug-flow-$timestamp"

$kafkaPayload = @{
    transactionId = $transactionId
    externalId = "DEBUG-FLOW-EXT-$timestamp"
    operation = "CREATE_ORDER"
    rejectedReason = "Event publishing flow debug test - $timestamp"
    customerId = "DEBUG-FLOW-CUST"
    locationCode = "DEBUG-FLOW-LOC"
} | ConvertTo-Json

Write-Host "Triggering event with transaction ID: $transactionId" -ForegroundColor Yellow

try {
    $triggerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/kafka/order-rejected" -Method Post -Headers $headers -Body $kafkaPayload -TimeoutSec 10
    Write-Host "Event triggered successfully" -ForegroundColor Green
    Write-Host "Response: $($triggerResponse | ConvertTo-Json -Compress)" -ForegroundColor Gray
} catch {
    Write-Host "Event trigger failed: $_" -ForegroundColor Red
    exit 1
}

# Wait and verify exception was created
Write-Host "`nStep 5: Waiting for exception processing..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

$verifyQuery = @{
    query = "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $verifyQuery -TimeoutSec 10
    $newCount = $verifyResponse.data.exceptions.totalCount
    
    if ($newCount -gt $initialCount) {
        Write-Host "Exception created successfully! Count increased from $initialCount to $newCount" -ForegroundColor Green
        
        # Find our specific exception
        $ourException = $verifyResponse.data.exceptions.edges | Where-Object { $_.node.transactionId -eq $transactionId }
        if ($ourException) {
            Write-Host "Found our exception: $transactionId" -ForegroundColor Green
            Write-Host "   Status: $($ourException.node.status)" -ForegroundColor Gray
            Write-Host "   Severity: $($ourException.node.severity)" -ForegroundColor Gray
        } else {
            Write-Host "Exception created but not found in recent list" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Exception count did not increase! Still at $newCount" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Verification query failed: $_" -ForegroundColor Red
    exit 1
}

# Application log analysis guidance
Write-Host "`nApplication Log Analysis" -ForegroundColor Blue
Write-Host "========================" -ForegroundColor Blue
Write-Host ""
Write-Host "Look for these specific log messages in your application logs:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. ExceptionProcessingService logs:" -ForegroundColor Cyan
Write-Host "   - Processing OrderRejected event for transaction: $transactionId" -ForegroundColor White
Write-Host "   - Created new exception with ID: [ID] for transaction: $transactionId" -ForegroundColor White
Write-Host "   - Published GraphQL subscription event for new exception: $transactionId" -ForegroundColor White
Write-Host ""
Write-Host "2. ExceptionEventPublisher logs:" -ForegroundColor Cyan
Write-Host "   - Publishing GraphQL exception created event for transaction: $transactionId" -ForegroundColor White
Write-Host "   - DEBUG: Exception details - ID: [ID], Status: NEW, Severity: HIGH" -ForegroundColor White
Write-Host "   - Calling subscriptionResolver.publishExceptionUpdate() for transaction: $transactionId" -ForegroundColor White
Write-Host "   - Successfully called subscriptionResolver.publishExceptionUpdate() for transaction: $transactionId" -ForegroundColor White
Write-Host "   - Broadcasted exception created event via WebSocket to [N] sessions" -ForegroundColor White
Write-Host ""
Write-Host "3. ExceptionSubscriptionResolver logs:" -ForegroundColor Cyan
Write-Host "   - Received exception update event for publishing: CREATED - transaction: $transactionId" -ForegroundColor White
Write-Host "   - DEBUG: Active subscriptions count: [N]" -ForegroundColor White
Write-Host "   - DEBUG: Sink has subscribers: [N]" -ForegroundColor White
Write-Host "   - Successfully published exception update event for transaction: $transactionId to [N] subscribers" -ForegroundColor White
Write-Host ""
Write-Host "If you see ERROR messages or missing logs, that's where the issue is!" -ForegroundColor Red
Write-Host ""
Write-Host "Common Issues:" -ForegroundColor Yellow
Write-Host "   - If no ExceptionEventPublisher logs: Dependency injection issue" -ForegroundColor White
Write-Host "   - If no ExceptionSubscriptionResolver logs: Method not being called" -ForegroundColor White
Write-Host "   - If 'Sink has subscribers: 0': No active WebSocket subscriptions" -ForegroundColor White
Write-Host "   - If 'Failed to emit': Sink configuration issue" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Check application logs for the messages above" -ForegroundColor White
Write-Host "2. If all logs are present but WebSocket still doesn't work:" -ForegroundColor White
Write-Host "   - Run test-websocket-subscription-live.ps1 to verify WebSocket connection" -ForegroundColor White
Write-Host "   - Check if there are active subscribers (should be > 0)" -ForegroundColor White
Write-Host "3. If logs are missing, we need to fix the dependency injection" -ForegroundColor White
Write-Host ""
Write-Host "Transaction ID to search for: $transactionId" -ForegroundColor Green