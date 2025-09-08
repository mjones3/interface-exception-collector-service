#!/bin/bash

# Test script to trigger events and verify GraphQL subscriptions work

echo "🧪 Testing GraphQL Subscription Events"
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

# Test GraphQL endpoint
echo "🔗 Testing GraphQL endpoint..."
GRAPHQL_TEST=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __schema { subscriptionType { fields { name } } } }"}')

if echo "$GRAPHQL_TEST" | grep -q "exceptionUpdated"; then
    echo "✅ GraphQL endpoint is working"
else
    echo "❌ GraphQL endpoint not working"
    echo "Response: $GRAPHQL_TEST"
    exit 1
fi

echo
echo "🚀 Triggering test events..."

# Trigger OrderRejected event via Kafka
echo "📤 Sending OrderRejected event..."
ORDER_REJECTED_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/test/kafka/order-rejected" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "transactionId": "test-order-'$(date +%s)'",
        "externalId": "EXT-'$(date +%s)'",
        "operation": "CREATE_ORDER",
        "rejectedReason": "Invalid customer ID",
        "customerId": "CUST-123",
        "locationCode": "LOC-001"
    }')

echo "Response: $ORDER_REJECTED_RESPONSE"

# Wait a moment for processing
sleep 2

# Trigger OrderCancelled event via Kafka
echo "📤 Sending OrderCancelled event..."
ORDER_CANCELLED_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/test/kafka/order-cancelled" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "transactionId": "test-cancel-'$(date +%s)'",
        "externalId": "EXT-CANCEL-'$(date +%s)'",
        "cancelReason": "Customer requested cancellation",
        "customerId": "CUST-456"
    }')

echo "Response: $ORDER_CANCELLED_RESPONSE"

# Wait a moment for processing
sleep 2

# Check if exceptions were created
echo "🔍 Checking created exceptions..."
EXCEPTIONS_QUERY=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity exceptionReason timestamp } } } }"}')

echo "Recent exceptions:"
if command -v jq >/dev/null 2>&1; then
    echo "$EXCEPTIONS_QUERY" | jq '.data.exceptions.edges[] | .node | {transactionId, status, severity, exceptionReason}'
else
    echo "$EXCEPTIONS_QUERY"
fi

echo
echo "✅ Test events triggered successfully!"
echo "💡 Now run './watch-subscriptions-enhanced.sh' in another terminal to see real-time events"
echo "🔄 You can run this script again to generate more test events"