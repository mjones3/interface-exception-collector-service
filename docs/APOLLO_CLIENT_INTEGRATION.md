# Apollo GraphQL Client Integration Guide

## Interface Exception Collector GraphQL API

### Overview

This guide provides comprehensive examples for integrating with the Interface Exception Collector GraphQL API using Apollo Client. It covers setup, configuration, and common usage patterns for React applications.

## Installation and Setup

### Dependencies

```bash
npm install @apollo/client graphql
npm install @apollo/client-react-streaming  # For React 18+ streaming
npm install graphql-ws                      # For WebSocket subscriptions
```

### Basic Apollo Client Setup

```javascript
// apollo-client.js
import { 
    ApolloClient, 
    InMemoryCache, 
    createHttpLink,
    from,
    split
} from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { onError } from '@apollo/client/link/error';
import { RetryLink } from '@apollo/client/link/retry';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { getMainDefinition } from '@apollo/client/utilities';
import { createClient } from 'graphql-ws';

// HTTP Link for queries and mutations
const httpLink = createHttpLink({
    uri: process.env.REACT_APP_GRAPHQL_ENDPOINT || 'http://localhost:8080/graphql',
});

// WebSocket Link for subscriptions
const wsLink = new GraphQLWsLink(createClient({
    url: process.env.REACT_APP_GRAPHQL_WS_ENDPOINT || 'ws://localhost:8080/subscriptions',
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

// Authentication link
const authLink = setContext((_, { headers }) => {
    const token = localStorage.getItem('jwt-token');
    return {
        headers: {
            ...headers,
            authorization: token ? `Bearer ${token}` : "",
        }
    };
});

// Error handling link
const errorLink = onError(({ graphQLErrors, networkError, operation, forward }) => {
    if (graphQLErrors) {
        graphQLErrors.forEach(({ message, locations, path, extensions }) => {
            console.error(
                `GraphQL error: Message: ${message}, Location: ${locations}, Path: ${path}`
            );
            
            // Handle specific error types
            switch (extensions?.code) {
                case 'AUTHORIZATION_ERROR':
                    localStorage.removeItem('jwt-token');
                    window.location.href = '/login';
                    break;
                case 'RATE_LIMIT_EXCEEDED':
                    // Show rate limit notification
                    break;
                default:
                    // Show generic error notification
                    break;
            }
        });
    }

    if (networkError) {
        console.error(`Network error: ${networkError}`);
        
        if (networkError.statusCode === 401) {
            localStorage.removeItem('jwt-token');
            window.location.href = '/login';
        }
    }
});

// Retry link for network failures
const retryLink = new RetryLink({
    delay: {
        initial: 300,
        max: Infinity,
        jitter: true
    },
    attempts: {
        max: 5,
        retryIf: (error, _operation) => {
            return !!error && (
                error.networkError?.statusCode >= 500 ||
                !error.networkError
            );
        }
    }
});

// Split link for HTTP vs WebSocket
const splitLink = split(
    ({ query }) => {
        const definition = getMainDefinition(query);
        return (
            definition.kind === 'OperationDefinition' &&
            definition.operation === 'subscription'
        );
    },
    wsLink,
    from([errorLink, authLink, retryLink, httpLink])
);

// Cache configuration
const cache = new InMemoryCache({
    typePolicies: {
        Query: {
            fields: {
                exceptions: {
                    keyArgs: ['filters', 'sorting'],
                    merge(existing, incoming, { args }) {
                        if (args?.pagination?.after) {
                            // Merge for pagination
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
                },
                exceptionSummary: {
                    keyArgs: ['timeRange', 'filters'],
                    merge: false, // Replace entirely
                }
            }
        },
        Exception: {
            fields: {
                retryHistory: {
                    merge(existing = [], incoming) {
                        return [...existing, ...incoming];
                    }
                }
            }
        }
    }
});

// Create Apollo Client
export const client = new ApolloClient({
    link: splitLink,
    cache,
    defaultOptions: {
        watchQuery: {
            errorPolicy: 'all',
            fetchPolicy: 'cache-and-network'
        },
        query: {
            errorPolicy: 'all',
            fetchPolicy: 'cache-first'
        }
    }
});
```

### Provider Setup

