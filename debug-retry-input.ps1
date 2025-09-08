# Debug Retry Input Issue
Write-Host "🔍 DEBUGGING RETRY INPUT ISSUE" -ForegroundColor Blue
Write-Host "===============================" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "✅ JWT token generated" -ForegroundColor Green

# Test 1: Check if retryException mutation exists in schema
Write-Host "1. Checking if retryException mutation exists..." -ForegroundColor Cyan
$mutationFieldsQuery = @{
    query = "{ __schema { mutationType { fields { name args { name type { name } } } } } }"
} | ConvertTo-Json

try {
    $mutationResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $mutationFieldsQuery
    
    $retryMutation = $mutationResponse.data.__schema.mutationType.fields | Where-Object { $_.name -eq "retryException" }
    
    if ($retryMutation) {
        Write-Host "✅ retryException mutation found" -ForegroundColor Green
        Write-Host "Arguments:" -ForegroundColor Gray
        foreach ($arg in $retryMutation.args) {
            Write-Host "  - $($arg.name): $($arg.type.name)" -ForegroundColor White
        }
    } else {
        Write-Host "❌ retryException mutation not found" -ForegroundColor Red
        Write-Host "Available mutations:" -ForegroundColor Gray
        foreach ($mutation in $mutationResponse.data.__schema.mutationType.fields) {
            Write-Host "  - $($mutation.name)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Failed to get mutation schema: $_" -ForegroundColor Red
}

# Test 2: Try minimal retry mutation
Write-Host "`n2. Testing minimal retry mutation..." -ForegroundColor Cyan
$transactionId = "c526bd6b-8ca4-42a6-87d9-8678d6afae1a"

$minimalRetryMutation = @{
    query = "mutation { retryException(input: { transactionId: `"$transactionId`", reason: `"test`" }) { success } }"
} | ConvertTo-Json

Write-Host "Minimal mutation:" -ForegroundColor Gray
Write-Host $minimalRetryMutation -ForegroundColor White

try {
    $minimalResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $minimalRetryMutation -TimeoutSec 10
    
    Write-Host "RAW MINIMAL RESPONSE:" -ForegroundColor Yellow
    Write-Host ($minimalResponse | ConvertTo-Json -Depth 5) -ForegroundColor White
} catch {
    Write-Host "❌ Minimal mutation failed: $_" -ForegroundColor Red
}

# Test 3: Try with variables
Write-Host "`n3. Testing with GraphQL variables..." -ForegroundColor Cyan
$variablesMutation = @{
    query = "mutation RetryException(`$`input: RetryExceptionInput!) { retryException(input: `$`input) { success errors { message } } }"
    variables = @{
        input = @{
            transactionId = $transactionId
            reason = "Testing with variables"
            priority = "NORMAL"
        }
    }
} | ConvertTo-Json -Depth 3

Write-Host "Variables mutation:" -ForegroundColor Gray
Write-Host $variablesMutation -ForegroundColor White

try {
    $variablesResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $variablesMutation -TimeoutSec 10
    
    Write-Host "RAW VARIABLES RESPONSE:" -ForegroundColor Yellow
    Write-Host ($variablesResponse | ConvertTo-Json -Depth 5) -ForegroundColor White
} catch {
    Write-Host "❌ Variables mutation failed: $_" -ForegroundColor Red
}

Write-Host "`n🔍 RETRY INPUT DEBUG COMPLETE" -ForegroundColor Blue