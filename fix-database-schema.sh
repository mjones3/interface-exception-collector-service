#!/bin/bash

echo "🔧 Fixing Database Schema Issue"
echo "==============================="

echo "1. Checking if migration job exists..."
if kubectl get job migration-job >/dev/null 2>&1; then
    echo "Migration job exists. Checking status..."
    JOB_STATUS=$(kubectl get job migration-job -o jsonpath='{.status.conditions[0].type}' 2>/dev/null)
    echo "Job status: $JOB_STATUS"
    
    if [ "$JOB_STATUS" = "Failed" ]; then
        echo "Migration job failed. Deleting and recreating..."
        kubectl delete job migration-job
    elif [ "$JOB_STATUS" = "Complete" ]; then
        echo "Migration job completed, but schema might still be missing. Checking..."
        
        TABLE_EXISTS=$(kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -t -c "
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_schema = 'public' 
                AND table_name = 'interface_exceptions'
            );
        " | tr -d ' ')
        
        if [ "$TABLE_EXISTS" = "f" ]; then
            echo "Table still doesn't exist. Recreating migration job..."
            kubectl delete job migration-job
        else
            echo "✅ Table exists. The issue might be elsewhere."
            exit 0
        fi
    fi
fi

echo ""
echo "2. Creating/Running database migration job..."
kubectl apply -f k8s/migration-job.yaml

echo ""
echo "3. Waiting for migration to complete..."
kubectl wait --for=condition=complete job/migration-job --timeout=120s

echo ""
echo "4. Checking migration job logs..."
kubectl logs job/migration-job

echo ""
echo "5. Verifying table creation..."
TABLE_EXISTS=$(kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -t -c "
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'interface_exceptions'
    );
" | tr -d ' ')

if [ "$TABLE_EXISTS" = "t" ]; then
    echo "✅ interface_exceptions table created successfully!"
    
    echo ""
    echo "6. Checking table structure..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "\d interface_exceptions"
    
    echo ""
    echo "7. Restarting application to refresh connections..."
    kubectl rollout restart deployment/interface-exception-collector
    kubectl rollout status deployment/interface-exception-collector --timeout=120s
    
    echo ""
    echo "8. Testing the API endpoint..."
    sleep 10
    
    TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
    RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    HTTP_CODE="${RESPONSE: -3}"
    RESPONSE_BODY="${RESPONSE%???}"
    
    echo "HTTP Code: $HTTP_CODE"
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "🎉 SUCCESS! Database schema fixed and API is working!"
        echo "Response: $RESPONSE_BODY"
    elif [ "$HTTP_CODE" = "503" ]; then
        echo "⚠️  Still getting 503. There might be another database issue."
        echo "Response: $RESPONSE_BODY"
    else
        echo "Response: $RESPONSE_BODY"
    fi
    
else
    echo "❌ Table creation failed!"
    echo ""
    echo "Migration job logs:"
    kubectl logs job/migration-job
    echo ""
    echo "PostgreSQL logs:"
    kubectl logs -l app=postgres --tail=10
fi