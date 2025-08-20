#!/bin/bash
echo "🔧 Testing Database Constraints and Empty Result Handling"
echo "========================================================"

# First, let's check the current table structure
echo "1. Checking current table structure..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    \d interface_exceptions;
"

echo ""
echo "2. Checking current table contents..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT COUNT(*) as record_count FROM interface_exceptions;
"

echo ""
echo "3. Testing API with empty table (current state)..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
EMPTY_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
EMPTY_CODE="${EMPTY_RESPONSE: -3}"
EMPTY_BODY="${EMPTY_RESPONSE%???}"
echo "Empty table HTTP Code: $EMPTY_CODE"
echo "Empty table Response: $EMPTY_BODY"

if [ "$EMPTY_CODE" = "503" ]; then
    echo "❌ Confirmed: Empty table returns 503"
    echo ""
    echo "4. Checking application logs for the exact error..."
    kubectl logs -l app=interface-exception-collector --tail=10
fi

echo ""
echo "5. Inserting a test record with all required fields and valid enum values..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    INSERT INTO interface_exceptions (
        transaction_id, interface_type, exception_reason, operation, 
        status, severity, category, retryable, timestamp, 
        retry_count, max_retries, created_at, updated_at
    ) VALUES (
        'test-constraint-123', 'ORDER', 'Test constraint exception', 'CREATE', 
        'NEW', 'MEDIUM', 'BUSINESS_RULE', true, NOW(), 
        0, 3, NOW(), NOW()
    );
"

echo ""
echo "6. Verifying the insert worked..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT transaction_id, interface_type, exception_reason, status, category 
    FROM interface_exceptions 
    WHERE transaction_id = 'test-constraint-123';
"

echo ""
echo "7. Testing API with data in table..."
WITH_DATA_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
WITH_DATA_CODE="${WITH_DATA_RESPONSE: -3}"
WITH_DATA_BODY="${WITH_DATA_RESPONSE%???}"
echo "With data HTTP Code: $WITH_DATA_CODE"
echo "With data Response: $WITH_DATA_BODY"

if [ "$WITH_DATA_CODE" = "200" ]; then
    echo "✅ SUCCESS! API works when table has data"
else
    echo "❌ Still failing even with data. Checking logs..."
    kubectl logs -l app=interface-exception-collector --tail=10
fi

echo ""
echo "8. Cleaning up test data..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    DELETE FROM interface_exceptions WHERE transaction_id = 'test-constraint-123';
"

echo ""
echo "9. Testing API with empty table again..."
FINAL_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
FINAL_CODE="${FINAL_RESPONSE: -3}"
FINAL_BODY="${FINAL_RESPONSE%???}"
echo "Final empty table HTTP Code: $FINAL_CODE"
echo "Final empty table Response: $FINAL_BODY"

echo ""
echo "=== DIAGNOSIS ==="
if [ "$EMPTY_CODE" = "503" ] && [ "$WITH_DATA_CODE" = "200" ] && [ "$FINAL_CODE" = "503" ]; then
    echo "🎯 ISSUE IDENTIFIED: The application fails when the table is empty"
    echo "   - This suggests the issue is in empty result handling, not schema constraints"
    echo "   - The JPA query or result mapping doesn't handle empty collections properly"
    echo "   - Need to check the repository method and controller for proper empty handling"
elif [ "$WITH_DATA_CODE" != "200" ]; then
    echo "🎯 ISSUE IDENTIFIED: Schema or mapping problem"
    echo "   - The application fails even with valid data"
    echo "   - Check JPA entity mapping and database schema alignment"
else
    echo "🎯 ISSUE RESOLVED: All tests passed"
fi