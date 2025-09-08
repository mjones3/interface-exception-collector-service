# Simple Retry Subscription Test
Write-Host "RETRY SUBSCRIPTION TEST" -ForegroundColor Blue
Write-Host "=======================" -ForegroundColor Blue

# Check if application is running
Write-Host "Checking application status..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "Application is running" -ForegroundColor Green
} catch {
    Write-Host "Application is not running. Please start it first." -ForegroundColor Red
    exit 1
}

# Generate JWT token
Write-Host "Generating JWT token..." -ForegroundColor Cyan
try {
    $token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
    if (-not $token) {
        throw "No token generated"
    }
    Write-Host "JWT token generated successfully" -ForegroundColor Green
} catch {
    Write-Host "Failed to generate JWT token" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Get an existing exception
Write-Host "Getting existing exception..." -ForegroundColor Cyan
try {
    $exceptionsQuery = @{
        query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId } } } }"
    } | ConvertTo-Json

    $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
    
    if ($exceptionsResponse.data.exceptions.edges.Count -eq 0) {
        Write-Host "No exceptions found in database" -ForegroundColor Red
        exit 1
    }
    
    $transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId
    Write-Host "Using exception: $transactionId" -ForegroundColor Green
} catch {
    Write-Host "Failed to get exceptions" -ForegroundColor Red
    exit 1
}

# Create simple JavaScript listener
$jsContent = 'const WebSocket = require("ws");
const token = process.argv[2];
console.log("Connecting to retry subscription...");

const ws = new WebSocket("ws://localhost:8080/graphql", {
    headers: { "Authorization": "Bearer " + token }
});

let eventsReceived = 0;

ws.on("open", () => {
    console.log("WebSocket connected");
    ws.send(JSON.stringify({
        type: "connection_init",
        payload: { Authorization: "Bearer " + token }
    }));
});

ws.on("message", (data) => {
    const message = JSON.parse(data.toString());
    
    if (message.type === "connection_ack") {
        console.log("Connection acknowledged");
        ws.send(JSON.stringify({
            id: "retry-test",
            type: "start",
            payload: {
                query: "subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }"
            }
        }));
        console.log("Retry subscription started");
    } else if (message.type === "next" && message.payload && message.payload.data) {
        const retryData = message.payload.data.retryStatusUpdated;
        if (retryData) {
            eventsReceived++;
            console.log("RETRY EVENT RECEIVED:");
            console.log("  Transaction ID:", retryData.transactionId);
            console.log("  Attempt Number:", retryData.retryAttempt.attemptNumber);
            console.log("  Event Type:", retryData.eventType);
            console.log("  Timestamp:", retryData.timestamp);
            console.log("");
        }
    }
});

ws.on("error", (error) => {
    console.log("WebSocket error:", error.message);
});

ws.on("close", () => {
    console.log("Connection closed");
    console.log("Total events received:", eventsReceived);
    if (eventsReceived > 0) {
        console.log("RETRY SUBSCRIPTION: WORKING");
    } else {
        console.log("RETRY SUBSCRIPTION: NO EVENTS RECEIVED");
    }
});

setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
}, 15000);'

$jsContent | Out-File -FilePath "retry-listener.js" -Encoding UTF8

# Start listener in background
Write-Host "Starting retry subscription listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node retry-listener.js $token
} -ArgumentList $token

Write-Host "Listener started" -ForegroundColor Green

# Wait for connection
Start-Sleep -Seconds 3

# Trigger retry events
Write-Host "Triggering retry events..." -ForegroundColor Cyan

$eventTypes = @("INITIATED", "IN_PROGRESS", "COMPLETED")
foreach ($eventType in $eventTypes) {
    Write-Host "Triggering $eventType event..." -ForegroundColor Yellow
    try {
        $body = @{ eventType = $eventType } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body $body
        Write-Host "$eventType event triggered successfully" -ForegroundColor Green
    } catch {
        Write-Host "$eventType event failed" -ForegroundColor Red
    }
    Start-Sleep -Seconds 2
}

# Wait for events to be processed
Write-Host "Waiting for events to be processed..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# Get listener output
Write-Host "Checking listener output..." -ForegroundColor Cyan
$output = Receive-Job -Job $listenerJob -Keep
if ($output) {
    $output | ForEach-Object { Write-Host $_ -ForegroundColor White }
}

# Wait for completion
Wait-Job -Job $listenerJob -Timeout 5 | Out-Null
$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host "FINAL OUTPUT:" -ForegroundColor Yellow
Write-Host "=============" -ForegroundColor Yellow
if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No output received" -ForegroundColor Gray
}

# Cleanup
Remove-Item "retry-listener.js" -ErrorAction SilentlyContinue

Write-Host "RETRY SUBSCRIPTION TEST COMPLETED" -ForegroundColor Blue