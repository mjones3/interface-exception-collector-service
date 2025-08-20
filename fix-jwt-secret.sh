#!/bin/bash

echo "🔧 Fixing JWT Secret in Kubernetes"
echo "=================================="

echo "1. Current ConfigMap JWT_SECRET:"
kubectl get configmap app-config -o jsonpath='{.data.JWT_SECRET}'
echo ""
echo ""

echo "2. Updating ConfigMap with correct JWT secret..."
kubectl apply -f k8s/interface-exception-collector.yaml

echo ""
echo "3. Verifying the update..."
kubectl get configmap app-config -o jsonpath='{.data.JWT_SECRET}'
echo ""
echo ""

echo "4. Restarting the pod to pick up new configuration..."
kubectl rollout restart deployment/interface-exception-collector

echo ""
echo "5. Waiting for pod to be ready..."
kubectl rollout status deployment/interface-exception-collector --timeout=120s

echo ""
echo "6. Checking if the service is using the correct secret now..."
echo "   (Wait a moment for the service to fully start, then check logs for 'JWT Secret being used')"

echo ""
echo "7. Testing JWT authentication with the correct secret..."
sleep 10

# Generate token with the correct secret from our application.yml
TOKEN=$(node -e "
const crypto = require('crypto');
const secret = 'mySecretKey1234567890123456789012345678901234567890';
const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
const now = Math.floor(Date.now()/1000);
const payload = Buffer.from(JSON.stringify({sub:'test-user',roles:['ADMIN'],iat:now,exp:now+3600})).toString('base64url');
const data = header + '.' + payload;
const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
console.log(data + '.' + signature);
")

echo "Generated token with application.yml secret: ${TOKEN:0:50}..."

RESPONSE=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "http://localhost:8080/api/v1/exceptions")
HTTP_CODE="${RESPONSE: -3}"

echo "HTTP Response Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "503" ]; then
    echo "✅ SUCCESS! JWT authentication is working!"
    echo "   (503 is expected due to database issues, but JWT auth passed)"
elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "❌ Still getting authentication error. The service might still be using the wrong secret."
    echo "   Check the service logs for 'JWT Secret being used: ...' to see what secret it's actually using."
else
    echo "⚠️  Unexpected response code: $HTTP_CODE"
fi

echo ""
echo "🔍 Next steps:"
echo "   1. Check service logs: kubectl logs deployment/interface-exception-collector | grep 'JWT Secret'"
echo "   2. If still wrong secret, the issue might be in the Docker image or Spring profiles"