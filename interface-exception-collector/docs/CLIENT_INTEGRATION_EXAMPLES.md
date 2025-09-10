# Client Integration Examples

## Overview

This document provides comprehensive examples for integrating with the Exception Management Mutations API using various GraphQL clients and programming languages.

## Table of Contents

- [Apollo GraphQL Client (React/TypeScript)](#apollo-graphql-client-reacttypescript)
- [Java Spring GraphQL Client](#java-spring-graphql-client)
- [Python GraphQL Client](#python-graphql-client)
- [cURL Examples](#curl-examples)
- [Error Handling Best Practices](#error-handling-best-practices)

## Apollo GraphQL Client (React/TypeScript)

### Setup and Configuration

```typescript
// apollo-client.ts
import { ApolloClient, InMemoryCache, createHttpLink, from } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { onError } from '@apollo/client/link/error';
import { RetryLink } from '@apollo/client/link/retry';

const httpLink = createHttpLink({
  uri: process.env.REACT_APP_GRAPHQL_ENDPOINT || 'https://api.biopro.com/graphql',
});

const authLink = setContext((_, { headers }) => {
  const token = localStorage.getItem('jwt-token');
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    }
  }
});

const errorLink = onError(({ graphQLErrors, networkError }) => {
  if (graphQLErrors) {
    graphQLErrors.forEach(({ message, extensions }) => {
      if (extensions?.code === 'RATE_LIMIT_EXCEEDED') {
        toast.error('Rate limit exceeded. Please try again later.');
      }
    });
  }
  if (networkError) {
    toast.error('Network error. Please check your connection.');
  }
});

export const apolloClient = new ApolloClient({
  link: from([authLink, errorLink, httpLink]),
  cache: new InMemoryCache(),
});
```

### GraphQL Operations

```typescript
// mutations.ts
import { gql } from '@apollo/client';

export const RETRY_EXCEPTION = gql`
  mutation RetryException($input: RetryExceptionInput!) {
    retryException(input: $input) {
      success
      message
      retryId
      estimatedCompletionTime
      exception {
        transactionId
        status
        retryCount
      }
      errors {
        message
        extensions {
          code
        }
      }
    }
  }
`;

export const ACKNOWLEDGE_EXCEPTION = gql`
  mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
    acknowledgeException(input: $input) {
      success
      message
      exception {
        transactionId
        status
        acknowledgedBy
        acknowledgedAt
      }
      errors {
        message
        extensions {
          code
        }
      }
    }
  }
`;
```

### React Hooks

```typescript
// hooks/useExceptionMutations.ts
import { useMutation } from '@apollo/client';
import { toast } from 'react-toastify';
import { RETRY_EXCEPTION, ACKNOWLEDGE_EXCEPTION } from '../mutations';

export const useRetryException = () => {
  const [retryException, { loading, error, data }] = useMutation(RETRY_EXCEPTION, {
    onCompleted: (data) => {
      if (data.retryException.success) {
        toast.success(`Retry initiated: ${data.retryException.retryId}`);
      } else {
        data.retryException.errors.forEach((err: any) => {
          toast.error(err.message);
        });
      }
    },
    onError: (error) => {
      toast.error('Failed to retry exception');
    }
  });

  const executeRetry = async (input: RetryExceptionInput) => {
    try {
      await retryException({ variables: { input } });
    } catch (error) {
      console.error('Execute retry error:', error);
    }
  };

  return { executeRetry, loading, error, data };
};
```

## Java Spring GraphQL Client

### Configuration

```java
// GraphQLClientConfig.java
@Configuration
public class GraphQLClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("https://api.biopro.com/graphql")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    public HttpGraphQlClient graphQlClient(WebClient webClient) {
        return HttpGraphQlClient.builder(webClient)
            .interceptor((request, next) -> {
                String token = getCurrentUserToken();
                if (token != null) {
                    request.getHeaders().setBearerAuth(token);
                }
                return next.execute(request);
            })
            .build();
    }
}
```

### Service Implementation

```java
// ExceptionManagementGraphQLService.java
@Service
@Slf4j
public class ExceptionManagementGraphQLService {

    private final HttpGraphQlClient graphQlClient;

    public CompletableFuture<RetryExceptionResult> retryException(RetryExceptionInput input) {
        String mutation = """
            mutation RetryException($input: RetryExceptionInput!) {
                retryException(input: $input) {
                    success
                    message
                    retryId
                    exception {
                        transactionId
                        status
                    }
                    errors {
                        message
                        extensions {
                            code
                        }
                    }
                }
            }
            """;

        return graphQlClient.document(mutation)
            .variable("input", input)
            .execute()
            .map(response -> response.field("retryException").toEntity(RetryExceptionResult.class))
            .toFuture();
    }
}
```

## Python GraphQL Client

```python
# graphql_client.py
import asyncio
from gql import gql, Client
from gql.transport.aiohttp import AIOHTTPTransport

class ExceptionManagementClient:
    def __init__(self, endpoint: str, auth_token: str):
        self.transport = AIOHTTPTransport(
            url=endpoint,
            headers={"Authorization": f"Bearer {auth_token}"}
        )
        self.client = Client(transport=self.transport)

    async def retry_exception(self, transaction_id: str, reason: str, priority: str = "NORMAL"):
        mutation = gql("""
            mutation RetryException($input: RetryExceptionInput!) {
                retryException(input: $input) {
                    success
                    message
                    retryId
                    errors {
                        message
                        extensions {
                            code
                        }
                    }
                }
            }
        """)

        variables = {
            "input": {
                "transactionId": transaction_id,
                "reason": reason,
                "priority": priority
            }
        }

        result = await self.client.execute_async(mutation, variable_values=variables)
        return result["retryException"]
```

## cURL Examples

### Retry Exception

```bash
curl -X POST https://api.biopro.com/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "query": "mutation RetryException($input: RetryExceptionInput!) { retryException(input: $input) { success message retryId errors { message extensions { code } } } }",
    "variables": {
      "input": {
        "transactionId": "TXN-2024-001234",
        "reason": "Network issue resolved",
        "priority": "HIGH"
      }
    }
  }'
```

### Acknowledge Exception

```bash
curl -X POST https://api.biopro.com/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "query": "mutation AcknowledgeException($input: AcknowledgeExceptionInput!) { acknowledgeException(input: $input) { success message errors { message } } }",
    "variables": {
      "input": {
        "transactionId": "TXN-2024-001234",
        "notes": "Investigating issue"
      }
    }
  }'
```

## Error Handling Best Practices

### Retry Logic Implementation

```typescript
export const withRetry = async <T>(
  operation: () => Promise<T>,
  maxRetries: number = 3
): Promise<T> => {
  let lastError: Error;
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error) {
      lastError = error as Error;
      
      if (error.message.includes('AUTHORIZATION_ERROR')) {
        throw error;
      }
      
      if (attempt < maxRetries) {
        const delay = 1000 * Math.pow(2, attempt);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  }
  
  throw lastError;
};
```

---

*Last Updated: January 2024*