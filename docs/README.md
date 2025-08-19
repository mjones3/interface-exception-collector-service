# GraphQL API Documentation

## Interface Exception Collector GraphQL API

Welcome to the comprehensive documentation for the Interface Exception Collector GraphQL API. This documentation provides everything you need to integrate with and use the API effectively.

## 📚 Documentation Overview

### Core Documentation

1. **[GraphQL API Documentation](GRAPHQL_API_DOCUMENTATION.md)**
   - Complete API reference with all queries, mutations, and subscriptions
   - Schema overview and type definitions
   - Performance considerations and best practices
   - Error handling and response formats

2. **[Developer Guide](GRAPHQL_DEVELOPER_GUIDE.md)**
   - Practical examples and integration patterns
   - Common query patterns and optimization techniques
   - Real-time subscriptions implementation
   - Testing strategies and debugging tips

3. **[Authentication & Authorization Guide](GRAPHQL_AUTHENTICATION_GUIDE.md)**
   - JWT token structure and validation
   - Role-based access control (RBAC)
   - Security best practices and audit logging
   - Permission troubleshooting

4. **[Apollo Client Integration](APOLLO_CLIENT_INTEGRATION.md)**
   - Complete Apollo Client setup and configuration
   - React hooks and component examples
   - Advanced patterns and optimization
   - WebSocket subscriptions with Apollo

5. **[Troubleshooting Guide](GRAPHQL_TROUBLESHOOTING_GUIDE.md)**
   - Common issues and solutions
   - Error codes reference
   - Performance debugging
   - Network and connectivity problems

### Generated Documentation

The `scripts/generate-graphql-docs.js` script automatically generates up-to-date documentation from the GraphQL schema:

- **schema.md** - Complete schema documentation with examples
- **types.md** - Detailed type reference
- **schema.graphql** - Raw GraphQL SDL

## 🚀 Quick Start

### 1. Authentication Setup

First, obtain a JWT token with appropriate roles:

```javascript
// Include JWT token in all requests
const headers = {
    'Authorization': `Bearer ${your_jwt_token}`,
    'Content-Type': 'application/json'
};
```

### 2. Basic Query Example

```graphql
query GetRecentExceptions {
    exceptions(
        pagination: { first: 10 }
        sorting: { field: "timestamp", direction: DESC }
    ) {
        edges {
            node {
                id
                transactionId
                interfaceType
                status
                severity
                timestamp
                exceptionReason
            }
        }
        totalCount
    }
}
```

### 3. Real-time Subscriptions

```graphql
subscription ExceptionUpdates {
    exceptionUpdated(
        filters: { 
            severities: [HIGH, CRITICAL]
            includeResolved: false 
        }
    ) {
        eventType
        exception {
            transactionId
            interfaceType
            status
            severity
        }
        timestamp
    }
}
```

### 4. Mutation Example

```graphql
mutation RetryException {
    retryException(input: {
        transactionId: "TXN-12345"
        reason: "Service restored, retrying failed order"
        priority: HIGH
    }) {
        success
        exception {
            status
            retryCount
        }
        errors {
            message
            code
        }
    }
}
```

## 🔧 API Endpoints

| Environment | GraphQL Endpoint | WebSocket Endpoint | GraphiQL |
|-------------|------------------|-------------------|----------|
| Development | `http://localhost:8080/graphql` | `ws://localhost:8080/subscriptions` | `http://localhost:8080/graphiql` |
| Staging | `https://staging-api.biopro.com/graphql` | `wss://staging-api.biopro.com/subscriptions` | Not available |
| Production | `https://api.biopro.com/graphql` | `wss://api.biopro.com/subscriptions` | Not available |

## 🔐 Authentication & Roles

### Role Hierarchy

- **ADMIN** - Full system access including configuration and user management
- **OPERATIONS** - Exception management, retry operations, payload access
- **VIEWER** - Read-only access to exceptions and summaries

### JWT Token Requirements

Your JWT token must include:
- `sub` - User ID
- `roles` - Array of user roles
- `exp` - Token expiration
- `iat` - Issued at timestamp

## 📊 API Features

### Queries
- **Exception Lists** - Paginated, filtered, and sorted exception data
- **Exception Details** - Complete exception information with nested data
- **Summary Statistics** - Aggregated metrics and trend data
- **Search** - Full-text search across exception data
- **System Health** - API and service health monitoring

