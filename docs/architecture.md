# Interface Exception Collector Service - Architecture Document

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architectural Patterns](#architectural-patterns)
4. [Component Architecture](#component-architecture)
5. [Data Architecture](#data-architecture)
6. [API Architecture](#api-architecture)
7. [Event-Driven Architecture](#event-driven-architecture)
8. [Security Architecture](#security-architecture)
9. [Performance and Scalability](#performance-and-scalability)
10. [Resilience and Reliability](#resilience-and-reliability)
11. [Monitoring and Observability](#monitoring-and-observability)
12. [Deployment Architecture](#deployment-architecture)
13. [Integration Patterns](#integration-patterns)
14. [Design Decisions](#design-decisions)
15. [Future Considerations](#future-considerations)

## Executive Summary

The Interface Exception Collector Service is a mission-critical microservice within the BioPro ecosystem that centralizes exception handling, monitoring, and recovery operations across all interface services (Order, Collection, Distribution, and Recruitment). The system implements a sophisticated event-driven architecture with dual API support (REST and GraphQL), comprehensive retry mechanisms, and real-time monitoring capabilities.

### Key Architectural Characteristics

- **Event-Driven**: Asynchronous processing via Apache Kafka
- **Dual API Support**: REST and GraphQL APIs sharing common infrastructure
- **Resilient**: Circuit breakers, retry patterns, and graceful degradation
- **Scalable**: Horizontal scaling with stateless design
- **Observable**: Comprehensive metrics, logging, and health monitoring
- **Secure**: JWT-based authentication with role-based access control

## System Overview

### High-Level Architecture

The Interface Exception Collector Service operates as the central nervous system for exception handling within the BioPro platform, providing:

- **Centralized Exception Collection**: Aggregates exceptions from all interface services
- **Intelligent Retry Orchestration**: Automated and manual retry capabilities
- **Real-time Monitoring**: Live dashboards and alerting for operations teams
- **Comprehensive Audit Trail**: Complete lifecycle tracking of exceptions
- **Multi-channel API Access**: REST for integrations, GraphQL for dashboards

### System Context

The service integrates with multiple BioPro interface services:
- **Order Service**: Handles order processing exceptions
- **Collection Service**: Manages collection workflow exceptions  
- **Distribution Service**: Processes distribution failures
- **Recruitment Service**: Handles recruitment process exceptions
- **Partner Order Service**: Manages external partner order exceptions

## Architectural Patterns

### 1. Event-Driven Architecture (EDA)

The system implements a comprehensive event-driven architecture pattern:

**Benefits:**
- **Loose Coupling**: Services communicate through events, reducing direct dependencies
- **Scalability**: Asynchronous processing enables independent scaling
- **Resilience**: Event persistence ensures no data loss during failures
- **Auditability**: Complete event history provides comprehensive audit trails

**Implementation:**
- Apache Kafka as the event backbone
- Event sourcing for exception lifecycle tracking
- CQRS (Command Query Responsibility Segregation) for read/write separation
- Saga pattern for distributed transaction management

### 2. Layered Architecture

The service follows a clean layered architecture:

```
┌─────────────────────────────────────┐
│           API Layer                 │
│  (REST Controllers, GraphQL         │
│   Resolvers, WebSocket Handlers)    │
├─────────────────────────────────────┤
│        Business Logic Layer        │
│  (Services, Domain Logic,           │
│   Business Rules)                   │
├─────────────────────────────────────┤
│       Data Access Layer            │
│  (Repositories, Cache Managers,     │
│   External Service Clients)        │
├─────────────────────────────────────┤
│      Infrastructure Layer          │
│  (Database, Cache, Message Queue,   │
│   External APIs)                    │
└─────────────────────────────────────┘
```

### 3. Microservices Architecture

**Service Boundaries:**
- **Single Responsibility**: Focused on exception management
- **Database per Service**: Dedicated PostgreSQL instance
- **API-First Design**: Well-defined REST and GraphQL interfaces
- **Independent Deployment**: Containerized with Kubernetes

### 4. CQRS (Command Query Responsibility Segregation)

**Command Side (Write Operations):**
- Exception processing and storage
- Retry operations
- Status updates
- Event publishing

**Query Side (Read Operations):**
- Exception retrieval and filtering
- Summary statistics
- Search operations
- Dashboard data
#
# Component Architecture

### API Layer Components

#### REST API Controllers

The REST API provides traditional HTTP-based access to exception data:

- **ExceptionController**: Exception listing, filtering, and retrieval
- **RetryController**: Retry initiation and history management
- **ManagementController**: Exception acknowledgment and resolution

#### GraphQL Resolvers

The GraphQL API enables efficient, flexible data fetching:

- **ExceptionQueryResolver**: Complex querying with DataLoaders
- **ExceptionMutationResolver**: Retry operations and status updates
- **ExceptionSubscriptionResolver**: Real-time updates and notifications

### Business Logic Layer

#### Exception Processing Service

**Responsibilities:**
- Process inbound exception events
- Apply business rules and categorization
- Manage exception lifecycle
- Trigger alerts and notifications

**Key Features:**
- Idempotent processing
- Duplicate detection
- Severity classification
- Category assignment

#### Exception Query Service

**Responsibilities:**
- Handle complex queries and filtering
- Provide pagination and sorting
- Generate summary statistics
- Support full-text search

**Optimization Features:**
- Query result caching
- Database query optimization
- Materialized view utilization
- Index-based filtering

#### Retry Service

**Responsibilities:**
- Orchestrate retry operations
- Manage retry history
- Handle external service calls
- Update exception status

**Resilience Features:**
- Circuit breaker pattern
- Exponential backoff
- Timeout handling
- Fallback mechanisms

### Event Processing Layer

#### Event Consumers

Event consumers handle inbound exception events from various interface services:

- **OrderExceptionConsumer**: Processes OrderRejected and OrderCancelled events
- **CollectionExceptionConsumer**: Handles CollectionRejected events
- **DistributionExceptionConsumer**: Processes DistributionFailed events
- **ValidationErrorConsumer**: Handles ValidationError events from all services

#### Event Publishers

Event publishers send outbound events to notify downstream systems:

- **ExceptionEventPublisher**: Publishes ExceptionCaptured and ExceptionResolved events
- **AlertPublisher**: Sends CriticalExceptionAlert events
- **RetryEventPublisher**: Publishes ExceptionRetryCompleted events

### Data Access Layer

#### Repository Pattern

The data access layer uses Spring Data JPA repositories with custom implementations for complex queries:

- **InterfaceExceptionRepository**: Main exception entity operations
- **RetryAttemptRepository**: Retry history management
- **StatusChangeRepository**: Audit trail tracking
- **OriginalPayloadRepository**: Payload caching

## Data Architecture

### Database Design

#### Core Entity Model

The database schema is designed for high performance and data integrity:

**Primary Tables:**
- `interface_exceptions`: Main exception data
- `retry_attempts`: Retry history and results
- `status_changes`: Audit trail of status changes
- `original_payloads`: Cached original request payloads

#### Performance Optimization

**Indexing Strategy:**
- Primary lookup indexes on transaction_id, interface_type, status, severity
- Time-based indexes for timestamp queries
- Composite indexes for common filter combinations
- Full-text search indexes using PostgreSQL's GIN indexes

**Query Optimization:**
- Materialized views for dashboard statistics
- Partitioning for large historical data
- Connection pooling with HikariCP
- Query result caching

### Caching Strategy

#### Multi-Level Caching

**L1 - Application Cache:**
- JVM-based caching for frequently accessed data
- Short TTL (1-5 minutes)
- Exception details and metadata

**L2 - Redis Distributed Cache:**
- Shared across service instances
- Medium TTL (5-60 minutes)
- Query results and aggregations

**L3 - Database Query Cache:**
- PostgreSQL query result cache
- Long TTL (1-24 hours)
- Materialized view results

#### Cache Key Patterns

```
exception:details:{transactionId}           # TTL: 1 hour
exception:list:{filters_hash}:{page}        # TTL: 5 minutes
exception:summary:{timeRange}:{filters}     # TTL: 15 minutes
payload:{transactionId}                     # TTL: 24 hours
retry:history:{transactionId}               # TTL: 30 minutes
search:results:{query_hash}:{page}          # TTL: 10 minutes
```

## API Architecture

### Dual API Strategy

The system implements both REST and GraphQL APIs to serve different client needs:

#### REST API Design

**Characteristics:**
- Resource-oriented design following REST principles
- HTTP-centric with proper status codes and caching headers
- Stateless operation with no server-side sessions
- Comprehensive error handling and validation

**Key Endpoints:**
```
GET    /api/v1/exceptions                    # List exceptions with filtering
GET    /api/v1/exceptions/{transactionId}    # Get detailed exception info
GET    /api/v1/exceptions/search             # Full-text search
GET    /api/v1/exceptions/summary            # Aggregated statistics
POST   /api/v1/exceptions/{id}/retry         # Initiate retry operation
PUT    /api/v1/exceptions/{id}/acknowledge   # Acknowledge exception
PUT    /api/v1/exceptions/{id}/resolve       # Mark as resolved
```

#### GraphQL API Design

**Characteristics:**
- Query-oriented with flexible data fetching
- Type-safe schema definition
- Single request for complex data requirements
- Real-time subscriptions for live updates

**Core Schema Types:**
- `Exception`: Main exception entity with nested relationships
- `ExceptionConnection`: Paginated exception results
- `ExceptionSummary`: Aggregated statistics and trends
- `RetryAttempt`: Retry history and results

### API Integration Architecture

Both APIs share common infrastructure:

- **Unified Security**: Same JWT authentication and authorization
- **Shared Services**: Common business logic layer
- **Consistent Caching**: Unified cache invalidation strategies
- **Common Monitoring**: Integrated metrics and logging

## Event-Driven Architecture

### Event Flow Architecture

The event-driven architecture enables loose coupling and scalability:

1. **Event Ingestion**: Interface services publish exception events to Kafka
2. **Event Processing**: Exception Collector consumes and processes events
3. **Data Storage**: Processed exceptions stored in PostgreSQL
4. **Event Publishing**: Downstream events published for notifications
5. **Real-time Updates**: WebSocket subscriptions for live dashboard updates

### Event Schema Design

#### Inbound Events

**OrderRejected Event:**
```json
{
  "eventId": "uuid",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-04T10:30:00Z",
  "source": "order-service",
  "correlationId": "uuid",
  "payload": {
    "transactionId": "uuid",
    "externalId": "ORDER-ABC123",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Order already exists",
    "customerId": "CUST001",
    "locationCode": "LOC001"
  }
}
```

#### Outbound Events

**ExceptionCaptured Event:**
```json
{
  "eventId": "uuid",
  "eventType": "ExceptionCaptured",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-04T10:30:05Z",
  "source": "exception-collector-service",
  "correlationId": "uuid",
  "payload": {
    "exceptionId": 12345,
    "transactionId": "uuid",
    "interfaceType": "ORDER",
    "severity": "MEDIUM",
    "category": "BUSINESS_RULE",
    "exceptionReason": "Order already exists",
    "customerId": "CUST001",
    "retryable": true
  }
}
```

### Event Processing Patterns

#### Consumer Group Strategy

```yaml
Consumer Groups:
  exception-collector-main:
    topics: [OrderRejected, CollectionRejected, DistributionFailed, ValidationError]
    partitions: 3
    instances: 3
    processing: parallel
    
  exception-collector-alerts:
    topics: [CriticalExceptionAlert]
    partitions: 1
    instances: 2
    processing: sequential
```

#### Dead Letter Queue Handling

Failed message processing is handled through dead letter queues with manual review and reprocessing capabilities.

## Security Architecture

### Authentication and Authorization

#### JWT-Based Authentication

The system uses JWT tokens for stateless authentication:

- **Token Validation**: Signature verification and expiration checking
- **Claims Extraction**: User identity and role information
- **Security Context**: Request-scoped security context setup

#### Role-Based Access Control

```yaml
Security Roles:
  ADMIN:
    permissions:
      - Full system access
      - Configuration management
      - All operations
      
  OPERATIONS:
    permissions:
      - Exception management
      - Retry operations
      - Acknowledgment and resolution
      
  VIEWER:
    permissions:
      - Read-only access
      - Dashboard viewing
      - Summary statistics
```

### Data Protection

#### Encryption Strategy

- **At Rest**: AES-256 encryption for sensitive database columns
- **In Transit**: TLS 1.3 for all HTTP communications
- **PII Masking**: Sensitive data masking in logs and responses

#### Security Monitoring

- **Access Logging**: Comprehensive audit trail of all operations
- **Threat Detection**: Monitoring for suspicious access patterns
- **Compliance**: GDPR and healthcare data protection compliance

## Performance and Scalability

### Scalability Architecture

#### Horizontal Scaling Strategy

The system is designed for horizontal scaling across multiple dimensions:

**API Layer Scaling:**
- Multiple service instances behind load balancer
- Stateless design enables unlimited horizontal scaling
- Auto-scaling based on CPU and memory metrics

**Event Processing Scaling:**
- Kafka consumer groups with multiple instances
- Partition-based parallel processing
- Dynamic consumer scaling based on lag

**Database Scaling:**
- Read replicas for query operations
- Connection pooling optimization
- Query performance tuning

**Cache Layer Scaling:**
- Redis cluster for high availability
- Distributed caching across nodes
- Cache partitioning by data type

### Performance Optimization Techniques

#### Database Optimizations

- **Indexing**: Comprehensive indexing strategy for common queries
- **Partitioning**: Table partitioning for large historical data
- **Query Optimization**: Optimized queries with proper execution plans
- **Connection Pooling**: Efficient database connection management

#### Caching Strategies

- **Multi-level Caching**: Application, distributed, and database caching
- **Cache Warming**: Proactive cache population for common queries
- **Cache Invalidation**: Intelligent cache invalidation on data updates
- **Cache Monitoring**: Performance metrics and hit rate monitoring

## Resilience and Reliability

### Fault Tolerance Patterns

#### Circuit Breaker Pattern

External service calls are protected with circuit breakers:

- **Failure Detection**: Automatic detection of service failures
- **Circuit Opening**: Temporary blocking of calls to failed services
- **Fallback Behavior**: Graceful degradation with cached or default responses
- **Recovery Testing**: Automatic testing for service recovery

#### Retry Mechanisms

Comprehensive retry strategies for different failure scenarios:

**Database Operations:**
- Exponential backoff with jitter
- Maximum retry attempts: 5
- Timeout handling and connection recovery

**External Service Calls:**
- Configurable retry policies
- Circuit breaker integration
- Timeout and fallback handling

**Kafka Message Processing:**
- Dead letter queue for failed messages
- Retry with exponential backoff
- Manual reprocessing capabilities

### Disaster Recovery

#### Backup Strategy

- **Database Backups**: Daily full backups with hourly incrementals
- **Event Store Backup**: Kafka topic backup for event replay
- **Configuration Backup**: Version-controlled configuration management

#### Recovery Procedures

- **RTO (Recovery Time Objective)**: 15 minutes for database, 5 minutes for service
- **RPO (Recovery Point Objective)**: 1 hour for database, 5 minutes for events
- **Automated Recovery**: Automated failover and recovery procedures

## Monitoring and Observability

### Metrics Architecture

#### Application Metrics

Comprehensive metrics collection for system monitoring:

- **Exception Processing**: Volume, rates, and processing times
- **API Performance**: Response times, throughput, and error rates
- **Database Performance**: Query times, connection pool utilization
- **Cache Performance**: Hit rates, eviction rates, and memory usage

#### Business Metrics

Key business indicators for operational insights:

- **Exception Volume**: Trends by interface type and severity
- **Resolution Metrics**: Average resolution time and success rates
- **Customer Impact**: Affected customers and SLA compliance
- **System Health**: Overall system performance indicators

### Logging Strategy

#### Structured Logging

- **JSON Format**: Structured logs for easy parsing and analysis
- **Correlation IDs**: Request tracing across service boundaries
- **PII Masking**: Automatic masking of sensitive information
- **Log Levels**: Configurable log levels by environment

#### Log Aggregation

- **Centralized Logging**: ELK stack for log aggregation and analysis
- **Log Retention**: Configurable retention policies by log type
- **Search and Analysis**: Full-text search and log correlation capabilities

### Health Monitoring

#### Health Check Implementation

Comprehensive health checks for all system components:

- **Database Connectivity**: Connection and query performance checks
- **Cache Connectivity**: Redis connection and operation checks
- **Kafka Connectivity**: Producer and consumer health verification
- **External Services**: Dependency health monitoring

#### Alerting Strategy

- **Proactive Alerting**: Early warning for potential issues
- **Escalation Procedures**: Automated escalation for critical alerts
- **Alert Correlation**: Intelligent alert grouping and correlation
- **Notification Channels**: Multiple notification channels for different alert types

## Deployment Architecture

### Containerization Strategy

#### Docker Configuration

The application is containerized using Docker with security best practices:

- **Base Image**: OpenJDK 21 slim for optimal performance
- **Security**: Non-root user execution
- **Health Checks**: Built-in container health monitoring
- **Resource Limits**: CPU and memory constraints

#### Container Security

- **Image Scanning**: Automated vulnerability scanning
- **Minimal Attack Surface**: Distroless base images where possible
- **Secret Management**: External secret injection
- **Network Security**: Container network isolation

### Kubernetes Deployment

#### Deployment Configuration

Kubernetes deployment with high availability and scalability:

- **Replica Sets**: Multiple instances for high availability
- **Rolling Updates**: Zero-downtime deployments
- **Resource Management**: CPU and memory requests/limits
- **Health Probes**: Liveness and readiness probes

#### Service Configuration

- **Load Balancing**: Automatic load balancing across instances
- **Service Discovery**: Kubernetes-native service discovery
- **Network Policies**: Network security and isolation
- **Ingress Configuration**: External traffic routing and SSL termination

### Configuration Management

#### Environment-Specific Configuration

- **Base Configuration**: Common settings across environments
- **Environment Overrides**: Environment-specific customizations
- **Secret Management**: Kubernetes secrets for sensitive data
- **Feature Flags**: Runtime feature toggling capabilities

## Integration Patterns

### External Service Integration

#### Service Discovery

Dynamic service discovery for external dependencies:

- **Service Registry**: Kubernetes service discovery
- **Load Balancing**: Client-side load balancing
- **Health Checking**: Continuous health monitoring
- **Circuit Breakers**: Fault tolerance for external calls

#### API Gateway Integration

Integration with API gateway for external access:

- **Route Configuration**: Path-based routing to service endpoints
- **Rate Limiting**: Request rate limiting and throttling
- **Authentication**: Centralized authentication and authorization
- **CORS Configuration**: Cross-origin resource sharing setup

### Event Integration Patterns

#### Saga Pattern for Distributed Transactions

Complex retry operations use the saga pattern:

- **Orchestration**: Centralized saga orchestration
- **Compensation**: Automatic compensation for failed operations
- **State Management**: Persistent saga state tracking
- **Error Handling**: Comprehensive error handling and recovery

#### Event Sourcing Implementation

Event sourcing for complete audit trails:

- **Event Store**: Persistent event storage
- **Event Replay**: Ability to replay events for debugging
- **Projections**: Read model projections from events
- **Snapshots**: Performance optimization with snapshots

## Design Decisions

### Key Architectural Decisions

#### 1. Event-Driven Architecture Choice

**Decision**: Implement comprehensive event-driven architecture with Apache Kafka

**Rationale**:
- **Scalability**: Asynchronous processing enables independent scaling
- **Resilience**: Event persistence ensures no data loss during failures
- **Decoupling**: Services communicate through events, reducing dependencies
- **Auditability**: Complete event history provides audit trails

**Trade-offs**:
- **Complexity**: Increased system complexity with eventual consistency
- **Debugging**: Distributed event flows can be harder to debug
- **Latency**: Asynchronous processing may introduce latency

#### 2. Dual API Strategy (REST + GraphQL)

**Decision**: Implement both REST and GraphQL APIs sharing common infrastructure

**Rationale**:
- **Client Flexibility**: Different clients have different data access patterns
- **Backward Compatibility**: Existing REST clients continue to work
- **Performance**: GraphQL enables efficient data fetching for dashboards
- **Developer Experience**: GraphQL provides better developer tooling

**Trade-offs**:
- **Maintenance Overhead**: Two APIs to maintain and test
- **Complexity**: Unified security and caching across different paradigms
- **Learning Curve**: Team needs expertise in both REST and GraphQL

#### 3. CQRS Pattern Implementation

**Decision**: Separate read and write operations with different optimization strategies

**Rationale**:
- **Performance**: Optimized read models for complex queries
- **Scalability**: Independent scaling of read and write operations
- **Flexibility**: Different data models for different use cases
- **Caching**: Efficient caching strategies for read operations

**Trade-offs**:
- **Consistency**: Eventual consistency between read and write models
- **Complexity**: Additional infrastructure for maintaining read models
- **Synchronization**: Need to keep read models in sync with write models

### Technology Choices

#### 1. Spring Boot Framework

**Decision**: Use Spring Boot as the primary application framework

**Rationale**:
- **Ecosystem**: Rich ecosystem with excellent integration capabilities
- **Productivity**: Convention over configuration reduces boilerplate
- **Community**: Large community and extensive documentation
- **Enterprise Features**: Built-in security, monitoring, and configuration

#### 2. PostgreSQL Database

**Decision**: Use PostgreSQL as the primary database

**Rationale**:
- **ACID Compliance**: Strong consistency guarantees for critical data
- **JSON Support**: Native JSONB support for flexible payload storage
- **Full-Text Search**: Built-in text search capabilities
- **Performance**: Excellent performance with proper indexing

#### 3. Redis Cache

**Decision**: Use Redis for distributed caching

**Rationale**:
- **Performance**: In-memory storage for fast data access
- **Scalability**: Distributed caching across multiple service instances
- **Data Structures**: Rich data structures for complex caching scenarios
- **Persistence**: Optional persistence for cache durability

#### 4. Apache Kafka

**Decision**: Use Apache Kafka for event streaming

**Rationale**:
- **Throughput**: High-throughput, low-latency event streaming
- **Durability**: Persistent event storage with configurable retention
- **Scalability**: Horizontal scaling with partitioning
- **Ecosystem**: Rich ecosystem with monitoring and management tools

## Future Considerations

### Scalability Enhancements

#### 1. Event Store Implementation

**Future Enhancement**: Implement dedicated event store for complete event sourcing

**Benefits**:
- Complete audit trail of all system changes
- Ability to replay events for debugging and recovery
- Support for temporal queries and point-in-time recovery
- Better support for distributed transactions

#### 2. Multi-Region Deployment

**Future Enhancement**: Support for multi-region deployment with data replication

**Benefits**:
- Improved disaster recovery capabilities
- Reduced latency for geographically distributed users
- Higher availability through regional redundancy
- Compliance with data residency requirements

#### 3. Advanced Analytics and ML

**Future Enhancement**: Implement machine learning for predictive analytics

**Benefits**:
- Predictive alerting for potential system issues
- Automated exception categorization and routing
- Anomaly detection for unusual exception patterns
- Intelligent retry strategies based on historical success rates

### Technology Evolution

#### 1. Cloud-Native Enhancements

**Future Considerations**:
- **Service Mesh**: Implement Istio or Linkerd for advanced traffic management
- **Serverless**: Consider serverless functions for event processing
- **Container Orchestration**: Advanced Kubernetes features like operators
- **Cloud Services**: Leverage managed services for databases and messaging

#### 2. Observability Improvements

**Future Enhancements**:
- **Distributed Tracing**: Implement OpenTelemetry for end-to-end tracing
- **Advanced Monitoring**: Custom dashboards and alerting rules
- **Log Analytics**: Advanced log analysis and correlation
- **Performance Profiling**: Continuous performance monitoring and optimization

#### 3. Security Enhancements

**Future Considerations**:
- **Zero Trust Architecture**: Implement comprehensive zero trust security model
- **Advanced Encryption**: Field-level encryption for sensitive data
- **Audit Compliance**: Enhanced audit logging for regulatory compliance
- **Threat Detection**: Automated threat detection and response

---

This architecture document provides a comprehensive overview of the Interface Exception Collector Service's design, implementation patterns, and future considerations. The architecture is designed to be scalable, resilient, and maintainable while providing excellent performance and developer experience.

The system successfully addresses all requirements from the specification documents:
- Centralized exception collection from all BioPro interface services
- Dual API support (REST and GraphQL) with shared infrastructure
- Comprehensive retry and recovery mechanisms
- Real-time monitoring and alerting capabilities
- Robust security and data protection measures
- High availability and disaster recovery capabilities
- Comprehensive observability and monitoring features