#!/bin/bash

echo "🔍 Testing Different JWT Secrets"
echo "==============================="

# Function to test a specific secret
test_secret() {
    local secret_name="$1"
    local secret_value="$2"
    
    echo "Testing secret '$secret_name': '$secret_value'"
    
    # Generate token with this specific secret
    local token=$(node -e "
const crypto = require('crypto');
const secret = process.argv[1];
const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
const now = Math.floor(Date.now()/1000);
const payload = Buffer.from(JSON.stringify({sub:'test-user',roles:['ADMIN'],iat:now,exp:now+3600})).toString('base64url');
const data = header + '.' + payload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
console.log(data + '.' + signature);
" "$secret_value")
    
    # Test the token
    local response=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $token" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
    local http_code="${response: -3}"
    
    echo "   Result: HTTP $http_code"
    
    if [ "$http_code" = "200" ]; then
        echo "   ✅ SUCCESS! This is the correct secret!"
        return 0
    fi
    
    return 1
}

echo "1. Testing expected default secret..."
if test_secret "default" "mySecretKey1234567890123456789012345678901234567890"; then
    exit 0
fi

echo ""
echo "2. Testing empty secret..."
if test_secret "empty" ""; then
    exit 0
fi

echo ""
echo "3. Testing 'secret'..."
if test_secret "simple" "secret"; then
    exit 0
fi

echo ""
echo "4. Testing 'null'..."
if test_secret "null" "null"; then
    exit 0
fi

echo ""
echo "5. Testing unresolved placeholder..."
if test_secret "placeholder" "\${JWT_SECRET:mySecretKey1234567890123456789012345678901234567890}"; then
    exit 0
fi

echo ""
echo "6. Testing property name as secret..."
if test_secret "property-name" "app.security.jwt.secret"; then
    exit 0
fi

echo ""
echo "7. Testing shorter version of default..."
if test_secret "short-default" "mySecretKey"; then
    exit 0
fi

echo ""
echo "❌ NONE of the tested secrets worked!"
echo ""
echo "🔧 Next steps:"
echo "   1. Check service startup logs for 'JWT Secret being used: ...'"
echo "   2. Check if there are environment variables overriding the config"
echo "   3. Verify the service is actually using the updated application.yml"
echo "   4. Check if there's a different configuration file being loaded"