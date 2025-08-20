#!/usr/bin/env node

const crypto = require('crypto');

// Test JWT compatibility with Java JJWT library
console.log('🧪 JWT Compatibility Test');
console.log('━'.repeat(80));

// Configuration - use same secret as application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';

// Test different payload formats that might be expected by Java
const testCases = [
    {
        name: 'Standard Format',
        payload: {
            'sub': 'test-user',
            'roles': ['ADMIN'],
            'iat': Math.floor(Date.now() / 1000),
            'exp': Math.floor(Date.now() / 1000) + 3600
        }
    },
    {
        name: 'Java Compatible Format',
        payload: {
            'sub': 'test-user',
            'roles': ['ADMIN'],
            'iat': Math.floor(Date.now() / 1000),
            'exp': Math.floor(Date.now() / 1000) + 3600,
            'iss': 'interface-exception-collector',
            'aud': 'api'
        }
    },
    {
        name: 'Compact JSON Format',
        payload: {
            sub: 'test-user',
            roles: ['ADMIN'],
            iat: Math.floor(Date.now() / 1000),
            exp: Math.floor(Date.now() / 1000) + 3600
        }
    }
];

const header = {
    'alg': 'HS256',
    'typ': 'JWT'
};

testCases.forEach((testCase, index) => {
    console.log(`\n${index + 1}. ${testCase.name}:`);
    
    // Use compact JSON serialization (no spaces)
    const headerJson = JSON.stringify(header, null, 0);
    const payloadJson = JSON.stringify(testCase.payload, null, 0);
    
    console.log(`   Header JSON: ${headerJson}`);
    console.log(`   Payload JSON: ${payloadJson}`);
    
    // Base64url encode
    const encodedHeader = Buffer.from(headerJson).toString('base64url');
    const encodedPayload = Buffer.from(payloadJson).toString('base64url');
    
    // Create signature
    const data = encodedHeader + '.' + encodedPayload;
    const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
    
    // Create final token
    const token = data + '.' + signature;
    
    console.log(`   Token: ${token}`);
    console.log(`   Length: ${token.length}`);
});

console.log('\n🔍 Testing with curl:');
console.log('Copy one of the tokens above and test with:');
console.log('curl -s -w "%{http_code}" -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions"');