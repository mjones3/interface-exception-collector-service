#!/bin/bash

echo "🧪 Testing Token from Debug Script"
echo "================================="

# Use the exact token from your debug output
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZWJ1Zy11c2VyIiwicm9sZXMiOlsiQURNSU4iXSwiaWF0IjoxNzU1Njg4NDM5LCJleHAiOjE3NTU2OTIwMzl9.cfIONjJN-Baax5ovnvR9UCsXCQGVwbVkXZpVB_NxkN0"

echo "Testing token: ${TOKEN:0:50}..."

RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "HTTP Code: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ SUCCESS! The debug token works!"
elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "❌ Token rejected - signature mismatch"
    echo ""
    echo "This means the service is using a DIFFERENT secret than:"
    echo "mySecretKey1234567890123456789012345678901234567890"
    echo ""
    echo "🔧 CRITICAL: We need to check the service startup logs for:"
    echo "   'JWT Secret being used: ...'"
    echo "   'JWT Secret length: ...'"
else
    echo "❌ Unexpected response: $HTTP_CODE"
fi