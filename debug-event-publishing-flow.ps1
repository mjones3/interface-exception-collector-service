# Comprehensive debugging script for GraphQL event publishing flow

Write-Host "🔍 GraphQL Event Publishing Flow Debug" -ForegroundColor Blue
Write-Host "=====================================" -ForegroundColor Blue

# Generate JWT token
Write-Host "`n🔑 Step 1: Generating JWT token..." -ForegroundColor Cyan
try {
    $tokenOutput = node generate-jwt-correct-secret.js 2>$null
    $token = ($tokenOutput -split "`n")[-1].Trim()
    Write-Host "✅ Token generated successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Token generation failed: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Step 2: Check server health
Write-Host "`n🌐 Step 2: Checking server health..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "✅ Server is healthy" -ForegroundColor Green
} catch {
    Write-Host "❌ Server health check failed: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Check GraphQL endpoint
Write-Host "`n📋 Step 3: Testing GraphQL endpoint..." -ForegroundColor Cyan
$basicQuery = @{
    query = "{ __typename }"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $basicQuery -TimeoutSec 10
    Write-Host "✅ GraphQL endpoint responding" -ForegroundColor Green
} catch {
    Write-Host "❌ GraphQL endpoint failed: $_" -ForegroundColor Red
    exit 1
}

# Step 4: Check subscription schema
Write-Host "`n🔍 Step 4: Verifying subscription schema..." -ForegroundColor Cyan
$schemaQuery = @{
    query = "{ __schema { subscriptionType { name fields { name type { name } } } } }"
} | ConvertTo-Json

try {
    $schemaResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $schemaQuery -TimeoutSec 10
    Write-Host "✅ Subscription schema verified" -ForegroundColor Green
    Write-Host "Available subscriptions:" -ForegroundColor Gray
    $schemaResponse.data.__schema.subscriptionType.fields | ForEach-Object {
        Write-Host "  - $($_.name): $($_.type.name)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Schema query failed: $_" -ForegroundColor Red
}

# Step 5: Get current exception count
Write-Host "`n📊 Step 5: Getting current exception count..." -ForegroundColor Cyan
$countQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { totalCount } }"
} | ConvertTo-Json

try {
    $countResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $countQuery -TimeoutSec 10
    $initialCount = $countResponse.data.exceptions.totalCount
    Write-Host "✅ Current exception count: $initialCount" -ForegroundColor Green
} catch {
    Write-Host "❌ Count query failed: $_" -ForegroundColor Red
    $initialCount = 0
}

# Step 6: Trigger a new exception event
Write-Host "`n📤 Step 6: Triggering new exception event..." -ForegroundColor Cyan
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$kafkaPayload = @{
    transactionId = "debug-flow-$timestamp"
    externalId = "DEBUG-FLOW-EXT-$timestamp"
    operation = "CREATE_ORDER"
    rejectedReason = "Event publishing flow debug test - $timestamp"
    customerId = "DEBUG-FLOW-CUST"
    locationCode = "DEBUG-FLOW-LOC"
} | ConvertTo-Json

Write-Host "Triggering event with transaction ID: debug-flow-$timestamp" -ForegroundColor Yellow

try {
    $triggerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/kafka/order-rejected" -Method Post -Headers $headers -Body $kafkaPayload -TimeoutSec 10
    Write-Host "✅ Event triggered successfully" -ForegroundColor Green
    Write-Host "Response: $($triggerResponse | ConvertTo-Json -Compress)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Event trigger failed: $_" -ForegroundColor Red
    exit 1
}

# Step 7: Wait and verify exception was created
Write-Host "`n⏳ Step 7: Waiting for exception processing..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

