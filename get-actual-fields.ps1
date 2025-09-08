# Get Actual GraphQL Fields
Write-Host "GETTING ACTUAL GRAPHQL FIELDS" -ForegroundColor Blue

$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Get all query fields with their types and args
Write-Host "Query fields:" -ForegroundColor Cyan
$fieldsQuery = @{
    query = '{ __schema { queryType { fields { name type { name } args { name type { name } } } } } }'
} | ConvertTo-Json

$fieldsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $fieldsQuery

if ($fieldsResponse.data.__schema.queryType.fields) {
    $fieldsResponse.data.__schema.queryType.fields | ForEach-Object {
        Write-Host "✅ $($_.name): $($_.type.name)" -ForegroundColor Green
        if ($_.args) {
            $_.args | ForEach-Object {
                Write-Host "    - arg: $($_.name) ($($_.type.name))" -ForegroundColor Gray
            }
        }
    }
} else {
    Write-Host "❌ No query fields found" -ForegroundColor Red
}

# Get subscription fields
Write-Host "`nSubscription fields:" -ForegroundColor Cyan
$subQuery = @{
    query = '{ __schema { subscriptionType { fields { name type { name } } } } }'
} | ConvertTo-Json

$subResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subQuery

if ($subResponse.data.__schema.subscriptionType.fields) {
    $subResponse.data.__schema.subscriptionType.fields | ForEach-Object {
        Write-Host "✅ $($_.name): $($_.type.name)" -ForegroundColor Green
    }
} else {
    Write-Host "❌ No subscription fields found" -ForegroundColor Red
}

Write-Host "`nFIELDS CHECK COMPLETE" -ForegroundColor Blue