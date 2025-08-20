#!/usr/bin/env node

const crypto = require('crypto');

// Same secret as in generate-jwt.js and application.yml
const secret = 'mySecretKey1234567890123456789012345678901234567890';

// The token you're trying to validate
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTU2MjgzNDQsImV4cCI6MTc1NTYzMTk0NH0.EPVq0hZf5QeYX4YMqskg4oOW5TulL1C_vAYLa1PbkMk';

// Split the token
const [headerB64, payloadB64, signatureB64] = token.split('.');

// Decode the payload to see what's in it
const payload = JSON.parse(Buffer.from(payloadB64, 'base64url').toString());
console.log('Token payload:', JSON.stringify(payload, null, 2));

// Recreate the signature
const data = headerB64 + '.' + payloadB64;
const expectedSignature = crypto.createHmac('sha256', secret).update(data).digest('base64url');

console.log('Expected signature:', expectedSignature);
console.log('Actual signature:  ', signatureB64);
console.log('Signatures match:  ', expectedSignature === signatureB64);

// Check if token is expired
const now = Math.floor(Date.now() / 1000);
console.log('Current time:', now);
console.log('Token expires:', payload.exp);
console.log('Token is expired:', now > payload.exp);