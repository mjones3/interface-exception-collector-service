#!/bin/bash

# Debug script to test GraphQL subscriptions and trigger events
echo "=== GraphQL Subscription Debug Test ==="

# Generate JWT token
echo "Generating JWT token..."
if ! command -v node >/dev/null 2>&1; then
    echo "❌ Node.js not found. Please install Node.js first."
    exit 1
fi

token=$(node generate-jwt-correct-secret.js 2>/dev/null)
if [ $? -ne 0 ] || [ -z "$token" ]; then
    echo "❌ Failed to generate JWT token"
    exit 1
fi

token=$(echo "$token" | tr -d '\r\n')
echo "✅ JWT Token generated successfully"

# Start subscription in background
echo "Starting GraphQL subscription..."

subscription_query='subscription {
  exceptionUpdated {
    eventType
    exception {
      transactionId
    }
    timestamp
    triggeredBy
  }
}'

# Create WebSocket test script
cat > debug-websocket.js << EOF
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
            payload: {
                query: \`$subscription_query\`
            }
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
EOF

# Start WebSocket subscription in background
echo "Starting WebSocket subscription..."
node debug-websocket.js &
WS_PID=$!

# Wait a moment for subscription to establish
sleep 3

# Now trigger an exception by creating an order
echo "Triggering exception by creating order..."

external_id="DEBUG-ORDER-$(date +%Y%m%d-%H%M%S)"
order_payload=$(cat << EOF
{
    "externalId": "$external_id",
    "customerId": "CUST-DEBUG-001",
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

echo "Order payload:"
echo "$order_payload"

# Create order that should trigger an exception
echo "Creating order..."
response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$order_payload")

http_code=$(echo "$response" | tail -n1)
response_body=$(echo "$response" | head -n -1)

if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
    echo "✅ Order creation response:"
    echo "$response_body"
else
    echo "⚠️  Order creation failed (this might be expected):"
    echo "HTTP Code: $http_code"
    echo "Response: $response_body"
    echo "This should generate an exception to watch!"
fi

# Wait for subscription events
echo "Waiting for subscription events (30 seconds)..."
sleep 30

# Clean up
echo "Stopping WebSocket subscription..."
kill $WS_PID 2>/dev/null
rm -f debug-websocket.js

echo "✅ Debug test completed. Check the WebSocket output above for any subscription events."