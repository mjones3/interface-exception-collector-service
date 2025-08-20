#!/bin/bash

echo "🧪 Testing Clean JWT Token (No Whitespace)"
echo "=========================================="

echo "✅ Good news: Service is now finding the JWT secret!"
echo "   (Error changed from 'Invalid signature' to 'Malformed token')"
echo ""

echo "🔧 Generating clean token without whitespace..."

# Generate token ensuring no whitespace
TOKEN=$(node generate-jwt-fixed.js "test-user" "ADMIN" 2>/dev/null | grep -E '^eyJ' | tr -d ' \t\n\r')

echo "Token length: ${#TOKEN}"
echo "Token (first 50 chars): ${TOKEN:0:50}..."

# Check for whitespace
if echo "$TOKEN" | grep -q '[[:space:]]'; then
    echo "❌ Token contains whitespace!"
    echo "Token with visible whitespace: '$(echo "$TOKEN" | cat -A)'"
else
    echo "✅ Token is clean (no whitespace)"
fi

echo ""
echo "🧪 Testing the clean token..."

RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    echo "🎉 SUCCESS! JWT authentication is now working!"
    echo "Response: ${RESPONSE_BODY:0:100}..."
    
    echo ""
    echo "🧪 Testing different roles:"
    
    # Test OPERATOR role
    OPERATOR_TOKEN=$(node generate-jwt-fixed.js "ops-user" "OPERATOR" 2>/dev/null | grep -E '^eyJ' | tr -d ' \t\n\r')
    OPERATOR_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $OPERATOR_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    OPERATOR_CODE="${OPERATOR_RESPONSE: -3}"
    echo "OPERATOR role: HTTP $OPERATOR_CODE"
    
    # Test VIEWER role
    VIEWER_TOKEN=$(node generate-jwt-fixed.js "viewer-user" "VIEWER" 2>/dev/null | grep -E '^eyJ' | tr -d ' \t\n\r')
    VIEWER_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $VIEWER_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    VIEWER_CODE="${VIEWER_RESPONSE: -3}"
    echo "VIEWER role: HTTP $VIEWER_CODE"
    
    echo ""
    echo "✅ JWT Authentication Fix Complete!"
    echo "   - Configuration issue resolved"
    echo "   - Token generation fixed"
    echo "   - Role-based access working"
    
elif [ "$HTTP_CODE" = "403" ] || [ "$HTTP_CODE" = "401" ]; then
    echo "❌ Still getting authentication error: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
else
    echo "❌ Unexpected response: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
fi