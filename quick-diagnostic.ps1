# Quick GraphQL Diagnostic
Write-Host "GRAPHQL DIAGNOSTIC" -ForegroundColor Blue

# Check if app is running
Write-Host "1. Checking application..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application running: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Application not running" -ForegroundColor Red
    exit 1
}

# Generate JWT
Write-Host "2. Generating JWT..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}
Write-Host "✅ JWT generated" -ForegroundColor Green

# Test GraphQL endpoint
Write-Host "3. Testing GraphQL endpoint..." -ForegroundColor Cyan
$basicQuery = @{ query = "{ __typename }" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $basicQuery
Write-Host "✅ GraphQL endpoint works" -ForegroundColor Green

# Check query fields
Write-Host "4. Checking query fields..." -ForegroundColor Cyan
$fieldsQuery = @{ query = "{ __schema { queryType { fields { name } } } }" } | ConvertTo-Json
$fieldsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $fieldsQuery

if ($fieldsResponse.data.__schema.queryType.fields) {
    Write-Host "✅ Query fields found:" -ForegroundColor Green
    $fieldsResponse.data.__schema.queryType.fields | ForEach-Object {
        Write-Host "  - $($_.name)" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ NO QUERY FIELDS!" -ForegroundColor Red
}

# Check subscription fields
Write-Host "5. Checking subscription fields..." -ForegroundColor Cyan
$subQuery = @{ query = "{ __schema { subscriptionType { fields { name } } } }" } | ConvertTo-Json
$subResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subQuery

if ($subResponse.data.__schema.subscriptionType.fields) {
    Write-Host "✅ Subscription fields found:" -ForegroundColor Green
    $subResponse.data.__schema.subscriptionType.fields | ForEach-Object {
        Write-Host "  - $($_.name)" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ NO SUBSCRIPTION FIELDS!" -ForegroundColor Red
}

Write-Host "DIAGNOSIS COMPLETE" -ForegroundColor Blue