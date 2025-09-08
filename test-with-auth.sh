#!/bin/bash

# Test GraphQL subscriptions with authentication

base_url="http://localhost:8080"

echo "GraphQL Subscriptions with Authentication"
echo "========================================="

# Step 1: Get JWT token
echo
echo "🔐 Getting JWT token..."
login_data='{"username":"admin","password":"password"}'

response=$(curl -s -w "\n%{http_code}" -X POST "$base_url/auth/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

http_code=$(echo "$response" | tail -n1)
response_body=$(echo "$response" | head -n -1)

if [ "$http_code" -eq 200 ]; then
    if command -v jq >/dev/null 2>&1; then
        token=$(echo "$response_body" | jq -r '.token')
    else
        # Fallback without jq
        token=$(echo "$response_body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi
    
    if [ -n "$token" ] && [ "$token" != "null" ]; then
        echo "✅ Token obtained: ${token:0:20}..."
    else
        echo "❌ Failed to extract token from response"
        token=""
    fi
else
    echo "❌ Login failed: HTTP $http_code"
    echo "Response: $response_body"
    echo "Trying without authentication..."
    token=""
fi

# Step 2: Test GraphQL with token
echo
echo "📊 Testing GraphQL subscriptions..."

headers=(-H "Content-Type: application/json")
if [ -n "$token" ]; then
    headers+=(-H "Authorization: Bearer $token")
fi

query='{"query": "{ __schema { subscriptionType { name fields { name } } } }"}'

response=$(curl -s -X POST "$base_url/graphql" \
    "${headers[@]}" \
    -d "$query")

if command -v jq >/dev/null 2>&1; then
    subscription_type=$(echo "$response" | jq -r '.data.__schema.subscriptionType')
    
    if [ "$subscription_type" != "null" ] && [ -n "$subscription_type" ]; then
        echo "✅ SUBSCRIPTIONS ARE ACTIVE!"
        echo
        echo "Available subscriptions:"
        echo "$response" | jq -r '.data.__schema.subscriptionType.fields[].name' | sed 's/^/  - /'
        
        echo
        echo "🎉 SUCCESS! You can now use subscriptions with this token."
        
    else
        echo "❌ No subscriptions found"
        echo "Response: $response"
    fi
else
    echo "Response (install jq for better parsing): $response"
    if echo "$response" | grep -q "subscriptionType"; then
        echo "✅ Subscriptions appear to be available!"
    else
        echo "❌ No subscriptions detected"
    fi
fi

# Step 3: Show how to use the token
if [ -n "$token" ]; then
    echo
    echo "🚀 How to use subscriptions:"
    echo "1. WebSocket URL: ws://localhost:8080/graphql?token=$token"
    echo "2. Or use Authorization header: Bearer $token"
    echo
    echo "3. Try this in GraphiQL or HTML client:"
    echo "   subscription { exceptionUpdated { eventType timestamp } }"
    
    echo
    echo "🔧 Command line watching:"
    echo "./watch-graphql.sh  # (token is embedded in script)"
    
    # Update the watch script with the new token
    if [ -f "watch-graphql.sh" ]; then
        sed -i "s/TOKEN=\".*\"/TOKEN=\"$token\"/" watch-graphql.sh
        echo "✅ Updated watch-graphql.sh with new token"
    fi
fi