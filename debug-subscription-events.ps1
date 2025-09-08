#!/usr/bin/env pwsh

# Debug script to test GraphQL subscriptions and trigger events
Write-Host "=== GraphQL Subscription Debug Test ===" -ForegroundColor Green

# Generate JWT token
Write-Host "Generating JWT token..." -ForegroundColor Yellow
$jwtResult = node generate-jwt-correct-secret.js
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to generate JWT token" -ForegroundColor Red
    exit 1
}

$token = $jwtResult.Trim()
Write-Host "JWT Token generated successfully" -ForegroundColor Green

# Start subscription in background
Write-Host "Starting GraphQL subscription..." -ForegroundColor Yellow

$subscriptionQuery = @"
subscription {
  exceptionUpdated {
    eventType
    exception {
      transactionId
    }
    timestamp
    triggeredBy
  }
}
"@

$subscriptionPayload = @{
    query = $subscriptionQuery
} | ConvertTo-Json -Compress

# Start WebSocket connection for subscription
$wsScript = @"
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': 'Bearer $token'
    }
});

ws.on('open', function open() {
    console.log('WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    console.log('Received:', JSON.stringify(msg, null, 2));
    
    if (msg.type === 'connection_ack') {
        console.log('Connection acknowledged, starting subscription...');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'sub1',
            type: 'start',
            payload: $subscriptionPayload
        }));
    }
});

ws.on('error', function error(err) {
    console.error('WebSocket error:', err);
});

ws.on('close', function close() {
    console.log('WebSocket closed');
});

// Keep alive
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
    }
}, 30000);
"@

# Save WebSocket script
$wsScript | Out-File -FilePath "debug-websocket.js" -Encoding UTF8

# Start WebSocket subscription in background
Write-Host "Starting WebSocket subscription..." -ForegroundColor Yellow
Start-Process -FilePath "node" -ArgumentList "debug-websocket.js" -NoNewWindow

# Wait a moment for subscription to establish
Start-Sleep -Seconds 3

# Now trigger an exception by creating an order
Write-Host "Triggering exception by creating order..." -ForegroundColor Yellow

$orderPayload = @{
    externalId = "DEBUG-ORDER-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    customerId = "CUST-DEBUG-001"
    locationCode = "LOC-001"
    items = @(
        @{
            productCode = "PROD-001"
            quantity = 1
            unitPrice = 10.50
        }
    )
    totalAmount = 10.50
    orderDate = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
} | ConvertTo-Json -Depth 3

Write-Host "Order payload:" -ForegroundColor Cyan
Write-Host $orderPayload

# Create order that should trigger an exception
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method POST -Body $orderPayload -ContentType "application/json" -Headers @{
        "Authorization" = "Bearer $token"
    }
    
    Write-Host "Order creation response:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3)
} catch {
    Write-Host "Order creation failed (this might be expected):" -ForegroundColor Yellow
    Write-Host $_.Exception.Message
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody" -ForegroundColor Yellow
    }
}

# Wait for subscription events
Write-Host "Waiting for subscription events (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "Debug test completed. Check the WebSocket output above for any subscription events." -ForegroundColor Green