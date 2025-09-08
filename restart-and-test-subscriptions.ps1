# Restart application and test subscriptions

Write-Host "Restarting Application and Testing Subscriptions" -ForegroundColor Blue
Write-Host "===============================================" -ForegroundColor Blue

# Kill existing Java processes
Write-Host "`nStopping existing Java processes..." -ForegroundColor Cyan
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 3

# Start the application
Write-Host "`nStarting application..." -ForegroundColor Cyan
Write-Host "Run this command in another terminal:" -ForegroundColor Yellow
Write-Host "cd interface-exception-collector && mvn spring-boot:run" -ForegroundColor White

Write-Host "`nWaiting for application to start..." -ForegroundColor Cyan
$maxWait = 60
$waited = 0

while ($waited -lt $maxWait) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2
        if ($health.status -eq "UP") {
            Write-Host "✅ Application started successfully!" -ForegroundColor Green
            break
        }
    } catch {
        # Still starting up
    }
    
    Start-Sleep -Seconds 2
    $waited += 2
    Write-Host "." -NoNewline -ForegroundColor Gray
}

if ($waited -ge $maxWait) {
    Write-Host "`n❌ Application failed to start within $maxWait seconds" -ForegroundColor Red
    exit 1
}

Write-Host "`n`nTesting subscription schema..." -ForegroundColor Cyan

# Generate token and test
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test subscription schema
$schemaQuery = @{
    query = "{ __schema { subscriptionType { name fields { name } } } }"
} | ConvertTo-Json

try {
    $schemaResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $schemaQuery -TimeoutSec 10
    
    if ($schemaResponse.data.__schema.subscriptionType) {
        Write-Host "✅ Subscription type found!" -ForegroundColor Green
        Write-Host "Available subscription fields:" -ForegroundColor Gray
        $schemaResponse.data.__schema.subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name)" -ForegroundColor Gray
        }
        
        # Test if our specific subscription exists
        $exceptionUpdated = $schemaResponse.data.__schema.subscriptionType.fields | Where-Object { $_.name -eq "exceptionUpdated" }
        if ($exceptionUpdated) {
            Write-Host "`n🎉 SUCCESS! exceptionUpdated subscription is registered!" -ForegroundColor Green
            Write-Host "The @SubscriptionMapping fix worked!" -ForegroundColor Green
        } else {
            Write-Host "`n⚠️ exceptionUpdated subscription not found in fields" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ No subscription type found in schema" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Schema test failed: $_" -ForegroundColor Red
}

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. If subscription is registered: Test WebSocket connection" -ForegroundColor White
Write-Host "2. If not registered: Check application startup logs for errors" -ForegroundColor White
Write-Host "3. Look for component scanning or annotation processing issues" -ForegroundColor White