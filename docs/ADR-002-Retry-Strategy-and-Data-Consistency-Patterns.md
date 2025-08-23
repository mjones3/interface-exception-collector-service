# ADR-002: Retry Strategy and Data Consistency Patterns

## Status
**Accepted** - 2025-08-21

## Context

The Interface Exception Collector Service operates in a distributed environment where multiple interface services (Order, Collection, Distribution, Recruitment, Partner Order) can experience various types of failures. The system must handle these failures gracefully while maintaining data consistency and providing reliable retry mechanisms for transient failures.

### Current Challenges

#### 1. **Distributed System Failures**
- **Network Partitions**: Temporary network connectivity issues between services
- **Service Unavailability**: Interface services may be temporarily down for maintenance or due to failures
- **Database Connection Issues**: Temporary database connectivity problems
- **Kafka Broker Failures**: Message broker unavailability affecting event processing
- **External Service Timeouts**: Third-party service calls exceeding timeout thresholds

#### 2. **Data Consistency Requirements**
- **Exception Lifecycle Integrity**: Exception status transitions must be consistent and auditable
- **Retry History Accuracy**: Complete and accurate tracking of all retry attempts
- **Event Ordering**: Maintaining proper event sequence for exception lifecycle events
- **Duplicate Prevention**: Avoiding duplicate exception records and retry attempts
- **Cross-Service Consistency**: Ensuring consistency between Exception Collector and source services

#### 3. **Retry Complexity**
- **Transient vs Permanent Failures**: Distinguishing between retryable and non-retryable failures
- **Exponential Backoff**: Implementing intelligent retry delays to avoid overwhelming failing services
- **Circuit Breaking**: Protecting against cascading failures in distributed systems
- **Retry Limits**: Preventing infinite retry loops while maximizing recovery chances
- **Payload Retrieval**: Reliably retrieving original payloads for retry operations

#### 4. **Performance and Scalability**
- **High Throughput**: Processing thousands of exceptions per minute during peak loads
- **Low Latency**: Maintaining sub-100ms processing times for exception events
- **Resource Efficiency**: Minimizing resource consumption during retry operations
- **Horizontal Scaling**: Supporting multiple service instances with consistent behavior

### Business Requirements

- **99.9% Exception Processing Reliability**: No exception events should be lost
- **Automated Recovery**: 80% of transient failures should be resolved automatically
- **Audit Compliance**: Complete audit trail of all retry attempts and outcomes
- **Performance SLA**: 95th percentile processing time under 100ms
- **Data Integrity**: Zero tolerance for data corruption or inconsistency
- **Operational Visibility**: Real-time monitoring of retry success rates and failure patterns

## Decision

We will implement a **Multi-Layered Retry Strategy with Eventual Consistency Patterns** that provides comprehensive failure handling while maintaining data integrity and system performance.

### Core Strategy Components

#### 1. **Hierarchical Retry Architecture**

**Layer 1: Application-Level Retries**
```yaml
Kafka Consumer Retries:
  immediate_retries: 3
  retry_interval: 1s
  backoff_strategy: fixed
  
Database Operation Retries:
  max_attempts: 5
  initial_delay: 1s
  max_delay: 30s
  backoff_strategy: exponential
  jitter: true
  
External Service Retries:
  max_attempts: 3
  initial_delay: 500ms
  max_delay: 5s
  backoff_strategy: exponential
  circuit_breaker: enabled
```

**Layer 2: Infrastructure-Level Retries**
```yaml
Kafka Producer Retries:
  retries: 3
  retry_backoff_ms: 1000
  delivery_timeout_ms: 30000
  
Database Connection Pool:
  connection_timeout: 30s
  validation_timeout: 5s
  leak_detection_threshold: 60s
  
HTTP Client Retries:
  connect_timeout: 5s
  read_timeout: 30s
  max_retries: 2
```

**Layer 3: Dead Letter Queue Processing**
```yaml
Dead Letter Queue:
  retention_period: 7_days
  max_delivery_attempts: 3
  reprocessing_schedule: "0 */15 * * * *"  # Every 15 minutes
  manual_review_threshold: 24_hours
```

