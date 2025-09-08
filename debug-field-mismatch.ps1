# Debug Field Mismatch
Write-Host "DEBUGGING FIELD MISMATCH" -ForegroundColor Blue

$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test 1: Simple introspection
Write-Host "1. Simple introspection..." -ForegroundColor Cyan
$simpleQuery = @{ query = '{ __schema { queryType { name } } }' } | ConvertTo-Json
$simpleResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $simpleQuery
Write-Host "Query type name: $($simpleResponse.data.__schema.queryType.name)" -ForegroundColor Gray

# Test 2: Check if fields property exists
Write-Host "2. Checking fields property..." -ForegroundColor Cyan
$fieldsExistQuery = @{ query = '{ __schema { queryType { fields } } }' } | ConvertTo-Json
try {
    $fieldsExistResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $fieldsExistQuery
    Write-Host "Fields response: $($fieldsExistResponse | ConvertTo-Json -Depth 5)" -ForegroundColor Gray
} catch {
    Write-Host "Fields query failed: $_" -ForegroundColor Red
}

# Test 3: Try the working exceptions query again
Write-Host "3. Testing exceptions query again..." -ForegroundColor Cyan
$exceptionsQuery = @{ query = '{ exceptions(pagination: { first: 1 }) { totalCount } }' } | ConvertTo-Json
try {
    $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
    Write-Host "✅ Exceptions query still works: $($exceptionsResponse.data.exceptions.totalCount) total" -ForegroundColor Green
} catch {
    Write-Host "❌ Exceptions query failed: $_" -ForegroundColor Red
}

# Test 4: Try subscription query
Write-Host "4. Testing subscription query..." -ForegroundColor Cyan
$subTestQuery = @{ query = 'subscription { exceptionEvents { eventType } }' } | ConvertTo-Json
try {
    $subTestResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subTestQuery
    Write-Host "✅ Subscription query works: $($subTestResponse | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Subscription query failed: $_" -ForegroundColor Red
}

Write-Host "DEBUG COMPLETE" -ForegroundColor Blue