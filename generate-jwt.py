#!/usr/bin/env python3

import sys
import json
import hmac
import hashlib
import base64
from datetime import datetime, timedelta

def base64url_encode(data):
    """Base64 URL-safe encode without padding"""
    return base64.urlsafe_b64encode(data).decode('utf-8').rstrip('=')

def generate_jwt(username='test-user', roles=['ADMIN'], expiration_hours=1):
    # Configuration
    secret = 'dev-secret-key-1234567890123456789012345678901234567890'
    
    # Create header
    header = {
        'alg': 'HS256',
        'typ': 'JWT'
    }
    
    # Create payload
    now = datetime.utcnow()
    payload = {
        'sub': username,
        'roles': roles,
        'iat': int(now.timestamp()),
        'exp': int((now + timedelta(hours=expiration_hours)).timestamp())
    }
    
    # Encode header and payload
    encoded_header = base64url_encode(json.dumps(header, separators=(',', ':')).encode())
    encoded_payload = base64url_encode(json.dumps(payload, separators=(',', ':')).encode())
    
    # Create signature
    message = f"{encoded_header}.{encoded_payload}"
    signature = hmac.new(
        secret.encode(),
        message.encode(),
        hashlib.sha256
    ).digest()
    encoded_signature = base64url_encode(signature)
    
    # Create final token
    token = f"{message}.{encoded_signature}"
    
    # Print results
    print('🔑 JWT Token Generated')
    print('━' * 80)
    print(f'👤 Username: {username}')
    print(f'🛡️  Roles: {", ".join(roles)}')
    print(f'⏰ Expires: {datetime.fromtimestamp(payload["exp"]).strftime("%m/%d/%Y, %I:%M:%S %p")}')
    print('━' * 80)
    print()
    print('📋 For Bruno Authorization Header:')
    print(f'Bearer {token}')
    print()
    print('🔗 Raw Token:')
    print(token)

if __name__ == '__main__':
    # Parse command line arguments
    username = sys.argv[1] if len(sys.argv) > 1 else 'test-user'
    roles = sys.argv[2].split(',') if len(sys.argv) > 2 else ['ADMIN']
    
    generate_jwt(username, roles)