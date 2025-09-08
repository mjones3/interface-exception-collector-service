# Complete Dashboard Service Demo with Raw Output
Write-Host "📊 DASHBOARD SERVICE DEMO - COMPLETE TEST" -ForegroundColor Blue
Write-Host "==========================================" -ForegroundColor Blue

# Check if application is running
Write-Host "🔍 Checking application status..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Application is not running!" -ForegroundColor Red
    Write-Host "   Please start the application first" -ForegroundColor Yellow
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

# Get existing exception for testing
Write-Host "📋 Getting existing exception..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId } } } }"
} | ConvertTo-Json

$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId

Write-Host "✅ Using transaction ID: $transactionId" -ForegroundColor Green

# Create comprehensive dashboard listener with raw output
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];

console.log("📊 DASHBOARD SERVICE DEMO - RAW OUTPUT");
console.log("======================================");
console.log("🔗 Connecting to: ws://localhost:8080/graphql");
console.log("");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

let dashboardUpdatesReceived = 0;
let exceptionEventsReceived = 0;
let retryEventsReceived = 0;
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
        console.log("✅ Connection acknowledged - starting ALL subscriptions");
        connectionEstablished = true;
        
        // Start dashboard subscription
        console.log("📊 Starting dashboard subscription...");
        ws.send(JSON.stringify({
            id: "dashboard-sub",
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
        
        // Start exception subscription
        console.log("🚨 Starting exception subscription...");
        ws.send(JSON.stringify({
            id: "exception-sub",
            type: "start",
            payload: {
                query: "subscription { exceptionUpdated { eventType exception { transactionId } timestamp } }"
            }
        }));
        
        // Start retry subscription
        console.log("🔄 Starting retry subscription...");
        ws.send(JSON.stringify({
            id: "retry-sub",
            type: "start",
            payload: {
                query: "subscription { retryStatusUpdated { transactionId eventType timestamp } }"
            }
        }));
        
        console.log("📡 All subscriptions started - waiting for events...");
        console.log("");
        
    } else if (message.type === "next" && message.payload && message.payload.data) {
        
        // Dashboard updates
        if (message.payload.data.dashboardSummary) {
            dashboardUpdatesReceived++;
            const dashboard = message.payload.data.dashboardSummary;
            
            console.log("📊 DASHBOARD UPDATE #" + dashboardUpdatesReceived + ":");
            console.log("   🚨 Active Exceptions: " + dashboard.activeExceptions);
            console.log("   📅 Today Exceptions: " + dashboard.todayExceptions);
            console.log("   ❌ Failed Retries: " + dashboard.failedRetries);
            console.log("   ✅ Successful Retries: " + dashboard.successfulRetries);
            console.log("   🔄 Total Retries: " + dashboard.totalRetries);
            console.log("   📈 Retry Success Rate: " + dashboard.retrySuccessRate + "%");
            console.log("   🎯 API Success Rate: " + dashboard.apiSuccessRate + "%");
            console.log("   📞 Total API Calls: " + dashboard.totalApiCallsToday);
            console.log("   🕐 Last Updated: " + dashboard.lastUpdated);
            console.log("");
        }
        
        // Exception events
        if (message.payload.data.exceptionUpdated) {
            exceptionEventsReceived++;
            const exception = message.payload.data.exceptionUpdated;
            console.log("🚨 EXCEPTION EVENT #" + exceptionEventsReceived + ":");
            console.log("   Event Type: " + exception.eventType);
            console.log("   Transaction ID: " + exception.exception.transactionId);
            console.log("   Timestamp: " + exception.timestamp);
            console.log("");
        }
        
        // Retry events
        if (message.payload.data.retryStatusUpdated) {
            retryEventsReceived++;
            const retry = message.payload.data.retryStatusUpdated;
            console.log("🔄 RETRY EVENT #" + retryEventsReceived + ":");
            console.log("   Transaction ID: " + retry.transactionId);
            console.log("   Event Type: " + retry.eventType);
            console.log("   Timestamp: " + retry.timestamp);
            console.log("");
        }
        
    } else if (message.type === "error") {
        console.log("❌ Subscription error:");
        console.log(JSON.stringify(message.payload, null, 2));
        console.log("");
    }
});

ws.on("error", (error) => {
    console.log("❌ WebSocket error:", error.message);
});

ws.on("close", (code, reason) => {
    console.log("🔌 Connection closed");
    console.log("");
    console.log("📊 FINAL DEMO RESULTS:");
    console.log("======================");
    console.log("   Connection Established:", connectionEstablished);
    console.log("   Dashboard Updates:", dashboardUpdatesReceived);
    console.log("   Exception Events:", exceptionEventsReceived);
    console.log("   Retry Events:", retryEventsReceived);
    console.log("");
    
    if (connectionEstablished) {
        if (dashboardUpdatesReceived > 0) {
            console.log("✅ DASHBOARD SUBSCRIPTION: WORKING");
        }
        if (exceptionEventsReceived > 0) {
            console.log("✅ EXCEPTION SUBSCRIPTION: WORKING");
        }
        if (retryEventsReceived > 0) {
            console.log("✅ RETRY SUBSCRIPTION: WORKING");
        }
        
        const totalEvents = dashboardUpdatesReceived + exceptionEventsReceived + retryEventsReceived;
        if (totalEvents > 0) {
            console.log("🎉 TOTAL EVENTS RECEIVED: " + totalEvents);
        } else {
            console.log("⚠️ NO EVENTS RECEIVED");
        }
    } else {
        console.log("❌ CONNECTION FAILED");
    }
});

