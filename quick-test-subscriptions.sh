#!/bin/bash

# Quick test for GraphQL subscriptions

echo "🚀 Quick GraphQL Subscription Test"
echo "=================================="

# Generate fresh JWT token
echo "Generating JWT token..."
if [ -f "generate-jwt-correct-secret.js" ]; then
    token=$(node generate-jwt-correct-secret.js 2>/dev/null | tr -d '\r\n')
    if [ -n "$token" ]; then
        echo "✅ Token generated: ${token:0:20}..."
    else
        echo "❌ Failed to generate token, using fallback"
        token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
    fi
else
    echo "⚠️  JWT generator not found, using fallback token"
    token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
fi

base_url="http://localhost:8080"

# Test 1: Check if GraphQL endpoint is available
echo
echo "🔍 Testing GraphQL endpoint..."
response=$(curl -s -w "%{http_code}" -o /dev/null "$base_url/graphql")
if [ "$response" -eq 200 ] || [ "$response" -eq 400 ]; then
    echo "✅ GraphQL endpoint is responding"
else
    echo "❌ GraphQL endpoint not available (HTTP $response)"
    exit 1
fi

# Test 2: Check authentication
echo
echo "🔐 Testing authentication..."
auth_query='{"query": "{ __typename }"}'
response=$(curl -s -X POST "$base_url/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$auth_query")

if echo "$response" | grep -q "__typename"; then
    echo "✅ Authentication successful"
else
    echo "❌ Authentication failed"
    echo "Response: $response"
    exit 1
fi

# Test 3: Check subscription availability
echo
echo "📡 Checking subscription availability..."
subscription_query='{"query": "{ __schema { subscriptionType { fields { name } } } }"}'
response=$(curl -s -X POST "$base_url/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$subscription_query")

if command -v jq >/dev/null 2>&1; then
    subscriptions=$(echo "$response" | jq -r '.data.__schema.subscriptionType.fields[]?.name' 2>/dev/null)
    if [ -n "$subscriptions" ]; then
        echo "✅ Subscriptions available:"
        echo "$subscriptions" | sed 's/^/  - /'
    else
        echo "❌ No subscriptions found"
        echo "Response: $response"
    fi
else
    if echo "$response" | grep -q "subscriptionType"; then
        echo "✅ Subscriptions appear to be available (install jq for details)"
    else
        echo "❌ No subscriptions detected"
        echo "Response: $response"
    fi
fi

# Test 4: Quick data check
echo
echo "📊 Checking current exceptions..."
exceptions_query='{"query": "{ exceptions(pagination: { first: 3 }) { totalCount edges { node { transactionId status } } } }"}'
response=$(curl -s -X POST "$base_url/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "$exceptions_query")

if command -v jq >/dev/null 2>&1; then
    total=$(echo "$response" | jq -r '.data.exceptions.totalCount' 2>/dev/null)
    if [ "$total" != "null" ] && [ -n "$total" ]; then
        echo "✅ Found $total exceptions in database"
        echo "$response" | jq -r '.data.exceptions.edges[]? | "  - " + .node.transactionId + " (" + .node.status + ")"' 2>/dev/null
    else
        echo "⚠️  No exceptions data or query failed"
        echo "Response: $response"
    fi
else
    if echo "$response" | grep -q "totalCount"; then
        echo "✅ Exceptions query successful (install jq for details)"
    else
        echo "❌ Exceptions query failed"
        echo "Response: $response"
    fi
fi

echo
echo "🎉 Quick test completed!"
echo
echo "💡 Next steps:"
echo "  - Run './watch-graphql.sh' to monitor live changes"
echo "  - Run './trigger-events.sh' to create test events"
echo "  - Open 'graphql-subscription-test.html' in browser for WebSocket testing"