# Comprehensive Retry Subscription Test with Raw Output
Write-Host "COMPREHENSIVE RETRY SUBSCRIPTION TEST" -ForegroundColor Blue
Write-Host "=====================================" -ForegroundColor Blue

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

# Create comprehensive JavaScript listener with detailed logging
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];

console.log("=== RETRY SUBSCRIPTION LISTENER ===");
console.log("Token length:", token.length);
console.log("Connecting to: ws://localhost:8080/graphql");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

let eventsReceived = 0;
let connectionEstablished = false;

ws.on("open", () => {
    console.log("✅ WebSocket connection opened");
    
    console.log("📤 Sending connection_init...");
    ws.send(JSON.stringify({
        type: "connection_init",
        payload: { Authorization: "Bearer " + token }
    }));
});

ws.on("message", (data) => {
    const message = JSON.parse(data.toString());
    
    console.log("📨 RAW MESSAGE RECEIVED:");
    console.log(JSON.stringify(message, null, 2));
    console.log("---");
    
    if (message.type === "connection_ack") {
        console.log("✅ Connection acknowledged - starting subscription");
        connectionEstablished = true;
        
        console.log("📤 Sending subscription request...");
        ws.send(JSON.stringify({
            id: "retry-subscription-test",
            type: "start",
            payload: {
                query: "subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }"
            }
        }));
        
        console.log("📡 Retry subscription request sent");
        
    } else if (message.type === "next" && message.payload && message.payload.data) {
        const retryData = message.payload.data.retryStatusUpdated;
        if (retryData) {
            eventsReceived++;
            console.log("🔄 RETRY EVENT #" + eventsReceived + " RECEIVED:");
            console.log("   Transaction ID:", retryData.transactionId);
            console.log("   Attempt Number:", retryData.retryAttempt.attemptNumber);
            console.log("   Event Type:", retryData.eventType);
            console.log("   Timestamp:", retryData.timestamp);
            console.log("");
        }
    } else if (message.type === "error") {
        console.log("❌ Subscription error:");
        console.log(JSON.stringify(message.payload, null, 2));
    } else if (message.type === "complete") {
        console.log("✅ Subscription completed");
    }
});

ws.on("error", (error) => {
    console.log("❌ WebSocket error:", error.message);
});

ws.on("close", (code, reason) => {
    console.log("🔌 WebSocket connection closed");
    console.log("   Code:", code);
    console.log("   Reason:", reason.toString());
    console.log("");
    console.log("📊 FINAL RESULTS:");
    console.log("   Connection Established:", connectionEstablished);
    console.log("   Retry Events Received:", eventsReceived);
    
    if (connectionEstablished && eventsReceived > 0) {
        console.log("✅ RETRY SUBSCRIPTION: WORKING");
    } else if (connectionEstablished && eventsReceived === 0) {
        console.log("⚠️ RETRY SUBSCRIPTION: CONNECTED BUT NO EVENTS");
    } else {
        console.log("❌ RETRY SUBSCRIPTION: CONNECTION FAILED");
    }
});

// Keep connection alive for 25 seconds
setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        console.log("⏰ Timeout reached - closing connection");
        ws.close();
    }
}, 25000);'

$jsContent | Out-File -FilePath "comprehensive-retry-listener.js" -Encoding UTF8

# Start listener in background
Write-Host "Starting comprehensive retry subscription listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node comprehensive-retry-listener.js $token
} -ArgumentList $token

Write-Host "Listener started (Job ID: $($listenerJob.Id))" -ForegroundColor Green

# Wait for connection to establish
Write-Host "Waiting for WebSocket connection..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Trigger retry events with detailed logging
Write-Host "Triggering retry events..." -ForegroundColor Cyan

$eventTypes = @("INITIATED", "IN_PROGRESS", "COMPLETED", "FAILED")
foreach ($eventType in $eventTypes) {
    Write-Host "Triggering $eventType event..." -ForegroundColor Yellow
    try {
        $body = @{ eventType = $eventType } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
        Write-Host "✅ $eventType response: $($response.message)" -ForegroundColor Green
        Write-Host "   Timestamp: $($response.timestamp)" -ForegroundColor Gray
    } catch {
        Write-Host "❌ $eventType event failed: $_" -ForegroundColor Red
    }
    Start-Sleep -Seconds 3
}

# Wait for all events to be processed
Write-Host "Waiting for all events to be processed..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check intermediate output
Write-Host "Checking intermediate listener output..." -ForegroundColor Cyan
$intermediateOutput = Receive-Job -Job $listenerJob -Keep
if ($intermediateOutput) {
    Write-Host "INTERMEDIATE OUTPUT:" -ForegroundColor Yellow
    Write-Host "===================" -ForegroundColor Yellow
    $intermediateOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
    Write-Host "===================" -ForegroundColor Yellow
}

# Wait for listener to complete
Write-Host "Waiting for listener to complete..." -ForegroundColor Cyan
Wait-Job -Job $listenerJob -Timeout 10 | Out-Null

$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host "FINAL COMPREHENSIVE OUTPUT:" -ForegroundColor Yellow
Write-Host "===========================" -ForegroundColor Yellow
if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No final output received" -ForegroundColor Gray
}
Write-Host "===========================" -ForegroundColor Yellow

# Cleanup
Remove-Item "comprehensive-retry-listener.js" -ErrorAction SilentlyContinue

Write-Host "COMPREHENSIVE RETRY SUBSCRIPTION TEST COMPLETED" -ForegroundColor Blue