# GraphQL Developer Guide

## Interface Exception Collector GraphQL API

### Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication Setup](#authentication-setup)
3. [Client Integration Examples](#client-integration-examples)
4. [Common Query Patterns](#common-query-patterns)
5. [Real-time Subscriptions](#real-time-subscriptions)
6. [Error Handling](#error-handling)
7. [Performance Optimization](#performance-optimization)
8. [Testing Strategies](#testing-strategies)

## Getting Started

### Prerequisites

- Node.js 16+ or equivalent runtime
- Valid JWT token with appropriate roles
- Network access to the GraphQL endpoint

### Quick Start

1. **Install Apollo Client** (recommended GraphQL client):

```bash
npm install @apollo/client graphql
```

2. **Basic Client Setup**:

```javascript
import { ApolloClient, InMemoryCache, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

const httpLink = createHttpLink({
    uri: 'https://your-domain.com/graphql',
});

const authLink = setContext((_, { headers }) => {
    const token = localStorage.getItem('jwt-token');
    return {
        headers: {
            ...headers,
            authorization: token ? `Bearer ${token}` : "",
        }
    };
});

const client = new ApolloClient({
    link: authLink.concat(httpLink),
    cache: new InMemoryCache(),
    defaultOptions: {
        watchQuery: {
            errorPolicy: 'all'
        },
        query: {
            errorPolicy: 'all'
        }
    }
});
```

## Authentication Setup

### JWT Token Requirements

Your JWT token must include these claims:

```json
{
    "sub": "user123",
    "roles": ["OPERATIONS", "VIEWER"],
    "exp": 1640995200,
    "iat": 1640908800,
    "iss": "biopro-auth-service",
    "aud": "interface-exception-api"
}
```

### Role-Based Access

| Role | Permissions |
|------|-------------|
| `VIEWER` | Read-only access to exceptions and summaries |
| `OPERATIONS` | VIEWER + retry/acknowledge operations + payload access |
| `ADMIN` | Full access including system health and configuration |

### Token Refresh Example

```javascript
import { from } from '@apollo/client';
import { onError } from '@apollo/client/link/error';

const errorLink = onError(({ graphQLErrors, networkError, operation, forward }) => {
    if (graphQLErrors) {
        for (let err of graphQLErrors) {
            if (err.extensions?.code === 'AUTHORIZATION_ERROR') {
                // Token expired, refresh it
                return from(
                    refreshToken().then(newToken => {
                        localStorage.setItem('jwt-token', newToken);
                        // Retry the operation
                        const oldHeaders = operation.getContext().headers;
                        operation.setContext({
                            headers: {
                                ...oldHeaders,
                                authorization: `Bearer ${newToken}`,
                            },
                        });
                        return forward(operation);
                    })
                );
            }
        }
    }
});
```

## Client Integration Examples

### React Hook Example

```javascript
import { useQuery, useMutation, useSubscription } from '@apollo/client';
import { gql } from '@apollo/client';

// Query hook for exception list
const GET_EXCEPTIONS = gql`
    query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput) {
        exceptions(filters: $filters, pagination: $pagination) {
            edges {
                node {
                    id
                    transactionId
                    interfaceType
                    exceptionReason
                    status
                    severity
                    timestamp
                    customerId
                }
                cursor
            }
            pageInfo {
                hasNextPage
                hasPreviousPage
                startCursor
                endCursor
            }
            totalCount
        }
    }
`;

function ExceptionList({ filters }) {
    const { loading, error, data, fetchMore } = useQuery(GET_EXCEPTIONS, {
        variables: {
            filters,
            pagination: { first: 20 }
        },
        pollInterval: 30000, // Poll every 30 seconds
    });

    const loadMore = () => {
        fetchMore({
            variables: {
                pagination: {
                    first: 20,
                    after: data.exceptions.pageInfo.endCursor
                }
            },
            updateQuery: (prev, { fetchMoreResult }) => {
                if (!fetchMoreResult) return prev;
                return {
                    exceptions: {
                        ...fetchMoreResult.exceptions,
                        edges: [
                            ...prev.exceptions.edges,
                            ...fetchMoreResult.exceptions.edges
                        ]
                    }
                };
            }
        });
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error.message}</div>;

    return (
        <div>
            {data.exceptions.edges.map(({ node }) => (
                <ExceptionCard key={node.id} exception={node} />
            ))}
            {data.exceptions.pageInfo.hasNextPage && (
                <button onClick={loadMore}>Load More</button>
            )}
        </div>
    );
}
```

### Mutation Hook Example

```javascript
const RETRY_EXCEPTION = gql`
    mutation RetryException($input: RetryExceptionInput!) {
        retryException(input: $input) {
            success
            exception {
                id
                transactionId
                status
                retryCount
            }
            retryAttempt {
                attemptNumber
                status
                initiatedAt
            }
            errors {
                message
                code
                path
            }
        }
    }
`;

function RetryButton({ transactionId }) {
    const [retryException, { loading, error }] = useMutation(RETRY_EXCEPTION, {
        onCompleted: (data) => {
            if (data.retryException.success) {
                toast.success('Retry initiated successfully');
            } else {
                toast.error('Retry failed: ' + data.retryException.errors[0]?.message);
            }
        },
        // Update cache after successful retry
        update: (cache, { data }) => {
            if (data.retryException.success) {
                cache.modify({
                    id: cache.identify(data.retryException.exception),
                    fields: {
                        status: () => data.retryException.exception.status,
                        retryCount: () => data.retryException.exception.retryCount,
                    },
                });
            }
        }
    });

    const handleRetry = () => {
        retryException({
            variables: {
                input: {
                    transactionId,
                    reason: 'Manual retry from dashboard',
                    priority: 'NORMAL'
                }
            }
        });
    };

    return (
        <button 
            onClick={handleRetry} 
            disabled={loading}
            className="retry-button"
        >
            {loading ? 'Retrying...' : 'Retry'}
        </button>
    );
}
```

### Subscription Hook Example

```javascript
const EXCEPTION_UPDATES = gql`
    subscription ExceptionUpdates($filters: SubscriptionFilters) {
        exceptionUpdated(filters: $filters) {
            eventType
            exception {
                id
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
`;

function RealTimeExceptionMonitor({ filters }) {
    const { data, loading, error } = useSubscription(EXCEPTION_UPDATES, {
        variables: { filters },
        onSubscriptionData: ({ subscriptionData }) => {
            const update = subscriptionData.data.exceptionUpdated;
            
            // Show notification for critical exceptions
            if (update.exception.severity === 'CRITICAL') {
                showNotification({
                    title: 'Critical Exception',
                    message: `${update.exception.interfaceType}: ${update.exception.transactionId}`,
                    type: 'error'
                });
            }
            
            // Update dashboard counters
            updateDashboardCounters(update);
        }
    });

    if (error) {
        console.error('Subscription error:', error);
        return <div>Real-time updates unavailable</div>;
    }

    return (
        <div className="real-time-monitor">
            <div className={`status ${loading ? 'connecting' : 'connected'}`}>
                {loading ? 'Connecting...' : 'Live Updates Active'}
            </div>
        </div>
    );
}
```

## Common Query Patterns

### 1. Dashboard Summary Query

```javascript
const DASHBOARD_SUMMARY = gql`
    query DashboardSummary($timeRange: TimeRange!) {
        exceptionSummary(timeRange: $timeRange) {
            totalExceptions
            
            byInterfaceType {
                interfaceType
                count
                percentage
            }
            
            bySeverity {
                severity
                count
                percentage
            }
            
            byStatus {
                status
                count
                percentage
            }
            
            keyMetrics {
                retrySuccessRate
                averageResolutionTime
                customerImpactCount
                criticalExceptionCount
            }
            
            trends {
                timestamp
                count
                interfaceType
            }
        }
    }
`;

// Usage with caching
function DashboardSummary() {
    const { data, loading, error } = useQuery(DASHBOARD_SUMMARY, {
        variables: {
            timeRange: { period: 'LAST_24_HOURS' }
        },
        fetchPolicy: 'cache-and-network',
        pollInterval: 60000, // Refresh every minute
    });

    return (
        <div className="dashboard-summary">
            <MetricsCards metrics={data?.exceptionSummary.keyMetrics} />
            <StatusChart data={data?.exceptionSummary.byStatus} />
            <TrendChart data={data?.exceptionSummary.trends} />
        </div>
    );
}
```

### 2. Exception Detail with Lazy Loading

```javascript
const EXCEPTION_DETAIL = gql`
    query ExceptionDetail($transactionId: String!) {
        exception(transactionId: $transactionId) {
            id
            transactionId
            externalId
            interfaceType
            exceptionReason
            operation
            status
            severity
            category
            customerId
            locationCode
            timestamp
            processedAt
            retryable
            retryCount
            maxRetries
            
            # Lazy load these expensive fields
            originalPayload @include(if: $includePayload) {
                content
                contentType
                retrievedAt
                sourceService
            }
            
            retryHistory @include(if: $includeHistory) {
                id
                attemptNumber
                status
                initiatedBy
                initiatedAt
                completedAt
                resultSuccess
                resultMessage
            }
        }
    }
`;

function ExceptionDetail({ transactionId }) {
    const [includePayload, setIncludePayload] = useState(false);
    const [includeHistory, setIncludeHistory] = useState(false);

    const { data, loading, error, refetch } = useQuery(EXCEPTION_DETAIL, {
        variables: {
            transactionId,
            includePayload,
            includeHistory
        }
    });

    const loadPayload = () => {
        setIncludePayload(true);
        refetch();
    };

    const loadHistory = () => {
        setIncludeHistory(true);
        refetch();
    };

    return (
        <div className="exception-detail">
            <ExceptionInfo exception={data?.exception} />
            
            {!includePayload ? (
                <button onClick={loadPayload}>Load Original Payload</button>
            ) : (
                <PayloadViewer payload={data?.exception.originalPayload} />
            )}
            
            {!includeHistory ? (
                <button onClick={loadHistory}>Load Retry History</button>
            ) : (
                <RetryHistory history={data?.exception.retryHistory} />
            )}
        </div>
    );
}
```

### 3. Advanced Search with Debouncing

```javascript
const SEARCH_EXCEPTIONS = gql`
    query SearchExceptions($search: SearchInput!, $pagination: PaginationInput) {
        searchExceptions(search: $search, pagination: $pagination) {
            edges {
                node {
                    id
                    transactionId
                    interfaceType
                    exceptionReason
                    status
                    severity
                    timestamp
                }
                cursor
            }
            pageInfo {
                hasNextPage
                hasPreviousPage
            }
            totalCount
        }
    }
`;

function ExceptionSearch() {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm] = useDebounce(searchTerm, 300);

    const { data, loading, error } = useQuery(SEARCH_EXCEPTIONS, {
        variables: {
            search: {
                query: debouncedSearchTerm,
                fields: ['EXCEPTION_REASON', 'TRANSACTION_ID', 'CUSTOMER_ID'],
                fuzzy: true
            },
            pagination: { first: 20 }
        },
        skip: !debouncedSearchTerm || debouncedSearchTerm.length < 3,
    });

    return (
        <div className="exception-search">
            <input
                type="text"
                placeholder="Search exceptions..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
            />
            
            {loading && <div>Searching...</div>}
            {error && <div>Search error: {error.message}</div>}
            
            {data?.searchExceptions.edges.map(({ node }) => (
                <SearchResult key={node.id} exception={node} />
            ))}
        </div>
    );
}
```

## Real-time Subscriptions

### WebSocket Connection Management

```javascript
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';

const wsLink = new GraphQLWsLink(createClient({
    url: 'wss://your-domain.com/subscriptions',
    connectionParams: () => ({
        authorization: `Bearer ${localStorage.getItem('jwt-token')}`,
    }),
    on: {
        connected: () => console.log('WebSocket connected'),
        closed: () => console.log('WebSocket closed'),
        error: (error) => console.error('WebSocket error:', error),
    },
    retryAttempts: 5,
    retryWait: async (retries) => {
        await new Promise(resolve => setTimeout(resolve, 2 ** retries * 1000));
    },
}));

// Split link for HTTP queries and WebSocket subscriptions
import { split } from '@apollo/client';
import { getMainDefinition } from '@apollo/client/utilities';

const splitLink = split(
    ({ query }) => {
        const definition = getMainDefinition(query);
        return (
            definition.kind === 'OperationDefinition' &&
            definition.operation === 'subscription'
        );
    },
    wsLink,
    authLink.concat(httpLink),
);

const client = new ApolloClient({
    link: splitLink,
    cache: new InMemoryCache(),
});
```

### Subscription with Filtering

```javascript
function LiveExceptionFeed({ filters }) {
    const { data, loading, error } = useSubscription(EXCEPTION_UPDATES, {
        variables: {
            filters: {
                severities: ['HIGH', 'CRITICAL'],
                interfaceTypes: filters.interfaceTypes,
                includeResolved: false
            }
        },
        shouldResubscribe: true, // Resubscribe on variable changes
    });

    useEffect(() => {
        if (data?.exceptionUpdated) {
            const update = data.exceptionUpdated;
            
            // Add to live feed
            addToLiveFeed(update);
            
            // Play sound for critical exceptions
            if (update.exception.severity === 'CRITICAL') {
                playAlertSound();
            }
            
            // Update browser title with count
            updateBrowserTitle(getCurrentExceptionCount());
        }
    }, [data]);

    return (
        <div className="live-feed">
            <div className="connection-status">
                {loading ? 'Connecting...' : 'Live'}
                {error && <span className="error">Disconnected</span>}
            </div>
            <LiveFeedItems />
        </div>
    );
}
```

## Error Handling

### Comprehensive Error Handler

```javascript
function GraphQLErrorHandler({ error, operation }) {
    const handleError = (error) => {
        if (error.networkError) {
            // Network errors
            if (error.networkError.statusCode === 401) {
                // Redirect to login
                window.location.href = '/login';
                return;
            }
            
            if (error.networkError.statusCode >= 500) {
                showNotification({
                    title: 'Server Error',
                    message: 'The server is experiencing issues. Please try again later.',
                    type: 'error'
                });
                return;
            }
        }

        if (error.graphQLErrors) {
            error.graphQLErrors.forEach(gqlError => {
                switch (gqlError.extensions?.code) {
                    case 'VALIDATION_ERROR':
                        showFieldValidationError(gqlError);
                        break;
                    case 'AUTHORIZATION_ERROR':
                        showNotification({
                            title: 'Access Denied',
                            message: 'You do not have permission to perform this action.',
                            type: 'warning'
                        });
                        break;
                    case 'NOT_FOUND':
                        showNotification({
                            title: 'Not Found',
                            message: 'The requested exception could not be found.',
                            type: 'info'
                        });
                        break;
                    case 'RATE_LIMIT_EXCEEDED':
                        showNotification({
                            title: 'Rate Limit Exceeded',
                            message: 'Too many requests. Please wait before trying again.',
                            type: 'warning'
                        });
                        break;
                    default:
                        console.error('GraphQL Error:', gqlError);
                        showNotification({
                            title: 'Operation Failed',
                            message: gqlError.message,
                            type: 'error'
                        });
                }
            });
        }
    };

    useEffect(() => {
        if (error) {
            handleError(error);
        }
    }, [error]);

    return null;
}
```

### Retry Logic for Failed Operations

```javascript
import { RetryLink } from '@apollo/client/link/retry';

const retryLink = new RetryLink({
    delay: {
        initial: 300,
        max: Infinity,
        jitter: true
    },
    attempts: {
        max: 5,
        retryIf: (error, _operation) => {
            // Retry on network errors and 5xx server errors
            return !!error && (
                error.networkError?.statusCode >= 500 ||
                !error.networkError // Network connectivity issues
            );
        }
    }
});
```

## Performance Optimization

### Query Optimization Strategies

```javascript
// 1. Use fragments for reusable field sets
const EXCEPTION_FRAGMENT = gql`
    fragment ExceptionBasicInfo on Exception {
        id
        transactionId
        interfaceType
        status
        severity
        timestamp
        exceptionReason
    }
`;

// 2. Implement field-level caching
const typePolicies = {
    Exception: {
        fields: {
            originalPayload: {
                merge: false, // Don't merge, replace entirely
            },
            retryHistory: {
                merge(existing = [], incoming) {
                    return [...existing, ...incoming];
                }
            }
        }
    },
    Query: {
        fields: {
            exceptionSummary: {
                keyArgs: ['timeRange', 'filters'],
                merge: false,
            }
        }
    }
};

// 3. Batch multiple queries
const GET_DASHBOARD_DATA = gql`
    query GetDashboardData($timeRange: TimeRange!, $filters: ExceptionFilters) {
        summary: exceptionSummary(timeRange: $timeRange, filters: $filters) {
            totalExceptions
            keyMetrics {
                retrySuccessRate
                criticalExceptionCount
            }
        }
        
        recentExceptions: exceptions(
            filters: $filters
            pagination: { first: 10 }
            sorting: { field: "timestamp", direction: DESC }
        ) {
            edges {
                node {
                    ...ExceptionBasicInfo
                }
            }
        }
        
        systemHealth {
            status
            database { status }
            cache { status }
        }
    }
    ${EXCEPTION_FRAGMENT}
`;
```

### Caching Strategies

```javascript
// Custom cache configuration
const cache = new InMemoryCache({
    typePolicies: {
        Query: {
            fields: {
                exceptions: {
                    keyArgs: ['filters', 'sorting'],
                    merge(existing, incoming, { args }) {
                        if (args?.pagination?.after) {
                            // Append for pagination
                            return {
                                ...incoming,
                                edges: [
                                    ...(existing?.edges || []),
                                    ...incoming.edges
                                ]
                            };
                        }
                        return incoming;
                    }
                }
            }
        }
    }
});

// Cache persistence
import { persistCache, LocalStorageWrapper } from 'apollo3-cache-persist';

await persistCache({
    cache,
    storage: new LocalStorageWrapper(window.localStorage),
    maxSize: 1048576, // 1MB
    debug: process.env.NODE_ENV === 'development',
});
```

## Testing Strategies

### Unit Testing with Mock Provider

```javascript
import { MockedProvider } from '@apollo/client/testing';
import { render, screen, waitFor } from '@testing-library/react';

const mocks = [
    {
        request: {
            query: GET_EXCEPTIONS,
            variables: {
                filters: { severities: ['HIGH'] },
                pagination: { first: 20 }
            }
        },
        result: {
            data: {
                exceptions: {
                    edges: [
                        {
                            node: {
                                id: '1',
                                transactionId: 'TXN-001',
                                interfaceType: 'ORDER_COLLECTION',
                                status: 'NEW',
                                severity: 'HIGH',
                                timestamp: '2024-01-01T10:00:00Z'
                            },
                            cursor: 'cursor1'
                        }
                    ],
                    pageInfo: {
                        hasNextPage: false,
                        hasPreviousPage: false
                    },
                    totalCount: 1
                }
            }
        }
    }
];

test('renders exception list', async () => {
    render(
        <MockedProvider mocks={mocks} addTypename={false}>
            <ExceptionList filters={{ severities: ['HIGH'] }} />
        </MockedProvider>
    );

    await waitFor(() => {
        expect(screen.getByText('TXN-001')).toBeInTheDocument();
    });
});
```

### Integration Testing

```javascript
import { createTestClient } from 'apollo-server-testing';
import { server } from '../src/server';

const { query, mutate } = createTestClient(server);

describe('GraphQL Integration Tests', () => {
    test('should fetch exceptions with filters', async () => {
        const GET_EXCEPTIONS = gql`
            query GetExceptions($filters: ExceptionFilters) {
                exceptions(filters: $filters) {
                    totalCount
                    edges {
                        node {
                            id
                            transactionId
                        }
                    }
                }
            }
        `;

        const result = await query({
            query: GET_EXCEPTIONS,
            variables: {
                filters: { severities: ['CRITICAL'] }
            }
        });

        expect(result.errors).toBeUndefined();
        expect(result.data.exceptions.totalCount).toBeGreaterThan(0);
    });

    test('should retry exception successfully', async () => {
        const RETRY_EXCEPTION = gql`
            mutation RetryException($input: RetryExceptionInput!) {
                retryException(input: $input) {
                    success
                    exception {
                        status
                        retryCount
                    }
                }
            }
        `;

        const result = await mutate({
            mutation: RETRY_EXCEPTION,
            variables: {
                input: {
                    transactionId: 'TXN-001',
                    reason: 'Test retry',
                    priority: 'NORMAL'
                }
            }
        });

        expect(result.errors).toBeUndefined();
        expect(result.data.retryException.success).toBe(true);
    });
});
```

This developer guide provides comprehensive examples and patterns for integrating with the GraphQL API effectively. Use these patterns as starting points and adapt them to your specific use cases.