# Test if Query resolvers are working
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "Testing Query Resolver Registration" -ForegroundColor Blue
Write-Host "===================================" -ForegroundColor Blue

# Test if Query type has fields
$queryFieldsTest = @{
    query = "{ __schema { queryType { fields { name } } } }"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $queryFieldsTest -TimeoutSec 10
    
    if ($response.data.__schema.queryType.fields) {
        Write-Host "✅ Query type has fields:" -ForegroundColor Green
        $response.data.__schema.queryType.fields | ForEach-Object {
            Write-Host "  - $($_.name)" -ForegroundColor Gray
        }
        
        # Test if exceptions query works
        Write-Host "`nTesting exceptions query..." -ForegroundColor Cyan
        $exceptionsQuery = @{
            query = "{ exceptions(pagination: { first: 1 }) { totalCount } }"
        } | ConvertTo-Json
        
        try {
            $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery -TimeoutSec 10
            Write-Host "✅ Exceptions query works! Total: $($exceptionsResponse.data.exceptions.totalCount)" -ForegroundColor Green
        } catch {
            Write-Host "❌ Exceptions query failed: $_" -ForegroundColor Red
        }
        
    } else {
        Write-Host "❌ Query type has no fields!" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Query fields test failed: $_" -ForegroundColor Red
}

Write-Host "`nConclusion:" -ForegroundColor Blue
Write-Host "If Query resolvers work but Subscription doesn't, the issue is:" -ForegroundColor Yellow
Write-Host "1. Subscription resolvers not being scanned" -ForegroundColor White
Write-Host "2. @SubscriptionMapping not supported in this GraphQL version" -ForegroundColor White
Write-Host "3. WebSocket transport not configured for subscriptions" -ForegroundColor White