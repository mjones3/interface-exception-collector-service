#!/bin/bash

# Live subscription watcher using WebSocket

echo "🔴 Live GraphQL Subscription Watcher (WebSocket)"
echo "================================================"

# Generate or use existing token
if [ -f "generate-jwt-correct-secret.js" ]; then
    echo "Generating fresh JWT token..."
    token=$(node generate-jwt-correct-secret.js 2>/dev/null | tr -d '\r\n')
    if [ -z "$token" ]; then
        echo "⚠️  Failed to generate token, using fallback"
        token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
    fi
else
    echo "Using fallback JWT token..."
    token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
fi

echo "Token: ${token:0:20}..."

# Check if Node.js and ws module are available
if ! command -v node >/dev/null 2>&1; then
    echo "❌ Node.js not found. Please install Node.js first."
    exit 1
fi

# Create WebSocket subscription client
cat > live-subscription-client.js << 'EOF'
const WebSocket = require('ws');

const token = process.argv[2];
if (!token) {
    console.error('❌ No token provided');
    process.exit(1);
}

console.log('🔗 Connecting to WebSocket...');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

let isConnected = false;
let subscriptionActive = false;

ws.on('open', function open() {
    console.log('✅ WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    const timestamp = new Date().toLocaleTimeString();
    
    switch (msg.type) {
        case 'connection_ack':
            console.log(`[${timestamp}] ✅ Connection acknowledged`);
            isConnected = true;
            
            // Start subscription
            console.log(`[${timestamp}] 📡 Starting subscription...`);
            ws.send(JSON.stringify({
                id: 'exception-subscription',
                type: 'start',
                payload: {
                    query: `
                        subscription {
                            exceptionUpdated {
                                eventType
                                exception {
                                    transactionId
                                    status
                                    severity
                                    exceptionReason
                                }
                                timestamp
                                triggeredBy
                            }
                        }
                    `
                }
            }));
            subscriptionActive = true;
            break;
            
        case 'data':
            console.log(`\n🔔 [${timestamp}] SUBSCRIPTION EVENT RECEIVED!`);
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
            
            if (msg.payload && msg.payload.data && msg.payload.data.exceptionUpdated) {
                const event = msg.payload.data.exceptionUpdated;
                console.log(`📋 Event Type: ${event.eventType}`);
                console.log(`⏰ Timestamp: ${event.timestamp}`);
                console.log(`👤 Triggered By: ${event.triggeredBy || 'System'}`);
                
                if (event.exception) {
                    const ex = event.exception;
                    console.log(`🆔 Transaction ID: ${ex.transactionId}`);
                    console.log(`📊 Status: ${ex.status}`);
                    console.log(`⚠️  Severity: ${ex.severity}`);
                    console.log(`💬 Reason: ${ex.exceptionReason}`);
                }
            } else {
                console.log('📄 Raw data:', JSON.stringify(msg.payload, null, 2));
            }
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
            
            // Beep sound (if terminal supports it)
            process.stdout.write('\x07');
            break;
            
        case 'error':
            console.log(`❌ [${timestamp}] Subscription error:`, msg.payload);
            break;
            
        case 'complete':
            console.log(`✅ [${timestamp}] Subscription completed`);
            subscriptionActive = false;
            break;
            
        default:
            console.log(`📨 [${timestamp}] Message:`, msg);
    }
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
});

ws.on('close', function close(code, reason) {
    console.log(`🔌 WebSocket closed (${code}): ${reason || 'No reason provided'}`);
    process.exit(0);
});

// Keep alive ping
const keepAlive = setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
    }
}, 30000);

// Status updates
const statusInterval = setInterval(() => {
    const timestamp = new Date().toLocaleTimeString();
    if (subscriptionActive) {
        console.log(`[${timestamp}] 👂 Listening for events... (Press Ctrl+C to stop)`);
    }
}, 60000);

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down...');
    clearInterval(keepAlive);
    clearInterval(statusInterval);
    
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    } else {
        process.exit(0);
    }
});

console.log('👂 Waiting for subscription events...');
console.log('💡 Tip: Run ./trigger-events.sh in another terminal to generate test events');
console.log('🛑 Press Ctrl+C to stop\n');
EOF

# Check if ws module is available
if ! node -e "require('ws')" 2>/dev/null; then
    echo "❌ WebSocket module 'ws' not found."
    echo "💡 Install it with: npm install ws"
    echo "🔄 Falling back to polling mode..."
    
    # Fallback to polling-based watching
    exec ./watch-graphql.sh
else
    # Run WebSocket client
    node live-subscription-client.js "$token"
fi

# Cleanup
rm -f live-subscription-client.js