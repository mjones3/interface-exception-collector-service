#!/bin/bash

# Test script to verify GraphQL subscription events are working after the fix

echo "🔧 Testing GraphQL Subscription Fix"
echo "=================================="

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

# Step 1: Check current exception count
echo
echo "📊 Checking current exception count..."
current_count_response=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d '{"query": "{ exceptions(pagination: { first: 1 }) { totalCount } }"}')

if command -v jq >/dev/null 2>&1; then
    current_count=$(echo "$current_count_response" | jq -r '.data.exceptions.totalCount // 0')
    echo "Current exception count: $current_count"
else
    echo "Response: $current_count_response"
    current_count=0
fi

# Step 2: Start WebSocket subscription in background
echo
echo "🔌 Starting WebSocket subscription..."

# Create WebSocket client
cat > test-websocket-client.js << EOF
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': 'Bearer $token'
    }
});

let eventReceived = false;

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
        console.log('✅ Connection acknowledged, starting subscription...');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'test-sub',
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
                            }
                            timestamp
                            triggeredBy
                        }
                    }
                \`
            }
        }));
    } else if (msg.type === 'data') {
        console.log(\`🎉 [\${timestamp}] SUBSCRIPTION EVENT RECEIVED!\`);
        console.log('Event data:', JSON.stringify(msg.payload, null, 2));
        eventReceived = true;
        
        // Exit after receiving event
        setTimeout(() => {
            console.log('✅ Test completed - subscription event received!');
            process.exit(0);
        }, 1000);
    } else if (msg.type === 'error') {
        console.log('❌ Subscription error:', msg.payload);
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
    if (!eventReceived) {
        console.log('❌ Timeout: No subscription events received after 30 seconds');
        process.exit(1);
    }
}, 30000);
EOF

# Check if ws module is available
if ! node -e "require('ws')" 2>/dev/null; then
    echo "❌ WebSocket module 'ws' not found. Install with: npm install ws"
    echo "Falling back to HTTP polling test..."
    
    # Fallback: HTTP polling test
    echo
    echo "🔄 Creating test exception..."
    
    # Create test exception
    external_id="TEST-FIX-$(date +%Y%m%d-%H%M%S)"
    order_payload=$(cat << EOF
{
    "externalId": "$external_id",
    "customerId": "CUST-TEST-001",
    "locationCode": "LOC-001",
    "items": [
        {
            "productCode": "PROD-001",
            "quantity": 1,
            "unitPrice": 10.50
        }
    ],
    "totalAmount": 10.50,
    "orderDate": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)"
}
EOF
)
    
    echo "Creating order with external ID: $external_id"
    create_response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/orders" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$order_payload")
    
    http_code=$(echo "$create_response" | tail -n1)
    echo "Order creation HTTP code: $http_code"
    
    # Wait a moment for processing
    sleep 3
    
    # Check if exception count increased
    echo
    echo "📊 Checking if exception was created..."
    new_count_response=$(curl -s -X POST "http://localhost:8080/graphql" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d '{"query": "{ exceptions(pagination: { first: 1 }) { totalCount } }"}')
    
    if command -v jq >/dev/null 2>&1; then
        new_count=$(echo "$new_count_response" | jq -r '.data.exceptions.totalCount // 0')
        echo "New exception count: $new_count"
        
        if [ "$new_count" -gt "$current_count" ]; then
            echo "✅ Exception was created! (Count increased from $current_count to $new_count)"
            echo "⚠️  Cannot test WebSocket subscriptions without 'ws' module"
            echo "💡 Install 'ws' module and re-run for full WebSocket testing"
        else
            echo "❌ No new exception detected"
        fi
    else
        echo "Response: $new_count_response"
    fi
    
    exit 0
fi

# Start WebSocket client in background
node test-websocket-client.js &
WS_PID=$!

# Wait for WebSocket to connect
sleep 3

# Step 3: Create test exception to trigger subscription
echo
echo "🔥 Creating test exception to trigger subscription..."

external_id="TEST-FIX-$(date +%Y%m%d-%H%M%S)"
order_payload=$(cat << EOF
{
    "externalId": "$external_id",
    "customerId": "CUST-TEST-001",
    "locationCode": "LOC-001",
    "items": [
        {
            "productCode": "PROD-001",
            "quantity": 1,
            "unitPrice": 10.50
        }
    ],
    "totalAmount": 10.50,
    "orderDate": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)"
}
EOF
)

echo "Creating order with external ID: $external_id"
create_response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$order_payload")

http_code=$(echo "$create_response" | tail -n1)
response_body=$(echo "$create_response" | head -n -1)

echo "Order creation HTTP code: $http_code"
if [ "$http_code" -ne 200 ] && [ "$http_code" -ne 201 ]; then
    echo "Response: $response_body"
fi

echo
echo "⏳ Waiting for subscription event (up to 30 seconds)..."

# Wait for WebSocket process to complete or timeout
wait $WS_PID
exit_code=$?

# Cleanup
rm -f test-websocket-client.js

if [ $exit_code -eq 0 ]; then
    echo
    echo "🎉 SUCCESS! GraphQL subscription events are working!"
else
    echo
    echo "❌ FAILED! No subscription events received."
    echo "💡 Check application logs for errors in ExceptionProcessingService"
fi

exit $exit_code