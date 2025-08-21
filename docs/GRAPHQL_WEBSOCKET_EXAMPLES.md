# GraphQL WebSocket Subscriptions

## Overview

The GraphQL API supports real-time subscriptions via WebSocket connections. This allows clients to receive live updates for exception events, retry status changes, and summary statistics.

## WebSocket Endpoint

- **URL**: `ws://localhost:8080/subscriptions`
- **Protocol**: `graphql-ws`

## Connection Flow

### 1. Establish WebSocket Connection

```javascript
const ws = new WebSocket('ws://localhost:8080/subscriptions', 'graphql-ws');
```

### 2. Initialize Connection

Send connection initialization message:

```json
{
  "type": "connection_init",
  "payload": {
    "Authorization": "Bearer your-jwt-token"
  }
}
```

### 3. Start Subscription

Send subscription start message:

```json
{
  "id": "unique-subscription-id",
  "type": "start",
  "payload": {
    "query": "subscription { exceptionUpdated(filters: { severities: [HIGH, CRITICAL] }) { eventType exception { transactionId status severity } timestamp } }",
    "variables": {}
  }
}
```

## Available Subscriptions

### 1. Exception Updates

Subscribe to real-time exception events:

```graphql
subscription ExceptionUpdates($filters: SubscriptionFilters) {
  exceptionUpdated(filters: $filters) {
    eventType
    exception {
      transactionId
      interfaceType
      status
      severity
      timestamp
    }
    timestamp
    triggeredBy
  }
}
```

**Variables:**
```json
{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION"],
    "severities": ["HIGH", "CRITICAL"],
    "includeResolved": false
  }
}
```

### 2. Retry Status Updates

Subscribe to retry operation status changes:

```graphql
subscription RetryStatusUpdates($transactionId: String) {
  retryStatusUpdated(transactionId: $transactionId) {
    transactionId
    retryAttempt {
      attemptNumber
      status
      initiatedBy
      initiatedAt
      completedAt
      resultSuccess
    }
    eventType
    timestamp
  }
}
```

**Variables:**
```json
{
  "transactionId": "TXN-12345-67890"
}
```

### 3. Summary Updates

Subscribe to real-time summary statistics:

```graphql
subscription SummaryUpdates($timeRange: TimeRange!) {
  summaryUpdated(timeRange: $timeRange) {
    totalExceptions
    byInterfaceType {
      interfaceType
      count
      percentage
    }
    keyMetrics {
      retrySuccessRate
      criticalExceptionCount
    }
  }
}
```

**Variables:**
```json
{
  "timeRange": {
    "period": "LAST_HOUR"
  }
}
```

## JavaScript Client Example

```javascript
class GraphQLSubscriptionClient {
  constructor(url, token) {
    this.url = url;
    this.token = token;
    this.subscriptions = new Map();
    this.connect();
  }

  connect() {
    this.ws = new WebSocket(this.url, 'graphql-ws');
    
    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.init();
    };

    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.handleMessage(message);
    };

    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      // Implement reconnection logic
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  init() {
    this.send({
      type: 'connection_init',
      payload: {
        Authorization: `Bearer ${this.token}`
      }
    });
  }

  subscribe(id, query, variables = {}) {
    const message = {
      id,
      type: 'start',
      payload: {
        query,
        variables
      }
    };

    this.send(message);
    return id;
  }

  unsubscribe(id) {
    this.send({
      id,
      type: 'stop'
    });
    this.subscriptions.delete(id);
  }

  send(message) {
    if (this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  handleMessage(message) {
    switch (message.type) {
      case 'connection_ack':
        console.log('Connection acknowledged');
        break;
      case 'data':
        console.log('Subscription data:', message.payload);
        // Handle subscription data
        break;
      case 'error':
        console.error('Subscription error:', message.payload);
        break;
      case 'complete':
        console.log('Subscription completed:', message.id);
        this.subscriptions.delete(message.id);
        break;
      case 'ka':
        // Keep alive message
        break;
    }
  }
}

// Usage example
const client = new GraphQLSubscriptionClient('ws://localhost:8080/subscriptions', 'your-jwt-token');

// Subscribe to exception updates
const subscriptionId = client.subscribe(
  'exception-updates',
  `subscription {
    exceptionUpdated(filters: { severities: [HIGH, CRITICAL] }) {
      eventType
      exception {
        transactionId
        status
        severity
      }
      timestamp
    }
  }`
);

// Subscribe to retry status for specific transaction
client.subscribe(
  'retry-status',
  `subscription($transactionId: String) {
    retryStatusUpdated(transactionId: $transactionId) {
      transactionId
      eventType
      retryAttempt {
        status
        resultSuccess
      }
    }
  }`,
  { transactionId: 'TXN-12345' }
);
```

## Message Types

### Client to Server

- **`connection_init`** - Initialize connection with authentication
- **`start`** - Start a subscription
- **`stop`** - Stop a subscription
- **`connection_terminate`** - Terminate connection

### Server to Client

- **`connection_ack`** - Connection acknowledged
- **`data`** - Subscription data event
- **`error`** - Error message
- **`complete`** - Subscription completed
- **`ka`** - Keep alive

## Authentication

Include JWT token in the connection initialization payload:

```json
{
  "type": "connection_init",
  "payload": {
    "Authorization": "Bearer your-jwt-token"
  }
}
```

## Error Handling

The server will send error messages for:

- **Authentication failures**
- **Invalid subscription queries**
- **Permission denied**
- **Internal server errors**

Example error response:

```json
{
  "type": "error",
  "payload": {
    "message": "Authentication required for subscriptions",
    "extensions": {
      "code": "AUTHENTICATION_ERROR"
    }
  }
}
```

## Testing with wscat

You can test subscriptions using `wscat`:

```bash
# Install wscat
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:8080/subscriptions -s graphql-ws

# Send connection init
{"type":"connection_init","payload":{"Authorization":"Bearer your-token"}}

# Start subscription
{"id":"test","type":"start","payload":{"query":"subscription { exceptionUpdated { eventType exception { transactionId } } }"}}
```

## Production Considerations

- **Connection limits** - Configure maximum concurrent connections
- **Authentication** - Validate JWT tokens on connection
- **Rate limiting** - Implement subscription rate limiting
- **Heartbeat** - Send periodic keep-alive messages
- **Reconnection** - Implement client-side reconnection logic
- **Error handling** - Graceful error handling and recovery