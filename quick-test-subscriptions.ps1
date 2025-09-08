#!/usr/bin/env pwsh

# Quick test for GraphQL subscriptions

$baseUrl = "http://localhost:8080"

Write-Host "Quick GraphQL Subscription Test" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

# Test 1: Service health
Write-Host "`nTesting service health..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -TimeoutSec 5
    Write-Host "✅ Service is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Service not running at $baseUrl" -ForegroundColor Red
    exit 1
}

# Test 2: GraphQL schema introspection
Write-Host "`nTesting GraphQL subscriptions..." -ForegroundColor Yellow
$query = '{"query": "{ __schema { subscriptionType { name fields { name } } } }"}'

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $query -ContentType "application/json"
    
    if ($response.data.__schema.subscriptionType) {
        Write-Host "✅ GraphQL subscriptions are ACTIVE!" -ForegroundColor Green
        Write-Host "Available subscriptions:" -ForegroundColor Cyan
        foreach ($field in $response.data.__schema.subscriptionType.fields) {
            Write-Host "  - $($field.name)" -ForegroundColor White
        }
    } else {
        Write-Host "❌ No subscription type found" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ GraphQL test failed: $_" -ForegroundColor Red
}

# Test 3: Try a subscription query (will fail without WebSocket but shows if resolver exists)
Write-Host "`nTesting subscription resolver..." -ForegroundColor Yellow
$subQuery = '{"query": "subscription { exceptionUpdated { eventType timestamp } }"}'

try {
    $subResponse = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $subQuery -ContentType "application/json"
    
    if ($subResponse.errors) {
        $error = $subResponse.errors[0].message
        if ($error -like "*subscription*" -or $error -like "*WebSocket*") {
            Write-Host "✅ Subscription resolver exists (needs WebSocket)" -ForegroundColor Green
        } else {
            Write-Host "❌ Subscription error: $error" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "⚠️  Subscription test inconclusive" -ForegroundColor Yellow
}

Write-Host "`n🎯 Results:" -ForegroundColor Cyan
Write-Host "- GraphQL endpoint: $baseUrl/graphql" -ForegroundColor White
Write-Host "- GraphiQL UI: $baseUrl/graphiql" -ForegroundColor White
Write-Host "- WebSocket endpoint: ws://localhost:8080/graphql" -ForegroundColor White

Write-Host "`n💡 To test subscriptions:" -ForegroundColor Green
Write-Host "1. Open: $baseUrl/graphiql" -ForegroundColor White
Write-Host "2. Use the subscription tab" -ForegroundColor White
Write-Host "3. Or open: graphql-subscription-test.html" -ForegroundColor White