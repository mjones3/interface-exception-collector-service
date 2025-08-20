#!/bin/bash

# Simple JWT test to debug the signature issue

echo "🔍 Testing JWT signature validation..."

# Generate token using Node.js script
echo "Generating token with Node.js..."
TOKEN=$(node generate-jwt.js "test-user" "ADMIN" 2>/dev/null | tail -1)
echo "Generated token: ${TOKEN:0:50}..."

# Test the token
echo "Testing token against API..."
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ JWT authentication working!"
else
    echo "❌ JWT authentication failed"
    
    # Let's also test the health endpoint
    echo "Testing health endpoint..."
    HEALTH_RESPONSE=$(curl -s -w '%{http_code}' "http://localhost:8080/actuator/health")
    HEALTH_CODE="${HEALTH_RESPONSE: -3}"
    HEALTH_BODY="${HEALTH_RESPONSE%???}"
    echo "Health HTTP Code: $HEALTH_CODE"
    echo "Health Response: $HEALTH_BODY"
fi