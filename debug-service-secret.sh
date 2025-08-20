#!/bin/bash

echo "🔍 Debugging Service JWT Secret"
echo "==============================="

echo "1. Checking what secret the service logs show..."
echo "   (Look for 'JWT Secret being used' in the service logs)"
echo ""

echo "2. Checking application.yml configuration..."
grep -A 2 -B 2 "jwt:" interface-exception-collector/src/main/resources/application.yml
echo ""

echo "3. Checking if there are environment variables overriding the secret..."
echo "   JWT_SECRET environment variable: ${JWT_SECRET:-'not set'}"
echo ""

echo "4. Testing with a simple curl to see the exact error..."
TOKEN=$(node generate-jwt-fixed.js "test-user" "ADMIN" 2>/dev/null | tail -1)
echo "Using token: ${TOKEN:0:50}..."

echo ""
echo "Making request with verbose output..."
curl -v -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions" 2>&1 | head -20

echo ""
echo "5. Let's also check if the JwtAuthenticationFilter is even being called..."
echo "   (Check service logs for '[JWT-AUTH]' messages)"