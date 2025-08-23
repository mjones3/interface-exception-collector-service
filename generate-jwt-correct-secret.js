#!/usr/bin/env node

const crypto = require('crypto');

// Get command line arguments
const args = process.argv.slice(2);
const username = args[0] || 'test-user';
const roles = args[1] ? args[1].split(',') : ['ADMIN'];

// Use the ACTUAL secret from application.yml (default value)
const secret = 'dev-secret-key-1234567890123456789012345678901234567890';
// const secret = 'mySecretKey1234567890123456789012345678901234567890';
console.log('🔑 Using CORRECT secret from application.yml default value');
console.log(`📏 Secret: ${secret}`);
console.log(`📏 Length: ${secret.length}`);

// Create header and payload
const header = {
  alg: 'HS256',
  typ: 'JWT'
};

const now = Math.floor(Date.now() / 1000);
const payload = {
  sub: username,
  roles: roles,
  iat: now,
  exp: now + 3600
};

// Base64url encode
const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');

// Create signature
const data = encodedHeader + '.' + encodedPayload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');

// Create final token
const token = data + '.' + signature;

console.log(`👤 User: ${username}`);
console.log(`🛡️  Roles: ${roles.join(', ')}`);
console.log(`⏰ Expires: ${new Date((now + 3600) * 1000).toISOString()}`);
console.log('');
console.log('🎯 Generated Token:');
console.log(token);