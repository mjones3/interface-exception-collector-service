# Test WebSocket Subscription
Write-Host "TESTING WEBSOCKET SUBSCRIPTION" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()

Write-Host "1. Testing WebSocket endpoint availability..." -ForegroundColor Cyan
try {
    # Test if WebSocket endpoint exists (will fail but show if endpoint exists)
    $wsTest = Invoke-WebRequest -Uri "http://localhost:8080/subscriptions" -Method Get -TimeoutSec 5
    Write-Host "WebSocket response: $($wsTest.StatusCode)" -ForegroundColor Gray
} catch {
    if ($_.Exception.Message -like "*426*" -or $_.Exception.Message -like "*Upgrade*") {
        Write-Host "✅ WebSocket endpoint exists (needs upgrade)" -ForegroundColor Green
    } elseif ($_.Exception.Message -like "*404*") {
        Write-Host "❌ WebSocket endpoint not found" -ForegroundColor Red
    } else {
        Write-Host "⚠️ WebSocket endpoint response: $_" -ForegroundColor Yellow
    }
}

Write-Host "2. Creating WebSocket test with Node.js..." -ForegroundColor Cyan

# Create a Node.js script to test WebSocket subscription
$nodeScript = @"
const WebSocket = require('ws');

const token = '$token';
const wsUrl = 'ws://localhost:8080/subscriptions';

console.log('🔌 Connecting to WebSocket:', wsUrl);

const ws = new WebSocket(wsUrl, 'graphql-ws', {
    headers: {
        'Authorization': `Bearer `${token}`
    }
});

ws.on('open', () => {
    console.log('✅ WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: {
            Authorization: `Bearer `${token}`
        }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    console.log('📨 Received:', JSON.stringify(message, null, 2));
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged, starting subscription...');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'test-sub',
            type: 'start',
            payload: {
                query: 'subscription { exceptionEvents { eventType exception { transactionId } } }'
            }
        }));
        
        // Close after 5 seconds
        setTimeout(() => {
            console.log('⏰ Closing connection after 5 seconds');
            ws.close();
        }, 5000);
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket closed:', code, reason.toString());
});
"@

# Write the Node.js script
$nodeScript | Out-File -FilePath "test-websocket.js" -Encoding UTF8

Write-Host "3. Running WebSocket test..." -ForegroundColor Cyan
try {
    node test-websocket.js
} catch {
    Write-Host "❌ Node.js WebSocket test failed: $_" -ForegroundColor Red
}

Write-Host "WEBSOCKET TEST COMPLETE" -ForegroundColor Blue