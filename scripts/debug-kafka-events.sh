#!/bin/bash

# Debug script for investigating Kafka event flow between Partner Order Service and Interface Exception Collector

set -e

echo "🔍 BioPro Kafka Event Flow Debugging"
echo "===================================="

# Check if we're running in Kubernetes
if command -v kubectl &> /dev/null; then
    echo "✅ Kubernetes environment detected"
    KAFKA_POD=$(kubectl get pods -l app=kafka -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    
    if [[ -z "$KAFKA_POD" ]]; then
        echo "❌ Kafka pod not found. Is Tilt running?"
        exit 1
    fi
    
    echo "📦 Kafka pod: $KAFKA_POD"
    
    # Function to run kafka commands in k8s
    kafka_cmd() {
        kubectl exec "$KAFKA_POD" -- "$@"
    }
    
    # Check service status
    echo ""
    echo "🏥 Service Health Status:"
    echo "========================"
    
    # Check Partner Order Service
    POS_POD=$(kubectl get pods -l app=partner-order-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -n "$POS_POD" ]]; then
        echo "✅ Partner Order Service pod: $POS_POD"
        POS_STATUS=$(kubectl get pod "$POS_POD" -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
        echo "   Status: $POS_STATUS"
        
        if [[ "$POS_STATUS" == "Running" ]]; then
            echo "   🔗 Testing health endpoint..."
            kubectl exec "$POS_POD" -- curl -s http://localhost:8090/actuator/health | jq '.status' 2>/dev/null || echo "   ❌ Health check failed"
        fi
    else
        echo "❌ Partner Order Service pod not found"
    fi
    
    # Check Interface Exception Collector
    IEC_POD=$(kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -n "$IEC_POD" ]]; then
        echo "✅ Interface Exception Collector pod: $IEC_POD"
        IEC_STATUS=$(kubectl get pod "$IEC_POD" -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
        echo "   Status: $IEC_STATUS"
        
        if [[ "$IEC_STATUS" == "Running" ]]; then
            echo "   🔗 Testing health endpoint..."
            kubectl exec "$IEC_POD" -- curl -s http://localhost:8080/actuator/health | jq '.status' 2>/dev/null || echo "   ❌ Health check failed"
        fi
    else
        echo "❌ Interface Exception Collector pod not found"
    fi
    
else
    echo "❌ kubectl not found. This script requires Kubernetes."
    exit 1
fi

echo ""
echo "📋 Kafka Topics:"
echo "================"

# List all topics
kafka_cmd kafka-topics --bootstrap-server localhost:9092 --list | sort

echo ""
echo "🔍 OrderRejected Topic Details:"
echo "==============================="

# Check if OrderRejected topic exists
if kafka_cmd kafka-topics --bootstrap-server localhost:9092 --list | grep -q "OrderRejected"; then
    echo "✅ OrderRejected topic exists"
    
    # Get topic details
    kafka_cmd kafka-topics --bootstrap-server localhost:9092 --describe --topic OrderRejected
    
    echo ""
    echo "📊 Recent messages in OrderRejected topic (last 10):"
    echo "===================================================="
    
    # Try to consume recent messages (timeout after 5 seconds)
    timeout 5s kafka_cmd kafka-console-consumer \
        --bootstrap-server localhost:9092 \
        --topic OrderRejected \
        --from-beginning \
        --max-messages 10 2>/dev/null || echo "No messages found or timeout reached"
        
else
    echo "❌ OrderRejected topic does not exist"
fi

echo ""
echo "🔍 Consumer Group Status:"
echo "========================"

# Check consumer groups
kafka_cmd kafka-consumer-groups --bootstrap-server localhost:9092 --list | grep -E "(interface-exception-collector|partner-order-service)" || echo "No relevant consumer groups found"

# Check Interface Exception Collector consumer group details
if kafka_cmd kafka-consumer-groups --bootstrap-server localhost:9092 --list | grep -q "interface-exception-collector"; then
    echo ""
    echo "📈 Interface Exception Collector Consumer Group Details:"
    kafka_cmd kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group interface-exception-collector
fi

echo ""
echo "🧪 Test Event Publishing:"
echo "========================="

echo "Would you like to publish a test OrderRejected event? (y/n)"
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    echo "📤 Publishing test OrderRejected event..."
    
    # Create a test event
    TEST_EVENT=$(cat <<EOF
{
  "eventId": "test-$(date +%s)",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)",
  "source": "debug-script",
  "correlationId": "debug-correlation-$(date +%s)",
  "transactionId": "debug-transaction-$(date +%s)",
  "payload": {
    "transactionId": "debug-transaction-$(date +%s)",
    "externalId": "DEBUG-ORDER-$(date +%s)",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Debug test event from troubleshooting script",
    "customerId": "DEBUG-CUSTOMER",
    "locationCode": "DEBUG-LOCATION",
    "orderItems": [
      {
        "bloodType": "O-",
        "productFamily": "RED_BLOOD_CELLS",
        "quantity": 1
      }
    ]
  }
}
EOF
)
    
    # Publish the test event
    echo "$TEST_EVENT" | kafka_cmd kafka-console-producer --bootstrap-server localhost:9092 --topic OrderRejected
    
    echo "✅ Test event published!"
    echo ""
    echo "🔍 Checking if the event was consumed..."
    sleep 3
    
    # Check Interface Exception Collector logs for the test event
    if [[ -n "$IEC_POD" ]]; then
        echo "📋 Recent Interface Exception Collector logs:"
        kubectl logs "$IEC_POD" --tail=20 | grep -i "debug\|orderrejected\|exception" || echo "No relevant log entries found"
    fi
fi

echo ""
echo "🎯 Troubleshooting Recommendations:"
echo "==================================="

if [[ -z "$POS_POD" ]]; then
    echo "❌ Partner Order Service is not running"
    echo "   💡 Run: tilt up (make sure partner-order-service is included)"
elif [[ "$POS_STATUS" != "Running" ]]; then
    echo "❌ Partner Order Service is not in Running state"
    echo "   💡 Check logs: kubectl logs $POS_POD"
    echo "   💡 Check events: kubectl describe pod $POS_POD"
fi

if [[ -z "$IEC_POD" ]]; then
    echo "❌ Interface Exception Collector is not running"
    echo "   💡 Run: tilt up (make sure interface-exception-collector is included)"
elif [[ "$IEC_STATUS" != "Running" ]]; then
    echo "❌ Interface Exception Collector is not in Running state"
    echo "   💡 Check logs: kubectl logs $IEC_POD"
    echo "   💡 Check events: kubectl describe pod $IEC_POD"
fi

echo ""
echo "📚 Useful Commands:"
echo "=================="
echo "• Check Tilt status: tilt status"
echo "• View service logs: kubectl logs -f deployment/partner-order-service"
echo "• View consumer logs: kubectl logs -f deployment/interface-exception-collector"
echo "• Test Partner Order Service: curl http://localhost:8090/actuator/health"
echo "• Test Exception Collector: curl http://localhost:8080/actuator/health"
echo "• View Kafka UI: http://localhost:8081"
echo "• Submit test order: curl -X POST -H 'Content-Type: application/json' -d '{...}' http://localhost:8090/v1/partner-order-provider/orders"

echo ""
echo "✨ Debug script completed!"