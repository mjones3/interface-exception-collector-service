# Comprehensive Retry Subscription Test with Raw Output
Write-Host "🔄 RETRY SUBSCRIPTION TEST - RAW OUTPUT" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue

# Wait for application to start
Write-Host "⏳ Waiting for application to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Generate JWT token
Write-Host "🔑 Generating JWT token..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()

if (-not $token) {
    Write-Host "❌ Failed to generate JWT token" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "✅ JWT token generated successfully" -ForegroundColor Green

# Test GraphQL endpoint
Write-Host "🌐 Testing GraphQL endpoint..." -ForegroundColor Cyan
try {
    $testQuery = @{
        query = "{ __schema { queryType { name } } }"
    } | ConvertTo-Json

    $testResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $testQuery -TimeoutSec 10
    Write-Host "✅ GraphQL endpoint is working" -ForegroundColor Green
} catch {
    Write-Host "❌ GraphQL endpoint test failed: $_" -ForegroundColor Red
    exit 1
}

# Get an existing exception for testing
Write-Host "📋 Getting existing exception for testing..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 1 }) { edges { node { transactionId exceptionReason } } } }"
} | ConvertTo-Json

try {
    $exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
    
    if ($exceptionsResponse.data.exceptions.edges.Count -eq 0) {
        Write-Host "❌ No exceptions found in database" -ForegroundColor Red
        exit 1
    }
    
    $transactionId = $exceptionsResponse.data.exceptions.edges[0].node.transactionId
    $exceptionReason = $exceptionsResponse.data.exceptions.edges[0].node.exceptionReason
    
    Write-Host "✅ Using exception: $transactionId" -ForegroundColor Green
    Write-Host "   Reason: $exceptionReason" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get exceptions: $_" -ForegroundColor Red
    exit 1
}

# Create retry subscription listener
Write-Host "🎧 Creating retry subscription listener..." -ForegroundColor Cyan

$retryListenerScript = @"
const WebSocket = require('ws');

const token = process.argv[2];
const transactionId = process.argv[3];

console.log('🔗 Connecting to retry subscription WebSocket...');
console.log('📋 Transaction ID:', transactionId);

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': 'Bearer ' + token }
});

let retryEventsReceived = 0;
let connectionEstablished = false;

ws.on('open', () => {
    console.log('🔌 WebSocket connection opened');
    
    // Initialize connection
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: { Authorization: 'Bearer ' + token }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    
    console.log('📨 RAW MESSAGE RECEIVED:');
    console.log('========================');
    console.log(JSON.stringify(message, null, 2));
    console.log('========================');
    console.log('');
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged - starting retry subscription');
        connectionEstablished = true;
        
        // Start retry subscription
        ws.send(JSON.stringify({
            id: 'retry-subscription-test',
            type: 'start',
            payload: {
                query: 'subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }'
            }
        }));
        
        console.log('📡 Retry subscription started - waiting for events...');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        const retryData = message.payload.data.retryStatusUpdated;
        if (retryData) {
            retryEventsReceived++;
            console.log('🔄 RETRY EVENT #' + retryEventsReceived + ' RECEIVED:');
            console.log('   Transaction ID:', retryData.transactionId);
            console.log('   Attempt Number:', retryData.retryAttempt.attemptNumber);
            console.log('   Event Type:', retryData.eventType);
            console.log('   Timestamp:', retryData.timestamp);
            console.log('');
        }
    } else if (message.type === 'error') {
        console.log('❌ Subscription error:', JSON.stringify(message.payload, null, 2));
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket connection closed');
    console.log('   Code:', code);
    console.log('   Reason:', reason.toString());
    console.log('');
    console.log('📊 FINAL RESULTS:');
    console.log('   Connection Established:', connectionEstablished);
    console.log('   Retry Events Received:', retryEventsReceived);
    
    if (connectionEstablished && retryEventsReceived > 0) {
        console.log('✅ RETRY SUBSCRIPTION: WORKING');
    } else if (connectionEstablished && retryEventsReceived === 0) {
        console.log('⚠️ RETRY SUBSCRIPTION: CONNECTED BUT NO EVENTS');
    } else {
        console.log('❌ RETRY SUBSCRIPTION: CONNECTION FAILED');
    }
});

// Keep connection alive for 30 seconds
setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        console.log('⏰ Test timeout - closing connection');
        ws.close();
    }
}, 30000);
"@

$retryListenerScript | Out-File -FilePath "retry-subscription-listener.js" -Encoding UTF8

# Start the retry subscription listener in background
Write-Host "🚀 Starting retry subscription listener..." -ForegroundColor Cyan
$listenerJob = Start-Job -ScriptBlock {
    param($token, $transactionId)
    Set-Location $using:PWD
    node retry-subscription-listener.js $token $transactionId
} -ArgumentList $token, $transactionId

Write-Host "✅ Retry subscription listener started (Job ID: $($listenerJob.Id))" -ForegroundColor Green

# Wait for listener to establish connection
Write-Host "⏳ Waiting for subscription connection..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Trigger retry events
Write-Host "🎯 Triggering retry events..." -ForegroundColor Cyan

# Test 1: Single retry event
Write-Host "1️⃣ Triggering single INITIATED retry event..." -ForegroundColor Yellow
try {
    $singleRetryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body '{}' -ContentType "application/json"
    Write-Host "✅ Single retry event response: $($singleRetryResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Single retry event failed: $_" -ForegroundColor Red
}

Start-Sleep -Seconds 3

# Test 2: Different event types
$eventTypes = @("INITIATED", "IN_PROGRESS", "COMPLETED", "FAILED", "CANCELLED")
foreach ($eventType in $eventTypes) {
    Write-Host "2️⃣ Triggering $eventType event..." -ForegroundColor Yellow
    try {
        $eventResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/trigger/$transactionId" -Method Post -Headers $headers -Body "{}" -ContentType "application/json" -Body (@{eventType = $eventType} | ConvertTo-Json)
        Write-Host "✅ $eventType event triggered successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ $eventType event failed: $_" -ForegroundColor Red
    }
    Start-Sleep -Seconds 2
}

# Test 3: Retry sequence
Write-Host "3️⃣ Triggering retry sequence..." -ForegroundColor Yellow
try {
    $sequenceResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/retry/sequence/$transactionId" -Method Post -Headers $headers -Body '{}' -ContentType "application/json"
    Write-Host "✅ Retry sequence response: $($sequenceResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Retry sequence failed: $_" -ForegroundColor Red
}

# Wait for all events to be processed
Write-Host "⏳ Waiting for all retry events to be processed..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check listener output
Write-Host "📊 Checking retry subscription listener output..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Yellow

$listenerOutput = Receive-Job -Job $listenerJob -Keep
if ($listenerOutput) {
    $listenerOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "⚠️ No output from retry subscription listener yet..." -ForegroundColor Gray
}

# Wait for listener to complete or timeout
Write-Host "⏳ Waiting for listener to complete..." -ForegroundColor Cyan
Wait-Job -Job $listenerJob -Timeout 15 | Out-Null

$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host "📋 FINAL RETRY SUBSCRIPTION OUTPUT:" -ForegroundColor Yellow
Write-Host "===================================" -ForegroundColor Yellow

if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "❌ No final output from retry subscription listener" -ForegroundColor Red
}

Write-Host "===================================" -ForegroundColor Yellow

# Cleanup
Remove-Item "retry-subscription-listener.js" -ErrorAction SilentlyContinue

Write-Host "🔄 RETRY SUBSCRIPTION TEST COMPLETED" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue