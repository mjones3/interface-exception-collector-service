# Trigger Retry Events for Testing
Write-Host "🎯 RETRY EVENT TRIGGER" -ForegroundColor Blue
Write-Host "=====================" -ForegroundColor Blue

# Generate JWT token
Write-Host "🔑 Generating JWT token..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Get an existing exception
Write-Host "📋 Getting existing exception..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId } } } }"
} | ConvertTo-Json

$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId

Write-Host "✅ Using transaction ID: $transactionId" -ForegroundColor Green
Write-Host ""

# Interactive menu for triggering events
do {
    Write-Host "🔄 RETRY EVENT TRIGGER MENU" -ForegroundColor Yellow
    Write-Host "===========================" -ForegroundColor Yellow
    Write-Host "1. Trigger INITIATED event" -ForegroundColor White
    Write-Host "2. Trigger IN_PROGRESS event" -ForegroundColor White
    Write-Host "3. Trigger COMPLETED event" -ForegroundColor White
    Write-Host "4. Trigger FAILED event" -ForegroundColor White
    Write-Host "5. Trigger CANCELLED event" -ForegroundColor White
    Write-Host "6. Trigger event sequence (INITIATED → IN_PROGRESS → COMPLETED)" -ForegroundColor White
    Write-Host "7. Exit" -ForegroundColor White
    Write-Host ""
    
    $choice = Read-Host "Select option (1-7)"
    
    switch ($choice) {
        "1" { $eventType = "INITIATED" }
        "2" { $eventType = "IN_PROGRESS" }
        "3" { $eventType = "COMPLETED" }
        "4" { $eventType = "FAILED" }
        "5" { $eventType = "CANCELLED" }
        "6" { 
            Write-Host "🔄 Triggering event sequence..." -ForegroundColor Cyan
            try {
                $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/sequence/$transactionId" -Method Post -Headers $headers -Body '{}'
                Write-Host "✅ Sequence triggered: $($response.message)" -ForegroundColor Green
            } catch {
                Write-Host "❌ Sequence failed: $_" -ForegroundColor Red
            }
            Write-Host ""
            continue
        }
        "7" { 
            Write-Host "👋 Exiting..." -ForegroundColor Green
            exit 
        }
        default { 
            Write-Host "❌ Invalid choice. Please select 1-7." -ForegroundColor Red
            Write-Host ""
            continue 
        }
    }
    
    if ($choice -in @("1", "2", "3", "4", "5")) {
        Write-Host "🎯 Triggering $eventType event..." -ForegroundColor Cyan
        try {
            $body = @{ eventType = $eventType } | ConvertTo-Json
            $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
            
            Write-Host "✅ $eventType event triggered successfully!" -ForegroundColor Green
            Write-Host "   Response: $($response.message)" -ForegroundColor Gray
            Write-Host "   Timestamp: $($response.timestamp)" -ForegroundColor Gray
            
        } catch {
            Write-Host "❌ $eventType event failed: $_" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Press any key to continue..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Write-Host ""
    
} while ($true)