#!/bin/bash

# Test script to verify Partner Order Service Kafka event publishing

set -e

echo "🧪 Testing Partner Order Service Kafka Event Publishing"
echo "======================================================"

# Check if we're running in Kubernetes
if command -v kubectl &> /dev/null; then
    echo "✅ Kubernetes environment detected"
    
    # Check if Partner Order Service is running
    POS_POD=$(kubectl get pods -l app=partner-order-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -z "$POS_POD" ]]; then
        echo "❌ Partner Order Service pod not found. Is Tilt running?"
        echo "   💡 Run: tilt up"
        exit 1
    fi
    
    POS_STATUS=$(kubectl get pod "$POS_POD" -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
    echo "📦 Partner Order Service pod: $POS_POD (Status: $POS_STATUS)"
    
    if [[ "$POS_STATUS" != "Running" ]]; then
        echo "❌ Partner Order Service is not running"
        echo "   💡 Check logs: kubectl logs $POS_POD"
        exit 1
    fi
    
    # Check if Interface Exception Collector is running
    IEC_POD=$(kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -z "$IEC_POD" ]]; then
        echo "❌ Interface Exception Collector pod not found"
        echo "   💡 Run: tilt up"
        exit 1
    fi
    
    IEC_STATUS=$(kubectl get pod "$IEC_POD" -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
    echo "📦 Interface Exception Collector pod: $IEC_POD (Status: $IEC_STATUS)"
    
    # Check Kafka
    KAFKA_POD=$(kubectl get pods -l app=kafka -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -z "$KAFKA_POD" ]]; then
        echo "❌ Kafka pod not found"
        exit 1
    fi
    
    echo "📦 Kafka pod: $KAFKA_POD"
    
else
    echo "❌ kubectl not found. This script requires Kubernetes."
    exit 1
fi

echo ""
echo "🏥 Health Checks:"
echo "================="

# Test Partner Order Service health
echo "🏪 Partner Order Service Health:"
if kubectl exec "$POS_POD" -- curl -s http://localhost:8090/actuator/health | jq '.status' 2>/dev/null | grep -q "UP"; then
    echo "   ✅ Healthy"
else
    echo "   ❌ Unhealthy"
    echo "   💡 Check logs: kubectl logs $POS_POD"
fi

# Test Interface Exception Collector health
echo "🔍 Interface Exception Collector Health:"
if kubectl exec "$IEC_POD" -- curl -s http://localhost:8080/actuator/health | jq '.status' 2>/dev/null | grep -q "UP"; then
    echo "   ✅ Healthy"
else
    echo "   ❌ Unhealthy"
    echo "   💡 Check logs: kubectl logs $IEC_POD"
fi

echo ""
echo "📋 Kafka Topics:"
echo "================"

# List relevant topics
kubectl exec "$KAFKA_POD" -- kafka-topics --bootstrap-server localhost:9092 --list | grep -E "(OrderRejected|OrderRecieved|InvalidOrderEvent)" || echo "No relevant topics found"

echo ""
echo "🚀 Testing Order Submission and Event Publishing:"
echo "================================================="

# Generate unique external ID
EXTERNAL_ID="TEST-KAFKA-$(date +%s)"
echo "📤 Submitting test order with external ID: $EXTERNAL_ID"

# Submit test order
ORDER_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"externalId\": \"$EXTERNAL_ID\",
    \"orderStatus\": \"OPEN\",
    \"locationCode\": \"HOSP-KAFKA-TEST\",
    \"shipmentType\": \"CUSTOMER\",
    \"productCategory\": \"BLOOD_PRODUCTS\",
    \"orderItems\": [
      {
        \"productFamily\": \"RED_BLOOD_CELLS_LEUKOREDUCED\",
        \"bloodType\": \"O-\",
        \"quantity\": 1,
        \"comments\": \"Kafka event publishing test\"
      }
    ]
  }" \
  "http://localhost:8090/v1/partner-order-provider/orders")

echo "📋 Order submission response:"
echo "$ORDER_RESPONSE" | jq '.' 2>/dev/null || echo "$ORDER_RESPONSE"

# Extract transaction ID
TRANSACTION_ID=$(echo "$ORDER_RESPONSE" | jq -r '.transactionId' 2>/dev/null || echo "")

if [[ "$TRANSACTION_ID" == "null" || -z "$TRANSACTION_ID" ]]; then
    echo "❌ Failed to submit order or extract transaction ID"
    exit 1
fi

echo "✅ Order submitted successfully with transaction ID: $TRANSACTION_ID"

echo ""
echo "⏳ Waiting for event processing (10 seconds)..."
sleep 10

