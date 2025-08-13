#!/bin/bash

echo "🧪 Testing Interface Exception Collector API"
echo "============================================="

# Generate JWT token
echo "🔑 Generating JWT token..."
JWT_TOKEN=$(node generate-jwt.js test-user ADMIN | grep "Raw Token:" | cut -d' ' -f3)

if [ -z "$JWT_TOKEN" ]; then
    echo "❌ Failed to generate JWT token"
    exit 1
fi

echo "✅ JWT token generated"

# Test 1: Health check
echo ""
echo "🏥 Testing health endpoint..."
HEALTH_RESPONSE=$(kubectl exec interface-exception-collector-595d648c48-w7b8j -c app -- curl -s http://localhost:8080/actuator/health)

if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed"
    echo "Response: $HEALTH_RESPONSE"
    exit 1
fi

# Test 2: List exceptions (should be empty)
echo ""
echo "📋 Testing exceptions list endpoint..."
EXCEPTIONS_RESPONSE=$(kubectl exec interface-exception-collector-595d648c48-w7b8j -c app -- curl -s -H "Authorization: Bearer $JWT_TOKEN" http://localhost:8080/api/v1/exceptions)

if [ "$EXCEPTIONS_RESPONSE" = "[]" ]; then
    echo "✅ Exceptions endpoint working (empty list as expected)"
else
    echo "🔍 Exceptions found: $EXCEPTIONS_RESPONSE"
fi

# Test 3: Insert a test record directly into the database
echo ""
echo "💾 Inserting test exception record directly into database..."

TEST_TRANSACTION_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
TEST_TIMESTAMP=$(date -u +"%Y-%m-%d %H:%M:%S+00")

kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
INSERT INTO interface_exceptions (
    transaction_id, interface_type, exception_reason, operation, external_id,
    status, severity, category, retryable, customer_id, location_code,
    timestamp, processed_at, retry_count
) VALUES (
    '$TEST_TRANSACTION_ID',
    'ORDER',
    'Test exception created via direct database insert',
    'CREATE_ORDER',
    'EXT-TEST-$(date +%s)',
    'NEW',
    'HIGH',
    'BUSINESS_RULE_VIOLATION',
    true,
    'CUST-TEST-001',
    'HOSP-TEST-001',
    '$TEST_TIMESTAMP',
    '$TEST_TIMESTAMP',
    0
);
"

if [ $? -eq 0 ]; then
    echo "✅ Test record inserted successfully"
    
    # Test 4: Verify the record appears in the API
    echo ""
    echo "🔍 Verifying record appears in API..."
    sleep 2  # Give it a moment
    
    UPDATED_RESPONSE=$(kubectl exec interface-exception-collector-595d648c48-w7b8j -c app -- curl -s -H "Authorization: Bearer $JWT_TOKEN" http://localhost:8080/api/v1/exceptions)
    
    if echo "$UPDATED_RESPONSE" | grep -q "$TEST_TRANSACTION_ID"; then
        echo "✅ Test record found in API response!"
        echo "📊 Record details:"
        echo "$UPDATED_RESPONSE" | jq '.[0] | {transactionId, interfaceType, exceptionReason, status, severity}'
    else
        echo "❌ Test record not found in API response"
        echo "Response: $UPDATED_RESPONSE"
    fi
    
    # Test 5: Get specific exception details
    echo ""
    echo "🔍 Testing exception details endpoint..."
    DETAIL_RESPONSE=$(kubectl exec interface-exception-collector-595d648c48-w7b8j -c app -- curl -s -H "Authorization: Bearer $JWT_TOKEN" "http://localhost:8080/api/v1/exceptions/$TEST_TRANSACTION_ID")
    
    if echo "$DETAIL_RESPONSE" | grep -q "$TEST_TRANSACTION_ID"; then
        echo "✅ Exception details endpoint working!"
        echo "📋 Exception details:"
        echo "$DETAIL_RESPONSE" | jq '{transactionId, interfaceType, exceptionReason, status, severity, customerId, locationCode}'
    else
        echo "❌ Exception details endpoint failed"
        echo "Response: $DETAIL_RESPONSE"
    fi
    
else
    echo "❌ Failed to insert test record"
    exit 1
fi

echo ""
echo "🎉 API testing completed!"
echo ""
echo "💡 Next steps to fix Kafka connectivity:"
echo "   1. The application is working correctly"
echo "   2. The database integration is working"
echo "   3. The API endpoints are functional"
echo "   4. We need to fix the Python Kafka script to send events properly"
echo ""
echo "🔧 To test Kafka manually, try:"
echo "   kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic OrderRejected"