#!/bin/bash
echo "🐛 Debugging DataAccessException"
echo "================================"

echo "1. Temporarily enabling ERROR level logging for the application..."
kubectl exec deployment/interface-exception-collector -- sh -c "
    echo 'Checking current log level...'
    curl -s http://localhost:8080/actuator/loggers/com.arcone.biopro.exception.collector.api.controller.GlobalExceptionHandler
"

echo ""
echo "2. Setting GlobalExceptionHandler to DEBUG level to see the full exception..."
kubectl exec deployment/interface-exception-collector -- sh -c "
    curl -s -X POST -H 'Content-Type: application/json' \
    -d '{\"configuredLevel\": \"DEBUG\"}' \
    http://localhost:8080/actuator/loggers/com.arcone.biopro.exception.collector.api.controller.GlobalExceptionHandler
"

echo ""
echo "3. Also enabling DEBUG for the repository layer..."
kubectl exec deployment/interface-exception-collector -- sh -c "
    curl -s -X POST -H 'Content-Type: application/json' \
    -d '{\"configuredLevel\": \"DEBUG\"}' \
    http://localhost:8080/actuator/loggers/com.arcone.biopro.exception.collector.infrastructure.repository
"

echo ""
echo "4. Making API call to trigger the exception..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" 2>/dev/null | tail -1)
curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions" > /dev/null

echo ""
echo "5. Checking logs immediately for the full DataAccessException..."
kubectl logs -l app=interface-exception-collector --tail=30 | grep -A 10 -B 5 "Database access error\|DataAccessException\|ERROR" || echo "No ERROR logs found"

echo ""
echo "6. Let's also check if there are any SQL-related errors..."
kubectl logs -l app=interface-exception-collector --tail=50 | grep -i -A 5 -B 5 "sql\|hibernate\|jpa\|constraint\|foreign\|key" || echo "No SQL-related logs found"

echo ""
echo "7. Checking for any startup errors that might indicate schema issues..."
kubectl logs -l app=interface-exception-collector --tail=200 | grep -i -A 3 -B 3 "error\|exception\|failed\|schema\|table" || echo "No startup errors found"