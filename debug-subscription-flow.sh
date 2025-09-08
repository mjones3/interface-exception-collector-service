#!/bin/bash

# Debug script to trace the subscription event flow

echo "🔍 Debugging GraphQL Subscription Flow"
echo "====================================="

# Generate JWT token
echo "🔑 Generating JWT token..."
if [ -f "generate-jwt-correct-secret.js" ]; then
    TOKEN=$(node generate-jwt-correct-secret.js 2>/dev/null | tail -n 1 | tr -d '\r\n')
    echo "✅ Token generated: ${TOKEN:0:20}..."
else
    echo "❌ JWT generator not found"
    exit 1
fi

echo
echo "🧪 Step 1: Test GraphQL Schema"
SCHEMA_TEST=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __schema { subscriptionType { fields { name type { name } } } } }"}')

echo "GraphQL Schema Response:"
if command -v jq >/dev/null 2>&1; then
    echo "$SCHEMA_TEST" | jq '.data.__schema.subscriptionType.fields[]'
else
    echo "$SCHEMA_TEST"
fi

echo
echo "🧪 Step 2: Test WebSocket Connection"
# Create a simple WebSocket test
cat > /tmp/ws-test.js << 'EOF'
const WebSocket = require('ws');

const token = process.argv[2];
console.log('🔗 Testing WebSocket connection...');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': `Bearer ${token}` }
});

let connected = false;

ws.on('open', function() {
    console.log('✅ WebSocket connected successfully');
    connected = true;
    
    // Send connection init
    console.log('📤 Sending connection_init...');
    ws.send(JSON.stringify({ type: 'connection_init' }));
});

ws.on('message', function(data) {
    const msg = JSON.parse(data.toString());
    console.log('📨 Received message:', msg.type);
    
    if (msg.type === 'connection_ack') {
        console.log('✅ Connection acknowledged');
        
        // Start subscription
        console.log('📡 Starting subscription...');
        ws.send(JSON.stringify({
            id: 'test-subscription',
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
                            }
                            timestamp
                        }
                    }
                `
            }
        }));
        
        // Wait for events for 10 seconds
        setTimeout(() => {
            console.log('⏰ Test timeout - closing connection');
            ws.close();
        }, 10000);
    } else if (msg.type === 'data') {
        console.log('🔔 SUBSCRIPTION EVENT RECEIVED!');
        console.log('Event data:', JSON.stringify(msg.payload, null, 2));
    } else if (msg.type === 'error') {
        console.log('❌ Subscription error:', msg.payload);
    }
});

ws.on('error', function(err) {
    console.error('❌ WebSocket error:', err.message);
    process.exit(1);
});

ws.on('close', function(code, reason) {
    console.log(`🔌 WebSocket closed (${code}): ${reason || 'Connection closed'}`);
    if (connected) {
        console.log('✅ WebSocket test completed');
        process.exit(0);
    } else {
        console.log('❌ WebSocket connection failed');
        process.exit(1);
    }
});

// Timeout if connection doesn't establish
setTimeout(() => {
    if (!connected) {
        console.log('❌ WebSocket connection timeout');
        process.exit(1);
    }
}, 5000);
EOF

if command -v node >/dev/null 2>&1 && node -e "require('ws')" 2>/dev/null; then
    echo "Running WebSocket test..."
    node /tmp/ws-test.js "$TOKEN" &
    WS_PID=$!
    
    # Wait a moment for WebSocket to connect
    sleep 3
    
    echo
    echo "🧪 Step 3: Trigger Test Event"
    echo "📤 Sending test event to trigger subscription..."
    
    # Trigger an event
    curl -s -X POST "http://localhost:8080/api/test/kafka/order-rejected" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "transactionId": "debug-test-'$(date +%s)'",
            "externalId": "DEBUG-'$(date +%s)'",
            "operation": "CREATE_ORDER",
            "rejectedReason": "Debug test event",
            "customerId": "DEBUG-CUST",
            "locationCode": "DEBUG-LOC"
        }' > /dev/null
    
    echo "✅ Test event sent"
    
    # Wait for WebSocket test to complete
    wait $WS_PID
    
    # Cleanup
    rm -f /tmp/ws-test.js
else
    echo "❌ Node.js or 'ws' module not available for WebSocket test"
fi

echo
echo "🧪 Step 4: Check Recent Exceptions"
RECENT_EXCEPTIONS=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ exceptions(pagination: { first: 3 }) { edges { node { transactionId status severity exceptionReason timestamp } } } }"}')

echo "Recent exceptions:"
if command -v jq >/dev/null 2>&1; then
    echo "$RECENT_EXCEPTIONS" | jq '.data.exceptions.edges[] | .node'
else
    echo "$RECENT_EXCEPTIONS"
fi

echo
echo "🔍 Debug Summary:"
echo "1. GraphQL schema should show 'exceptionUpdated' subscription"
echo "2. WebSocket should connect and acknowledge"
echo "3. Test event should trigger a subscription notification"
echo "4. Recent exceptions should show the test event was processed"
echo
echo "💡 If WebSocket connects but no events are received, the issue is in the event publishing chain"