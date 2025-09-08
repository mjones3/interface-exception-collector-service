# Check the current state of GraphQL subscriptions without WebSocket

Write-Host "GraphQL Subscription State Check" -ForegroundColor Blue
Write-Host "================================" -ForegroundColor Blue

# Generate token
$tokenOutput = node generate-jwt-correct-secret.js 2>$null
$token = ($tokenOutput -split "`n")[-1].Trim()
Write-Host "Token generated successfully" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test basic GraphQL connectivity
Write-Host "`nTesting GraphQL endpoint..." -ForegroundColor Cyan
$basicQuery = @{
    query = "{ __typename }"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $basicQuery -TimeoutSec 10
    Write-Host "GraphQL endpoint is responding" -ForegroundColor Green
} catch {
    Write-Host "GraphQL endpoint failed: $_" -ForegroundColor Red
    exit 1
}

# Check subscription schema
Write-Host "`nChecking subscription schema..." -ForegroundColor Cyan
$schemaQuery = @{
    query = "{ __schema { subscriptionType { name fields { name args { name type { name } } type { name } } } } }"
} | ConvertTo-Json

try {
    $schemaResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $schemaQuery -TimeoutSec 10
    Write-Host "Subscription schema found:" -ForegroundColor Green
    
    if ($schemaResponse.data.__schema.subscriptionType) {
        $schemaResponse.data.__schema.subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name): $($_.type.name)" -ForegroundColor Gray
            if ($_.args.Count -gt 0) {
                $_.args | ForEach-Object {
                    Write-Host "    arg: $($_.name) ($($_.type.name))" -ForegroundColor DarkGray
                }
            }
        }
    } else {
        Write-Host "No subscription type found in schema!" -ForegroundColor Red
    }
} catch {
    Write-Host "Schema query failed: $_" -ForegroundColor Red
}

# Get current exceptions to see if data exists
Write-Host "`nChecking existing exceptions..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 3 }) { totalCount edges { node { id transactionId status severity timestamp exceptionReason } } } }"
} | ConvertTo-Json

try {
    $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery -TimeoutSec 10
    $totalCount = $exceptionsResponse.data.exceptions.totalCount
    Write-Host "Total exceptions in database: $totalCount" -ForegroundColor Green
    
    if ($totalCount -gt 0) {
        Write-Host "Recent exceptions:" -ForegroundColor Gray
        $exceptionsResponse.data.exceptions.edges | ForEach-Object {
            $ex = $_.node
            Write-Host "  - $($ex.transactionId): $($ex.status) ($($ex.severity)) - $($ex.exceptionReason.Substring(0, [Math]::Min(50, $ex.exceptionReason.Length)))..." -ForegroundColor DarkGray
        }
    } else {
        Write-Host "No exceptions found in database" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Exceptions query failed: $_" -ForegroundColor Red
}

# Try to test subscription setup (this won't actually subscribe but will test the resolver)
Write-Host "`nTesting subscription resolver availability..." -ForegroundColor Cyan
$subscriptionTest = @{
    query = 'query { __type(name: "Subscription") { fields { name type { name } } } }'
} | ConvertTo-Json

try {
    $subTestResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subscriptionTest -TimeoutSec 10
    if ($subTestResponse.data.__type) {
        Write-Host "Subscription type is properly registered" -ForegroundColor Green
        $subTestResponse.data.__type.fields | ForEach-Object {
            Write-Host "  - $($_.name): $($_.type.name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "Subscription type not found!" -ForegroundColor Red
    }
} catch {
    Write-Host "Subscription type test failed: $_" -ForegroundColor Red
}

Write-Host "`nDiagnostic Summary:" -ForegroundColor Blue
Write-Host "==================" -ForegroundColor Blue
Write-Host "1. GraphQL endpoint: Working" -ForegroundColor Green
Write-Host "2. Subscription schema: Check results above" -ForegroundColor Yellow
Write-Host "3. Exception data: $totalCount exceptions exist" -ForegroundColor Yellow
Write-Host ""
Write-Host "To test the actual subscription mechanism:" -ForegroundColor Cyan
Write-Host "1. Check application logs for these messages when the app starts:" -ForegroundColor White
Write-Host "   - Bean creation messages for ExceptionEventPublisher" -ForegroundColor Gray
Write-Host "   - Bean creation messages for ExceptionSubscriptionResolver" -ForegroundColor Gray
Write-Host "   - WebSocket configuration messages" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Look for these runtime messages:" -ForegroundColor White
Write-Host "   - 'DEBUG: Sink has subscribers: [N]' when someone subscribes" -ForegroundColor Gray
Write-Host "   - 'Publishing GraphQL exception created event' when events are created" -ForegroundColor Gray
Write-Host "   - 'Successfully published exception update event' when events are published" -ForegroundColor Gray
Write-Host ""
Write-Host "3. The key issue is likely:" -ForegroundColor Yellow
Write-Host "   - WebSocket transport not properly configured" -ForegroundColor White
Write-Host "   - Subscription resolver not receiving events from event publisher" -ForegroundColor White
Write-Host "   - Event publisher not being called by the processing service" -ForegroundColor White