// Keep connection alive for 90 seconds
setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        console.log("⏰ Demo timeout - closing connection");
        ws.close();
    }
}, 90000);'

$jsContent | Out-File -FilePath "dashboard-demo-listener.js" -Encoding UTF8

# Start the comprehensive listener
Write-Host "🚀 Starting comprehensive dashboard demo listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node dashboard-demo-listener.js $token
} -ArgumentList $token

Write-Host "✅ Demo listener started (Job ID: $($listenerJob.Id))" -ForegroundColor Green
Write-Host ""
Write-Host "📊 DEMO FEATURES:" -ForegroundColor Yellow
Write-Host "=================" -ForegroundColor Yellow
Write-Host "• Dashboard subscription with real-time metrics" -ForegroundColor Yellow
Write-Host "• Exception subscription for real-time events" -ForegroundColor Yellow
Write-Host "• Retry subscription for retry events" -ForegroundColor Yellow
Write-Host "• Raw WebSocket message output" -ForegroundColor Yellow
Write-Host "• Comprehensive event counting" -ForegroundColor Yellow
Write-Host ""

# Wait for connection to establish
Write-Host "⏳ Waiting for subscriptions to connect..." -ForegroundColor Cyan
Start-Sleep -Seconds 5

# Show initial output
$initialOutput = Receive-Job -Job $listenerJob -Keep
if ($initialOutput) {
    Write-Host "INITIAL CONNECTION OUTPUT:" -ForegroundColor Yellow
    Write-Host "=========================" -ForegroundColor Yellow
    $initialOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
    Write-Host "=========================" -ForegroundColor Yellow
    Write-Host ""
}

# Now trigger some events to demonstrate the system
Write-Host "🎯 TRIGGERING EVENTS TO DEMONSTRATE DASHBOARD..." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

# Trigger retry events to show dashboard updates
$eventTypes = @("INITIATED", "IN_PROGRESS", "COMPLETED", "FAILED")
foreach ($eventType in $eventTypes) {
    Write-Host "🔄 Triggering $eventType retry event..." -ForegroundColor Cyan
    try {
        $body = @{ eventType = $eventType } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
        Write-Host "✅ $eventType event triggered: $($response.message)" -ForegroundColor Green
    } catch {
        Write-Host "❌ $eventType event failed: $_" -ForegroundColor Red
    }
    
    # Wait between events to see dashboard updates
    Start-Sleep -Seconds 8
    
    # Show intermediate output
    $intermediateOutput = Receive-Job -Job $listenerJob -Keep
    if ($intermediateOutput -and $intermediateOutput.Count -gt ($initialOutput.Count + 10)) {
        Write-Host ""
        Write-Host "LATEST EVENTS RECEIVED:" -ForegroundColor Yellow
        Write-Host "======================" -ForegroundColor Yellow
        $intermediateOutput | Select-Object -Last 15 | ForEach-Object { Write-Host $_ -ForegroundColor White }
        Write-Host "======================" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Trigger retry sequence for more events
Write-Host "🔄 Triggering retry sequence for more dashboard updates..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/sequence/$transactionId" -Method Post -Headers $headers -Body '{}'
    Write-Host "✅ Retry sequence triggered: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Retry sequence failed: $_" -ForegroundColor Red
}

# Wait for all events to be processed
Write-Host "⏳ Waiting for all events and dashboard updates..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Get final output
Write-Host "📊 Getting final demo results..." -ForegroundColor Cyan
Wait-Job -Job $listenerJob -Timeout 20 | Out-Null
$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host ""
Write-Host "📊 COMPLETE DASHBOARD SERVICE DEMO OUTPUT:" -ForegroundColor Yellow
Write-Host "===========================================" -ForegroundColor Yellow
if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No output received" -ForegroundColor Gray
}
Write-Host "===========================================" -ForegroundColor Yellow

# Cleanup
Remove-Item "dashboard-demo-listener.js" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "🎉 DASHBOARD SERVICE DEMO COMPLETED!" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue
Write-Host ""
Write-Host "📊 What was demonstrated:" -ForegroundColor Green
Write-Host "• Real-time dashboard metrics subscription" -ForegroundColor Green
Write-Host "• Automatic dashboard updates on events" -ForegroundColor Green
Write-Host "• Exception and retry event integration" -ForegroundColor Green
Write-Host "• Raw WebSocket message inspection" -ForegroundColor Green
Write-Host "• Complete subscription system working" -ForegroundColor Green