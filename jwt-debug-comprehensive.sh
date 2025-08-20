#!/bin/bash

echo "🔍 Comprehensive JWT Authentication Debug"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if application is running
echo "1. 🏥 Checking application health..."
HEALTH_STATUS=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/actuator/health)
if [ "$HEALTH_STATUS" = "200" ]; then
    echo "   ✅ Application is running"
else
    echo "   ❌ Application is not running (HTTP $HEALTH_STATUS)"
    exit 1
fi

# Check application logs for JWT secret
echo ""
echo "2. 🔑 Checking JWT secret from application logs..."
echo "   Looking for 'JWT Secret being used' in logs..."

# Try to find the actual secret being used by the application
# This would require access to application logs
echo "   ⚠️  Manual check required: Look at application startup logs for:"
echo "      'JWT Secret being used: ...'"
echo "      'JWT Secret length: ...'"

# Test different secret possibilities
echo ""
echo "3. 🧪 Testing different secret configurations..."

SECRETS=(
    "mySecretKey1234567890123456789012345678901234567890"
    "mySecretKey"
    "secret"
    "defaultSecret"
    "interface-exception-collector-secret"
)

for i in "${!SECRETS[@]}"; do
    SECRET="${SECRETS[$i]}"
    echo ""
    echo "   Testing secret $((i+1)): '$SECRET' (length: ${#SECRET})"
    
    # Generate token with this secret
    TOKEN=$(node -e "
        const crypto = require('crypto');
        const secret = '$SECRET';
        const header = {alg: 'HS256', typ: 'JWT'};
        const payload = {
            sub: 'debug-user',
            roles: ['ADMIN'],
            iat: Math.floor(Date.now() / 1000),
            exp: Math.floor(Date.now() / 1000) + 3600
        };
        const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
        const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');
        const data = encodedHeader + '.' + encodedPayload;
        const signature = crypto.createHmac('sha256', Buffer.from(secret, 'utf8')).update(data, 'utf8').digest('base64url');
        console.log(data + '.' + signature);
    ")
    
    # Test the token
    RESPONSE=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions" -o /tmp/jwt_test_response.json)
    
    if [ "$RESPONSE" = "200" ]; then
        echo "   ✅ SUCCESS! This secret works!"
        echo "   🎯 Correct secret: '$SECRET'"
        echo "   🔗 Working token: $TOKEN"
        break
    elif [ "$RESPONSE" = "401" ]; then
        ERROR_MSG=$(cat /tmp/jwt_test_response.json 2>/dev/null | grep -o '"message":"[^"]*"' | cut -d'"' -f4)
        echo "   ❌ Failed: $ERROR_MSG"
    else
        echo "   ❌ Failed: HTTP $RESPONSE"
    fi
done

# Test environment variable override
echo ""
echo "4. 🌍 Checking for environment variable overrides..."
echo "   JWT_SECRET environment variable: ${JWT_SECRET:-'Not set'}"

# Check if there's a different configuration file
echo ""
echo "5. 📁 Checking for configuration overrides..."
if [ -f "application-local.yml" ]; then
    echo "   Found application-local.yml"
    grep -n "jwt:" application-local.yml || echo "   No JWT config in application-local.yml"
fi

if [ -f "application-dev.yml" ]; then
    echo "   Found application-dev.yml"
    grep -n "jwt:" application-dev.yml || echo "   No JWT config in application-dev.yml"
fi

# Check Docker environment
echo ""
echo "6. 🐳 Checking Docker/container environment..."
if command -v docker &> /dev/null; then
    CONTAINER_ID=$(docker ps --filter "name=interface-exception-collector" --format "{{.ID}}" | head -1)
    if [ -n "$CONTAINER_ID" ]; then
        echo "   Found container: $CONTAINER_ID"
        echo "   Container environment variables:"
        docker exec "$CONTAINER_ID" env | grep -i jwt || echo "   No JWT environment variables found"
    else
        echo "   No interface-exception-collector container found"
    fi
else
    echo "   Docker not available"
fi

echo ""
echo "7. 🔧 Recommendations:"
echo "   1. Check application startup logs for actual JWT secret being used"
echo "   2. Verify no environment variables are overriding the configuration"
echo "   3. Ensure application.yml is being loaded correctly"
echo "   4. Check for profile-specific configuration files"

rm -f /tmp/jwt_test_response.json