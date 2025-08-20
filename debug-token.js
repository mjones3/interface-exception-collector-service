#!/usr/bin/env node

const crypto = require('crypto');

// The exact same secret as in both configs
const secret = 'mySecretKey1234567890123456789012345678901234567890';

// Let's decode a token that's failing
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2a'; // This is the start we see in logs

console.log('🔍 Token Analysis');
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

// Let's generate a fresh token and see what happens
const header = {
  'alg': 'HS256',
  'typ': 'JWT'
};

const payload = {
  'sub': 'test-user',
  'roles': ['ADMIN'],
  'iat': Math.floor(Date.now() / 1000),
  'exp': Math.floor(Date.now() / 1000) + (60 * 60)
};

const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');

console.log('📝 Header (decoded):', JSON.stringify(header));
console.log('📝 Payload (decoded):', JSON.stringify(payload));
console.log('');
console.log('🔤 Header (base64url):', encodedHeader);
console.log('🔤 Payload (base64url):', encodedPayload);

// Create signature with base64url
const data = encodedHeader + '.' + encodedPayload;
const signatureBase64url = crypto.createHmac('sha256', secret).update(data).digest('base64url');

// Also try with regular base64 (what Java might expect)
const signatureBase64 = crypto.createHmac('sha256', secret).update(data).digest('base64');

console.log('');
console.log('🔐 Signature (base64url):', signatureBase64url);
console.log('🔐 Signature (base64):', signatureBase64);

const tokenBase64url = data + '.' + signatureBase64url;
const tokenBase64 = data + '.' + signatureBase64;

console.log('');
console.log('🎫 Token with base64url signature:');
console.log(tokenBase64url);
console.log('');
console.log('🎫 Token with base64 signature:');
console.log(tokenBase64);

// Let's also check what the secret looks like as bytes
console.log('');
console.log('🔑 Secret as UTF-8 bytes:', Array.from(Buffer.from(secret, 'utf8')));
console.log('🔑 Secret length:', secret.length);