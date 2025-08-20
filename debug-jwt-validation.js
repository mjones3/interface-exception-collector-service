#!/usr/bin/env node

const crypto = require('crypto');

// Configuration - use same secret as application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';

console.log('🔍 JWT Debug Information');
console.log('━'.repeat(80));
console.log(`🔑 Secret: "${secret}"`);
console.log(`📏 Secret Length: ${secret.length}`);
console.log(`🔢 Secret Bytes: ${Buffer.from(secret).length}`);
console.log('');

// Test different encoding methods
console.log('🧪 Secret Encoding Tests:');
console.log(`UTF-8 Bytes: ${Buffer.from(secret, 'utf8').toString('hex')}`);
console.log(`ASCII Bytes: ${Buffer.from(secret, 'ascii').toString('hex')}`);
console.log('');

// Generate a test token
const header = {
  'alg': 'HS256',
  'typ': 'JWT'
};

const payload = {
  'sub': 'debug-user',
  'roles': ['ADMIN'],
  'iat': Math.floor(Date.now() / 1000),
  'exp': Math.floor(Date.now() / 1000) + (60 * 60) // 1 hour
};

// Base64url encode
const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');

console.log('🏗️  Token Construction:');
console.log(`Header: ${JSON.stringify(header)}`);
console.log(`Payload: ${JSON.stringify(payload)}`);
console.log(`Encoded Header: ${encodedHeader}`);
console.log(`Encoded Payload: ${encodedPayload}`);
console.log('');

// Create signature
const data = encodedHeader + '.' + encodedPayload;
console.log(`🔗 Data to Sign: ${data}`);

// Test different signature methods
console.log('');
console.log('🔐 Signature Tests:');

// Method 1: Direct HMAC
const signature1 = crypto.createHmac('sha256', secret).update(data).digest('base64url');
console.log(`Method 1 (Direct): ${signature1}`);

// Method 2: UTF-8 encoded secret
const signature2 = crypto.createHmac('sha256', Buffer.from(secret, 'utf8')).update(data).digest('base64url');
console.log(`Method 2 (UTF-8): ${signature2}`);

// Method 3: ASCII encoded secret
const signature3 = crypto.createHmac('sha256', Buffer.from(secret, 'ascii')).update(data).digest('base64url');
console.log(`Method 3 (ASCII): ${signature3}`);

// Create final tokens
const token1 = data + '.' + signature1;
const token2 = data + '.' + signature2;
const token3 = data + '.' + signature3;

console.log('');
console.log('🎯 Generated Tokens:');
console.log(`Token 1: ${token1}`);
console.log(`Token 2: ${token2}`);
console.log(`Token 3: ${token3}`);

console.log('');
console.log('✅ Use Token 1 for testing (should match our generate-jwt.js output)');