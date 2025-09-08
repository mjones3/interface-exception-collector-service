#!/bin/bash

# Live GraphQL subscription watcher

TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
BASE_URL="http://localhost:8080"

echo "🔴 LIVE GraphQL Subscription Watcher"
echo "===================================="
echo "Token: ${TOKEN:0:20}..."
echo "Press Ctrl+C to stop"
echo

# Test authentication
echo "🔐 Testing authentication..."
AUTH_TEST=$(curl -s -X POST "$BASE_URL/graphql" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"query": "{ __schema { subscriptionType { fields { name } } } }"}')

if echo "$AUTH_TEST" | grep -q "subscriptionType"; then
    echo "✅ Authentication successful!"
    echo "Available subscriptions:"
    echo "$AUTH_TEST" | jq -r '.data.__schema.subscriptionType.fields[].name' | sed 's/^/  - /'
else
    echo "❌ Authentication failed"
    exit 1
fi

echo
echo "📊 Starting live monitoring..."

LAST_HASH=""
POLL_COUNT=0

while true; do
    POLL_COUNT=$((POLL_COUNT + 1))
    TIMESTAMP=$(date '+%H:%M:%S')
    
    echo
    echo "[$TIMESTAMP] Poll #$POLL_COUNT"
    
    # Query for exceptions
    RESPONSE=$(curl -s -X POST "$BASE_URL/graphql" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "query": "{ exceptions(pagination: { first: 10 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason retryCount } } } }"
      }')
    
    if echo "$RESPONSE" | grep -q "exceptions"; then
        TOTAL=$(echo "$RESPONSE" | jq -r '.data.exceptions.totalCount')
        CURRENT_HASH=$(echo "$RESPONSE" | md5sum | cut -d' ' -f1)
        
        echo "  Total exceptions: $TOTAL"
        
        if [ "$CURRENT_HASH" != "$LAST_HASH" ]; then
            echo "  🔔 NEW DATA DETECTED!"
            echo "  Recent exceptions:"
            
            echo "$RESPONSE" | jq -r '.data.exceptions.edges[] | 
                "    🔸 " + (.node.timestamp | split("T")[0] + " " + (.node.timestamp | split("T")[1] | split(".")[0])) + " " + .node.transactionId + 
                "\n      Status: " + .node.status + " | Severity: " + .node.severity + " | Retries: " + (.node.retryCount | tostring) +
                "\n      Type: " + .node.interfaceType +
                "\n      Reason: " + .node.exceptionReason'
            
            LAST_HASH="$CURRENT_HASH"
            
            # Beep (if available)
            command -v tput >/dev/null && tput bel
            
        else
            echo "  No changes detected"
        fi
        
    elif echo "$RESPONSE" | grep -q "errors"; then
        echo "  ❌ GraphQL errors:"
        echo "$RESPONSE" | jq -r '.errors[].message' | sed 's/^/    /'
    else
        echo "  ⚠️  No data received"
    fi
    
    sleep 3
done