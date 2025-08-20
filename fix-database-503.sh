#!/bin/bash

echo "🔧 Fixing Database 503 Error"
echo "============================"

echo "1. Checking if PostgreSQL pod is running..."
POSTGRES_STATUS=$(kubectl get pods -l app=postgres -o jsonpath='{.items[0].status.phase}' 2>/dev/null || echo "NotFound")

if [ "$POSTGRES_STATUS" != "Running" ]; then
    echo "❌ PostgreSQL pod is not running. Status: $POSTGRES_STATUS"
    echo "   Starting PostgreSQL..."
    
    # Apply PostgreSQL configuration
    kubectl apply -f k8s/postgres.yaml
    
    echo "   Waiting for PostgreSQL to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
else
    echo "✅ PostgreSQL pod is running"
fi

echo ""
echo "2. Checking PostgreSQL service..."
kubectl get svc postgres || {
    echo "❌ PostgreSQL service not found. Creating service..."
    kubectl apply -f k8s/postgres.yaml
}

echo ""
echo "3. Testing database connectivity..."
echo "   Attempting connection test..."

# Test connection from within cluster
kubectl run db-test --rm -i --tty --restart=Never --image=postgres:15-alpine -- sh -c "
    echo 'Testing connection to postgres:5432...'
    pg_isready -h postgres -p 5432 -U exception_user
    if [ \$? -eq 0 ]; then
        echo '✅ Database is reachable'
        psql -h postgres -U exception_user -d exception_collector_db -c 'SELECT version();'
    else
        echo '❌ Database is not reachable'
    fi
" 2>/dev/null || echo "Connection test failed"

echo ""
echo "4. Checking if database schema exists..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT table_name FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'interface_exceptions';
" || echo "Schema check failed"

echo ""
echo "5. Running database migrations if needed..."
echo "   Checking if migration job exists..."

if kubectl get job migration-job >/dev/null 2>&1; then
    echo "   Migration job exists. Checking status..."
    kubectl get job migration-job
    
    # If job failed, delete and recreate
    JOB_STATUS=$(kubectl get job migration-job -o jsonpath='{.status.conditions[0].type}' 2>/dev/null)
    if [ "$JOB_STATUS" = "Failed" ]; then
        echo "   Migration job failed. Recreating..."
        kubectl delete job migration-job
        kubectl apply -f k8s/migration-job.yaml
        kubectl wait --for=condition=complete job/migration-job --timeout=120s
    fi
else
    echo "   Creating migration job..."
    kubectl apply -f k8s/migration-job.yaml
    kubectl wait --for=condition=complete job/migration-job --timeout=120s
fi

echo ""
echo "6. Restarting application to refresh database connections..."
kubectl rollout restart deployment/interface-exception-collector
kubectl rollout status deployment/interface-exception-collector --timeout=120s

echo ""
echo "7. Testing the API endpoint again..."
sleep 10

# Generate a test token
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)

echo "   Testing with JWT token..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "   HTTP Code: $HTTP_CODE"

case $HTTP_CODE in
    200)
        echo "✅ SUCCESS! Database connection is working!"
        echo "   Response: ${RESPONSE_BODY:0:100}..."
        ;;
    503)
        echo "❌ Still getting 503 error. Database connectivity issue persists."
        echo "   Response: $RESPONSE_BODY"
        echo ""
        echo "🔍 Additional troubleshooting needed:"
        echo "   1. Check PostgreSQL logs: kubectl logs -l app=postgres"
        echo "   2. Check application logs: kubectl logs -l app=interface-exception-collector"
        echo "   3. Verify database credentials and connection string"
        ;;
    401|403)
        echo "⚠️  Authentication error (HTTP $HTTP_CODE) - JWT issue returned"
        ;;
    *)
        echo "⚠️  Unexpected response: HTTP $HTTP_CODE"
        echo "   Response: $RESPONSE_BODY"
        ;;
esac

echo ""
echo "8. Final status check..."
echo "   PostgreSQL pod:"
kubectl get pods -l app=postgres
echo "   Application pod:"
kubectl get pods -l app=interface-exception-collector
echo "   Services:"
kubectl get svc postgres interface-exception-collector