#### 2. **Circuit Breaker Pattern Implementation**

**Service-Specific Circuit Breakers**:
```java
@Component
public class ExternalServiceClients {
    
    @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackOrderService")
    @TimeLimiter(name = "order-service")
    @Retry(name = "order-service")
    public CompletableFuture<PayloadResponse> getOrderPayload(String transactionId) {
        return orderServiceClient.getPayload(transactionId);
    }
    
    @CircuitBreaker(name = "collection-service", fallbackMethod = "fallbackCollectionService")
    @TimeLimiter(name = "collection-service")
    @Retry(name = "collection-service")
    public CompletableFuture<PayloadResponse> getCollectionPayload(String transactionId) {
        return collectionServiceClient.getPayload(transactionId);
    }
    
    // Fallback methods provide graceful degradation
    public CompletableFuture<PayloadResponse> fallbackOrderService(String transactionId, Exception ex) {
        return getCachedPayload(transactionId, "ORDER")
            .orElse(CompletableFuture.completedFuture(null));
    }
}
```

**Circuit Breaker Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      order-service:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 5s
        permitted-number-of-calls-in-half-open-state: 3
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 30s
        
      collection-service:
        failure-rate-threshold: 60
        slow-call-rate-threshold: 60
        slow-call-duration-threshold: 10s
        permitted-number-of-calls-in-half-open-state: 2
        sliding-window-size: 8
        minimum-number-of-calls: 4
        wait-duration-in-open-state: 45s
```

#### 3. **Idempotency and Duplicate Prevention**

**Transaction-Based Idempotency**:
```java
@Service
@Transactional
public class ExceptionProcessingService {
    
    @Retryable(value = {TransientDataAccessException.class}, 
               maxAttempts = 5, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public void processExceptionEvent(ExceptionEvent event) {
        // Idempotent processing using transaction ID
        Optional<InterfaceException> existing = 
            exceptionRepository.findByTransactionId(event.getTransactionId());
            
        if (existing.isPresent()) {
            // Update existing record (idempotent)
            updateExistingException(existing.get(), event);
        } else {
            // Create new exception record
            createNewException(event);
        }
        
        // Publish downstream events (with deduplication)
        publishExceptionCapturedEvent(event);
    }
    
    private void publishExceptionCapturedEvent(ExceptionEvent event) {
        // Use deterministic event ID for deduplication
        String eventId = generateDeterministicEventId(event.getTransactionId(), "ExceptionCaptured");
        
        if (!eventRepository.existsByEventId(eventId)) {
            ExceptionCapturedEvent capturedEvent = createCapturedEvent(event, eventId);
            eventPublisher.publish(capturedEvent);
            eventRepository.save(createEventRecord(capturedEvent));
        }
    }
}
```

**Retry Attempt Deduplication**:
```java
@Service
public class RetryService {
    
    @Transactional
    public RetryResult initiateRetry(String transactionId, RetryRequest request) {
        InterfaceException exception = getExceptionOrThrow(transactionId);
        
        // Check for concurrent retry attempts
        Optional<RetryAttempt> pendingRetry = retryAttemptRepository
            .findByExceptionIdAndStatus(exception.getId(), RetryStatus.PENDING);
            
        if (pendingRetry.isPresent()) {
            throw new ConcurrentRetryException("Retry already in progress");
        }
        
        // Create retry attempt record (atomic)
        RetryAttempt attempt = createRetryAttempt(exception, request);
        retryAttemptRepository.save(attempt);
        
        // Execute retry asynchronously
        CompletableFuture.runAsync(() -> executeRetry(attempt));
        
        return RetryResult.accepted(attempt.getId());
    }
}
```

#### 4. **Event Ordering and Consistency**

**Kafka Partition Strategy**:
```yaml
Topic Partitioning:
  OrderRejected:
    partitions: 3
    partition_key: transaction_id  # Ensures ordering per transaction
    
  CollectionRejected:
    partitions: 3
    partition_key: transaction_id
    
  DistributionFailed:
    partitions: 3
    partition_key: transaction_id
    
  ExceptionCaptured:
    partitions: 6
    partition_key: transaction_id
    
