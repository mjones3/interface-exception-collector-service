#!/usr/bin/env node

const crypto = require('crypto');

// Configuration - use same secret as application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';
const expirationHours = 1;

// Get command line arguments
const args = process.argv.slice(2);
const username = args[0] || 'test-user';
const roles = args[1] ? args[1].split(',') : ['ADMIN'];

// Create header and payload
const header = {
  'alg': 'HS256',
  'typ': 'JWT'
};

const payload = {
  'sub': username,
  'roles': roles,
  'iat': Math.floor(Date.now() / 1000),
  'exp': Math.floor(Date.now() / 1000) + (60 * 60 * expirationHours)
};

// Base64url encode (JJWT expects base64url format)
const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');

// Create signature
const data = encodedHeader + '.' + encodedPayload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');

// Create final token
const token = data + '.' + signature;

console.log('🔑 JWT Token Generated');
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
console.log(`👤 Username: ${username}`);
console.log(`🛡️  Roles: ${roles.join(', ')}`);
console.log(`⏰ Expires: ${new Date((payload.exp) * 1000).toLocaleString()}`);
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
console.log('');
console.log('📋 For Bruno Authorization Header:');
console.log(`Bearer ${token}`);
console.log('');
console.log('🔗 Raw Token:');
console.log(token);