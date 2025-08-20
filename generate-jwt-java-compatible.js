#!/usr/bin/env node

const crypto = require('crypto');

// Configuration - EXACTLY match Java application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';

console.log('🔧 Java-Compatible JWT Generator');
console.log('━'.repeat(80));
console.log(`🔑 Secret: "${secret}"`);
console.log(`📏 Secret Length: ${secret.length} bytes`);
console.log('');

// Get command line arguments
const args = process.argv.slice(2);
const username = args[0] || 'test-user';
const roles = args[1] ? args[1].split(',') : ['ADMIN'];

// Create header - EXACTLY as JJWT expects
const header = {
  alg: 'HS256',
  typ: 'JWT'
};

// Create payload - EXACTLY as JJWT expects
const now = Math.floor(Date.now() / 1000);
const payload = {
  sub: username,
  roles: roles,
  iat: now,
  exp: now + 3600  // 1 hour
};

console.log('📋 Token Details:');
console.log(`👤 Username: ${username}`);
console.log(`🛡️  Roles: ${roles.join(', ')}`);
console.log(`⏰ Issued At: ${new Date(now * 1000).toISOString()}`);
console.log(`⏰ Expires At: ${new Date((now + 3600) * 1000).toISOString()}`);
console.log('');

// JSON serialization - NO SPACES (compact)
const headerJson = JSON.stringify(header);
const payloadJson = JSON.stringify(payload);

console.log('🏗️  JSON Serialization:');
console.log(`Header JSON: ${headerJson}`);
console.log(`Payload JSON: ${payloadJson}`);
console.log('');

// Base64url encode (JJWT compatible)
const encodedHeader = Buffer.from(headerJson, 'utf8').toString('base64url');
const encodedPayload = Buffer.from(payloadJson, 'utf8').toString('base64url');

console.log('🔤 Base64URL Encoding:');
console.log(`Encoded Header: ${encodedHeader}`);
console.log(`Encoded Payload: ${encodedPayload}`);
console.log('');

// Create signature data
const signatureData = encodedHeader + '.' + encodedPayload;
console.log(`🔗 Signature Data: ${signatureData}`);

// Create HMAC signature - EXACTLY as JJWT does it
// JJWT uses UTF-8 encoding for the secret
const secretBuffer = Buffer.from(secret, 'utf8');
const signature = crypto.createHmac('sha256', secretBuffer)
  .update(signatureData, 'utf8')
  .digest('base64url');

console.log(`🔐 Signature: ${signature}`);
console.log('');

// Create final token
const token = signatureData + '.' + signature;

console.log('✅ Final JWT Token:');
console.log('━'.repeat(80));
console.log(token);
console.log('━'.repeat(80));
console.log(`📏 Token Length: ${token.length}`);
console.log('');

console.log('🧪 Test Command:');
console.log(`curl -s -w "%{http_code}" -H "Authorization: Bearer ${token}" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions"`);
console.log('');

console.log('📋 For Bruno/Postman:');
console.log(`Bearer ${token}`);