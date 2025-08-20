#!/bin/bash

echo "🧪 Testing Fixed JWT Generation"
echo "==============================="

# Make the script executable
chmod +x generate-jwt-fixed.js

# Generate token with the fixed script
echo "1. Generating token with fixed script..."
TOKEN=$(node generate-jwt-fixed.js "test-user" "ADMIN" | tail -1)

echo ""
echo "2. Testing token against API..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response Body: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ SUCCESS: JWT authentication is working!"
else
    echo "❌ FAILED: JWT authentication still not working"
    echo ""
    echo "Let's compare with the original script:"
    echo "3. Testing original script token..."
    
    OLD_TOKEN=$(node generate-jwt.js "test-user" "ADMIN" 2>/dev/null | tail -1)
    echo "Original token: ${OLD_TOKEN:0:50}..."
    echo "Fixed token:    ${TOKEN:0:50}..."
    
    if [ "$OLD_TOKEN" = "$TOKEN" ]; then
        echo "Tokens are identical - issue is elsewhere"
    else
        echo "Tokens are different - this might be the fix"
    fi
fi