#!/bin/bash
echo "🔧 Testing AutoCommit Fix"
echo "========================="

echo "1. Restarting the application to apply the autoCommit fix..."
kubectl rollout restart deployment/interface-exception-collector

echo ""
echo "2. Waiting for the application to be ready..."
kubectl rollout status deployment/interface-exception-collector --timeout=120s

echo ""
echo "3. Waiting a bit more for the application to fully initialize..."
sleep 10

echo ""
echo "4. Testing the API endpoint..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo ""
    echo "🎉 SUCCESS! The autoCommit fix worked!"
    echo "✅ API now returns 200 OK"
    echo "✅ No more 'Cannot commit when autoCommit is enabled' error"
    echo ""
    echo "The issue was that PostgreSQL connections had autoCommit=true by default,"
    echo "but Spring's @Transactional annotation was trying to manage transactions manually."
    echo "Setting 'auto-commit: false' in HikariCP configuration resolved the conflict."
elif [ "$HTTP_CODE" = "503" ]; then
    echo ""
    echo "❌ Still getting 503 error. Let's check the logs for any remaining issues..."
    kubectl logs -l app=interface-exception-collector --tail=20
else
    echo ""
    echo "⚠️  Unexpected response code: $HTTP_CODE"
    echo "Let's check the application logs..."
    kubectl logs -l app=interface-exception-collector --tail=15
fi

echo ""
echo "5. Testing with some data in the table..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    INSERT INTO interface_exceptions (
        transaction_id, interface_type, exception_reason, operation, 
        status, severity, category, retryable, timestamp, 
        retry_count, max_retries, created_at, updated_at
    ) VALUES (
        'test-autocommit-fix', 'ORDER', 'Test after autocommit fix', 'CREATE', 
        'NEW', 'MEDIUM', 'BUSINESS_RULE', true, NOW(), 
        0, 3, NOW(), NOW()
    );
" > /dev/null

echo ""
echo "6. Testing API with data..."
WITH_DATA_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
WITH_DATA_CODE="${WITH_DATA_RESPONSE: -3}"
WITH_DATA_BODY="${WITH_DATA_RESPONSE%???}"

echo "With data HTTP Code: $WITH_DATA_CODE"
if [ "$WITH_DATA_CODE" = "200" ]; then
    echo "✅ API works with data too!"
    echo "Response contains: $(echo "$WITH_DATA_BODY" | jq -r '.[0].transactionId // "No transaction ID found"' 2>/dev/null || echo "Response received")"
else
    echo "❌ Still issues with data. Response: $WITH_DATA_BODY"
fi

echo ""
echo "7. Cleaning up test data..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    DELETE FROM interface_exceptions WHERE transaction_id = 'test-autocommit-fix';
" > /dev/null

echo ""
echo "8. Final test with empty table..."
FINAL_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
FINAL_CODE="${FINAL_RESPONSE: -3}"

echo "Final empty table HTTP Code: $FINAL_CODE"
if [ "$FINAL_CODE" = "200" ]; then
    echo "🎉 PERFECT! Empty table also returns 200 OK"
    echo "✅ The autoCommit configuration fix completely resolved the issue!"
else
    echo "❌ Still issues with empty table"
fi