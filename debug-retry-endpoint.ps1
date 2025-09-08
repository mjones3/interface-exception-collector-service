# Debug Retry Endpoint Test
Write-Host "DEBUG RETRY ENDPOINT TEST" -ForegroundColor Blue
Write-Host "=========================" -ForegroundColor Blue

# Generate JWT token
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Get an existing exception
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId } } } }"
} | ConvertTo-Json

$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId

Write-Host "Using transaction ID: $transactionId" -ForegroundColor Green

# Test the retry endpoint directly
Write-Host "Testing retry endpoint directly..." -ForegroundColor Cyan

try {
    $body = @{ eventType = "INITIATED" } | ConvertTo-Json
    Write-Host "Request body: $body" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body -Verbose
    
    Write-Host "Response received:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor White
    
} catch {
    Write-Host "Error occurred:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody" -ForegroundColor Red
    }
}

# Test GraphQL introspection to see available subscriptions
Write-Host "Checking available subscriptions..." -ForegroundColor Cyan

try {
    $introspectionQuery = @{
        query = "{ __schema { subscriptionType { fields { name description } } } }"
    } | ConvertTo-Json

    $introspectionResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $introspectionQuery
    
    Write-Host "Available subscriptions:" -ForegroundColor Green
    $introspectionResponse.data.__schema.subscriptionType.fields | ForEach-Object {
        Write-Host "  - $($_.name): $($_.description)" -ForegroundColor White
    }
    
} catch {
    Write-Host "Failed to get subscription info: $_" -ForegroundColor Red
}

Write-Host "DEBUG TEST COMPLETED" -ForegroundColor Blue