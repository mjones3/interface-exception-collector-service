#!/bin/bash

echo "🔍 Final JWT Secret Diagnostic"
echo "============================="

echo "1. Checking what secret the service is actually using..."
echo "   (The JwtService constructor should log: 'JWT Secret being used: ...')"
echo "   Look for this in the service startup logs!"
echo ""

echo "2. Our configuration says:"
echo "   Path: app.security.jwt.secret"
echo "   Value: \${JWT_SECRET:mySecretKey1234567890123456789012345678901234567890}"
echo "   Since JWT_SECRET env var is not set, it should use the default."
echo ""

echo "3. Let's verify the YAML structure is correct:"
sed -n '340,360p' interface-exception-collector/src/main/resources/application.yml
echo ""

echo "4. Environment check:"
echo "   JWT_SECRET env var: ${JWT_SECRET:-'NOT SET'}"
echo ""

echo "5. Testing with multiple possible secrets..."

# Function to generate token with specific secret
generate_token() {
    local secret="$1"
    node -e "
const crypto = require('crypto');
const secret = '$secret';
const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
const now = Math.floor(Date.now()/1000);
const payload = Buffer.from(JSON.stringify({sub:'test-user',roles:['ADMIN'],iat:now,exp:now+3600})).toString('base64url');
const data = header + '.' + payload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
console.log(data + '.' + signature);
"
}

# Test with the default secret
echo "Testing with default secret: 'mySecretKey1234567890123456789012345678901234567890'"
TOKEN1=$(generate_token "mySecretKey1234567890123456789012345678901234567890")
RESPONSE1=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN1" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
CODE1="${RESPONSE1: -3}"
echo "   Default secret result: HTTP $CODE1"

# Test with empty secret (in case property injection failed)
echo "Testing with empty secret: ''"
TOKEN2=$(generate_token "")
RESPONSE2=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN2" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
CODE2="${RESPONSE2: -3}"
echo "   Empty secret result: HTTP $CODE2"

# Test with property placeholder not resolved
echo "Testing with unresolved placeholder: '\${JWT_SECRET:mySecretKey1234567890123456789012345678901234567890}'"
TOKEN3=$(generate_token "\${JWT_SECRET:mySecretKey1234567890123456789012345678901234567890}")
RESPONSE3=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN3" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
CODE3="${RESPONSE3: -3}"
echo "   Unresolved placeholder result: HTTP $CODE3"

# Test with common default secrets
echo "Testing with 'secret': 'secret'"
TOKEN4=$(generate_token "secret")
RESPONSE4=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN4" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
CODE4="${RESPONSE4: -3}"
echo "   'secret' result: HTTP $CODE4"

# Test with null/undefined (Spring default)
echo "Testing with Spring default: 'null'"
TOKEN5=$(generate_token "null")
RESPONSE5=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN5" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
CODE5="${RESPONSE5: -3}"
echo "   'null' result: HTTP $CODE5"

echo ""
echo "6. Results summary:"
echo "   Default secret (expected): HTTP $CODE1"
echo "   Empty secret: HTTP $CODE2"
echo "   Unresolved placeholder: HTTP $CODE3"
echo "   'secret': HTTP $CODE4"
echo "   'null': HTTP $CODE5"
echo ""

if [ "$CODE1" = "200" ]; then
    echo "✅ SUCCESS: Default secret is working!"
elif [ "$CODE2" = "200" ]; then
    echo "⚠️  WARNING: Service is using empty secret!"
elif [ "$CODE3" = "200" ]; then
    echo "⚠️  WARNING: Service is using unresolved placeholder as secret!"
elif [ "$CODE4" = "200" ]; then
    echo "⚠️  WARNING: Service is using 'secret' as the secret!"
elif [ "$CODE5" = "200" ]; then
    echo "⚠️  WARNING: Service is using 'null' as the secret!"
else
    echo "❌ NONE of the tested secrets worked. The service is using a different secret."
    echo ""
    echo "🔧 CRITICAL: Check the service startup logs for:"
    echo "   'JWT Secret being used: ...'"
    echo "   'JWT Secret length: ...'"
    echo ""
    echo "   This will tell us exactly what secret the service is using."
    echo ""
    echo "   Also check if there are any environment variables or external config"
    echo "   that might be overriding the application.yml settings."
fi