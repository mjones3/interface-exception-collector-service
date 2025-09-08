#!/bin/bash

# Test GraphQL subscriptions using the new test endpoint

echo "🧪 Testing GraphQL Subscriptions with Test Endpoint"
echo "=================================================="

# Generate JWT token
echo "Generating JWT token..."
if [ -f "generate-jwt-correct-secret.js" ]; then
    token=$(node generate-jwt-correct-secret.js 2>/dev/null | tr -d '\r\n')
    if [ -z "$token" ]; then
        echo "❌ Failed to generate token, using fallback"
        token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
    fi
else
    echo "Using fallback JWT token..."
    token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
fi

echo "✅ Token ready: ${token:0:20}..."

# Check if WebSocket is available
if ! command -v node >/dev/null 2>&1 || ! node -e "require('ws')" 2>/dev/null; then
    echo "❌ WebSocket testing not available (need Node.js + ws module)"
    echo "💡 Install with: npm install ws"
    exit 1
fi

# Step 1: Start WebSocket subscription
echo
echo "🔌 Starting WebSocket subscription..."

cat > subscription-test.js << EOF
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': 'Bearer $token'
    }
});

let eventReceived = false;
let subscriptionReady = false;

ws.on('open', function open() {
    console.log('✅ WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    const timestamp = new Date().toISOString();
    
    if (msg.type === 'connection_ack') {
        console.log('✅ Connection acknowledged');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'test-subscription',
            type: 'start',
            payload: {
                query: \`
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
                \`
            }
        }));
        
        subscriptionReady = true;
        console.log('📡 Subscription started - ready for events!');
        
    } else if (msg.type === 'data') {
        console.log(\`\\n🎉 [\${timestamp}] SUBSCRIPTION EVENT RECEIVED!\`);
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        
        if (msg.payload && msg.payload.data && msg.payload.data.exceptionUpdated) {
            const event = msg.payload.data.exceptionUpdated;
            console.log(\`📋 Event Type: \${event.eventType}\`);
            console.log(\`⏰ Timestamp: \${event.timestamp}\`);
            console.log(\`👤 Triggered By: \${event.triggeredBy || 'System'}\`);
            
            if (event.exception) {
                const ex = event.exception;
                console.log(\`🆔 Transaction ID: \${ex.transactionId}\`);
                console.log(\`📊 Status: \${ex.status}\`);
                console.log(\`⚠️  Severity: \${ex.severity}\`);
                console.log(\`💬 Reason: \${ex.exceptionReason}\`);
            }
        } else {
            console.log('📄 Raw event data:', JSON.stringify(msg.payload, null, 2));
        }
        
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\n');
        
        eventReceived = true;
        
        // Exit after receiving event
        setTimeout(() => {
            console.log('✅ SUCCESS! Subscription event received - test passed!');
            process.exit(0);
        }, 1000);
        
    } else if (msg.type === 'error') {
        console.log('❌ Subscription error:', msg.payload);
        process.exit(1);
        
    } else {
        console.log(\`📨 Message (\${msg.type}):\`, msg.payload || '');
    }
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
    process.exit(1);
});

ws.on('close', function close() {
    if (!eventReceived) {
        console.log('❌ WebSocket closed without receiving events');
        process.exit(1);
    }
});

// Timeout after 30 seconds
setTimeout(() => {
    if (!subscriptionReady) {
        console.log('❌ Timeout: Subscription not ready after 30 seconds');
        process.exit(1);
    } else if (!eventReceived) {
        console.log('❌ Timeout: No subscription events received after 30 seconds');
        console.log('💡 This might indicate the GraphQL event publishing is not working');
        process.exit(1);
    }
}, 30000);

// Signal when ready for testing
setTimeout(() => {
    if (subscriptionReady) {
        console.log('🚀 Subscription ready - you can now create test exceptions!');
    }
}, 2000);
EOF

# Start WebSocket subscription in background
node subscription-test.js &
WS_PID=$!

# Wait for subscription to be ready
echo "⏳ Waiting for subscription to be ready..."
sleep 5

# Step 2: Create test exception using the new endpoint
echo
echo "🔥 Creating test exception via REST endpoint..."

external_id="ENDPOINT-TEST-$(date +%Y%m%d-%H%M%S)"
test_payload=$(cat << EOF
{
    "externalId": "$external_id"
}
EOF
)

echo "Creating exception with external ID: $external_id"

create_response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/test/exceptions/order" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$test_payload")

http_code=$(echo "$create_response" | tail -n1)
response_body=$(echo "$create_response" | head -n -1)

echo "HTTP Code: $http_code"

if [ "$http_code" -eq 200 ]; then
    echo "✅ Test exception created successfully!"
    
    if command -v jq >/dev/null 2>&1; then
        echo "Exception details:"
        echo "$response_body" | jq -r '
            "  Transaction ID: " + .transactionId + 
            "\n  External ID: " + .externalId + 
            "\n  Status: " + .status + 
            "\n  Severity: " + .severity'
    else
        echo "Response: $response_body"
    fi
    
    echo
    echo "⏳ Waiting for WebSocket subscription event..."
    
    # Wait for WebSocket process to complete
    wait $WS_PID
    exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo
        echo "🎉 SUCCESS! GraphQL subscription events are working correctly!"
    else
        echo
        echo "❌ FAILED! No subscription event received."
        echo "💡 Check application logs for GraphQL event publishing errors"
    fi
    
else
    echo "❌ Failed to create test exception"
    echo "Response: $response_body"
    
    # Kill WebSocket process
    kill $WS_PID 2>/dev/null
    exit 1
fi

# Cleanup
rm -f subscription-test.js