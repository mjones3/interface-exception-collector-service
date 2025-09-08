# Test Actual Subscription Fields
Write-Host "TESTING ACTUAL SUBSCRIPTION FIELDS" -ForegroundColor Blue

$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test 1: Check if subscription fields are visible in introspection
Write-Host "1. Checking subscription fields in schema..." -ForegroundColor Cyan
$subFieldsQuery = @{
    query = '{ __schema { subscriptionType { fields { name args { name type { name } } } } } }'
} | ConvertTo-Json

try {
    $subFieldsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subFieldsQuery
    
    if ($subFieldsResponse.data.__schema.subscriptionType.fields) {
        Write-Host "✅ Subscription fields found:" -ForegroundColor Green
        $subFieldsResponse.data.__schema.subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name)" -ForegroundColor Gray
            if ($_.args) {
                $_.args | ForEach-Object {
                    Write-Host "    arg: $($_.name) ($($_.type.name))" -ForegroundColor DarkGray
                }
            }
        }
    } else {
        Write-Host "❌ No subscription fields found in introspection" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Subscription introspection failed: $_" -ForegroundColor Red
}

# Test 2: Try to execute subscription queries (will fail but show if fields exist)
Write-Host "`n2. Testing subscription queries..." -ForegroundColor Cyan

$subscriptionTests = @(
    @{ name = "testSubscription"; query = "subscription { testSubscription }" },
    @{ name = "exceptionUpdated"; query = "subscription { exceptionUpdated { eventType } }" },
    @{ name = "retryStatusUpdated"; query = "subscription { retryStatusUpdated { eventType } }" }
)

foreach ($test in $subscriptionTests) {
    $query = @{ query = $test.query } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $query -TimeoutSec 5
        
        if ($response.errors) {
            $errorMsg = $response.errors[0].message
            if ($errorMsg -like "*not defined*" -or $errorMsg -like "*cannot query*") {
                Write-Host "❌ $($test.name): Field not defined" -ForegroundColor Red
            } elseif ($errorMsg -like "*WebSocket*" -or $errorMsg -like "*subscription*") {
                Write-Host "✅ $($test.name): Field exists, needs WebSocket" -ForegroundColor Green
            } else {
                Write-Host "⚠️ $($test.name): Other error - $errorMsg" -ForegroundColor Yellow
            }
        } else {
            Write-Host "✅ $($test.name): Unexpected success - $($response.data | ConvertTo-Json -Compress)" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ $($test.name): Request failed - $_" -ForegroundColor Red
    }
}

Write-Host "`nSUBSCRIPTION FIELD TEST COMPLETE" -ForegroundColor Blue