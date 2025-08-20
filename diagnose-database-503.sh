#!/bin/bash

echo "🔍 Diagnosing Database 503 Error"
echo "================================"

echo "1. Checking Kubernetes pods status..."
kubectl get pods -l app=postgres
echo ""

echo "2. Checking PostgreSQL service..."
kubectl get svc postgres
echo ""

echo "3. Checking if PostgreSQL is accessible from within the cluster..."
kubectl run postgres-test --rm -i --tty --image=postgres:15-alpine -- psql -h postgres -U exception_user -d exception_collector_db -c "SELECT 1;" || echo "Connection failed"
echo ""

echo "4. Checking PostgreSQL pod logs..."
kubectl logs -l app=postgres --tail=20
echo ""

echo "5. Checking application database configuration..."
echo "From ConfigMap:"
kubectl get configmap app-config -o jsonpath='{.data.SPRING_DATASOURCE_URL}'
echo ""
kubectl get configmap app-config -o jsonpath='{.data.SPRING_DATASOURCE_USERNAME}'
echo ""
echo ""

echo "6. Checking application logs for database errors..."
kubectl logs -l app=interface-exception-collector --tail=20 | grep -i "database\|connection\|postgres\|sql" || echo "No database-related logs found"
echo ""

echo "7. Testing direct database connection..."
echo "Attempting to connect to PostgreSQL..."
kubectl exec -it deployment/postgres -- psql -U exception_user -d exception_collector_db -c "SELECT version();" || echo "Direct connection failed"