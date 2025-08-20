#!/bin/bash

echo "🎯 Final JWT Authentication Test"
echo "==============================="

# Generate clean token
echo "Generating clean JWT token..."
TOKEN=$(node generate-jwt-clean.js "test-user" "ADMIN")

echo "Token: $TOKEN"
echo "Token length: ${#TOKEN}"
echo "Token parts: $(echo "$TOKEN" | tr '.' '\n' | wc -l)"

# Validate token format
if [[ "$TOKEN" =~ ^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$ ]]; then
    echo "✅ Token format is valid"
else
    echo "❌ Token format is invalid"
    exit 1
fi

# Test the token
echo ""
echo "Testing JWT authentication..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"

case $HTTP_CODE in
    200)
        echo "🎉 SUCCESS! JWT authentication is working!"
        echo "Response: ${RESPONSE_BODY:0:100}..."
        
        echo ""
        echo "Testing other roles..."
        
        # Test OPERATOR
        OPERATOR_TOKEN=$(node generate-jwt-clean.js "ops-user" "OPERATOR")
        OPERATOR_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $OPERATOR_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
        OPERATOR_CODE="${OPERATOR_RESPONSE: -3}"
        echo "OPERATOR role: HTTP $OPERATOR_CODE"
        
        # Test VIEWER
        VIEWER_TOKEN=$(node generate-jwt-clean.js "viewer-user" "VIEWER")
        VIEWER_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $VIEWER_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
        VIEWER_CODE="${VIEWER_RESPONSE: -3}"
        echo "VIEWER role: HTTP $VIEWER_CODE"
        
        echo ""
        echo "✅ JWT Authentication Fix Complete!"
        ;;
    401|403)
        echo "❌ Authentication failed: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        ;;
    *)
        echo "❌ Unexpected response: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        ;;
esac