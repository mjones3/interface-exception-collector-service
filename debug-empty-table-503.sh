#!/bin/bash

echo "🔍 Debugging Empty Table 503 Issue"
echo "=================================="

echo "1. Checking table structure (fixed command)..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable 
    FROM information_schema.columns 
    WHERE table_name = 'interface_exceptions' 
    ORDER BY ordinal_position;
"

echo ""
echo "2. Testing a simple INSERT to see if table works..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    INSERT INTO interface_exceptions (
        transaction_id, interface_type, exception_reason, operation, 
        status, severity, retryable, timestamp, created_at, updated_at
    ) VALUES (
        'test-123', 'ORDER', 'Test exception', 'CREATE', 
        'NEW', 'MEDIUM', true, NOW(), NOW(), NOW()
    );
"

echo ""
echo "3. Checking if the insert worked..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT transaction_id, interface_type, exception_reason, status 
    FROM interface_exceptions 
    WHERE transaction_id = 'test-123';
"

echo ""
echo "4. Now testing the API endpoint with data in the table..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ SUCCESS! The issue was an empty table causing problems with the query!"
elif [ "$HTTP_CODE" = "503" ]; then
    echo "❌ Still 503. Let's check application logs for the actual error..."
    echo ""
    echo "5. Checking application logs for database errors..."
    kubectl logs -l app=interface-exception-collector --tail=20 | grep -i "error\|exception\|sql\|database" || echo "No error logs found"
    
    echo ""
    echo "6. Let's test a simpler query directly..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
        SELECT COUNT(*) FROM interface_exceptions;
    "
    
    echo ""
    echo "7. The issue might be in the JPA/Hibernate configuration. Let's check the entity mapping..."
    echo "   The application might be expecting different column names or types."
fi

echo ""
echo "8. Cleaning up test data..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    DELETE FROM interface_exceptions WHERE transaction_id = 'test-123';
"