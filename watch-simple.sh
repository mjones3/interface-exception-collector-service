#!/bin/bash

# Simple GraphQL watcher

TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"

echo "🔴 GraphQL Live Watcher"
echo "Press Ctrl+C to stop"
echo

LAST_COUNT=-1
POLL=0

while true; do
    POLL=$((POLL + 1))
    TIME=$(date '+%H:%M:%S')
    
    RESPONSE=$(curl -s -X POST http://localhost:8080/graphql \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{"query": "{ exceptions { totalCount edges { node { transactionId status severity timestamp } } } }"}')
    
    if command -v jq >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq -r '.data.exceptions.totalCount // 0')
        
        printf "[$TIME] Poll #%d - Total: %d" "$POLL" "$COUNT"
        
        if [ "$COUNT" != "$LAST_COUNT" ]; then
            echo " 🔔 CHANGED!"
            echo "$RESPONSE" | jq -r '.data.exceptions.edges[0].node | "  Latest: " + .transactionId + " - " + .status + " (" + .severity + ")"' 2>/dev/null
            LAST_COUNT="$COUNT"
        else
            echo " (no change)"
        fi
    else
        echo "[$TIME] Poll #$POLL - Response: ${RESPONSE:0:100}..."
    fi
    
    sleep 5
done