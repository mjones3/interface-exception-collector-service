# Test Dashboard Summary Subscription
Write-Host "📊 DASHBOARD SUMMARY SUBSCRIPTION TEST" -ForegroundColor Blue
Write-Host "======================================" -ForegroundColor Blue

# Check if application is running
Write-Host "🔍 Checking application status..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Application is not running!" -ForegroundColor Red
    exit 1
}

# Generate JWT token
Write-Host "🔑 Generating JWT token..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "✅ JWT token generated (length: $($token.Length))" -ForegroundColor Green

# Create dashboard subscription listener
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];

console.log("📊 DASHBOARD SUMMARY SUBSCRIPTION LISTENER");
console.log("==========================================");
console.log("🔗 Connecting to: ws://localhost:8080/graphql");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

let dashboardUpdatesReceived = 0;
let connectionEstablished = false;

ws.on("open", () => {
    console.log("✅ WebSocket connection opened");
    
    ws.send(JSON.stringify({
        type: "connection_init",
        payload: { Authorization: "Bearer " + token }
    }));
});

ws.on("message", (data) => {
    const message = JSON.parse(data.toString());
    
    console.log("📨 RAW MESSAGE RECEIVED:");
    console.log("========================");
    console.log(JSON.stringify(message, null, 2));
    console.log("========================");
    console.log("");
    
    if (message.type === "connection_ack") {
        console.log("✅ Connection acknowledged - starting dashboard subscription");
        connectionEstablished = true;
        
        ws.send(JSON.stringify({
            id: "dashboard-summary",
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
        
        console.log("📊 Dashboard subscription started - waiting for updates...");
        
    } else if (message.type === "next" && message.payload && message.payload.data) {
        
        if (message.payload.data.dashboardSummary) {
            dashboardUpdatesReceived++;
            const dashboard = message.payload.data.dashboardSummary;
            
            console.log("📊 DASHBOARD UPDATE #" + dashboardUpdatesReceived + ":");
            console.log("   🚨 Active Exceptions:", dashboard.activeExceptions);
            console.log("   📅 Today Exceptions:", dashboard.todayExceptions);
            console.log("   ❌ Failed Retries:", dashboard.failedRetries);
            console.log("   ✅ Successful Retries:", dashboard.successfulRetries);
            console.log("   🔄 Total Retries:", dashboard.totalRetries);
            console.log("   📈 Retry Success Rate:", dashboard.retrySuccessRate + "%");
            console.log("   🎯 API Success Rate:", dashboard.apiSuccessRate + "%");
            console.log("   📞 Total API Calls Today:", dashboard.totalApiCallsToday);
            console.log("   🕐 Last Updated:", dashboard.lastUpdated);
            console.log("");
        }
    } else if (message.type === "error") {
        console.log("❌ Subscription error:");
        console.log(JSON.stringify(message.payload, null, 2));
    }
});

ws.on("error", (error) => {
    console.log("❌ WebSocket error:", error.message);
});

ws.on("close", (code, reason) => {
    console.log("🔌 Connection closed");
    console.log("📊 FINAL RESULTS:");
    console.log("   Connection Established:", connectionEstablished);
    console.log("   Dashboard Updates Received:", dashboardUpdatesReceived);
    
    if (connectionEstablished && dashboardUpdatesReceived > 0) {
        console.log("✅ DASHBOARD SUBSCRIPTION: WORKING");
    } else if (connectionEstablished) {
        console.log("⚠️ DASHBOARD SUBSCRIPTION: CONNECTED BUT NO UPDATES");
    } else {
        console.log("❌ DASHBOARD SUBSCRIPTION: CONNECTION FAILED");
    }
});

// Keep connection alive for 60 seconds
setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        console.log("⏰ Test timeout - closing connection");
        ws.close();
    }
}, 60000);'

$jsContent | Out-File -FilePath "dashboard-listener.js" -Encoding UTF8

# Start dashboard listener
Write-Host "🚀 Starting dashboard subscription listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node dashboard-listener.js $token
} -ArgumentList $token

Write-Host "✅ Dashboard listener started (Job ID: $($listenerJob.Id))" -ForegroundColor Green
Write-Host ""
Write-Host "📊 The dashboard will show:" -ForegroundColor Yellow
Write-Host "   • Active exceptions (NEW, ACKNOWLEDGED)" -ForegroundColor Yellow
Write-Host "   • Today's exception count" -ForegroundColor Yellow
Write-Host "   • Failed vs successful retries" -ForegroundColor Yellow
Write-Host "   • Retry success rate percentage" -ForegroundColor Yellow
Write-Host "   • Overall API success rate" -ForegroundColor Yellow
Write-Host "   • Total API calls today" -ForegroundColor Yellow
Write-Host ""
Write-Host "🎯 To trigger updates, run retry events in another window:" -ForegroundColor Cyan
Write-Host "   powershell -File trigger-retry-test.ps1" -ForegroundColor Cyan
Write-Host ""

# Wait for connection
Start-Sleep -Seconds 5

# Get initial output
$output = Receive-Job -Job $listenerJob -Keep
if ($output) {
    Write-Host "INITIAL DASHBOARD OUTPUT:" -ForegroundColor Yellow
    Write-Host "========================" -ForegroundColor Yellow
    $output | ForEach-Object { Write-Host $_ -ForegroundColor White }
    Write-Host "========================" -ForegroundColor Yellow
}

Write-Host "⏳ Waiting for dashboard updates (60 seconds)..." -ForegroundColor Yellow
Write-Host "   Dashboard updates every 30 seconds automatically" -ForegroundColor Gray
Write-Host "   Or immediately when exceptions/retries occur" -ForegroundColor Gray

# Wait for completion
Wait-Job -Job $listenerJob -Timeout 65 | Out-Null
$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host ""
Write-Host "📊 FINAL DASHBOARD SUBSCRIPTION OUTPUT:" -ForegroundColor Yellow
Write-Host "=======================================" -ForegroundColor Yellow
if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No output received" -ForegroundColor Gray
}

# Cleanup
Remove-Item "dashboard-listener.js" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "📊 DASHBOARD SUBSCRIPTION TEST COMPLETED" -ForegroundColor Blue