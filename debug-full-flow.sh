#!/bin/bash

# Comprehensive debug script to trace the entire exception creation and subscription flow

echo "🔍 Full Flow Debug - GraphQL Subscriptions"
echo "=========================================="

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

# Step 1: Check GraphQL endpoint availability
echo
echo "🌐 Step 1: Checking GraphQL endpoint..."
graphql_response=$(curl -s -w "%{http_code}" -o /dev/null "http://localhost:8080/graphql")
if [ "$graphql_response" -eq 200 ] || [ "$graphql_response" -eq 400 ]; then
    echo "✅ GraphQL endpoint is available (HTTP $graphql_response)"
else
    echo "❌ GraphQL endpoint not available (HTTP $graphql_response)"
    exit 1
fi

# Step 2: Test authentication
echo
echo "🔐 Step 2: Testing authentication..."
auth_test=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d '{"query": "{ __typename }"}')

if echo "$auth_test" | grep -q "__typename"; then
    echo "✅ Authentication successful"
else
    echo "❌ Authentication failed"
    echo "Response: $auth_test"
    exit 1
fi

# Step 3: Check subscription availability
echo
echo "📡 Step 3: Checking subscription availability..."
subscription_test=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d '{"query": "{ __schema { subscriptionType { fields { name } } } }"}')

if command -v jq >/dev/null 2>&1; then
    subscriptions=$(echo "$subscription_test" | jq -r '.data.__schema.subscriptionType.fields[]?.name' 2>/dev/null)
    if [ -n "$subscriptions" ]; then
        echo "✅ Subscriptions available:"
        echo "$subscriptions" | sed 's/^/  - /'
    else
        echo "❌ No subscriptions found"
        echo "Response: $subscription_test"
        exit 1
    fi
else
    if echo "$subscription_test" | grep -q "exceptionUpdated"; then
        echo "✅ Subscriptions appear to be available"
    else
        echo "❌ No subscriptions detected"
        echo "Response: $subscription_test"
        exit 1
    fi
fi

# Step 4: Get current exception count
echo
echo "📊 Step 4: Getting current exception count..."
count_query='{"query": "{ exceptions(pagination: { first: 1 }) { totalCount edges { node { transactionId timestamp } } } }"}'
count_response=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$count_query")

if command -v jq >/dev/null 2>&1; then
    current_count=$(echo "$count_response" | jq -r '.data.exceptions.totalCount // 0')
    echo "Current exception count: $current_count"
    
    # Show latest exception if any
    latest=$(echo "$count_response" | jq -r '.data.exceptions.edges[0]?.node | "Latest: " + .transactionId + " at " + .timestamp' 2>/dev/null)
    if [ "$latest" != "null" ] && [ -n "$latest" ]; then
        echo "$latest"
    fi
else
    echo "Response: $count_response"
    current_count=0
fi

# Step 5: Test WebSocket connection (if available)
echo
echo "🔌 Step 5: Testing WebSocket connection..."

if command -v node >/dev/null 2>&1 && node -e "require('ws')" 2>/dev/null; then
    echo "✅ WebSocket testing available"
    
    # Create WebSocket test
    cat > websocket-test.js << EOF
const WebSocket = require('ws');

console.log('Connecting to WebSocket...');
const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': 'Bearer $token'
    }
});

let connected = false;
let subscriptionStarted = false;

