#!/bin/bash

echo "🔧 Testing JWT After Configuration Fix"
echo "======================================"

echo "1. The service needs to be restarted to pick up the new configuration."
echo "   Please restart the service and then run this test."
echo ""

echo "2. Generating token..."
TOKEN=$(node generate-jwt-fixed.js "test-user" "ADMIN" 2>/dev/null | tail -1)
echo "Token: ${TOKEN:0:50}..."

echo ""
echo "3. Testing token against API..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response Body: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ SUCCESS: JWT authentication is now working!"
    echo ""
    echo "4. Testing different roles..."
    
    # Test OPERATOR role
    OPERATOR_TOKEN=$(node generate-jwt-fixed.js "ops-user" "OPERATOR" 2>/dev/null | tail -1)
    OPERATOR_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $OPERATOR_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    OPERATOR_CODE="${OPERATOR_RESPONSE: -3}"
    echo "OPERATOR role test: HTTP $OPERATOR_CODE"
    
    # Test VIEWER role
    VIEWER_TOKEN=$(node generate-jwt-fixed.js "viewer-user" "VIEWER" 2>/dev/null | tail -1)
    VIEWER_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $VIEWER_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    VIEWER_CODE="${VIEWER_RESPONSE: -3}"
    echo "VIEWER role test: HTTP $VIEWER_CODE"
    
elif [ "$HTTP_CODE" = "403" ]; then
    echo "❌ Still getting 403 - check if service was restarted"
    echo "   The service needs to restart to pick up the new app.security.jwt.secret configuration"
else
    echo "❌ Unexpected response code: $HTTP_CODE"
fi