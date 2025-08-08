# Performance Tuning Runbook

## Overview

This runbook provides guidance for optimizing the performance of the Interface Exception Collector Service across different components and scenarios.

## Performance Baseline

### Key Performance Indicators (KPIs)

- **Exception Processing Rate**: > 1000 exceptions/minute
- **API Response Time**: < 200ms (95th percentile)
- **Database Query Time**: < 100ms (95th percentile)
- **Kafka Consumer Lag**: < 100 messages
- **Memory Usage**: < 80% of allocated heap
- **CPU Usage**: < 70% under normal load

### Measurement Tools

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics/exception.processing.rate
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Load testing
ab -n 1000 -c 10 http://localhost:8080/api/v1/exceptions
wrk -t12 -c400 -d30s http://localhost:8080/api/v1/exceptions

# Database performance
psql -c "SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10"
```

## JVM Performance Tuning

### Memory Configuration

```bash
# Optimal JVM settings for production
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Djava.awt.headless=true \
  -Dfile.encoding=UTF-8"
```

### Garbage Collection Tuning

```bash
# G1GC tuning for low latency
JAVA_OPTS="$JAVA_OPTS \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:G1HeapRegionSize=16m \
  -XX:G1NewSizePercent=30 \
  -XX:G1MaxNewSizePercent=40 \
  -XX:+G1UseAdaptiveIHOP \
  -XX:G1MixedGCCountTarget=8"

# Enable GC logging
JAVA_OPTS="$JAVA_OPTS \
  -Xlog:gc*:gc.log:time,tags \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=10M"
```

### JIT Compiler Optimization

```bash
# C2 compiler optimizations
JAVA_OPTS="$JAVA_OPTS \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=4 \
  -XX:+UseCodeCacheFlushing \
  -XX:ReservedCodeCacheSize=256m"
```

### Monitoring JVM Performance

```bash
# Monitor GC performance
jstat -gc -t $PID 5s

# Monitor memory usage
jmap -histo $PID | head -20

# Monitor thread usage
jstack $PID | grep "java.lang.Thread.State" | sort | uniq -c

# JVM metrics via actuator
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.threads.live
```

## Database Performance Optimization

### Connection Pool Tuning

```yaml
# Optimal HikariCP configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      pool-name: ExceptionCollectorPool
```

### Query Optimization

```sql
-- Add performance indexes
CREATE INDEX CONCURRENTLY idx_exceptions_customer_timestamp 
ON interface_exceptions(customer_id, timestamp DESC) 
WHERE status IN ('NEW', 'ACKNOWLEDGED');

CREATE INDEX CONCURRENTLY idx_exceptions_interface_status 
ON interface_exceptions(interface_type, status, timestamp DESC);

CREATE INDEX CONCURRENTLY idx_exceptions_severity_timestamp 
ON interface_exceptions(severity, timestamp DESC) 
WHERE severity IN ('HIGH', 'CRITICAL');

-- Partial index for active exceptions
CREATE INDEX CONCURRENTLY idx_exceptions_active 
ON interface_exceptions(timestamp DESC) 
WHERE status NOT IN ('RESOLVED', 'CLOSED');
```

### Database Configuration

```sql
-- PostgreSQL performance settings
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

SELECT pg_reload_conf();
```

### Query Performance Monitoring

```sql
-- Monitor slow queries
SELECT query, mean_exec_time, calls, total_exec_time, stddev_exec_time
FROM pg_stat_statements 
WHERE mean_exec_time > 100 
ORDER BY mean_exec_time DESC 
LIMIT 10;

-- Monitor index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE schemaname = 'public' 
ORDER BY idx_scan DESC;

-- Check for missing indexes
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE schemaname = 'public' 
  AND n_distinct > 100 
  AND correlation < 0.1;
```

## Kafka Performance Tuning

### Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      # Throughput optimization
      fetch-min-size: 50000          # Increase batch size
      fetch-max-wait: 500            # Reduce wait time
      max-poll-records: 500          # Increase records per poll
      max-poll-interval-ms: 300000   # Allow longer processing time
      
      # Memory optimization
      receive-buffer-bytes: 65536    # Increase receive buffer
      send-buffer-bytes: 131072      # Increase send buffer
      
      # Reliability settings
      enable-auto-commit: false      # Manual commit for better control
      auto-offset-reset: latest      # Start from latest on reset
```

