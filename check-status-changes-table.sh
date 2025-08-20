#!/bin/bash
echo "🔍 Checking exception_status_changes Table"
echo "=========================================="

echo "1. Checking exception_status_changes table structure..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable, character_maximum_length
    FROM information_schema.columns 
    WHERE table_name = 'exception_status_changes' 
    ORDER BY ordinal_position;
"

echo ""
echo "2. Checking table constraints for exception_status_changes..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT 
        tc.constraint_name,
        tc.constraint_type,
        kcu.column_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu 
        ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'exception_status_changes';
"

echo ""
echo "3. Testing a simple JPA query by enabling more detailed logging..."
echo "Let's check if there are any specific JPA/Hibernate errors in the logs..."
kubectl logs -l app=interface-exception-collector --tail=100 | grep -i -E "(error|exception|failed|sql)" || echo "No specific errors found"

echo ""
echo "4. Let's try to reproduce the exact error by making another API call and checking logs immediately..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
echo "Making API call..."
curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions" > /dev/null

echo ""
echo "5. Checking logs immediately after API call..."
kubectl logs -l app=interface-exception-collector --tail=20