  ExceptionRetryCompleted:
    partitions: 3
    partition_key: transaction_id
```

**Event Sequence Management**:
```java
@Component
public class EventSequenceManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isValidEventSequence(String transactionId, String eventType, long eventTimestamp) {
        String sequenceKey = "event_sequence:" + transactionId;
        String lastEventInfo = redisTemplate.opsForValue().get(sequenceKey);
        
        if (lastEventInfo == null) {
            // First event for this transaction
            updateEventSequence(sequenceKey, eventType, eventTimestamp);
            return true;
        }
        
        EventInfo lastEvent = parseEventInfo(lastEventInfo);
        
        // Validate event ordering rules
        if (isValidTransition(lastEvent.getType(), eventType) && 
            eventTimestamp >= lastEvent.getTimestamp()) {
            updateEventSequence(sequenceKey, eventType, eventTimestamp);
            return true;
        }
        
        log.warn("Invalid event sequence detected: transaction={}, lastEvent={}, currentEvent={}", 
                transactionId, lastEvent, eventType);
        return false;
    }
    
    private boolean isValidTransition(String fromEvent, String toEvent) {
        // Define valid state transitions
        Map<String, Set<String>> validTransitions = Map.of(
            "OrderRejected", Set.of("ExceptionCaptured", "ExceptionRetryCompleted"),
            "ExceptionCaptured", Set.of("ExceptionRetryCompleted", "ExceptionResolved"),
            "ExceptionRetryCompleted", Set.of("ExceptionResolved", "ExceptionRetryCompleted")
        );
        
        return validTransitions.getOrDefault(fromEvent, Set.of()).contains(toEvent);
    }
}
```

#### 5. **Saga Pattern for Complex Retry Operations**

**Retry Orchestration Saga**:
```java
@Component
public class RetryOrchestrationSaga {
    
    @SagaOrchestrationStart
    public void startRetryProcess(RetryExceptionCommand command) {
        log.info("Starting retry saga for transaction: {}", command.getTransactionId());
        
        // Step 1: Validate retry eligibility
        sagaManager.choreography()
            .step("validateRetry")
            .compensatedBy("revertRetryInitiation")
            .invoke(() -> validateRetryEligibility(command));
    }
    
    @SagaOrchestrationStep(step = "validateRetry")
    public void handleRetryValidated(RetryValidatedEvent event) {
        // Step 2: Retrieve original payload
        sagaManager.choreography()
            .step("retrievePayload")
            .compensatedBy("cleanupRetryAttempt")
            .invoke(() -> retrieveOriginalPayload(event.getTransactionId()));
    }
    
    @SagaOrchestrationStep(step = "retrievePayload")
    public void handlePayloadRetrieved(PayloadRetrievedEvent event) {
        // Step 3: Submit retry request
        sagaManager.choreography()
            .step("submitRetry")
            .compensatedBy("markRetryFailed")
            .invoke(() -> submitRetryRequest(event));
    }
    
    @SagaOrchestrationStep(step = "submitRetry")
    public void handleRetrySubmitted(RetrySubmittedEvent event) {
        // Step 4: Update exception status
        sagaManager.choreography()
            .step("updateStatus")
            .invoke(() -> updateExceptionStatus(event));
    }
    
    // Compensation methods for rollback
    @SagaOrchestrationCompensation(step = "validateRetry")
    public void revertRetryInitiation(RetryExceptionCommand command) {
        log.info("Reverting retry initiation for transaction: {}", command.getTransactionId());
        retryService.cancelRetryAttempt(command.getTransactionId());
    }
    
    @SagaOrchestrationCompensation(step = "retrievePayload")
    public void cleanupRetryAttempt(RetryValidatedEvent event) {
        log.info("Cleaning up retry attempt for transaction: {}", event.getTransactionId());
        retryService.markRetryAttemptFailed(event.getTransactionId(), "Payload retrieval failed");
    }
}
```

#### 6. **Data Consistency Patterns**

**Eventually Consistent Read Models**:
```java
@Component
public class ExceptionProjectionService {
    