### Producer Configuration

```yaml
spring:
  kafka:
    producer:
      # Throughput optimization
      batch-size: 32768              # Increase batch size
      linger-ms: 10                  # Add small delay for batching
      compression-type: snappy       # Enable compression
      buffer-memory: 67108864        # Increase buffer memory
      
      # Reliability settings
      acks: all                      # Wait for all replicas
      retries: 3                     # Retry failed sends
      max-in-flight-requests-per-connection: 1  # Ensure ordering
```

### Consumer Scaling

```java
// Increase consumer concurrency
@KafkaListener(
    topics = {"OrderRejected", "OrderCancelled"},
    concurrency = "5"  // Scale based on partition count
)
public void handleOrderEvents(OrderEvent event) {
    // Process event
}

// Use batch processing for better throughput
@KafkaListener(topics = "OrderRejected")
public void handleOrderRejectedBatch(List<OrderRejectedEvent> events) {
    // Process batch of events
    processBatch(events);
}
```

## Caching Optimization

### Redis Configuration

```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
```

### Cache Strategy

```java
// Optimize cache usage
@Cacheable(value = "exceptions", key = "#transactionId", unless = "#result == null")
public InterfaceException findByTransactionId(String transactionId) {
    return repository.findByTransactionId(transactionId);
}

// Cache with TTL
@Cacheable(value = "payloads", key = "#transactionId")
@CacheEvict(value = "payloads", key = "#transactionId", condition = "#result == null")
public Object getOriginalPayload(String transactionId) {
    return externalService.getPayload(transactionId);
}

// Batch cache operations
@CacheEvict(value = "exceptions", allEntries = true)
public void clearExceptionCache() {
    // Clear cache when needed
}
```

### Cache Monitoring

```bash
# Redis performance metrics
redis-cli info stats
redis-cli info memory
redis-cli info clients

# Application cache metrics
curl http://localhost:8080/actuator/metrics/cache.gets
curl http://localhost:8080/actuator/metrics/cache.puts
curl http://localhost:8080/actuator/metrics/cache.evictions
```

## API Performance Optimization

### Response Optimization

```java
// Use pagination for large result sets
@GetMapping("/exceptions")
public ResponseEntity<PagedResponse<ExceptionListResponse>> listExceptions(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") @Max(100) int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<InterfaceException> exceptions = service.findAll(pageable);
    return ResponseEntity.ok(mapper.toPagedResponse(exceptions));
}

// Implement field selection
@GetMapping("/exceptions/{id}")
public ResponseEntity<ExceptionDetailResponse> getException(
    @PathVariable Long id,
    @RequestParam(required = false) Set<String> fields) {
    
    InterfaceException exception = service.findById(id);
    ExceptionDetailResponse response = mapper.toDetailResponse(exception, fields);
    return ResponseEntity.ok(response);
}
```

### HTTP Configuration

```yaml
# Tomcat optimization
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    max-connections: 8192
    accept-count: 100
    max-http-post-size: 2MB
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024
```

### Async Processing

```java
// Use async processing for heavy operations
@Async("taskExecutor")
@EventListener
public CompletableFuture<Void> handleExceptionEvent(ExceptionCapturedEvent event) {
    // Process event asynchronously
    processException(event);
    return CompletableFuture.completedFuture(null);
}

// Configure thread pool
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

## Monitoring and Alerting

### Performance Metrics

```bash
# Create performance monitoring script
#!/bin/bash
# performance-monitor.sh

