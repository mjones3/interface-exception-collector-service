# Trigger Events and Test Subscription System
Write-Host "TRIGGERING EVENTS AND TESTING SUBSCRIPTION SYSTEM" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "1. Testing current exception count..." -ForegroundColor Cyan
$countQuery = @{ query = '{ exceptions(pagination: { first: 1 }) { totalCount } }' } | ConvertTo-Json
$countResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $countQuery
$initialCount = $countResponse.data.exceptions.totalCount
Write-Host "Current exception count: $initialCount" -ForegroundColor Gray

Write-Host "2. Triggering a new exception via Kafka..." -ForegroundColor Cyan

# Create a test Kafka message
$kafkaMessage = @{
    transactionId = "test-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    interfaceType = "ORDER_PROCESSING"
    exceptionType = "VALIDATION_ERROR"
    message = "Test exception for subscription testing"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    severity = "HIGH"
    customerImpact = "MEDIUM"
    retryable = $true
    metadata = @{
        orderId = "ORDER-12345"
        customerId = "CUST-67890"
    }
} | ConvertTo-Json -Depth 3

Write-Host "Kafka message: $kafkaMessage" -ForegroundColor Gray

# Try to send to Kafka (this might fail if Kafka isn't set up, but let's see)
try {
    # Check if we have a Kafka endpoint or producer
    Write-Host "Checking for Kafka producer endpoint..." -ForegroundColor Yellow
    
    # Look for any REST endpoints that might trigger exceptions
    $endpoints = @(
        "http://localhost:8080/actuator/health",
        "http://localhost:8080/api/exceptions/trigger-test",
        "http://localhost:8080/test/trigger-exception"
    )
    
    foreach ($endpoint in $endpoints) {
        try {
            $response = Invoke-RestMethod -Uri $endpoint -Headers $headers -TimeoutSec 3
            Write-Host "✅ Endpoint $endpoint accessible" -ForegroundColor Green
        } catch {
            if ($_.Exception.Message -like "*404*") {
                Write-Host "❌ Endpoint $endpoint not found" -ForegroundColor Red
            } else {
                Write-Host "⚠️ Endpoint $endpoint error: $_" -ForegroundColor Yellow
            }
        }
    }
    
} catch {
    Write-Host "❌ Kafka trigger failed: $_" -ForegroundColor Red
}

Write-Host "3. Checking if exception count changed..." -ForegroundColor Cyan
Start-Sleep -Seconds 2
$newCountResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $countQuery
$newCount = $newCountResponse.data.exceptions.totalCount
Write-Host "New exception count: $newCount" -ForegroundColor Gray

if ($newCount -gt $initialCount) {
    Write-Host "✅ Exception count increased! Events are being processed." -ForegroundColor Green
} else {
    Write-Host "❌ No new exceptions detected." -ForegroundColor Red
}

Write-Host "4. Testing GraphQL subscription field availability..." -ForegroundColor Cyan
$subFieldsQuery = @{ query = '{ __schema { subscriptionType { fields { name } } } }' } | ConvertTo-Json
try {
    $subFieldsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subFieldsQuery
    
    if ($subFieldsResponse.data.__schema.subscriptionType.fields) {
        Write-Host "✅ Subscription fields found:" -ForegroundColor Green
        $subFieldsResponse.data.__schema.subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ No subscription fields found" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Subscription fields query failed: $_" -ForegroundColor Red
}

Write-Host "EVENT TRIGGER TEST COMPLETE" -ForegroundColor Blue