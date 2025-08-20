#!/bin/bash

echo "🔍 Checking JPA Entity Mapping"
echo "=============================="

echo "1. Checking application logs for JPA/Hibernate errors..."
kubectl logs -l app=interface-exception-collector --tail=50 | grep -i "hibernate\|jpa\|entity\|table\|column\|sql" || echo "No JPA-related logs found"

echo ""
echo "2. Checking if Hibernate is trying to validate the schema..."
echo "   Looking for DDL validation errors..."
kubectl logs -l app=interface-exception-collector --tail=100 | grep -i "ddl\|validate\|schema" || echo "No DDL validation logs found"

echo ""
echo "3. Checking the application configuration for JPA settings..."
echo "   From ConfigMap:"
kubectl get configmap app-config -o jsonpath='{.data.SPRING_JPA_HIBERNATE_DDL_AUTO}'
echo ""

echo ""
echo "4. Let's check what the actual error is by looking at recent application logs..."
echo "   Recent application logs:"
kubectl logs -l app=interface-exception-collector --tail=30

echo ""
echo "5. Testing database connectivity from the application pod..."
kubectl exec deployment/interface-exception-collector -- sh -c "
    echo 'Testing database connection from application pod...'
    nc -zv postgres 5432 || echo 'Cannot reach postgres:5432'
"

echo ""
echo "6. Checking if there are any connection pool issues..."
kubectl logs -l app=interface-exception-collector --tail=100 | grep -i "connection\|pool\|timeout" || echo "No connection pool logs found"