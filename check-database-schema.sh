#!/bin/bash

echo "🔍 Checking Database Schema"
echo "=========================="

echo "1. Checking if PostgreSQL is accessible..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "SELECT version();" || {
    echo "❌ Cannot connect to PostgreSQL"
    exit 1
}

echo ""
echo "2. Checking if interface_exceptions table exists..."
TABLE_EXISTS=$(kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -t -c "
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'interface_exceptions'
    );
" | tr -d ' ')

echo "Table exists: $TABLE_EXISTS"

if [ "$TABLE_EXISTS" = "f" ]; then
    echo "❌ interface_exceptions table does not exist!"
    echo ""
    echo "3. Checking what tables do exist..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
        SELECT table_name FROM information_schema.tables 
        WHERE table_schema = 'public' 
        ORDER BY table_name;
    "
    
    echo ""
    echo "4. Checking if Flyway migration history exists..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
        SELECT table_name FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name LIKE '%flyway%' OR table_name LIKE '%schema%';
    "
    
    echo ""
    echo "5. This explains the 503 error! The application is trying to query a table that doesn't exist."
    echo "   We need to run database migrations to create the schema."
    
else
    echo "✅ interface_exceptions table exists"
    echo ""
    echo "3. Checking table structure..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
        \d interface_exceptions
    "
    
    echo ""
    echo "4. Checking if table has any data..."
    kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
        SELECT COUNT(*) as row_count FROM interface_exceptions;
    "
fi

echo ""
echo "6. Checking migration job status..."
if kubectl get job migration-job >/dev/null 2>&1; then
    echo "Migration job exists:"
    kubectl get job migration-job
    echo ""
    echo "Migration job logs:"
    kubectl logs job/migration-job --tail=10
else
    echo "❌ Migration job does not exist!"
fi