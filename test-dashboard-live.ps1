# Live Dashboard Service Test - Shows Raw Events
Write-Host "📊 LIVE DASHBOARD SERVICE TEST" -ForegroundColor Blue
Write-Host "==============================" -ForegroundColor Blue

# Generate JWT and setup
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Get transaction ID
$exceptionsQuery = @{ query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId } } } }" } | ConvertTo-Json
$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId

Write-Host "✅ Using transaction ID: $transactionId" -ForegroundColor Green

# Simple dashboard listener focused on showing the service
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];

console.log("📊 DASHBOARD SERVICE - RAW OUTPUT");
console.log("=================================");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

let dashboardCount = 0;

ws.on("open", () => {
    console.log("✅ Connected - starting dashboard subscription");
    
    ws.send(JSON.stringify({
        type: "connection_init",
        payload: { Authorization: "Bearer " + token }
    }));
});

ws.on("message", (data) => {
    const message = JSON.parse(data.toString());
    
    if (message.type === "connection_ack") {
        console.log("✅ Authenticated - requesting dashboard");
        
        ws.send(JSON.stringify({
            id: "dashboard",
            type: "start",
            payload: {
                query: `subscription { 
                    dashboardSummary { 
                        activeExceptions 
                        todayExceptions 
                        failedRetries 
                        successfulRetries 
                        totalRetries 
                        retrySuccessRate 
                        apiSuccessRate 
                        totalApiCallsToday 
                        lastUpdated 
                    } 
                }`
            }
        }));
        
    } else if (message.type === "next" && message.payload?.data?.dashboardSummary) {
        dashboardCount++;
        const d = message.payload.data.dashboardSummary;
        
        console.log("");
        console.log("📊 DASHBOARD EVENT #" + dashboardCount + " - RAW DATA:");
        console.log("================================================");
        console.log(JSON.stringify(message, null, 2));
        console.log("================================================");
        console.log("");
        console.log("📊 PARSED DASHBOARD METRICS:");
        console.log("   🚨 Active Exceptions: " + d.activeExceptions);
        console.log("   📅 Today Total: " + d.todayExceptions);
        console.log("   ❌ Failed Retries: " + d.failedRetries);
        console.log("   ✅ Success Retries: " + d.successfulRetries);
        console.log("   📈 Retry Rate: " + d.retrySuccessRate + "%");
        console.log("   🎯 API Rate: " + d.apiSuccessRate + "%");
        console.log("   📞 API Calls: " + d.totalApiCallsToday);
        console.log("   🕐 Updated: " + d.lastUpdated);
        console.log("");
        
    } else {
        console.log("📨 Other message:", message.type);
    }
});

ws.on("close", () => {
    console.log("🔌 Connection closed");
    console.log("📊 Dashboard events received: " + dashboardCount);
    if (dashboardCount > 0) {
        console.log("✅ DASHBOARD SERVICE: WORKING");
    } else {
        console.log("⚠️ DASHBOARD SERVICE: NO EVENTS");
    }
});

setTimeout(() => ws.close(), 45000);'

$jsContent | Out-File -FilePath "dashboard-test.js" -Encoding UTF8

# Start listener
Write-Host "🚀 Starting dashboard service test..." -ForegroundColor Cyan
$job = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node dashboard-test.js $token
} -ArgumentList $token

# Wait for initial connection
Start-Sleep -Seconds 3

# Trigger an event to show dashboard update
Write-Host "🎯 Triggering retry event to demonstrate dashboard update..." -ForegroundColor Yellow
try {
    $body = @{ eventType = "INITIATED" } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
    Write-Host "✅ Event triggered: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Event failed: $_" -ForegroundColor Red
}

# Show output as it comes
Write-Host "📊 DASHBOARD SERVICE OUTPUT:" -ForegroundColor Yellow
Write-Host "============================" -ForegroundColor Yellow

# Monitor output in real-time
$timeout = 40
$elapsed = 0
while ($job.State -eq "Running" -and $elapsed -lt $timeout) {
    $output = Receive-Job -Job $job -Keep
    if ($output) {
        $output | ForEach-Object { Write-Host $_ -ForegroundColor White }
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

# Get final output
Wait-Job -Job $job -Timeout 5 | Out-Null
$finalOutput = Receive-Job -Job $job
Remove-Job -Job $job

if ($finalOutput) {
    Write-Host ""
    Write-Host "FINAL OUTPUT:" -ForegroundColor Yellow
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
}

# Cleanup
Remove-Item "dashboard-test.js" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "📊 DASHBOARD SERVICE TEST COMPLETED" -ForegroundColor Blue