```javascript
// App.js
import React from 'react';
import { ApolloProvider } from '@apollo/client';
import { client } from './apollo-client';
import Dashboard from './components/Dashboard';

function App() {
    return (
        <ApolloProvider client={client}>
            <div className="App">
                <Dashboard />
            </div>
        </ApolloProvider>
    );
}

export default App;
```

## GraphQL Queries and Fragments

### Fragment Definitions

```javascript
// fragments.js
import { gql } from '@apollo/client';

export const EXCEPTION_BASIC_INFO = gql`
    fragment ExceptionBasicInfo on Exception {
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
        lastRetryAt
        acknowledgedBy
        acknowledgedAt
    }
`;

export const RETRY_ATTEMPT_INFO = gql`
    fragment RetryAttemptInfo on RetryAttempt {
        id
        attemptNumber
        status
        initiatedBy
        initiatedAt
        completedAt
        resultSuccess
        resultMessage
        resultResponseCode
        resultErrorDetails
    }
`;

export const EXCEPTION_SUMMARY_INFO = gql`
    fragment ExceptionSummaryInfo on ExceptionSummary {
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
    }
`;
```

### Query Definitions

```javascript
// queries.js
import { gql } from '@apollo/client';
import { EXCEPTION_BASIC_INFO, RETRY_ATTEMPT_INFO, EXCEPTION_SUMMARY_INFO } from './fragments';

export const GET_EXCEPTIONS = gql`
    query GetExceptions(
        $filters: ExceptionFilters
        $pagination: PaginationInput
        $sorting: SortingInput
    ) {
        exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
            edges {
                node {
                    ...ExceptionBasicInfo
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
    ${EXCEPTION_BASIC_INFO}
`;

export const GET_EXCEPTION_DETAIL = gql`
    query GetExceptionDetail($transactionId: String!) {
        exception(transactionId: $transactionId) {
            ...ExceptionBasicInfo
            
            originalPayload {
                content
                contentType
                retrievedAt
                sourceService
            }
            
            retryHistory {
                ...RetryAttemptInfo
            }
            
            statusHistory {
                id
                fromStatus
                toStatus
                changedBy
                changedAt
                reason
                notes
            }
        }
    }
    ${EXCEPTION_BASIC_INFO}
    ${RETRY_ATTEMPT_INFO}
`;

export const GET_EXCEPTION_SUMMARY = gql`
    query GetExceptionSummary($timeRange: TimeRange!, $filters: ExceptionFilters) {
        exceptionSummary(timeRange: $timeRange, filters: $filters) {
            ...ExceptionSummaryInfo
            trends {
                timestamp
                count
                interfaceType
            }
        }
    }
    ${EXCEPTION_SUMMARY_INFO}
`;

export const SEARCH_EXCEPTIONS = gql`
    query SearchExceptions($search: SearchInput!, $pagination: PaginationInput) {
        searchExceptions(search: $search, pagination: $pagination) {
            edges {
                node {
                    ...ExceptionBasicInfo
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
    ${EXCEPTION_BASIC_INFO}
`;
```

### Mutation Definitions

```javascript
// mutations.js
import { gql } from '@apollo/client';
import { EXCEPTION_BASIC_INFO, RETRY_ATTEMPT_INFO } from './fragments';

export const RETRY_EXCEPTION = gql`
    mutation RetryException($input: RetryExceptionInput!) {
        retryException(input: $input) {
            success
            exception {
                ...ExceptionBasicInfo
            }
            retryAttempt {
                ...RetryAttemptInfo
            }
            errors {
                message
                code
                path
                extensions
            }
        }
    }
    ${EXCEPTION_BASIC_INFO}
    ${RETRY_ATTEMPT_INFO}
`;

export const ACKNOWLEDGE_EXCEPTION = gql`
    mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
        acknowledgeException(input: $input) {
            success
            exception {
                ...ExceptionBasicInfo
            }
            errors {
                message
                code
                path
            }
        }
    }
    ${EXCEPTION_BASIC_INFO}
`;

export const BULK_RETRY_EXCEPTIONS = gql`
    mutation BulkRetryExceptions($input: BulkRetryInput!) {
        bulkRetryExceptions(input: $input) {
            successCount
            failureCount
            results {
                success
                exception {
                    id
                    transactionId
                    status
                    retryCount
                }
                errors {
                    message
                    code
                }
            }
            errors {
                message
                code
            }
        }
    }
`;

export const RESOLVE_EXCEPTION = gql`
    mutation ResolveException(
        $transactionId: String!
        $resolutionMethod: ResolutionMethod!
        $notes: String
    ) {
        resolveException(
            transactionId: $transactionId
            resolutionMethod: $resolutionMethod
            notes: $notes
        ) {
            success
            exception {
                ...ExceptionBasicInfo
            }
            errors {
                message
                code
            }
        }
    }
    ${EXCEPTION_BASIC_INFO}
`;
```

### Subscription Definitions

```javascript
// subscriptions.js
import { gql } from '@apollo/client';
import { EXCEPTION_BASIC_INFO, RETRY_ATTEMPT_INFO } from './fragments';

export const EXCEPTION_UPDATES = gql`
    subscription ExceptionUpdates($filters: SubscriptionFilters) {
        exceptionUpdated(filters: $filters) {
            eventType
            exception {
                ...ExceptionBasicInfo
            }
            timestamp
            triggeredBy
        }
    }
    ${EXCEPTION_BASIC_INFO}
`;

export const SUMMARY_UPDATES = gql`
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
`;

export const RETRY_STATUS_UPDATES = gql`
    subscription RetryStatusUpdates($transactionId: String) {
        retryStatusUpdated(transactionId: $transactionId) {
            transactionId
            retryAttempt {
                ...RetryAttemptInfo
            }
            eventType
            timestamp
        }
    }
    ${RETRY_ATTEMPT_INFO}
`;
```

## React Component Examples

### Exception List Component

```javascript
// components/ExceptionList.js
import React, { useState, useCallback } from 'react';
import { useQuery } from '@apollo/client';
import { GET_EXCEPTIONS } from '../queries';
import ExceptionCard from './ExceptionCard';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';

const ExceptionList = ({ filters, onExceptionSelect }) => {
    const [sortConfig, setSortConfig] = useState({
        field: 'timestamp',
        direction: 'DESC'
    });

    const { loading, error, data, fetchMore, refetch } = useQuery(GET_EXCEPTIONS, {
        variables: {
            filters,
            pagination: { first: 20 },
            sorting: sortConfig
        },
        notifyOnNetworkStatusChange: true,
        errorPolicy: 'all'
    });

    const loadMore = useCallback(() => {
        if (data?.exceptions.pageInfo.hasNextPage) {
            fetchMore({
                variables: {
                    pagination: {
                        first: 20,
                        after: data.exceptions.pageInfo.endCursor
                    }
                }
            });
        }
    }, [data, fetchMore]);

    const handleSort = useCallback((field) => {
        setSortConfig(prev => ({
            field,
            direction: prev.field === field && prev.direction === 'ASC' ? 'DESC' : 'ASC'
        }));
    }, []);

    if (loading && !data) return <LoadingSpinner />;
    if (error && !data) return <ErrorMessage error={error} onRetry={refetch} />;

    const exceptions = data?.exceptions.edges || [];
    const totalCount = data?.exceptions.totalCount || 0;

    return (
        <div className="exception-list">
            <div className="list-header">
                <h2>Exceptions ({totalCount})</h2>
                <div className="sort-controls">
                    <button onClick={() => handleSort('timestamp')}>
                        Sort by Time {sortConfig.field === 'timestamp' && (
                            sortConfig.direction === 'ASC' ? '↑' : '↓'
                        )}
                    </button>
                    <button onClick={() => handleSort('severity')}>
                        Sort by Severity {sortConfig.field === 'severity' && (
                            sortConfig.direction === 'ASC' ? '↑' : '↓'
                        )}
                    </button>
                </div>
            </div>

            <div className="exception-cards">
                {exceptions.map(({ node }) => (
                    <ExceptionCard
                        key={node.id}
                        exception={node}
                        onClick={() => onExceptionSelect(node)}
                    />
                ))}
            </div>

            {data?.exceptions.pageInfo.hasNextPage && (
                <button 
                    className="load-more-button"
                    onClick={loadMore}
                    disabled={loading}
                >
                    {loading ? 'Loading...' : 'Load More'}
                </button>
            )}
        </div>
    );
};

export default ExceptionList;
```

### Exception Detail Component

```javascript
// components/ExceptionDetail.js
import React, { useState } from 'react';
import { useQuery } from '@apollo/client';
import { GET_EXCEPTION_DETAIL } from '../queries';
import RetryButton from './RetryButton';
import AcknowledgeButton from './AcknowledgeButton';
import PayloadViewer from './PayloadViewer';
import RetryHistory from './RetryHistory';

const ExceptionDetail = ({ transactionId, onClose }) => {
    const [activeTab, setActiveTab] = useState('details');

    const { loading, error, data, refetch } = useQuery(GET_EXCEPTION_DETAIL, {
        variables: { transactionId },
        errorPolicy: 'all'
    });

    if (loading) return <div className="loading">Loading exception details...</div>;
    if (error) return <div className="error">Error loading exception: {error.message}</div>;
    if (!data?.exception) return <div className="not-found">Exception not found</div>;

    const exception = data.exception;

    return (
        <div className="exception-detail">
            <div className="detail-header">
                <h2>Exception Details</h2>
                <button className="close-button" onClick={onClose}>×</button>
            </div>

            <div className="exception-info">
                <div className="info-grid">
                    <div className="info-item">
                        <label>Transaction ID:</label>
                        <span>{exception.transactionId}</span>
                    </div>
                    <div className="info-item">
                        <label>Interface Type:</label>
                        <span>{exception.interfaceType}</span>
                    </div>
                    <div className="info-item">
                        <label>Status:</label>
                        <span className={`status ${exception.status.toLowerCase()}`}>
                            {exception.status}
                        </span>
                    </div>
                    <div className="info-item">
                        <label>Severity:</label>
                        <span className={`severity ${exception.severity.toLowerCase()}`}>
                            {exception.severity}
                        </span>
                    </div>
                    <div className="info-item">
                        <label>Timestamp:</label>
                        <span>{new Date(exception.timestamp).toLocaleString()}</span>
                    </div>
                    <div className="info-item">
                        <label>Retry Count:</label>
                        <span>{exception.retryCount} / {exception.maxRetries}</span>
                    </div>
                </div>

                <div className="exception-reason">
                    <label>Exception Reason:</label>
                    <p>{exception.exceptionReason}</p>
                </div>
            </div>

            <div className="action-buttons">
                {exception.retryable && exception.status !== 'RESOLVED' && (
                    <RetryButton 
                        transactionId={transactionId}
                        onSuccess={refetch}
                    />
                )}
                {exception.status === 'NEW' && (
                    <AcknowledgeButton 
                        transactionId={transactionId}
                        onSuccess={refetch}
                    />
                )}
            </div>

            <div className="detail-tabs">
                <button 
                    className={activeTab === 'details' ? 'active' : ''}
                    onClick={() => setActiveTab('details')}
                >
                    Details
                </button>
                <button 
                    className={activeTab === 'payload' ? 'active' : ''}
                    onClick={() => setActiveTab('payload')}
                >
                    Original Payload
                </button>
                <button 
                    className={activeTab === 'history' ? 'active' : ''}
                    onClick={() => setActiveTab('history')}
                >
                    Retry History
                </button>
            </div>

            <div className="tab-content">
                {activeTab === 'payload' && (
                    <PayloadViewer payload={exception.originalPayload} />
                )}
                {activeTab === 'history' && (
                    <RetryHistory history={exception.retryHistory} />
                )}
            </div>
        </div>
    );
};

export default ExceptionDetail;
```

### Retry Button Component

```javascript
// components/RetryButton.js
import React, { useState } from 'react';
import { useMutation } from '@apollo/client';
import { RETRY_EXCEPTION } from '../mutations';
import { GET_EXCEPTIONS } from '../queries';

const RetryButton = ({ transactionId, onSuccess }) => {
    const [reason, setReason] = useState('');
    const [priority, setPriority] = useState('NORMAL');
    const [showForm, setShowForm] = useState(false);

    const [retryException, { loading, error }] = useMutation(RETRY_EXCEPTION, {
        onCompleted: (data) => {
            if (data.retryException.success) {
                setShowForm(false);
                setReason('');
                onSuccess?.();
                // Show success notification
                showNotification('Retry initiated successfully', 'success');
            } else {
                // Show error from mutation result
                const errorMsg = data.retryException.errors[0]?.message || 'Retry failed';
                showNotification(errorMsg, 'error');
            }
        },
        onError: (error) => {
            console.error('Retry mutation error:', error);
            showNotification('Failed to initiate retry', 'error');
        },
        // Update cache after successful retry
        update: (cache, { data }) => {
            if (data.retryException.success) {
                // Update the exception in cache
                cache.modify({
                    id: cache.identify(data.retryException.exception),
                    fields: {
                        status: () => data.retryException.exception.status,
                        retryCount: () => data.retryException.exception.retryCount,
                        lastRetryAt: () => data.retryException.exception.lastRetryAt,
                    },
                });

                // Optionally refetch exception list
                cache.evict({ fieldName: 'exceptions' });
            }
        }
    });

    const handleRetry = () => {
        if (!reason.trim()) {
            showNotification('Please provide a reason for the retry', 'warning');
            return;
        }

        retryException({
            variables: {
                input: {
                    transactionId,
                    reason: reason.trim(),
                    priority,
                    notes: `Manual retry initiated from dashboard`
                }
            }
        });
    };

    if (!showForm) {
        return (
            <button 
                className="retry-button primary"
                onClick={() => setShowForm(true)}
            >
                Retry Exception
            </button>
        );
    }

    return (
        <div className="retry-form">
            <div className="form-group">
                <label htmlFor="retry-reason">Reason for Retry:</label>
                <textarea
                    id="retry-reason"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="Explain why this exception should be retried..."
                    rows={3}
                />
            </div>

            <div className="form-group">
                <label htmlFor="retry-priority">Priority:</label>
                <select
                    id="retry-priority"
                    value={priority}
                    onChange={(e) => setPriority(e.target.value)}
                >
                    <option value="LOW">Low</option>
                    <option value="NORMAL">Normal</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                </select>
            </div>

            {error && (
                <div className="error-message">
                    {error.message}
                </div>
            )}

            <div className="form-actions">
                <button 
                    className="retry-button primary"
                    onClick={handleRetry}
                    disabled={loading || !reason.trim()}
                >
                    {loading ? 'Retrying...' : 'Confirm Retry'}
                </button>
                <button 
                    className="cancel-button"
                    onClick={() => setShowForm(false)}
                    disabled={loading}
                >
                    Cancel
                </button>
            </div>
        </div>
    );
};

// Utility function for notifications (implement based on your notification system)
const showNotification = (message, type) => {
    // Implementation depends on your notification library
    console.log(`${type.toUpperCase()}: ${message}`);
};

export default RetryButton;
```

### Real-time Dashboard Component

```javascript
// components/RealTimeDashboard.js
import React, { useState, useEffect } from 'react';
import { useQuery, useSubscription } from '@apollo/client';
import { GET_EXCEPTION_SUMMARY } from '../queries';
import { EXCEPTION_UPDATES, SUMMARY_UPDATES } from '../subscriptions';
import SummaryCards from './SummaryCards';
import ExceptionFeed from './ExceptionFeed';

const RealTimeDashboard = () => {
    const [timeRange] = useState({ period: 'LAST_24_HOURS' });
    const [liveUpdates, setLiveUpdates] = useState([]);

    // Query for initial summary data
    const { data: summaryData, loading: summaryLoading } = useQuery(GET_EXCEPTION_SUMMARY, {
        variables: { timeRange },
        pollInterval: 60000, // Poll every minute as fallback
    });

    // Subscribe to real-time summary updates
    const { data: summaryUpdate } = useSubscription(SUMMARY_UPDATES, {
        variables: { timeRange },
        onSubscriptionData: ({ subscriptionData }) => {
            console.log('Summary updated:', subscriptionData.data);
        }
    });

    // Subscribe to exception updates
    const { data: exceptionUpdate } = useSubscription(EXCEPTION_UPDATES, {
        variables: {
            filters: {
                severities: ['HIGH', 'CRITICAL'],
                includeResolved: false
            }
        },
        onSubscriptionData: ({ subscriptionData }) => {
            const update = subscriptionData.data.exceptionUpdated;
            
            // Add to live feed
            setLiveUpdates(prev => [update, ...prev.slice(0, 49)]); // Keep last 50
            
            // Show browser notification for critical exceptions
            if (update.exception.severity === 'CRITICAL') {
                showBrowserNotification(
                    'Critical Exception',
                    `${update.exception.interfaceType}: ${update.exception.transactionId}`
                );
            }
        }
    });

    // Use subscription data if available, otherwise use query data
    const currentSummary = summaryUpdate?.summaryUpdated || summaryData?.exceptionSummary;

    return (
        <div className="real-time-dashboard">
            <div className="dashboard-header">
                <h1>Exception Dashboard</h1>
                <div className="live-indicator">
                    <span className="live-dot"></span>
                    Live Updates
                </div>
            </div>

            {summaryLoading ? (
                <div className="loading">Loading dashboard...</div>
            ) : (
                <SummaryCards summary={currentSummary} />
            )}

            <div className="dashboard-content">
                <div className="live-feed-section">
                    <h2>Live Exception Feed</h2>
                    <ExceptionFeed updates={liveUpdates} />
                </div>
            </div>
        </div>
    );
};

const showBrowserNotification = (title, body) => {
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(title, { body });
    }
};

export default RealTimeDashboard;
```

## Advanced Patterns

### Optimistic Updates

```javascript
// hooks/useOptimisticRetry.js
import { useMutation } from '@apollo/client';
import { RETRY_EXCEPTION } from '../mutations';

export const useOptimisticRetry = () => {
    return useMutation(RETRY_EXCEPTION, {
        optimisticResponse: (variables) => ({
            retryException: {
                __typename: 'RetryExceptionResult',
                success: true,
                exception: {
                    __typename: 'Exception',
                    id: `temp-${Date.now()}`,
                    transactionId: variables.input.transactionId,
                    status: 'IN_PROGRESS',
                    retryCount: 1, // Optimistically increment
                    lastRetryAt: new Date().toISOString(),
                },
                retryAttempt: {
                    __typename: 'RetryAttempt',
                    id: `temp-retry-${Date.now()}`,
                    attemptNumber: 1,
                    status: 'PENDING',
                    initiatedBy: 'current-user',
                    initiatedAt: new Date().toISOString(),
                },
                errors: []
            }
        }),
        update: (cache, { data }) => {
            // Update will be called with both optimistic and real data
            if (data.retryException.success) {
                // Update exception status in cache
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
};
```

### Custom Hooks

```javascript
// hooks/useExceptionFilters.js
import { useState, useMemo } from 'react';

export const useExceptionFilters = (initialFilters = {}) => {
    const [filters, setFilters] = useState({
        interfaceTypes: [],
        statuses: [],
        severities: [],
        dateRange: null,
        excludeResolved: true,
        ...initialFilters
    });

    const updateFilter = (key, value) => {
        setFilters(prev => ({
            ...prev,
            [key]: value
        }));
    };

    const clearFilters = () => {
        setFilters({
            interfaceTypes: [],
            statuses: [],
            severities: [],
            dateRange: null,
            excludeResolved: true
        });
    };

    const hasActiveFilters = useMemo(() => {
        return filters.interfaceTypes.length > 0 ||
               filters.statuses.length > 0 ||
               filters.severities.length > 0 ||
               filters.dateRange !== null ||
               !filters.excludeResolved;
    }, [filters]);

    return {
        filters,
        updateFilter,
        clearFilters,
        hasActiveFilters
    };
};

// hooks/useExceptionSearch.js
import { useState, useEffect } from 'react';
import { useLazyQuery } from '@apollo/client';
import { SEARCH_EXCEPTIONS } from '../queries';

export const useExceptionSearch = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');

    const [searchExceptions, { loading, data, error }] = useLazyQuery(SEARCH_EXCEPTIONS, {
        errorPolicy: 'all'
    });

    // Debounce search term
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
        }, 300);

        return () => clearTimeout(timer);
    }, [searchTerm]);

    // Execute search when debounced term changes
    useEffect(() => {
        if (debouncedSearchTerm && debouncedSearchTerm.length >= 3) {
            searchExceptions({
                variables: {
                    search: {
                        query: debouncedSearchTerm,
                        fields: ['EXCEPTION_REASON', 'TRANSACTION_ID', 'CUSTOMER_ID'],
                        fuzzy: true
                    },
                    pagination: { first: 20 }
                }
            });
        }
    }, [debouncedSearchTerm, searchExceptions]);

    return {
        searchTerm,
        setSearchTerm,
        loading,
        results: data?.searchExceptions.edges || [],
        error,
        hasResults: data?.searchExceptions.totalCount > 0
    };
};
```

This comprehensive Apollo Client integration guide provides all the necessary patterns and examples for building a robust GraphQL client application with the Interface Exception Collector API.