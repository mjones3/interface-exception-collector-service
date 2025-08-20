#!/bin/bash

echo "🎯 Testing with CORRECT Secret"
echo "=============================="

echo "Service is using: 'dev-secret-key-1234567890123456789012345678901234567890'"
echo "We were using:    'mySecretKey1234567890123456789012345678901234567890'"
echo ""

# Generate token with correct secret
echo "Generating token with correct secret..."
TOKEN=$(node generate-jwt-correct-secret.js "test-user" "ADMIN" | tail -1)

echo "Token: ${TOKEN:0:50}..."
echo ""

# Test the token
echo "Testing JWT authentication..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    echo "🎉 SUCCESS! JWT authentication is now working!"
    echo "Response: ${RESPONSE_BODY:0:100}..."
    
    echo ""
    echo "Testing different roles with correct secret..."
    
    # Test OPERATOR
    echo "Testing OPERATOR role..."
    OPERATOR_TOKEN=$(node generate-jwt-correct-secret.js "ops-user" "OPERATOR" | tail -1)
    OPERATOR_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $OPERATOR_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    OPERATOR_CODE="${OPERATOR_RESPONSE: -3}"
    echo "OPERATOR role: HTTP $OPERATOR_CODE"
    
    # Test VIEWER
    echo "Testing VIEWER role..."
    VIEWER_TOKEN=$(node generate-jwt-correct-secret.js "viewer-user" "VIEWER" | tail -1)
    VIEWER_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $VIEWER_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    VIEWER_CODE="${VIEWER_RESPONSE: -3}"
    echo "VIEWER role: HTTP $VIEWER_CODE"
    
    echo ""
    echo "✅ JWT Authentication is WORKING!"
    echo ""
    echo "🔧 Root Cause Found:"
    echo "   The service is using 'dev-secret-key-...' instead of 'mySecretKey...'"
    echo "   This suggests there's an environment variable JWT_SECRET set"
    echo "   or a different configuration source being used."
    
elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "❌ Still getting authentication error: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    echo ""
    echo "This is unexpected - we used the exact secret from the service logs!"
else
    echo "❌ Unexpected response: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
fi