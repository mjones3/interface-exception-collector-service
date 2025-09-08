#!/bin/bash

# Trigger events to test the subscription watcher

token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
base_url="http://localhost:8080"

echo "🎯 Event Trigger for Subscription Testing"
echo "========================================="

# Function to create a test exception using real business API
create_test_exception() {
    local external_id="$1"
    
    echo
    echo "🔥 Creating business exception: $external_id"
    
    # Use the real business API to create exceptions via Kafka
    exception_payload=$(cat << EOF
{
    "externalId": "$external_id",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Test business exception for GraphQL subscription verification",
    "customerId": "CUST-TEST-$(date +%s)",
    "locationCode": "LOC-TEST",
    "orderItems": [
        {
            "bloodType": "A+",
            "productFamily": "RBC",
            "quantity": 1
        }
    ]
}
EOF
)
    
    response=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/v1/exceptions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$exception_payload")
    
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 201 ]; then
        echo "✅ Business exception published to Kafka: $external_id"
        
        if command -v jq >/dev/null 2>&1; then
            transaction_id=$(echo "$response_body" | jq -r '.transactionId')
            topic=$(echo "$response_body" | jq -r '.topic')
            echo "  Transaction ID: $transaction_id"
            echo "  Kafka Topic: $topic"
            echo "  Processing via Kafka consumer → ExceptionProcessingService → GraphQL events"
        fi
    else
        echo "❌ Failed to publish business exception: HTTP $http_code"
        echo "Response: $response_body"
    fi
}

# Function to retry an exception
retry_exception() {
    local transaction_id="$1"
    
    echo
    echo "🔄 Retrying exception: $transaction_id"
    
    retry_mutation=$(cat << EOF
{
    "query": "mutation { retryException(input: {transactionId: \"$transaction_id\"}) { success exception { transactionId status } } }"
}
EOF
)
    
    response=$(curl -s -X POST "$base_url/graphql" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$retry_mutation")
    
    if command -v jq >/dev/null 2>&1; then
        success=$(echo "$response" | jq -r '.data.retryException.success')
        if [ "$success" = "true" ]; then
            echo "✅ Retry initiated successfully"
        else
            echo "⚠️  Retry failed or not found"
            echo "Response: $response"
        fi
    else
        echo "Response: $response"
        if echo "$response" | grep -q '"success":true'; then
            echo "✅ Retry appears successful"
        else
            echo "⚠️  Retry may have failed"
        fi
    fi
}

# Function to list current exceptions
list_exceptions() {
    echo
    echo "📋 Current exceptions:"
    
    query='{"query": "{ exceptions(pagination: { first: 5 }) { edges { node { transactionId status severity exceptionReason } } } }"}'
    
    response=$(curl -s -X POST "$base_url/graphql" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$query")
    
    if command -v jq >/dev/null 2>&1; then
        echo "$response" | jq -r '.data.exceptions.edges[] | "  - " + .node.transactionId + " | " + .node.status + " | " + .node.severity'
    else
        echo "Response (install jq for better formatting): $response"
    fi
}

# Main menu
while true; do
    echo
    echo "🎮 Choose an action:"
    echo "1. Create test exception"
    echo "2. Retry an exception"
    echo "3. Create multiple exceptions"
    echo "4. List current exceptions"
    echo "5. Exit"
    
    read -p "Enter choice (1-5): " choice
    
    case $choice in
        1)
            external_id="TEST-$(date +%Y%m%d-%H%M%S)"
            create_test_exception "$external_id"
            ;;
        2)
            read -p "Enter transaction ID to retry: " transaction_id
            if [ -n "$transaction_id" ]; then
                retry_exception "$transaction_id"
            fi
            ;;
        3)
            echo
            echo "🔥 Creating 5 test exceptions..."
            for i in {1..5}; do
                external_id="BULK-TEST-$i-$(date +%H%M%S)"
                create_test_exception "$external_id"
                sleep 1
            done
            ;;
        4)
            list_exceptions
            ;;
        5)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid choice"
            ;;
    esac
done