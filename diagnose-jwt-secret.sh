#!/bin/bash

echo "🔍 Diagnosing JWT Secret Configuration"
echo "====================================="

echo "1. Checking if JwtService is logging the secret during startup..."
echo "   (Look for 'JWT Secret being used' in recent service logs)"
echo ""

echo "2. Current application.yml JWT configuration:"
grep -A 3 -B 1 "jwt:" interface-exception-collector/src/main/resources/application.yml
echo ""

echo "3. Verifying the app section exists and is properly formatted:"
grep -A 10 -B 2 "^app:" interface-exception-collector/src/main/resources/application.yml
echo ""

echo "4. Testing if the service can resolve the property:"
echo "   Expected property path: app.security.jwt.secret"
echo "   Expected value: mySecretKey1234567890123456789012345678901234567890"
echo ""

echo "5. Let's test with a token generated using the exact same secret:"
echo "   Generating token with the exact secret from application.yml..."

# Generate token using the exact secret from the config
TOKEN=$(node -e "
const crypto = require('crypto');
const secret = 'mySecretKey1234567890123456789012345678901234567890';
console.log('Using secret:', secret);
console.log('Secret length:', secret.length);

const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
const now = Math.floor(Date.now()/1000);
const payload = Buffer.from(JSON.stringify({
    sub:'test-user',
    roles:['ADMIN'],
    iat:now,
    exp:now+3600
})).toString('base64url');
const data = header + '.' + payload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
const token = data + '.' + signature;
console.log('Generated token:', token);
" 2>&1 | tail -1)

echo "   Token: ${TOKEN:0:50}..."
echo ""

echo "6. Testing the token:"
RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

echo "   HTTP Code: $HTTP_CODE"
if [ "$HTTP_CODE" != "200" ]; then
    echo "   Still failing - this suggests the service is using a different secret"
    echo ""
    echo "7. Possible issues:"
    echo "   a) Service hasn't fully restarted and picked up new config"
    echo "   b) There's an environment variable JWT_SECRET overriding the config"
    echo "   c) The JwtService isn't finding the app.security.jwt.secret property"
    echo "   d) There's a caching issue with the configuration"
    echo ""
    echo "8. Debugging steps:"
    echo "   - Check service startup logs for 'JWT Secret being used: ...' messages"
    echo "   - Verify no JWT_SECRET environment variable is set"
    echo "   - Ensure the service has fully restarted (not just reloaded)"
else
    echo "   ✅ SUCCESS! JWT authentication is working"
fi