    @EventHandler
    public void on(ExceptionCapturedEvent event) {
        // Update read model for dashboard queries
        ExceptionSummaryProjection projection = exceptionSummaryRepository
            .findByDateAndInterfaceType(LocalDate.now(), event.getInterfaceType())
            .orElse(new ExceptionSummaryProjection());
            
        projection.incrementExceptionCount();
        projection.updateSeverityDistribution(event.getSeverity());
        
        exceptionSummaryRepository.save(projection);
        
        // Invalidate related caches
        cacheManager.evict("exception-summary", event.getInterfaceType());
    }
    
    @EventHandler
    public void on(ExceptionResolvedEvent event) {
        // Update resolution metrics
        ExceptionSummaryProjection projection = exceptionSummaryRepository
            .findByDateAndInterfaceType(event.getResolvedAt().toLocalDate(), event.getInterfaceType())
            .orElse(new ExceptionSummaryProjection());
            
        projection.incrementResolvedCount();
        projection.updateResolutionTime(event.getResolutionTimeMinutes());
        
        exceptionSummaryRepository.save(projection);
        
        // Update real-time metrics
        meterRegistry.counter("exceptions.resolved", 
            "interface_type", event.getInterfaceType(),
            "resolution_method", event.getResolutionMethod())
            .increment();
    }
}
```

**Optimistic Locking for Concurrent Updates**:
```java
@Entity
@Table(name = "interface_exceptions")
public class InterfaceException {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;  // Optimistic locking
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    private ExceptionStatus status;
    
    private Integer retryCount = 0;
    
