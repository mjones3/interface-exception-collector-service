#!/bin/bash

echo "🔍 Debugging JWT Secret Configuration"
echo "===================================="

echo "1. Secret from application.yml:"
grep -A 1 -B 1 "jwt:" interface-exception-collector/src/main/resources/application.yml
echo ""

echo "2. Secret from generate-jwt.js:"
grep "secret.*=" generate-jwt.js
echo ""

echo "3. Secret from generate-jwt.py:"
grep "secret.*=" generate-jwt.py
echo ""

echo "4. Testing token generation with explicit secret verification:"

# Generate a token and show the secret being used
node -e "
const crypto = require('crypto');
const secret = 'mySecretKey1234567890123456789012345678901234567890';
console.log('Secret used in Node.js script:');
console.log('Length:', secret.length);
console.log('Value:', secret);

const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
const payload = Buffer.from(JSON.stringify({
    sub:'test-user',
    roles:['ADMIN'],
    iat:Math.floor(Date.now()/1000),
    exp:Math.floor(Date.now()/1000)+3600
})).toString('base64url');
const data = header + '.' + payload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
const token = data + '.' + signature;

console.log('Generated token parts:');
console.log('Header:', header);
console.log('Payload:', payload);
console.log('Signature:', signature);
console.log('Full token:', token);
"