while true; do
  # API response times
  API_P95=$(curl -s http://localhost:8080/actuator/metrics/http.server.requests | \
    jq '.measurements[] | select(.statistic=="0.95") | .value')
  
  if (( $(echo "$API_P95 > 0.5" | bc -l) )); then
    echo "$(date): High API response time: ${API_P95}s"
  fi
  
  # Memory usage
  MEMORY_USED=$(curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | \
    jq '.measurements[0].value')
  MEMORY_MAX=$(curl -s http://localhost:8080/actuator/metrics/jvm.memory.max | \
    jq '.measurements[0].value')
  MEMORY_PCT=$(echo "scale=2; $MEMORY_USED / $MEMORY_MAX * 100" | bc)
  
  if (( $(echo "$MEMORY_PCT > 80" | bc -l) )); then
    echo "$(date): High memory usage: ${MEMORY_PCT}%"
  fi
  
  # Database connection pool
  DB_ACTIVE=$(curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | \
    jq '.measurements[0].value')
  
  if (( $(echo "$DB_ACTIVE > 15" | bc -l) )); then
    echo "$(date): High database connection usage: $DB_ACTIVE"
  fi
  
  sleep 60
done
```

### Custom Metrics

```java
// Add custom performance metrics
@Component
public class PerformanceMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Timer exceptionProcessingTimer;
    private final Counter exceptionProcessingCounter;
    
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.exceptionProcessingTimer = Timer.builder("exception.processing.time")
            .description("Time taken to process exceptions")
            .register(meterRegistry);
        this.exceptionProcessingCounter = Counter.builder("exception.processing.count")
            .description("Number of exceptions processed")
            .register(meterRegistry);
    }
    
    public void recordProcessingTime(Duration duration) {
        exceptionProcessingTimer.record(duration);
        exceptionProcessingCounter.increment();
    }
}
```

## Load Testing

### API Load Testing

```bash
# Apache Bench
ab -n 10000 -c 100 -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/exceptions

# wrk load testing
wrk -t12 -c400 -d30s --script=load-test.lua \
  http://localhost:8080/api/v1/exceptions

# JMeter test plan
jmeter -n -t exception-collector-load-test.jmx -l results.jtl
```

### Kafka Load Testing

```bash
# Producer performance test
kafka-producer-perf-test.sh \
  --topic OrderRejected \
  --num-records 100000 \
  --record-size 1024 \
  --throughput 1000 \
  --producer-props bootstrap.servers=$KAFKA_BOOTSTRAP_SERVERS

# Consumer performance test
kafka-consumer-perf-test.sh \
  --topic OrderRejected \
  --messages 100000 \
  --threads 5 \
  --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS
```

### Database Load Testing

```bash
# pgbench for database load testing
pgbench -i -s 10 $DATABASE_NAME
pgbench -c 10 -j 2 -t 1000 $DATABASE_NAME
```

## Performance Troubleshooting

### High CPU Usage

```bash
# Identify CPU-intensive threads
top -H -p $PID

# Java thread dump
jstack $PID > thread-dump.txt

# Analyze thread dump
grep -A 5 -B 5 "RUNNABLE" thread-dump.txt
```

### High Memory Usage

```bash
# Memory analysis
jmap -histo $PID | head -20
jmap -dump:format=b,file=heap-dump.hprof $PID

# Analyze heap dump with Eclipse MAT or VisualVM
```

### Slow Database Queries

```sql
-- Enable query logging
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- Analyze slow queries
SELECT query, mean_exec_time, calls, total_exec_time
FROM pg_stat_statements 
WHERE mean_exec_time > 100 
ORDER BY total_exec_time DESC;

-- Check for lock contention
SELECT blocked_locks.pid AS blocked_pid,
       blocking_locks.pid AS blocking_pid,
       blocked_activity.query AS blocked_statement,
       blocking_activity.query AS current_statement_in_blocking_process
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;
```

## Capacity Planning

### Resource Requirements

```bash
# Calculate resource needs based on load
# Exception processing rate: 1000/minute
# Average exception size: 2KB
# Storage per day: 1000 * 60 * 24 * 2KB = 2.88GB/day
# Database storage (30 days): ~87GB
# Memory for caching (10% of daily data): ~300MB
```

### Scaling Guidelines

```yaml
# Horizontal scaling configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: interface-exception-collector-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: interface-exception-collector
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Contact Information

- **Performance Team**: perf-team@biopro.com
- **On-call Engineer**: +1-555-0126
- **Infrastructure Team**: infra-team@biopro.com

## Related Documentation

- [Service Lifecycle Management](service-lifecycle.md)
- [Database Troubleshooting](database-troubleshooting.md)
- [Monitoring Setup](monitoring-setup.md)