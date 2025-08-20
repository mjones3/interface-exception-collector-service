#!/bin/bash

echo "🔧 JWT Authentication Test (After Service Restart)"
echo "=================================================="

echo "✅ Configuration Fix Applied:"
echo "   - Fixed duplicate 'app:' key in application.yml"
echo "   - Proper 'app.security.jwt.secret' configuration is now in place"
echo "   - JwtService will now find the secret at the correct path"
echo ""

echo "🔄 Service Status Check:"
HEALTH_RESPONSE=$(curl -s -w '%{http_code}' "http://localhost:8080/actuator/health")
HEALTH_CODE="${HEALTH_RESPONSE: -3}"

if [ "$HEALTH_CODE" = "200" ]; then
    echo "✅ Service is running and healthy"
    
    echo ""
    echo "🧪 Testing JWT Authentication:"
    
    # Generate token with fixed script
    TOKEN=$(node generate-jwt-fixed.js "test-user" "ADMIN" 2>/dev/null | tail -1)
    echo "Generated token: ${TOKEN:0:50}..."
    
    # Test the token
    RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    HTTP_CODE="${RESPONSE: -3}"
    RESPONSE_BODY="${RESPONSE%???}"
    
    echo "HTTP Code: $HTTP_CODE"
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "🎉 SUCCESS: JWT authentication is now working!"
        echo "   Response: ${RESPONSE_BODY:0:100}..."
        
        echo ""
        echo "🧪 Testing different roles:"
        
        # Test OPERATOR role
        OPERATOR_TOKEN=$(node generate-jwt-fixed.js "ops-user" "OPERATOR" 2>/dev/null | tail -1)
        OPERATOR_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $OPERATOR_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
        OPERATOR_CODE="${OPERATOR_RESPONSE: -3}"
        echo "   OPERATOR role: HTTP $OPERATOR_CODE"
        
        # Test VIEWER role
        VIEWER_TOKEN=$(node generate-jwt-fixed.js "viewer-user" "VIEWER" 2>/dev/null | tail -1)
        VIEWER_RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $VIEWER_TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
        VIEWER_CODE="${VIEWER_RESPONSE: -3}"
        echo "   VIEWER role: HTTP $VIEWER_CODE"
        
        echo ""
        echo "✅ JWT authentication fix is complete and working!"
        
    elif [ "$HTTP_CODE" = "403" ] || [ "$HTTP_CODE" = "401" ]; then
        echo "❌ Still getting authentication error (HTTP $HTTP_CODE)"
        echo "   This suggests the service may not have restarted yet"
        echo "   or there might be another configuration issue"
        echo ""
        echo "🔍 Troubleshooting steps:"
        echo "   1. Ensure the service has been restarted to pick up new configuration"
        echo "   2. Check service logs for 'JWT Secret being used' messages"
        echo "   3. Verify no environment variables are overriding JWT_SECRET"
        
    else
        echo "❌ Unexpected response code: $HTTP_CODE"
        echo "   Response: $RESPONSE_BODY"
    fi
    
else
    echo "❌ Service is not running or not healthy (HTTP $HEALTH_CODE)"
    echo "   Please start the service first"
fi

echo ""
echo "📋 Summary of Changes Made:"
echo "   ✅ Fixed SecurityConfig to use correct role names (OPERATOR vs OPERATIONS)"
echo "   ✅ Added missing app.security.jwt.secret configuration"
echo "   ✅ Removed duplicate app: key from application.yml"
echo "   ✅ Created comprehensive test scripts for validation"
echo "   ✅ Fixed JWT token generation to match Java JJWT library"