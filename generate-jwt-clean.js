#!/usr/bin/env node

const crypto = require('crypto');

// Get command line arguments
const args = process.argv.slice(2);
const username = args[0] || 'test-user';
const roles = args[1] ? args[1].split(',') : ['ADMIN'];

// Configuration - use the actual secret the service is using (from service logs)
const secret = 'dev-secret-key-1234567890123456789012345678901234567890';

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

// Output ONLY the token (no extra text)
console.log(token);