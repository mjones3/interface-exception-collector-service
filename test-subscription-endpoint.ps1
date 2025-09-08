#!/usr/bin/env pwsh

# Test if GraphQL subscription endpoints are active

param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Testing GraphQL Subscription Endpoints" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

# Test 1: Check if service is running
Write-Host "`n🔍 Step 1: Testing service availability..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -TimeoutSec 5
    Write-Host "✅ Service is running (Status: $($healthResponse.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "❌ Service is not running or not accessible at $BaseUrl" -ForegroundColor Red
    Write-Host "Please start the service first:" -ForegroundColor Yellow
    Write-Host "  cd interface-exception-collector" -ForegroundColor Gray
    Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
    exit 1
}

# Test 2: Check GraphQL endpoint
Write-Host "`n🔍 Step 2: Testing GraphQL endpoint..." -ForegroundColor Yellow
$healthQuery = @{
    query = "{ __schema { subscriptionType { name } } }"
} | ConvertTo-Json

try {
    $graphqlResponse = Invoke-RestMethod -Uri "$BaseUrl/graphql" -Method POST -Body $healthQuery -ContentType "application/json"
    
    if ($graphqlResponse.data -and $graphqlResponse.data.__schema.subscriptionType) {
        Write-Host "✅ GraphQL subscriptions are ENABLED!" -ForegroundColor Green
        Write-Host "   Subscription type: $($graphqlResponse.data.__schema.subscriptionType.name)" -ForegroundColor Gray
    } else {
        Write-Host "❌ GraphQL subscriptions are NOT enabled" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ GraphQL endpoint test failed: $_" -ForegroundColor Red
}

# Test 3: Check available subscription fields
Write-Host "`n🔍 Step 3: Checking available subscription fields..." -ForegroundColor Yellow
$subscriptionQuery = @{
    query = @"
{
  __schema {
    subscriptionType {
      fields {
        name
        description
        args {
          name
          type {
            name
          }
        }
      }
    }
  }
}
"@
} | ConvertTo-Json

try {
    $subscriptionResponse = Invoke-RestMethod -Uri "$BaseUrl/graphql" -Method POST -Body $subscriptionQuery -ContentType "application/json"
    
    if ($subscriptionResponse.data.__schema.subscriptionType.fields) {
        Write-Host "✅ Available subscription fields:" -ForegroundColor Green
        foreach ($field in $subscriptionResponse.data.__schema.subscriptionType.fields) {
            Write-Host "   - $($field.name)" -ForegroundColor Cyan
            if ($field.args) {
                foreach ($arg in $field.args) {
                    Write-Host "     └─ arg: $($arg.name)" -ForegroundColor Gray
                }
            }
        }
    } else {
        Write-Host "❌ No subscription fields found" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Subscription fields query failed: $_" -ForegroundColor Red
}

# Test 4: Check WebSocket endpoint
Write-Host "`n🔍 Step 4: Testing WebSocket endpoint..." -ForegroundColor Yellow
try {
    # Try to connect to WebSocket endpoint
    $wsUri = $BaseUrl.Replace("http://", "ws://").Replace("https://", "wss://") + "/graphql"
    Write-Host "WebSocket URL: $wsUri" -ForegroundColor Gray
    
    # Note: PowerShell WebSocket testing is complex, so we'll just check if the endpoint exists
    Write-Host "⚠️  WebSocket endpoint test requires manual verification" -ForegroundColor Yellow
    Write-Host "   Try connecting with: $wsUri" -ForegroundColor Gray
} catch {
    Write-Host "❌ WebSocket test failed: $_" -ForegroundColor Red
}

# Test 5: Check GraphiQL availability
Write-Host "`n🔍 Step 5: Checking GraphiQL availability..." -ForegroundColor Yellow
try {
    $graphiqlResponse = Invoke-WebRequest -Uri "$BaseUrl/graphiql" -TimeoutSec 5
    Write-Host "✅ GraphiQL is available at: $BaseUrl/graphiql" -ForegroundColor Green
} catch {
    Write-Host "⚠️  GraphiQL might not be enabled" -ForegroundColor Yellow
}

# Test 6: Try a simple subscription (without WebSocket)
Write-Host "`n🔍 Step 6: Testing subscription resolver availability..." -ForegroundColor Yellow
$testSubscription = @{
    query = @"
subscription {
  exceptionUpdated {
    eventType
    timestamp
  }
}
"@
} | ConvertTo-Json

try {
    $subResponse = Invoke-RestMethod -Uri "$BaseUrl/graphql" -Method POST -Body $testSubscription -ContentType "application/json"
    
    if ($subResponse.errors) {
        $errorMsg = $subResponse.errors[0].message
        if ($errorMsg -like "*subscription*" -or $errorMsg -like "*WebSocket*") {
            Write-Host "✅ Subscription resolver exists (needs WebSocket connection)" -ForegroundColor Green
            Write-Host "   Error: $errorMsg" -ForegroundColor Gray
        } else {
            Write-Host "❌ Subscription error: $errorMsg" -ForegroundColor Red
        }
    } else {
        Write-Host "✅ Subscription query accepted" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Subscription test failed: $_" -ForegroundColor Red
}

Write-Host "`n📊 Summary:" -ForegroundColor Cyan
Write-Host "- Service URL: $BaseUrl" -ForegroundColor White
Write-Host "- GraphQL Endpoint: $BaseUrl/graphql" -ForegroundColor White
Write-Host "- GraphiQL UI: $BaseUrl/graphiql" -ForegroundColor White
Write-Host "- WebSocket URL: $($BaseUrl.Replace('http://', 'ws://').Replace('https://', 'wss://'))/graphql" -ForegroundColor White

Write-Host "`n🎯 Next Steps:" -ForegroundColor Green
Write-Host "1. Open GraphiQL: $BaseUrl/graphiql" -ForegroundColor White
Write-Host "2. Try the subscription in the GraphiQL interface" -ForegroundColor White
Write-Host "3. Or use the HTML test client: graphql-subscription-test.html" -ForegroundColor White

Write-Host "`n🔧 If subscriptions don't work:" -ForegroundColor Yellow
Write-Host "- Check service logs for WebSocket errors" -ForegroundColor White
Write-Host "- Verify authentication (may need JWT token)" -ForegroundColor White
Write-Host "- Try without authentication first" -ForegroundColor White