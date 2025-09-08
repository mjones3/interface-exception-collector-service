# Test if subscription resolver is being registered
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "Testing GraphQL Subscription Registration" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue

# Test introspection query to see all available types
Write-Host "`nChecking GraphQL schema introspection..." -ForegroundColor Cyan
$introspectionQuery = @{
    query = "query IntrospectionQuery { __schema { types { name kind fields { name } } } }"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $introspectionQuery -TimeoutSec 10
    
    # Look for Subscription type
    $subscriptionType = $response.data.__schema.types | Where-Object { $_.name -eq "Subscription" }
    
    if ($subscriptionType) {
        Write-Host "✅ Subscription type found in schema!" -ForegroundColor Green
        Write-Host "Available subscription fields:" -ForegroundColor Gray
        $subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ Subscription type NOT found in schema" -ForegroundColor Red
        Write-Host "Available types:" -ForegroundColor Yellow
        $response.data.__schema.types | Where-Object { $_.name -notlike "__*" } | ForEach-Object {
            Write-Host "  - $($_.name) ($($_.kind))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "❌ Introspection query failed: $_" -ForegroundColor Red
}

# Test if the resolver bean exists by checking actuator beans (if available)
Write-Host "`nChecking if ExceptionSubscriptionResolver bean exists..." -ForegroundColor Cyan
try {
    $beans = Invoke-RestMethod -Uri "http://localhost:8080/actuator/beans" -Headers $headers -TimeoutSec 5
    $resolverBean = $beans.contexts.application.beans | Get-Member -Name "*ExceptionSubscriptionResolver*"
    
    if ($resolverBean) {
        Write-Host "✅ ExceptionSubscriptionResolver bean found" -ForegroundColor Green
    } else {
        Write-Host "❌ ExceptionSubscriptionResolver bean NOT found" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠️ Could not check beans endpoint: $_" -ForegroundColor Yellow
}

# Test a simple subscription query (this will fail but shows if the field is recognized)
Write-Host "`nTesting subscription field recognition..." -ForegroundColor Cyan
$subscriptionTest = @{
    query = "subscription { exceptionUpdated { eventType } }"
} | ConvertTo-Json

try {
    $subResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subscriptionTest -TimeoutSec 10
    
    if ($subResponse.errors) {
        $errorMessage = $subResponse.errors[0].message
        if ($errorMessage -like "*exceptionUpdated*" -and $errorMessage -like "*not defined*") {
            Write-Host "❌ Subscription field 'exceptionUpdated' not recognized" -ForegroundColor Red
        } elseif ($errorMessage -like "*subscription*" -and $errorMessage -like "*not supported*") {
            Write-Host "⚠️ Subscriptions not supported over HTTP (expected)" -ForegroundColor Yellow
            Write-Host "✅ But field 'exceptionUpdated' is recognized!" -ForegroundColor Green
        } else {
            Write-Host "⚠️ Subscription test error: $errorMessage" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✅ Subscription query accepted (unexpected over HTTP)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Subscription test failed: $_" -ForegroundColor Red
}

Write-Host "`nDiagnosis:" -ForegroundColor Blue
Write-Host "==========" -ForegroundColor Blue
Write-Host "1. If Subscription type is missing from schema: Resolver not being scanned" -ForegroundColor White
Write-Host "2. If Subscription type exists but no fields: @SubscriptionMapping not working" -ForegroundColor White
Write-Host "3. If fields exist but bean missing: Component scanning issue" -ForegroundColor White
Write-Host "4. If everything exists: WebSocket transport configuration needed" -ForegroundColor White