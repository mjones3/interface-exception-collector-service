#!/bin/bash

echo "🔍 Testing Kafka Event Deserialization Fix"
echo "=========================================="

# Function to check if a pod is ready
wait_for_pod() {
    local pod_name=$1
    echo "⏳ Waiting for $pod_name to be ready..."
    kubectl wait --for=condition=ready pod -l app=$pod_name --timeout=60s
}

# Function to test Kafka connectivity
test_kafka_connectivity() {
    echo "🔗 Testing Kafka connectivity..."
    kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ Kafka is accessible"
        return 0
    else
        echo "❌ Kafka is not accessible"
        return 1
    fi
}

# Function to check consumer group status
check_consumer_group() {
    echo "👥 Checking consumer group status..."
    kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group interface-exception-collector
}

# Function to send a test order and check if it's processed
test_order_processing() {
    echo "📦 Sending test order to Partner Order Service..."
    
    # Send a test order
    kubectl exec deployment/partner-order-service -- curl -s -X POST http://localhost:8090/api/v1/orders \
        -H "Content-Type: application/json" \
        -d '{
            "externalId": "TEST-KAFKA-FIX-'$(date +%s)'",
            "orderStatus": "OPEN",
            "locationCode": "TEST-LOCATION",
            "shipmentType": "CUSTOMER",
            "productCategory": "BLOOD_PRODUCTS",
            "orderItems": [
                {
                    "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                    "bloodType": "O-",
                    "quantity": 1,
                    "comments": "Testing Kafka deserialization fix"
                }
            ]
        }' > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ Test order sent successfully"
        return 0
    else
        echo "❌ Failed to send test order"
        return 1
    fi
}

# Function to check Interface Exception Collector logs for processing
check_exception_collector_logs() {
    echo "📋 Checking Interface Exception Collector logs for event processing..."
    
    # Wait a moment for processing
    sleep 5
    
    # Check for successful event processing in logs
    kubectl logs deployment/interface-exception-collector --tail=50 | grep -i "processing.*orderrejected\|successfully.*processed.*orderrejected\|received.*orderrejected"
    
    if [ $? -eq 0 ]; then
        echo "✅ Found OrderRejected event processing in logs"
        return 0
    else
        echo "⚠️  No OrderRejected event processing found in recent logs"
        return 1
    fi
}

# Function to check for any deserialization errors
check_deserialization_errors() {
    echo "🔍 Checking for deserialization errors..."
    
    kubectl logs deployment/interface-exception-collector --tail=100 | grep -i "deserializ\|json\|parse.*error\|failed.*process"
    
    if [ $? -eq 0 ]; then
        echo "⚠️  Found potential deserialization issues"
        return 1
    else
        echo "✅ No deserialization errors found"
        return 0
    fi
}

# Main execution
main() {
    echo "Starting Kafka deserialization test..."
    echo ""
    
    # Check if pods are ready
    wait_for_pod "kafka"
    wait_for_pod "interface-exception-collector" 
    wait_for_pod "partner-order-service"
    
    echo ""
    
    # Test Kafka connectivity
    if ! test_kafka_connectivity; then
        echo "❌ Cannot proceed - Kafka is not accessible"
        exit 1
    fi
    
    echo ""
    
    # Check initial consumer group status
    echo "📊 Initial consumer group status:"
    check_consumer_group
    
    echo ""
    
    # Send test order
    if ! test_order_processing; then
        echo "❌ Cannot proceed - Failed to send test order"
        exit 1
    fi
    
    echo ""
    
    # Check for event processing
    check_exception_collector_logs
    processing_result=$?
    
    echo ""
    
    # Check for deserialization errors
    check_deserialization_errors
    error_result=$?
    
    echo ""
    echo "=========================================="
    echo "🏁 Test Results Summary:"
    echo "=========================================="
    
    if [ $processing_result -eq 0 ] && [ $error_result -eq 0 ]; then
        echo "✅ SUCCESS: Kafka deserialization appears to be working correctly"
        echo "   - OrderRejected events are being processed"
        echo "   - No deserialization errors detected"
    elif [ $processing_result -eq 0 ] && [ $error_result -ne 0 ]; then
        echo "⚠️  PARTIAL SUCCESS: Events are processing but there may be some errors"
        echo "   - OrderRejected events are being processed"
        echo "   - Some deserialization issues detected"
    elif [ $processing_result -ne 0 ] && [ $error_result -eq 0 ]; then
        echo "⚠️  ISSUE: No event processing detected but no obvious errors"
        echo "   - OrderRejected events are NOT being processed"
        echo "   - No deserialization errors detected"
    else
        echo "❌ FAILURE: Events are not being processed and errors detected"
        echo "   - OrderRejected events are NOT being processed"
        echo "   - Deserialization errors detected"
    fi
    
    echo ""
    echo "📋 Final consumer group status:"
    check_consumer_group
}

# Run the test
main "$@"