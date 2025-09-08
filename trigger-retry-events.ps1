# Trigger Retry Events and Test Subscription
Write-Host "🔄 TRIGGERING RETRY EVENTS AND TESTING SUBSCRIPTION" -ForegroundColor Blue
Write-Host "===================================================" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "✅ JWT token generated" -ForegroundColor Green

# Get existing exceptions to retry
Write-Host "1. Getting existing exceptions..." -ForegroundColor Cyan
$exceptionsQuery = @{
    query = "{ exceptions(pagination: { first: 3 }) { edges { node { transactionId status retryable retryCount } } } }"
} | ConvertTo-Json

$exceptionsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $exceptionsQuery
$exceptions = $exceptionsResponse.data.exceptions.edges

Write-Host "Found $($exceptions.Count) exceptions:" -ForegroundColor Gray
foreach ($edge in $exceptions) {
    $ex = $edge.node
    Write-Host "  - $($ex.transactionId): Status=$($ex.status), Retryable=$($ex.retryable), RetryCount=$($ex.retryCount)" -ForegroundColor White
}

# Start retry subscription listener in background
Write-Host "`n2. Starting retry subscription listener..." -ForegroundColor Cyan

# Create background retry listener script
$retryListenerScript = @"
const WebSocket = require('ws');

const token = process.argv[2];
const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': 'Bearer ' + token }
});

let retryEventsReceived = 0;

ws.on('open', () => {
    console.log('🔗 Retry listener connected');
    
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: { Authorization: 'Bearer ' + token }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    
    if (message.type === 'connection_ack') {
        console.log('📡 Starting retry subscription...');
        
        ws.send(JSON.stringify({
            id: 'retry-listener',
            type: 'start',
            payload: {
                query: 'subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }'
            }
        }));
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        const retryData = message.payload.data.retryStatusUpdated;
        if (retryData) {
            retryEventsReceived++;
            console.log('🔄 RETRY EVENT RECEIVED:');
            console.log('   Transaction ID:', retryData.transactionId);
            console.log('   Attempt Number:', retryData.retryAttempt.attemptNumber);
            console.log('   Event Type:', retryData.eventType);
            console.log('   Timestamp:', retryData.timestamp);
            console.log('');
        }
    }
});

ws.on('error', (error) => {
    console.log('❌ Retry listener error:', error.message);
});

ws.on('close', () => {
    console.log('🔌 Retry listener closed');
    console.log('📊 Total retry events received:', retryEventsReceived);
});

// Keep alive for 30 seconds
setTimeout(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
}, 30000);
"@

$retryListenerScript | Out-File -FilePath "retry-listener.js" -Encoding UTF8

# Start the listener in background
$listenerJob = Start-Job -ScriptBlock {
    param($token)
    Set-Location $using:PWD
    node retry-listener.js $token
} -ArgumentList $token

Write-Host "✅ Retry listener started (Job ID: $($listenerJob.Id))" -ForegroundColor Green

# Wait for listener to connect
Start-Sleep -Seconds 3

# Trigger retry events
Write-Host "`n3. Triggering retry events..." -ForegroundColor Cyan

foreach ($edge in $exceptions) {
    $ex = $edge.node
    $transactionId = $ex.transactionId
    
    Write-Host "Triggering retry for: $transactionId" -ForegroundColor Yellow
    
    # Create proper retry mutation with correct input format
    $retryMutation = @{
        query = "mutation { retryException(input: { transactionId: `"$transactionId`", reason: `"Testing retry subscription`", priority: NORMAL }) { success exception { transactionId retryCount } errors { message } } }"
    } | ConvertTo-Json
    
    try {
        $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $retryMutation -TimeoutSec 10
        
        if ($retryResponse.errors) {
            Write-Host "❌ Retry error: $($retryResponse.errors[0].message)" -ForegroundColor Red
        } elseif ($retryResponse.data.retryException.errors -and $retryResponse.data.retryException.errors.Count -gt 0) {
            Write-Host "❌ Retry business error: $($retryResponse.data.retryException.errors[0].message)" -ForegroundColor Red
        } else {
            $success = $retryResponse.data.retryException.success
            Write-Host "✅ Retry triggered: Success=$success" -ForegroundColor Green
            
            if ($retryResponse.data.retryException.exception) {
                $newRetryCount = $retryResponse.data.retryException.exception.retryCount
                Write-Host "   New retry count: $newRetryCount" -ForegroundColor Gray
            }
        }
    } catch {
        Write-Host "❌ Retry request failed: $_" -ForegroundColor Red
    }
    
    # Wait between retries
    Start-Sleep -Seconds 2
}

# Wait for events to be processed
Write-Host "`n4. Waiting for retry events to be processed..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# Check listener output
Write-Host "`n5. Checking retry listener output..." -ForegroundColor Cyan
Write-Host "RAW RETRY LISTENER OUTPUT:" -ForegroundColor Yellow
Write-Host "=========================" -ForegroundColor Yellow

$listenerOutput = Receive-Job -Job $listenerJob -Keep
if ($listenerOutput) {
    $listenerOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No output from retry listener yet..." -ForegroundColor Gray
}

# Wait for listener to complete
Write-Host "`n6. Waiting for listener to complete..." -ForegroundColor Cyan
Wait-Job -Job $listenerJob -Timeout 15 | Out-Null

$finalOutput = Receive-Job -Job $listenerJob
Remove-Job -Job $listenerJob

Write-Host "`nFINAL RETRY LISTENER OUTPUT:" -ForegroundColor Yellow
Write-Host "============================" -ForegroundColor Yellow

if ($finalOutput) {
    $finalOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No final output from retry listener" -ForegroundColor Gray
}

Write-Host "============================" -ForegroundColor Yellow

# Cleanup
Remove-Item "retry-listener.js" -ErrorAction SilentlyContinue

Write-Host "`n🔄 RETRY EVENT TRIGGER TEST COMPLETE" -ForegroundColor Blue