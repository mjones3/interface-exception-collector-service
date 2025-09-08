# Test Both Exception and Retry Subscriptions
Write-Host "TESTING BOTH SUBSCRIPTIONS" -ForegroundColor Blue
Write-Host "==========================" -ForegroundColor Blue

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

# Create JavaScript listener for BOTH subscriptions
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];

console.log("=== DUAL SUBSCRIPTION LISTENER ===");
console.log("Connecting to: ws://localhost:8080/graphql");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

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
    
    console.log("📨 RAW MESSAGE:");
    console.log(JSON.stringify(message, null, 2));
    console.log("---");
    
    if (message.type === "connection_ack") {
        console.log("✅ Connection acknowledged");
        connectionEstablished = true;
        
        // Subscribe to BOTH exception and retry events
        console.log("📤 Starting exception subscription...");
        ws.send(JSON.stringify({
            id: "exception-sub",
            type: "start",
            payload: {
                query: "subscription { exceptionUpdated { eventType exception { transactionId } timestamp } }"
            }
        }));
        
        console.log("📤 Starting retry subscription...");
        ws.send(JSON.stringify({
            id: "retry-sub",
            type: "start",
            payload: {
                query: "subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }"
            }
        }));
        
    } else if (message.type === "next" && message.payload && message.payload.data) {
        
        // Check for exception events
        if (message.payload.data.exceptionUpdated) {
            exceptionEventsReceived++;
            const exceptionData = message.payload.data.exceptionUpdated;
            console.log("🚨 EXCEPTION EVENT #" + exceptionEventsReceived + ":");
            console.log("   Event Type:", exceptionData.eventType);
            console.log("   Transaction ID:", exceptionData.exception.transactionId);
            console.log("   Timestamp:", exceptionData.timestamp);
            console.log("");
        }
        
        // Check for retry events
        if (message.payload.data.retryStatusUpdated) {
            retryEventsReceived++;
            const retryData = message.payload.data.retryStatusUpdated;
            console.log("🔄 RETRY EVENT #" + retryEventsReceived + ":");
            console.log("   Transaction ID:", retryData.transactionId);
            console.log("   Attempt Number:", retryData.retryAttempt.attemptNumber);
            console.log("   Event Type:", retryData.eventType);
            console.log("   Timestamp:", retryData.timestamp);
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
    console.log("");
    console.log("📊 FINAL RESULTS:");
    console.log("   Connection Established:", connectionEstablished);
    console.log("   Exception Events Received:", exceptionEventsReceived);
    console.log("   Retry Events Received:", retryEventsReceived);
    
    if (connectionEstablished) {
        if (exceptionEventsReceived > 0) {
            console.log("✅ EXCEPTION SUBSCRIPTION: WORKING");
        }
        if (retryEventsReceived > 0) {
            console.log("✅ RETRY SUBSCRIPTION: WORKING");
        }
        if (exceptionEventsReceived === 0 && retryEventsReceived === 0) {
            console.log("⚠️ NO EVENTS RECEIVED ON EITHER SUBSCRIPTION");
        }
    } else {
        console.log("❌ CONNECTION FAILED");
    }
});

setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        console.log("⏰ Timeout - closing connection");
        ws.close();
    }
}, 20000);'

$jsContent | Out-File -FilePath "dual-subscription-listener.js" -Encoding UTF8

# Start listener
Write-Host "Starting dual subscription listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node dual-subscription-listener.js $token
} -ArgumentList $token

Write-Host "Listener started" -ForegroundColor Green
Start-Sleep -Seconds 3

# Trigger retry events
Write-Host "Triggering retry events..." -ForegroundColor Cyan
$eventTypes = @("INITIATED", "COMPLETED")
foreach ($eventType in $eventTypes) {
    Write-Host "Triggering $eventType..." -ForegroundColor Yellow
    try {
        $body = @{ eventType = $eventType } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
        Write-Host "✅ ${eventType}: $($response.message)" -ForegroundColor Green
    } catch {
        Write-Host "❌ ${eventType} failed: $_" -ForegroundColor Red
    }
    Start-Sleep -Seconds 3
}

# Wait and get results
Write-Host "Waiting for events..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Wait-Job -Job $listenerJob -Timeout 8 | Out-Null
$output = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host "DUAL SUBSCRIPTION OUTPUT:" -ForegroundColor Yellow
Write-Host "=========================" -ForegroundColor Yellow
if ($output) {
    $output | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No output received" -ForegroundColor Gray
}

# Cleanup
Remove-Item "dual-subscription-listener.js" -ErrorAction SilentlyContinue

Write-Host "DUAL SUBSCRIPTION TEST COMPLETED" -ForegroundColor Blue