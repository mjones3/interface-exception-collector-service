#!/bin/bash
echo "🔍 Checking Related Tables and JPA Relationships"
echo "==============================================="

echo "1. Checking if related tables exist..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_type = 'BASE TABLE'
    ORDER BY table_name;
"

echo ""
echo "2. Checking for retry_attempts table..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns 
    WHERE table_name = 'retry_attempts' 
    ORDER BY ordinal_position;
" || echo "retry_attempts table does not exist"

echo ""
echo "3. Checking for order_items table..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns 
    WHERE table_name = 'order_items' 
    ORDER BY ordinal_position;
" || echo "order_items table does not exist"

echo ""
echo "4. Checking for status_changes table..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns 
    WHERE table_name = 'status_changes' 
    ORDER BY ordinal_position;
" || echo "status_changes table does not exist"

echo ""
echo "5. Getting more specific application startup logs..."
kubectl logs -l app=interface-exception-collector --tail=200 | grep -E "(JPA|Hibernate|Entity|Table|Column|Foreign|Constraint)" || echo "No JPA/Hibernate logs found"

echo ""
echo "6. Testing a simple direct database query to see if basic connectivity works..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT COUNT(*) as total_records FROM interface_exceptions;
"