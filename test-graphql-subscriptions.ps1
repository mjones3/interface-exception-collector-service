#!/usr/bin/env pwsh

# Script to test GraphQL subscriptions on local machine

Write-Host "GraphQL Subscriptions Testing Guide" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

# Configuration
$baseUrl = "http://localhost:8080"
$graphqlEndpoint = "$baseUrl/graphql"
$subscriptionEndpoint = "ws://localhost:8080/subscriptions"

Write-Host "`nService Configuration:" -ForegroundColor Yellow
Write-Host "Base URL: $baseUrl"
Write-Host "GraphQL Endpoint: $graphqlEndpoint"
Write-Host "WebSocket Subscriptions: $subscriptionEndpoint"

Write-Host "`nAvailable Subscriptions:" -ForegroundColor Cyan
Write-Host "1. exceptionUpdated - Real-time exception updates"
Write-Host "2. summaryUpdated - System-wide statistics updates"
Write-Host "3. retryStatusUpdated - Retry operation updates"

Write-Host "`nTesting Methods:" -ForegroundColor Magenta

Write-Host "`n1. Using GraphiQL (Recommended for beginners):" -ForegroundColor Yellow
Write-Host "   - Start the service: mvn spring-boot:run -Dspring-boot.run.profiles=local"
Write-Host "   - Open browser: http://localhost:8080/graphiql"
Write-Host "   - Use the subscription tab to test subscriptions"

Write-Host "`n2. Using curl for HTTP GraphQL queries:" -ForegroundColor Yellow
Write-Host "   # Test basic query first"
Write-Host "   curl -X POST $graphqlEndpoint \"
Write-Host "     -H 'Content-Type: application/json' \"
Write-Host "     -d '{\"query\": \"{ systemHealth { status } }\"}'`n"

Write-Host "3. Using WebSocket clients for subscriptions:" -ForegroundColor Yellow

# Create sample subscription queries
$exceptionSubscription = @"
{
  "type": "start",
  "payload": {
    "query": "subscription { exceptionUpdated { eventType exception { transactionId status severity } timestamp } }"
  }
}
"@

$retrySubscription = @"
{
  "type": "start", 
  "payload": {
    "query": "subscription { retryStatusUpdated { transactionId eventType timestamp } }"
  }
}
"@

Write-Host "`nSample Subscription Queries:" -ForegroundColor Green

Write-Host "`nException Updates Subscription:"
Write-Host $exceptionSubscription -ForegroundColor Gray

Write-Host "`nRetry Status Subscription:"
Write-Host $retrySubscription -ForegroundColor Gray

Write-Host "`n4. Using Node.js WebSocket client:" -ForegroundColor Yellow
$nodeScript = @"
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/subscriptions', {
  headers: {
    'Sec-WebSocket-Protocol': 'graphql-ws'
  }
});

ws.on('open', function open() {
  console.log('Connected to GraphQL subscriptions');
  
  // Send connection init
  ws.send(JSON.stringify({ type: 'connection_init' }));
  
  // Subscribe to exception updates
  ws.send(JSON.stringify({
    id: '1',
    type: 'start',
    payload: {
      query: 'subscription { exceptionUpdated { eventType exception { transactionId status } timestamp } }'
    }
  }));
});

ws.on('message', function message(data) {
  console.log('Received:', JSON.parse(data));
});
"@

Write-Host $nodeScript -ForegroundColor Gray

Write-Host "`n5. Testing with Bruno/Postman:" -ForegroundColor Yellow
Write-Host "   - Import the GraphQL schema"
Write-Host "   - Use WebSocket connection for subscriptions"
Write-Host "   - Set protocol to 'graphql-ws'"

Write-Host "`nStep-by-Step Testing Process:" -ForegroundColor Cyan
Write-Host "1. Start the interface-exception-collector service"
Write-Host "2. Start partner-order-service (for generating events)"
Write-Host "3. Open GraphiQL at http://localhost:8080/graphiql"
Write-Host "4. Set up a subscription in one tab"
Write-Host "5. Trigger events by creating/retrying exceptions in another tab"
Write-Host "6. Watch real-time updates in the subscription tab"

Write-Host "`nTrigger Events for Testing:" -ForegroundColor Green
Write-Host "# Create an exception by sending invalid data to partner-order-service"
Write-Host "curl -X POST http://localhost:8090/v1/partner-order-provider/orders \"
Write-Host "  -H 'Content-Type: application/json' \"
Write-Host "  -d '{\"externalId\": \"TEST-001\", \"invalidField\": \"test\"}'`n"

Write-Host "# Retry an exception via GraphQL mutation"
Write-Host "curl -X POST $graphqlEndpoint \"
Write-Host "  -H 'Content-Type: application/json' \"
Write-Host "  -d '{\"query\": \"mutation { retryException(input: {transactionId: \\\"test-id\\\"}) { success } }\"}'`n"

Write-Host "`nAuthentication (if required):" -ForegroundColor Yellow
Write-Host "# Generate JWT token first"
Write-Host "curl -X POST $baseUrl/auth/login \"
Write-Host "  -H 'Content-Type: application/json' \"
Write-Host "  -d '{\"username\": \"admin\", \"password\": \"password\"}'`n"

Write-Host "# Use token in WebSocket connection"
Write-Host "ws://localhost:8080/subscriptions?token=YOUR_JWT_TOKEN"

Write-Host "`nTroubleshooting:" -ForegroundColor Red
Write-Host "- Ensure service is running on port 8080"
Write-Host "- Check if GraphQL endpoint responds: curl $graphqlEndpoint"
Write-Host "- Verify WebSocket connection: telnet localhost 8080"
Write-Host "- Check logs for subscription events"
Write-Host "- Ensure proper WebSocket protocol: 'graphql-ws'"

Write-Host "`nUseful GraphQL Queries for Testing:" -ForegroundColor Cyan

$queries = @"
# List all exceptions
query {
  exceptions {
    edges {
      node {
        transactionId
        status
        severity
        interfaceType
      }
    }
  }
}

# Get system health
query {
  systemHealth {
    status
    database { status }
  }
}

# Subscribe to all exception updates
subscription {
  exceptionUpdated {
    eventType
    exception {
      transactionId
      status
      severity
    }
    timestamp
  }
}

# Subscribe to retry updates for specific transaction
subscription {
  retryStatusUpdated(transactionId: "your-transaction-id") {
    transactionId
    eventType
    timestamp
  }
}
"@

Write-Host $queries -ForegroundColor Gray

Write-Host "`nReady to test! Start with GraphiQL for the easiest experience." -ForegroundColor Green