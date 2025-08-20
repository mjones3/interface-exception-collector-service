#!/bin/bash
echo "🔍 Checking Schema Mismatch Between JPA Entity and Database"
echo "=========================================================="

echo "1. Getting actual database schema..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT column_name, data_type, is_nullable, character_maximum_length, column_default
    FROM information_schema.columns 
    WHERE table_name = 'interface_exceptions' 
    ORDER BY ordinal_position;
"

echo ""
echo "2. Checking if the table exists and has the expected structure..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT 
        table_name,
        column_name,
        data_type,
        is_nullable,
        column_default
    FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'interface_exceptions'
    ORDER BY ordinal_position;
"

echo ""
echo "3. Checking table constraints..."
kubectl exec deployment/postgres -- psql -U exception_user -d exception_collector_db -c "
    SELECT 
        tc.constraint_name,
        tc.constraint_type,
        kcu.column_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu 
        ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'interface_exceptions';
"

echo ""
echo "4. Getting more detailed application logs to see the actual error..."
kubectl logs -l app=interface-exception-collector --tail=50 | grep -E "(ERROR|Exception|SQLException|JPA|Hibernate)" || echo "No specific database errors found in recent logs"

echo ""
echo "5. Checking if Hibernate is trying to create/validate the schema..."
kubectl logs -l app=interface-exception-collector --tail=100 | grep -E "(schema|table|column|DDL)" || echo "No schema-related logs found"