echo ""
echo "🔍 Checking for Published Events:"
echo "================================="

# Check OrderRejected topic for our event
echo "📨 Checking OrderRejected topic for events..."
REJECTED_EVENTS=$(kubectl exec "$KAFKA_POD" -- timeout 5s kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic OrderRejected \
    --from-beginning \
    --max-messages 50 2>/dev/null | grep "$TRANSACTION_ID" || echo "")

if [[ -n "$REJECTED_EVENTS" ]]; then
    echo "✅ Found OrderRejected event for transaction: $TRANSACTION_ID"
    echo "$REJECTED_EVENTS" | jq '.' 2>/dev/null || echo "$REJECTED_EVENTS"
else
    echo "❌ No OrderRejected event found for transaction: $TRANSACTION_ID"
fi

# Check OrderRecieved topic for our event
echo ""
echo "📨 Checking OrderRecieved topic for events..."
RECEIVED_EVENTS=$(kubectl exec "$KAFKA_POD" -- timeout 5s kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic OrderRecieved \
    --from-beginning \
    --max-messages 50 2>/dev/null | grep "$TRANSACTION_ID" || echo "")

if [[ -n "$RECEIVED_EVENTS" ]]; then
    echo "✅ Found OrderReceived event for transaction: $TRANSACTION_ID"
    echo "$RECEIVED_EVENTS" | jq '.' 2>/dev/null || echo "$RECEIVED_EVENTS"
else
    echo "❌ No OrderReceived event found for transaction: $TRANSACTION_ID"
fi

echo ""
echo "🔍 Checking Interface Exception Collector Processing:"
echo "===================================================="

# Wait a bit more for the Interface Exception Collector to process
echo "⏳ Waiting for Interface Exception Collector processing (5 seconds)..."
sleep 5

# Check Interface Exception Collector logs for our transaction
echo "📋 Recent Interface Exception Collector logs:"
kubectl logs "$IEC_POD" --tail=20 | grep -i "$TRANSACTION_ID\|orderrejected\|exception" || echo "No relevant log entries found"

# Try to query the exceptions API
echo ""
echo "🔍 Querying Interface Exception Collector API:"
EXCEPTIONS_RESPONSE=$(curl -s "http://localhost:8080/api/v1/exceptions" 2>/dev/null || echo "")

if [[ -n "$EXCEPTIONS_RESPONSE" ]]; then
    echo "📋 Recent exceptions:"
    echo "$EXCEPTIONS_RESPONSE" | jq '.content[] | select(.transactionId == "'$TRANSACTION_ID'") // empty' 2>/dev/null || echo "No matching exceptions found"
else
    echo "❌ Could not query exceptions API (authentication required)"
    echo "   💡 Generate token: node scripts/generate-admin-token.js"
fi

echo ""
echo "📊 Test Summary:"
echo "================"

if [[ -n "$REJECTED_EVENTS" || -n "$RECEIVED_EVENTS" ]]; then
    echo "✅ SUCCESS: Kafka events are being published by Partner Order Service"
    
    if [[ -n "$REJECTED_EVENTS" ]]; then
        echo "   ✅ OrderRejected events: Published"
    else
        echo "   ❌ OrderRejected events: Not found"
    fi
    
    if [[ -n "$RECEIVED_EVENTS" ]]; then
        echo "   ✅ OrderReceived events: Published"
    else
        echo "   ❌ OrderReceived events: Not found"
    fi
    
    echo ""
    echo "🎯 Next Steps:"
    echo "   1. Check Interface Exception Collector logs: kubectl logs -f $IEC_POD"
    echo "   2. Generate auth token and query exceptions API"
    echo "   3. Test retry functionality"
    
else
    echo "❌ FAILURE: No Kafka events found"
    echo ""
    echo "🔧 Troubleshooting:"
    echo "   1. Check Partner Order Service logs: kubectl logs $POS_POD"
    echo "   2. Check Kafka connectivity: kubectl exec $KAFKA_POD -- kafka-topics --bootstrap-server localhost:9092 --list"
    echo "   3. Verify service configuration"
fi

echo ""
echo "🛠️  Useful Commands:"
echo "==================="
echo "• View Partner Order Service logs: kubectl logs -f $POS_POD"
echo "• View Interface Exception Collector logs: kubectl logs -f $IEC_POD"
echo "• Monitor Kafka topics: kubectl exec $KAFKA_POD -- kafka-console-consumer --bootstrap-server localhost:9092 --topic OrderRejected --from-beginning"
echo "• Test order submission: curl -X POST -H 'Content-Type: application/json' -d '{...}' http://localhost:8090/v1/partner-order-provider/orders"

echo ""
echo "✨ Test completed!"