ws.on('open', function open() {
    console.log('✅ WebSocket connected');
    connected = true;
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    
    if (msg.type === 'connection_ack') {
        console.log('✅ Connection acknowledged');
        
        // Start subscription
        console.log('📡 Starting subscription...');
        ws.send(JSON.stringify({
            id: 'debug-sub',
            type: 'start',
            payload: {
                query: \`
                    subscription {
                        exceptionUpdated {
                            eventType
                            exception {
                                transactionId
                            }
                            timestamp
                        }
                    }
                \`
            }
        }));
        subscriptionStarted = true;
        console.log('✅ Subscription started, ready for events');
        
    } else if (msg.type === 'data') {
        console.log('🎉 SUBSCRIPTION EVENT RECEIVED!');
        console.log('Event:', JSON.stringify(msg.payload, null, 2));
        
    } else if (msg.type === 'error') {
        console.log('❌ Subscription error:', msg.payload);
        
    } else {
        console.log('📨 Message:', msg.type, msg.payload || '');
    }
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
    process.exit(1);
});

ws.on('close', function close() {
    console.log('🔌 WebSocket closed');
    if (!connected) {
        console.log('❌ Failed to connect to WebSocket');
        process.exit(1);
    }
});

// Timeout
setTimeout(() => {
    if (!connected) {
        console.log('❌ WebSocket connection timeout');
        process.exit(1);
    } else if (!subscriptionStarted) {
        console.log('❌ Subscription not started');
        process.exit(1);
    } else {
        console.log('✅ WebSocket and subscription ready');
        process.exit(0);
    }
}, 10000);
EOF

    # Test WebSocket connection
    if timeout 15 node websocket-test.js; then
        echo "✅ WebSocket connection successful"
    else
        echo "❌ WebSocket connection failed"
        rm -f websocket-test.js
        exit 1
    fi
    
    rm -f websocket-test.js
else
    echo "⚠️  WebSocket testing not available (need Node.js + ws module)"
fi

# Step 6: Create test exception and monitor
echo
echo "🔥 Step 6: Creating test exception..."

external_id="DEBUG-FULL-$(date +%Y%m%d-%H%M%S)"
echo "External ID: $external_id"

# Try multiple endpoints to create an exception
echo
echo "Trying different endpoints to create exception..."

# Method 1: Direct order creation (should fail and create exception)
echo "Method 1: Direct order creation..."
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

create_response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$order_payload")

http_code=$(echo "$create_response" | tail -n1)
response_body=$(echo "$create_response" | head -n -1)

echo "HTTP Code: $http_code"
if [ "$http_code" -ne 200 ] && [ "$http_code" -ne 201 ]; then
    echo "Response: $response_body"
    echo "✅ Order creation failed (expected - should create exception)"
else
    echo "⚠️  Order creation succeeded (unexpected)"
fi

# Method 2: Try partner order service directly
echo
echo "Method 2: Partner order service..."
partner_response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8090/v1/partner-order-provider/orders" \
    -H "Content-Type: application/json" \
    -d "$order_payload")

partner_code=$(echo "$partner_response" | tail -n1)
echo "Partner service HTTP Code: $partner_code"

# Wait for processing
echo
echo "⏳ Waiting 5 seconds for exception processing..."
sleep 5

# Step 7: Check if exception was created
echo
echo "📊 Step 7: Checking if exception was created..."
new_count_response=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$count_query")

if command -v jq >/dev/null 2>&1; then
    new_count=$(echo "$new_count_response" | jq -r '.data.exceptions.totalCount // 0')
    echo "New exception count: $new_count"
    
    if [ "$new_count" -gt "$current_count" ]; then
        echo "✅ Exception was created! (Count: $current_count → $new_count)"
        
        # Get the latest exception details
        latest_exception=$(echo "$new_count_response" | jq -r '.data.exceptions.edges[0]?.node | "Transaction ID: " + .transactionId + "\nTimestamp: " + .timestamp' 2>/dev/null)
        echo "Latest exception:"
        echo "$latest_exception"
        
    else
        echo "❌ No new exception detected"
        echo "This means the exception creation flow is not working"
    fi
else
    echo "Response: $new_count_response"
fi

# Step 8: Check application logs (if accessible)
echo
echo "💡 Step 8: Debugging suggestions..."
echo
echo "If no exception was created, check:"
echo "1. Application logs for ExceptionProcessingService"
echo "2. Kafka consumer logs"
echo "3. Database connection"
echo "4. Event processing pipeline"
echo
echo "If exception was created but no WebSocket events:"
echo "1. Check for 'Published GraphQL subscription event' in logs"
echo "2. Verify ExceptionEventPublisher is being called"
echo "3. Check WebSocket connection and subscription setup"
echo "4. Verify subscription resolver is working"

echo
echo "🏁 Debug completed!"
echo "Summary:"
echo "- GraphQL endpoint: ✅"
echo "- Authentication: ✅"
echo "- Subscriptions available: ✅"
echo "- Exception creation: $([ "$new_count" -gt "$current_count" ] && echo "✅" || echo "❌")"
echo "- WebSocket ready: $(command -v node >/dev/null 2>&1 && node -e "require('ws')" 2>/dev/null && echo "✅" || echo "⚠️")"