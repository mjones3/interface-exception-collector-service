#!/bin/bash

# Alternative script using curl to send OrderRejected events via Kafka REST Proxy
# Note: This requires Kafka REST Proxy to be running

set -e

KAFKA_REST_URL="http://localhost:8082"
TOPIC_NAME="OrderRejected"

echo "🩸 Blood Banking Order Rejection Event Generator (curl version)"
echo "=============================================================="

# Check if Kafka REST Proxy is available
if ! curl -s "$KAFKA_REST_URL/topics" > /dev/null; then
    echo "❌ Kafka REST Proxy is not available at $KAFKA_REST_URL"
    echo "   This script requires Kafka REST Proxy to be running."
    echo "   Use the Python version instead: ./send_test_events.sh"
    exit 1
fi

# Function to generate a UUID (simplified)
generate_uuid() {
    if command -v uuidgen &> /dev/null; then
        uuidgen | tr '[:upper:]' '[:lower:]'
    else
        # Fallback: generate a pseudo-UUID
        echo "$(date +%s)-$(shuf -i 1000-9999 -n 1)-$(shuf -i 1000-9999 -n 1)-$(shuf -i 1000-9999 -n 1)"
    fi
}

# Blood banking test data arrays
BLOOD_PRODUCTS=(
    '{"itemId":"RBC-O-NEG-001","itemType":"Red Blood Cells","quantity":2,"unitPrice":245.50}'
    '{"itemId":"PLT-AB-POS-003","itemType":"Platelets","quantity":1,"unitPrice":520.75}'
    '{"itemId":"FFP-B-NEG-004","itemType":"Fresh Frozen Plasma","quantity":3,"unitPrice":185.25}'
    '{"itemId":"CRYO-O-POS-005","itemType":"Cryoprecipitate","quantity":1,"unitPrice":165.00}'
)

CUSTOMERS=(
    "CUST-MOUNT-SINAI-001"
    "CUST-CEDARS-SINAI-002"
    "CUST-MAYO-CLINIC-003"
    "CUST-JOHNS-HOPKINS-004"
)

LOCATIONS=(
    "HOSP-NYC-001"
    "HOSP-LA-002"
    "HOSP-CHI-003"
    "HOSP-HOU-004"
)

REJECTION_REASONS=(
    "Insufficient inventory for blood type O-negative"
    "Product expired - expiration date exceeded by 2 days"
    "Cross-match incompatibility detected for patient"
    "Invalid customer credentials - account suspended"
    "Order quantity exceeds maximum allowed limit"
)

OPERATIONS=(
    "CREATE_BLOOD_ORDER"
    "RESERVE_BLOOD_UNITS"
    "VALIDATE_COMPATIBILITY"
    "PROCESS_URGENT_ORDER"
)

# Send 10 events
for i in {1..10}; do
    # Generate random selections
    CUSTOMER=${CUSTOMERS[$((RANDOM % ${#CUSTOMERS[@]}))]}
    LOCATION=${LOCATIONS[$((RANDOM % ${#LOCATIONS[@]}))]}
    REASON=${REJECTION_REASONS[$((RANDOM % ${#REJECTION_REASONS[@]}))]}
    OPERATION=${OPERATIONS[$((RANDOM % ${#OPERATIONS[@]}))]}
    PRODUCT=${BLOOD_PRODUCTS[$((RANDOM % ${#BLOOD_PRODUCTS[@]}))]}
    
    # Generate IDs
    EVENT_ID=$(generate_uuid)
    CORRELATION_ID=$(generate_uuid)
    CAUSATION_ID=$(generate_uuid)
    TRANSACTION_ID="TXN-$((100000 + RANDOM % 900000))"
    EXTERNAL_ID="EXT-ORD-$((10000 + RANDOM % 90000))"
    
    # Get current timestamp
    TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")
    
    # Create the event JSON
    EVENT_JSON=$(cat <<EOF
{
    "eventId": "$EVENT_ID",
    "eventType": "OrderRejected",
    "eventVersion": "1.0",
    "occurredOn": "$TIMESTAMP",
    "source": "order-service",
    "correlationId": "$CORRELATION_ID",
    "causationId": "$CAUSATION_ID",
    "payload": {
        "transactionId": "$TRANSACTION_ID",
        "externalId": "$EXTERNAL_ID",
        "operation": "$OPERATION",
        "rejectedReason": "$REASON",
        "customerId": "$CUSTOMER",
        "locationCode": "$LOCATION",
        "orderItems": [$PRODUCT]
    }
}
EOF
)

    # Create Kafka message payload
    KAFKA_MESSAGE=$(cat <<EOF
{
    "records": [
        {
            "key": "$TRANSACTION_ID",
            "value": $EVENT_JSON
        }
    ]
}
EOF
)

    # Send to Kafka via REST Proxy
    RESPONSE=$(curl -s -X POST \
        -H "Content-Type: application/vnd.kafka.json.v2+json" \
        -d "$KAFKA_MESSAGE" \
        "$KAFKA_REST_URL/topics/$TOPIC_NAME")

    if echo "$RESPONSE" | grep -q "error_code"; then
        echo "❌ Error sending event $i: $RESPONSE"
    else
        echo "✅ Event $i/10 sent successfully:"
        echo "   📋 Transaction ID: $TRANSACTION_ID"
        echo "   🏥 Customer: $CUSTOMER"
        echo "   📍 Location: $LOCATION"
        echo "   🚫 Reason: ${REASON:0:50}..."
        echo
    fi
    
    # Small delay between messages
    sleep 0.5
done

echo "🎉 Finished sending events!"
echo "💡 Check your application logs to see the events being processed."