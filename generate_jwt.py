import jwt
import datetime

# JWT secret from application.yml config
secret = "mySecretKey1234567890123456789012345678901234567890"

# Create payload
payload = {
    "sub": "test-user",  # username
    "roles": ["ADMIN"],  # roles for full access
    "iat": datetime.datetime.utcnow(),
    "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
}

# Generate token
token = jwt.encode(payload, secret, algorithm="HS256")
print("JWT Token:")
print(token)
print("\nUse this in Bruno Authorization header:")
print(f"Bearer {token}")
