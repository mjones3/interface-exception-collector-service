#!/usr/bin/env node

const crypto = require('crypto');

// Configuration - use same secret as application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';
const expirationHours = 1;

// Get command line arguments
const args = process.argv.slice(2);
const username = args[0] || 'test-user';
const roles = args[1] ? args[1].split(',') : ['ADMIN'];

console.log('🔧 Fixed JWT Generator (matching Java JJWT library)');
console.log('━'.repeat(80));
console.log(`🔑 Secret: '${secret}'`);
console.log(`📏 Secret Length: ${secret.length}`);

// Create header and payload exactly as Java JJWT would
const header = {
  'alg': 'HS256',
  'typ': 'JWT'
};

const now = Math.floor(Date.now() / 1000);
const payload = {
  'sub': username,
  'roles': roles,
  'iat': now,
  'exp': now + (60 * 60 * expirationHours)
};

console.log(`👤 Username: ${username}`);
console.log(`🛡️  Roles: ${roles.join(', ')}`);
console.log(`⏰ Issued At: ${new Date(now * 1000).toISOString()}`);
console.log(`⏰ Expires: ${new Date(payload.exp * 1000).toISOString()}`);

// Base64url encode (JJWT expects base64url format)
const encodedHeader = Buffer.from(JSON.stringify(header, null, 0)).toString('base64url');
const encodedPayload = Buffer.from(JSON.stringify(payload, null, 0)).toString('base64url');

console.log(`📦 Header: ${JSON.stringify(header)}`);
console.log(`📦 Payload: ${JSON.stringify(payload)}`);
console.log(`🔗 Encoded Header: ${encodedHeader}`);
console.log(`🔗 Encoded Payload: ${encodedPayload}`);

// Create signature using HMAC-SHA256 (same as Java Keys.hmacShaKeyFor)
const data = encodedHeader + '.' + encodedPayload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');

console.log(`🔐 Signature Data: ${data}`);
console.log(`🔐 Signature: ${signature}`);

// Create final token
const token = data + '.' + signature;

console.log('━'.repeat(80));
console.log('✅ Generated JWT Token:');
console.log(token);
console.log('━'.repeat(80));
console.log(`📏 Token Length: ${token.length}`);

console.log('\n🧪 Test Command:');
console.log(`curl -s -w "%{http_code}" -H "Authorization: Bearer ${token}" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions"`);

console.log('\n📋 For Bruno Authorization Header:');
console.log(`Bearer ${token}`);

// Verify the token locally
console.log('\n🔍 Local Verification:');
try {
  const [headerPart, payloadPart, signaturePart] = token.split('.');
  
  // Verify signature
  const expectedSignature = crypto.createHmac('sha256', secret).update(headerPart + '.' + payloadPart).digest('base64url');
  
  if (expectedSignature === signaturePart) {
    console.log('✅ Signature verification: PASSED');
    
    // Decode payload
    const decodedPayload = JSON.parse(Buffer.from(payloadPart, 'base64url').toString());
    console.log('📋 Decoded payload:', JSON.stringify(decodedPayload, null, 2));
    
    // Check expiration
    const currentTime = Math.floor(Date.now() / 1000);
    if (decodedPayload.exp > currentTime) {
      console.log('✅ Expiration check: PASSED (token is valid)');
    } else {
      console.log('❌ Expiration check: FAILED (token is expired)');
    }
  } else {
    console.log('❌ Signature verification: FAILED');
    console.log(`Expected: ${expectedSignature}`);
    console.log(`Got: ${signaturePart}`);
  }
} catch (error) {
  console.log('❌ Local verification failed:', error.message);
}

// Output just the token for scripts
console.log('\n🔗 Raw Token (for scripts):');
console.log(token);