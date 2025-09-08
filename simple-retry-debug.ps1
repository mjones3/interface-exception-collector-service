# Simple Retry Debug
Write-Host "SIMPLE RETRY DEBUG" -ForegroundColor Blue

$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Check mutation fields
Write-Host "1. Checking mutation fields..." -ForegroundColor Cyan
$mutationQuery = @{ query = "{ __schema { mutationType { fields { name } } } }" } | ConvertTo-Json
$mutationResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $mutationQuery

Write-Host "Available mutations:" -ForegroundColor Gray
$mutationResponse.data.__schema.mutationType.fields | ForEach-Object {
    Write-Host "  - $($_.name)" -ForegroundColor White
}

# Test simple retry
Write-Host "2. Testing simple retry..." -ForegroundColor Cyan
$simpleRetry = @{
    query = 'mutation { retryException(input: { transactionId: "c526bd6b-8ca4-42a6-87d9-8678d6afae1a", reason: "test" }) { success } }'
} | ConvertTo-Json

Write-Host "Query: $($simpleRetry)" -ForegroundColor Gray

$retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $simpleRetry

Write-Host "Response:" -ForegroundColor Yellow
Write-Host ($retryResponse | ConvertTo-Json -Depth 3) -ForegroundColor White

Write-Host "SIMPLE RETRY DEBUG COMPLETE" -ForegroundColor Blue