$verifyQuery = @{
    query = "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $verifyQuery -TimeoutSec 10
    $newCount = $verifyResponse.data.exceptions.totalCount
    
    if ($newCount -gt $initialCount) {
        Write-Host "✅ Exception created successfully! Count increased from $initialCount to $newCount" -ForegroundColor Green
        
        # Find our specific exception
        $ourException = $verifyResponse.data.exceptions.edges | Where-Object { $_.node.transactionId -eq "debug-flow-$timestamp" }
        if ($ourException) {
            Write-Host "✅ Found our exception: debug-flow-$timestamp" -ForegroundColor Green
            Write-Host "   Status: $($ourException.node.status)" -ForegroundColor Gray
            Write-Host "   Severity: $($ourException.node.severity)" -ForegroundColor Gray
            Write-Host "   Timestamp: $($ourException.node.timestamp)" -ForegroundColor Gray
        } else {
            Write-Host "⚠️ Exception created but not found in recent list" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ Exception count did not increase! Still at $newCount" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Verification query failed: $_" -ForegroundColor Red
    exit 1
}

# Step 8: Check application logs guidance
Write-Host "`n📋 Step 8: Application Log Analysis" -ForegroundColor Blue
Write-Host "=================================" -ForegroundColor Blue
Write-Host ""
Write-Host "🔍 Look for these specific log messages in your application logs:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. ExceptionProcessingService logs:" -ForegroundColor Cyan
Write-Host "   ✅ 'Processing OrderRejected event for transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ 'Created new exception with ID: [ID] for transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ 'Published GraphQL subscription event for new exception: debug-flow-$timestamp'" -ForegroundColor White
Write-Host ""
Write-Host "2. ExceptionEventPublisher logs:" -ForegroundColor Cyan
Write-Host "   ✅ '🔔 Publishing GraphQL exception created event for transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ '🔍 DEBUG: Exception details - ID: [ID], Status: NEW, Severity: HIGH'" -ForegroundColor White
Write-Host "   ✅ '📡 Calling subscriptionResolver.publishExceptionUpdate() for transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ '✅ Successfully called subscriptionResolver.publishExceptionUpdate() for transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ '📡 Broadcasted exception created event via WebSocket to [N] sessions'" -ForegroundColor White
Write-Host ""
Write-Host "3. ExceptionSubscriptionResolver logs:" -ForegroundColor Cyan
Write-Host "   ✅ '📡 Received exception update event for publishing: CREATED - transaction: debug-flow-$timestamp'" -ForegroundColor White
Write-Host "   ✅ '🔍 DEBUG: Active subscriptions count: [N]'" -ForegroundColor White
Write-Host "   ✅ '🔍 DEBUG: Sink has subscribers: [N]'" -ForegroundColor White
Write-Host "   ✅ '✅ Successfully published exception update event for transaction: debug-flow-$timestamp to [N] subscribers'" -ForegroundColor White
Write-Host ""
Write-Host "❌ If you see ERROR messages or missing logs, that's where the issue is!" -ForegroundColor Red
Write-Host ""
Write-Host "🔧 Common Issues:" -ForegroundColor Yellow
Write-Host "   - If no ExceptionEventPublisher logs: Dependency injection issue" -ForegroundColor White
Write-Host "   - If no ExceptionSubscriptionResolver logs: Method not being called" -ForegroundColor White
Write-Host "   - If 'Sink has subscribers: 0': No active WebSocket subscriptions" -ForegroundColor White
Write-Host "   - If 'Failed to emit': Sink configuration issue" -ForegroundColor White
Write-Host ""
Write-Host "💡 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Check application logs for the messages above" -ForegroundColor White
Write-Host "2. If all logs are present but WebSocket still doesn't work:" -ForegroundColor White
Write-Host "   - Run './test-websocket.ps1' to verify WebSocket connection" -ForegroundColor White
Write-Host "   - Check if there are active subscribers (should be > 0)" -ForegroundColor White
Write-Host "3. If logs are missing, we need to fix the dependency injection" -ForegroundColor White
Write-Host ""
Write-Host "🎯 Transaction ID to search for: debug-flow-$timestamp" -ForegroundColor Green