### Mutations
- **Retry Operations** - Initiate exception retries with priority
- **Acknowledgments** - Mark exceptions as acknowledged
- **Bulk Operations** - Perform actions on multiple exceptions
- **Status Management** - Update exception status and resolution

### Subscriptions
- **Real-time Updates** - Live exception events and status changes
- **Summary Updates** - Real-time dashboard statistics
- **Retry Status** - Live retry operation progress

## 🎯 Performance Guidelines

### Query Optimization
- Use pagination for large result sets (max 100 items per page)
- Request only needed fields to reduce payload size
- Implement client-side caching for frequently accessed data
- Use fragments for reusable field sets

### Rate Limits
| Role | Queries/min | Mutations/min | Concurrent Subscriptions |
|------|-------------|---------------|-------------------------|
| VIEWER | 100 | 0 | 5 |
| OPERATIONS | 200 | 50 | 10 |
| ADMIN | 500 | 100 | 20 |

### Caching Strategy
- **Summary queries** - 5-minute TTL
- **Exception details** - 1-hour TTL  
- **Payload data** - 24-hour TTL

## 🛠 Development Tools

### Schema Documentation Generator

Generate up-to-date documentation from the live schema:

```bash
# Install dependencies
cd scripts && npm install

# Generate documentation (requires running API)
npm run generate-docs

# Generate with authentication
JWT_TOKEN=your_token npm run generate-docs-with-auth
```

### Apollo Client DevTools

Install the Apollo Client DevTools browser extension for:
- Query and mutation inspection
- Cache exploration and debugging
- Subscription monitoring
- Performance analysis

### GraphiQL Interface

Access the interactive GraphQL explorer in development:
- URL: `http://localhost:8080/graphiql`
- Features: Query building, schema exploration, documentation

## 🔍 Monitoring & Observability

### Health Checks

```graphql
query SystemHealth {
    systemHealth {
        status
        database { status responseTime }
        cache { status responseTime }
        externalServices {
            serviceName
            status
            lastChecked
        }
    }
}
```

### Metrics Collection

The API collects metrics on:
- Query performance and complexity
- Error rates by operation type
- Cache hit rates and effectiveness
- WebSocket connection counts
- Authentication failures

### Logging

All operations are logged with:
- User identification and roles
- Operation names and variables
- Response times and error details
- Security events and access attempts

## 🚨 Error Handling

### Common Error Codes

| Code | Description | Action |
|------|-------------|--------|
| `AUTHORIZATION_ERROR` | Authentication failed | Check token validity |
| `VALIDATION_ERROR` | Invalid input | Validate request data |
| `NOT_FOUND` | Resource not found | Verify resource exists |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Implement throttling |
| `QUERY_COMPLEXITY_EXCEEDED` | Query too complex | Simplify query |

### Error Response Format

```json
{
    "errors": [
        {
            "message": "Access denied",
            "extensions": {
                "code": "AUTHORIZATION_ERROR",
                "reason": "INSUFFICIENT_PERMISSIONS",
                "requiredRole": "OPERATIONS"
            },
            "path": ["retryException"]
        }
    ]
}
```

## 📞 Support & Contact

### Development Team
- **GraphQL API**: graphql-team@biopro.com
- **Authentication**: auth-team@biopro.com
- **Infrastructure**: devops-team@biopro.com

### Resources
- **API Status**: https://status.biopro.com
- **Issue Tracker**: https://jira.biopro.com/projects/GRAPHQL
- **Slack Channel**: #graphql-api-support

### Contributing

1. Review the [API Documentation](GRAPHQL_API_DOCUMENTATION.md)
2. Check the [Troubleshooting Guide](GRAPHQL_TROUBLESHOOTING_GUIDE.md)
3. Test with the development environment
4. Submit issues with detailed reproduction steps

## 📝 Changelog

### Version 1.0.0 (Current)
- Initial GraphQL API implementation
- Complete CRUD operations for exceptions
- Real-time subscriptions
- JWT authentication and RBAC
- Comprehensive documentation

### Upcoming Features
- GraphQL Federation support
- Enhanced search capabilities
- Advanced analytics queries
- Webhook integrations

---

**Last Updated**: Generated automatically on each documentation build

For the most current information, always refer to the generated schema documentation and the live GraphiQL interface in development environments.