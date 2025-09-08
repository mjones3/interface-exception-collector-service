# Watch GraphQL Subscriptions with curl

## 🔐 Step 1: Get JWT Token

```bash
# Generate token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.token')

echo "Token: $TOKEN"
```

**PowerShell version:**
```powershell
$response = curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"password"}' | ConvertFrom-Json
$token = $response.token
Write-Host "Token: $token"
```

## 📊 Step 2: Watch Exceptions (Polling)

### Bash version:
```bash
# Watch exceptions every 5 seconds
while true; do
  echo "=== $(date) ==="
  curl -s -X POST http://localhost:8080/graphql \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason } } } }"
    }' | jq '.data.exceptions.edges[].node | "\(.timestamp) | \(.transactionId) | \(.status) | \(.severity)"'
  
  sleep 5
done
```

### PowerShell version:
```powershell
# Watch exceptions every 5 seconds
while ($true) {
    Write-Host "=== $(Get-Date) ===" -ForegroundColor Cyan
    
    $query = '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason } } } }"}'
    
    $response = curl -s -X POST http://localhost:8080/graphql `
      -H "Content-Type: application/json" `
      -H "Authorization: Bearer $token" `
      -d $query | ConvertFrom-Json
    
    if ($response.data.exceptions.edges) {
        foreach ($edge in $response.data.exceptions.edges) {
            $ex = $edge.node
            Write-Host "$($ex.timestamp) | $($ex.transactionId) | $($ex.status) | $($ex.severity)" -ForegroundColor White
        }
    } else {
        Write-Host "No exceptions found" -ForegroundColor Gray
    }
    
    Start-Sleep -Seconds 5
}
```

## 🔄 Step 3: Watch Retry Status

```bash
# Watch retry status
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "{ exceptions { edges { node { transactionId retryHistory { attemptNumber status initiatedAt completedAt } } } } }"
  }' | jq '.data.exceptions.edges[].node | select(.retryHistory | length > 0)'
```

## 📈 Step 4: Watch Summary Stats

```bash
# Watch summary statistics
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "{ exceptionSummary(timeRange: LAST_HOUR) { totalExceptions keyMetrics { retrySuccessRate criticalExceptionCount } } }"
  }' | jq '.data.exceptionSummary'
```

## 🚀 Quick One-Liners

### Get token and watch exceptions:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"password"}' | jq -r '.token') && \
watch -n 5 "curl -s -X POST http://localhost:8080/graphql -H 'Content-Type: application/json' -H 'Authorization: Bearer $TOKEN' -d '{\"query\": \"{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }\"}' | jq '.data.exceptions.edges[].node'"
```

### PowerShell one-liner:
```powershell
$token = (curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"password"}' | ConvertFrom-Json).token; while($true) { (curl -s -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"}' | ConvertFrom-Json).data.exceptions.edges | ForEach-Object { Write-Host "$($_.node.timestamp) | $($_.node.transactionId) | $($_.node.status)" }; Start-Sleep 5 }
```

## 🎯 Trigger Events to Watch

While watching, trigger events in another terminal:

```bash
# Create an exception by sending invalid data
curl -X POST http://localhost:8090/v1/partner-order-provider/orders \
  -H "Content-Type: application/json" \
  -d '{"externalId": "TEST-001", "invalidField": "test"}'

# Retry an exception
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"query": "mutation { retryException(input: {transactionId: \"your-transaction-id\"}) { success } }"}'
```

## 🔧 Troubleshooting

### If you get 403 Forbidden:
1. Check if authentication is required
2. Verify token is valid: `echo $TOKEN`
3. Try without token first
4. Check service logs for auth errors

### If no data appears:
1. Verify service is running: `curl http://localhost:8080/actuator/health`
2. Check GraphQL endpoint: `curl http://localhost:8080/graphql`
3. Trigger some exceptions to generate data
4. Check service logs for errors

### Alternative without authentication:
```bash
# Try without token first
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ systemHealth { status } }"}' | jq
```