    // Atomic retry count increment with optimistic locking
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = OffsetDateTime.now();
    }
    
    // Status transition with validation
    public void updateStatus(ExceptionStatus newStatus, String updatedBy) {
        if (!isValidStatusTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid status transition from %s to %s", this.status, newStatus));
        }
        
        this.status = newStatus;
        this.updatedAt = OffsetDateTime.now();
        
        // Add status change audit record
        this.statusChanges.add(new StatusChange(this.status, newStatus, updatedBy));
    }
}
```

## Rationale

### Why Multi-Layered Retry Strategy?

#### 1. **Comprehensive Failure Coverage**
- **Problem**: Different types of failures require different retry strategies
- **Solution**: Multiple retry layers handle different failure scenarios appropriately
- **Benefit**: Maximizes recovery success rate while minimizing resource waste

#### 2. **Performance Optimization**
- **Problem**: Aggressive retries can overwhelm failing systems
- **Solution**: Intelligent backoff and circuit breaking prevent cascade failures
- **Benefit**: Maintains system stability during partial failures

#### 3. **Operational Simplicity**
- **Problem**: Complex retry logic is difficult to monitor and debug
- **Solution**: Standardized retry patterns with comprehensive observability
- **Benefit**: Easier troubleshooting and performance tuning

### Why Eventual Consistency Pattern?

#### 1. **Scalability Requirements**
- **Problem**: Strong consistency limits system scalability
- **Solution**: Eventual consistency allows independent scaling of components
- **Benefit**: Supports high-throughput exception processing

#### 2. **Availability Over Consistency**
- **Problem**: CAP theorem forces choice between consistency and availability
- **Solution**: Prioritize availability for exception processing with eventual consistency
- **Benefit**: System remains operational during network partitions

#### 3. **Business Requirements Alignment**
- **Problem**: Exception processing doesn't require immediate consistency
- **Solution**: Business processes can tolerate short consistency delays
- **Benefit**: Better user experience with faster response times

### Why Saga Pattern for Complex Operations?

#### 1. **Distributed Transaction Management**
- **Problem**: ACID transactions don't work across distributed services
- **Solution**: Saga pattern provides distributed transaction semantics
- **Benefit**: Reliable retry operations across multiple services

#### 2. **Failure Recovery**
- **Problem**: Partial failures in distributed operations are difficult to handle
- **Solution**: Compensation actions provide automatic rollback capabilities
- **Benefit**: System automatically recovers from partial failures

#### 3. **Auditability**
- **Problem**: Complex distributed operations are hard to trace and audit
- **Solution**: Saga steps provide complete audit trail of operations
- **Benefit**: Full visibility into retry operation lifecycle

## Consequences

### Positive Consequences

#### 1. **Improved System Resilience**
- **Automatic Recovery**: 80% of transient failures resolved without manual intervention
- **Graceful Degradation**: System continues operating with reduced functionality during failures
- **Fault Isolation**: Failures in one component don't cascade to others
- **Data Durability**: Zero data loss even during system failures

#### 2. **Enhanced Performance**
- **Reduced Latency**: Intelligent retry strategies minimize processing delays
- **Resource Efficiency**: Circuit breakers prevent resource waste on failing services
- **Horizontal Scalability**: Stateless retry logic supports unlimited scaling
- **Cache Optimization**: Consistent caching strategies improve response times

#### 3. **Better Operational Visibility**
- **Comprehensive Metrics**: Detailed retry success rates and failure patterns
- **Real-time Monitoring**: Live dashboards showing system health and retry status
- **Audit Compliance**: Complete audit trail of all retry attempts and outcomes
- **Alerting Integration**: Proactive alerts for retry failures and circuit breaker trips

#### 4. **Simplified Development**
- **Standardized Patterns**: Consistent retry patterns across all components
- **Declarative Configuration**: Retry behavior configured through annotations and YAML
- **Testing Support**: Built-in testing utilities for retry scenarios
- **Documentation**: Clear patterns and examples for developers

### Negative Consequences

#### 1. **Increased Complexity**
- **Distributed State Management**: Managing state across multiple retry layers
- **Debugging Challenges**: Tracing issues across asynchronous retry operations
- **Configuration Complexity**: Multiple retry configurations to maintain
- **Testing Complexity**: Comprehensive testing of retry scenarios

**Mitigation Strategies**:
- Comprehensive monitoring and distributed tracing
- Standardized retry configuration templates
- Automated testing of retry scenarios
- Clear documentation and runbooks

#### 2. **Eventual Consistency Challenges**
- **Data Lag**: Temporary inconsistencies between read and write models
- **Ordering Issues**: Events may be processed out of order
- **Reconciliation Complexity**: Handling consistency violations
- **User Experience**: Users may see stale data temporarily

**Mitigation Strategies**:
- Clear SLA definitions for consistency lag
- Event sequence validation and ordering
- Automated reconciliation processes
- User interface indicators for data freshness

#### 3. **Resource Overhead**
- **Memory Usage**: Retry state and circuit breaker state storage
- **CPU Overhead**: Retry logic and backoff calculations
- **Network Traffic**: Additional retry attempts and health checks
- **Storage Requirements**: Audit trail and retry history storage

**Mitigation Strategies**:
- Efficient retry state management
- Configurable retry limits and timeouts
- Resource monitoring and alerting
- Automated cleanup of old retry records

#### 4. **Operational Complexity**
- **Monitoring Requirements**: Multiple metrics and dashboards to monitor
- **Alert Fatigue**: Potential for too many retry-related alerts
- **Capacity Planning**: Complex capacity planning with retry traffic
- **Troubleshooting**: More complex failure scenarios to diagnose

**Mitigation Strategies**:
- Intelligent alerting with proper thresholds
- Automated capacity scaling based on retry patterns
- Comprehensive troubleshooting guides
- Regular operational training and drills

## Implementation Plan

### Phase 1: Foundation (Weeks 1-2)
- Implement basic retry annotations and configuration
- Set up circuit breaker infrastructure
- Create retry metrics and monitoring
- Establish dead letter queue processing

### Phase 2: Core Patterns (Weeks 3-4)
- Implement idempotency patterns
- Add event sequence validation
- Create saga orchestration framework
- Build optimistic locking mechanisms

### Phase 3: Advanced Features (Weeks 5-6)
- Implement complex retry scenarios
- Add compensation patterns
- Create consistency reconciliation processes
- Build advanced monitoring dashboards

### Phase 4: Testing and Optimization (Weeks 7-8)
- Comprehensive retry scenario testing
- Performance optimization and tuning
- Failure injection testing
- Documentation and training materials

## Monitoring and Success Metrics

### Technical Metrics
- **Retry Success Rate**: > 80% of retries should succeed
- **Circuit Breaker Effectiveness**: < 5% of requests should hit open circuits
- **Processing Latency**: 95th percentile < 100ms including retries
- **Data Consistency Lag**: < 5 seconds for eventual consistency

### Business Metrics
- **Exception Resolution Rate**: 90% of exceptions resolved within 1 hour
- **Manual Intervention Reduction**: 80% reduction in manual retry operations
- **System Availability**: 99.9% uptime including retry capabilities
- **Customer Impact**: < 1% of exceptions affect customer operations

### Operational Metrics
- **Alert Accuracy**: < 5% false positive rate for retry-related alerts
- **Troubleshooting Time**: < 15 minutes average time to identify retry issues
- **Configuration Drift**: Zero configuration inconsistencies across environments
- **Documentation Coverage**: 100% of retry patterns documented with examples

## Alternatives Considered

### 1. Simple Retry with Fixed Delays

**Approach**: Basic retry mechanism with fixed delays and maximum attempts

**Pros**:
- Simple to implement and understand
- Minimal configuration required
- Low resource overhead

**Cons**:
- Ineffective for different failure types
- Can overwhelm failing services
- No circuit breaking protection
- Limited observability

**Decision**: Rejected due to insufficient failure handling capabilities

### 2. Database-Based Retry Queue

**Approach**: Store retry requests in database queue with polling mechanism

**Pros**:
- Persistent retry state
- Strong consistency guarantees
- Simple transaction management

**Cons**:
- Database becomes bottleneck
- Polling introduces latency
- Limited scalability
- Complex queue management

**Decision**: Rejected due to scalability and performance concerns

### 3. External Retry Service

**Approach**: Dedicated microservice for handling all retry operations

**Pros**:
- Centralized retry logic
- Independent scaling
- Specialized optimization

**Cons**:
- Additional service to maintain
- Network overhead for retry requests
- Single point of failure
- Increased system complexity

**Decision**: Rejected to minimize system complexity and network overhead

### 4. Message Queue-Based Retries

**Approach**: Use message queue features (RabbitMQ, AWS SQS) for retry handling

**Pros**:
- Built-in retry capabilities
- Dead letter queue support
- Managed infrastructure

**Cons**:
- Limited retry customization
- Vendor lock-in potential
- Additional infrastructure dependency
- Less control over retry logic

**Decision**: Rejected to maintain flexibility and avoid vendor lock-in

## References

### Technical Documentation
- [Spring Retry Documentation](https://docs.spring.io/spring-retry/docs/current/reference/html/)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Saga Pattern Implementation](https://microservices.io/patterns/data/saga.html)
- [Apache Kafka Retry Patterns](https://kafka.apache.org/documentation/#retries)

### Architecture Patterns
- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns/)
- [Building Microservices by Sam Newman](https://samnewman.io/books/building_microservices/)
- [Release It! by Michael Nygard](https://pragprog.com/titles/mnee2/release-it-second-edition/)

### BioPro Platform Documentation
- Interface Exception Collector Service Requirements (`.kiro/specs/interface-exception-collector/requirements.md`)
- Interface Exception Collector Service Design (`.kiro/specs/interface-exception-collector/design.md`)
- REST/GraphQL Dual API Support Requirements (`.kiro/specs/rest-graphql-dual-api-support/requirements.md`)
- Partner Order Service Design (`.kiro/specs/partner-order-service/design.md`)

### Industry Best Practices
- [AWS Well-Architected Framework - Reliability Pillar](https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/)
- [Google SRE Book - Handling Overload](https://sre.google/sre-book/handling-overload/)
- [Netflix Technology Blog - Fault Tolerance](https://netflixtechblog.com/fault-tolerance-in-a-high-volume-distributed-system-91ab4faae74a)

---

**Document Information**
- **Author**: Architecture Team
- **Date**: 2025-08-21
- **Version**: 1.0
- **Status**: Accepted
- **Reviewers**: Engineering Team, Operations Team, SRE Team
- **Next Review**: 2025-11-21 (3 months)
- **Related ADRs**: ADR-001 (Event-Driven Exception Management Architecture)