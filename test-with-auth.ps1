#!/usr/bin/env pwsh

# Test GraphQL subscriptions with authentication

$baseUrl = "http://localhost:8080"

Write-Host "GraphQL Subscriptions with Authentication" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# Step 1: Get JWT token
Write-Host "`n🔐 Getting JWT token..." -ForegroundColor Yellow
$loginData = '{"username":"admin","password":"password"}'

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ Token obtained: $($token.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed: $_" -ForegroundColor Red
    Write-Host "Trying without authentication..." -ForegroundColor Yellow
    $token = $null
}

# Step 2: Test GraphQL with token
Write-Host "`n📊 Testing GraphQL subscriptions..." -ForegroundColor Yellow
$headers = @{ "Content-Type" = "application/json" }
if ($token) {
    $headers["Authorization"] = "Bearer $token"
}

$query = '{"query": "{ __schema { subscriptionType { name fields { name } } } }"}'

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $query -Headers $headers
    
    if ($response.data.__schema.subscriptionType) {
        Write-Host "✅ SUBSCRIPTIONS ARE ACTIVE!" -ForegroundColor Green
        Write-Host "`nAvailable subscriptions:" -ForegroundColor Cyan
        foreach ($field in $response.data.__schema.subscriptionType.fields) {
            Write-Host "  - $($field.name)" -ForegroundColor White
        }
        
        Write-Host "`n🎉 SUCCESS! You can now use subscriptions with this token." -ForegroundColor Green
        
    } else {
        Write-Host "❌ No subscriptions found" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Still getting error: $_" -ForegroundColor Red
}

# Step 3: Show how to use the token
if ($token) {
    Write-Host "`n🚀 How to use subscriptions:" -ForegroundColor Cyan
    Write-Host "1. WebSocket URL: ws://localhost:8080/graphql?token=$token" -ForegroundColor White
    Write-Host "2. Or use Authorization header: Bearer $token" -ForegroundColor White
    Write-Host "`n3. Try this in GraphiQL or HTML client:" -ForegroundColor White
    Write-Host "   subscription { exceptionUpdated { eventType timestamp } }" -ForegroundColor Gray
    
    Write-Host "`n🔧 Command line watching:" -ForegroundColor Yellow
    Write-Host ".\curl-watch-graphql.ps1 -Token `"$token`"" -ForegroundColor Gray
}