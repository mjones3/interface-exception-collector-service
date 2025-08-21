# GraphQL Subscriptions Implementation

## Overview

This document describes the implementation of GraphQL subscriptions for real-time updates in the Interface Exception Collector service, as part of task 3.3 "Enable Subscription Resolvers".

## Components Implemented

### 1. Subscription Resolvers (`ExceptionSubscriptionResolver`)

**Location**: `src/main/java/com/arcone/biopro/exception/collector/api/graphql/resolver/ExceptionSubscriptionResolver.java`

**Features**:
- Real-time exception updates via `exceptionUpdated` subscription
- Retry status updates via `retryStatusUpdated` subscription
- Security filtering based on user roles (VIEWER, OPERATIONS, ADMIN)
- Subscription filtering based on user-provided criteria
- Connection timeout handling (30 minutes for exceptions, 15 minutes for retries)
- Active subscription tracking for monitoring

**Security**:
- `@PreAuthorize("hasRole('VIEWER')")` for exception updates
- `@PreAuthorize("hasRole('OPERATIONS')")` for retry status updates
- Field-level security filtering through `GraphQLSecurityService`

### 2. WebSocket Configuration (`GraphQLWebSocketConfig`)

**Location**: `src/main/java/com/arcone/biopro/exception/collector/infrastructure/config/GraphQLWebSocketConfig.java`

**Features**:
- WebSocket endpoint configuration for GraphQL subscriptions
- JWT token authentication support (via Authorization header or query parameter)
- Connection monitoring and management
- Handshake interceptors for authentication validation

**Configuration**:
- WebSocket path: `/subscriptions` (configured in `application.yml`)
- Supports JWT authentication for secure connections
- Connection tracking for monitoring purposes

### 3. Event Bridge (`SubscriptionEventBridge`)

**Location**: `src/main/java/com/arcone/biopro/exception/collector/api/graphql/service/SubscriptionEventBridge.java`

**Features**:
- Bridges domain events to GraphQL subscription events
- Listens for Spring application events and publishes to GraphQL subscribers
- Supports all exception lifecycle events (created, updated, acknowledged, resolved)
- Supports retry lifecycle events (initiated, completed, failed, cancelled)

**Event Types Supported**:
- `ExceptionCreatedEvent`
- `ExceptionUpdatedEvent`
- `ExceptionAcknowledgedEvent`
- `ExceptionResolvedEvent`
- `RetryInitiatedEvent`
- `RetryCompletedEvent`
- `RetryCancelledEvent`

### 4. Service Integration

**Modified Services**:
- `ExceptionManagementService`: Now publishes application events for acknowledgment and resolution
- `RetryService`: Now publishes application events for retry initiation, completion, and failure

**Integration Points**:
- Services publish Spring application events
- `SubscriptionEventBridge` listens for these events
- Events are transformed and published to GraphQL subscribers
- Existing Kafka event publishing is preserved

## GraphQL Schema

**Subscription Types**:
```graphql
type Subscription {
    # Real-time exception updates
    exceptionUpdated(filters: SubscriptionFilters): ExceptionUpdateEvent!
    
    # Retry operation updates
    retryStatusUpdated(transactionId: String): RetryStatusEvent!
}
```

**Event Types**:
```graphql
type ExceptionUpdateEvent {
    eventType: ExceptionEventType!
    exception: Exception!
    timestamp: DateTime!
    triggeredBy: String
}

type RetryStatusEvent {
    transactionId: String!
    retryAttempt: RetryAttempt!
    eventType: RetryEventType!
    timestamp: DateTime!
}
```

## Security Implementation

### Authentication
- JWT token support via WebSocket handshake
- Token can be provided in Authorization header or as query parameter
- Authentication context preserved throughout subscription lifecycle

### Authorization
- Role-based access control (RBAC)
- VIEWER: Can subscribe to basic exception updates
- OPERATIONS: Can subscribe to exception updates and retry status
- ADMIN: Full access to all subscription types

### Filtering
- Security filtering applied at subscription level
- User permissions checked for each event before delivery
- Subscription filters allow users to narrow down events of interest

## Configuration

### Application Properties
```yaml
spring:
  graphql:
    websocket:
      path: /subscriptions
    graphiql:
      enabled: true

graphql:
  websocket:
    heartbeat:
      interval: 30
    max-connections: 1000
    connection-timeout: 30
    subscription:
      buffer-size: 1000
      latency-threshold-ms: 2000
```

### Security Configuration
- WebSocket connections support CORS configuration
- JWT authentication integrated with existing security setup
- Rate limiting can be applied at the WebSocket level

## Testing

### Test Coverage
- **SubscriptionBasicTest**: Basic functionality and component availability
- **WebSocketConnectivityTest**: WebSocket connection establishment and authentication
- **SubscriptionIntegrationTest**: End-to-end subscription functionality (when dependencies are available)

### Test Scenarios
- Subscription creation and management
- Event publishing and delivery
- Security filtering and authorization
- WebSocket connection handling
- Timeout and error handling

## Monitoring and Observability

### Metrics
- Active subscription count tracking
- Connection establishment/termination events
- Event publishing success/failure rates
- Subscription timeout occurrences

### Logging
- WebSocket handshake events
- Subscription lifecycle events
- Security filtering decisions
- Event publishing activities

## Usage Examples

### Client Subscription (JavaScript)
```javascript
const subscription = `
  subscription {
    exceptionUpdated(filters: { severity: CRITICAL }) {
      eventType
      exception {
        transactionId
        severity
        status
      }
      timestamp
      triggeredBy
    }
  }
`;

// WebSocket connection with JWT
const client = new GraphQLWebSocketClient('ws://localhost:8080/subscriptions?token=your-jwt-token');
client.subscribe(subscription, (data) => {
  console.log('Exception update:', data.exceptionUpdated);
});
```

### Server Event Publishing
```java
// In service methods
applicationEventPublisher.publishEvent(
    new SubscriptionEventBridge.ExceptionAcknowledgedEvent(exception, "user123")
);
```

## Performance Considerations

### Scalability
- Subscription filtering reduces unnecessary network traffic
- Connection timeouts prevent resource leaks
- Buffer size limits prevent memory issues

### Resource Management
- Active connection tracking for monitoring
- Automatic cleanup of stale connections
- Configurable connection limits

## Future Enhancements

### Potential Improvements
1. **Advanced Filtering**: More sophisticated subscription filters
2. **Batch Updates**: Grouping multiple events for efficiency
3. **Persistence**: Replay missed events for reconnecting clients
4. **Clustering**: Support for multi-instance deployments
5. **Metrics Dashboard**: Real-time subscription monitoring UI

### Integration Opportunities
1. **Push Notifications**: Mobile/browser notifications for critical events
2. **Webhook Integration**: HTTP callbacks for external systems
3. **Message Queues**: Integration with external messaging systems
4. **Analytics**: Event streaming for business intelligence

## Troubleshooting

### Common Issues
1. **Connection Failures**: Check JWT token validity and network connectivity
2. **Missing Events**: Verify subscription filters and user permissions
3. **Performance Issues**: Monitor active connection count and buffer usage
4. **Authentication Errors**: Validate JWT configuration and user roles

### Debug Configuration
```yaml
logging:
  level:
    com.arcone.biopro.exception.collector.api.graphql: DEBUG
    org.springframework.graphql: DEBUG
```

## Conclusion

The GraphQL subscriptions implementation provides real-time updates for exception and retry events with comprehensive security, filtering, and monitoring capabilities. The system is designed to be scalable, secure, and maintainable while integrating seamlessly with the existing service architecture.