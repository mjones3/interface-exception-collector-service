# Kafka Troubleshooting Runbook

## Overview

This runbook covers common Kafka-related issues and their resolution procedures for the Interface Exception Collector Service.

## Quick Diagnostics

### Kafka Connectivity

```bash
# Test basic connectivity
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Check cluster health
kafka-broker-api-versions.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS

# Test from application pod
kubectl exec -it <pod-name> -- kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list
```

### Consumer Group Status

```bash
# Check consumer group status
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector

# List all consumer groups
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Check consumer lag
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector --verbose
```

### Topic Information

```bash
# List topics
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Describe specific topic
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --topic OrderRejected

# Check topic configuration
kafka-configs.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --entity-type topics --entity-name OrderRejected
```

## Common Issues and Solutions

### Issue 1: Consumer Lag

**Symptoms:**
- High consumer lag in monitoring dashboards
- Delayed exception processing
- Messages piling up in topics

**Diagnosis:**

```bash
# Check consumer lag details
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector

# Check partition assignment
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector --verbose

# Monitor consumer metrics
curl http://localhost:8080/actuator/metrics/kafka.consumer.records.lag.max
curl http://localhost:8080/actuator/metrics/kafka.consumer.records.consumed.rate
```

**Resolution:**

1. **Scale up consumers**:
   ```bash
   # Increase replica count
   kubectl scale deployment interface-exception-collector --replicas=5
   ```

2. **Optimize consumer configuration**:
   ```yaml
   # In application.yml
   spring:
     kafka:
       consumer:
         max-poll-records: 100  # Reduce batch size
         fetch-min-size: 1024   # Increase fetch size
         fetch-max-wait: 500    # Reduce wait time
   ```

3. **Check for slow message processing**:
   ```java
   // Add timing metrics to message handlers
   @EventListener
   @Timed(name = "exception.processing.time")
   public void handleOrderRejected(OrderRejectedEvent event) {
       // Processing logic
   }
   ```

### Issue 2: Consumer Not Receiving Messages

**Symptoms:**
- No new exceptions being processed
- Consumer group shows no activity
- Topics have messages but consumers aren't processing them

**Diagnosis:**

```bash
# Check if messages are being produced
kafka-console-consumer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic OrderRejected --from-beginning --max-messages 5

# Check consumer group membership
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector --members

# Check application logs for consumer errors
kubectl logs -f deployment/interface-exception-collector | grep -i kafka
```

**Resolution:**

1. **Restart consumer group**:
   ```bash
   # Reset consumer group offsets
   kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
     --group interface-exception-collector --reset-offsets --to-latest --all-topics --execute
   
   # Restart application
   kubectl rollout restart deployment/interface-exception-collector
   ```

2. **Check consumer configuration**:
   ```yaml
   # Verify consumer group ID
   spring:
     kafka:
       consumer:
         group-id: interface-exception-collector
         auto-offset-reset: latest
         enable-auto-commit: true
   ```

3. **Verify topic subscriptions**:
   ```java
   @KafkaListener(topics = {"OrderRejected", "OrderCancelled", "CollectionRejected", "DistributionFailed", "ValidationError"})
   public void handleExceptionEvent(String message) {
       // Handler logic
   }
   ```

### Issue 3: Message Deserialization Errors

**Symptoms:**
- "Deserialization failed" errors in logs
- Messages being skipped
- Consumer lag increasing without processing

**Diagnosis:**

```bash
# Check application logs for deserialization errors
kubectl logs deployment/interface-exception-collector | grep -i "deserialization\|serialization"

# Examine message format
kafka-console-consumer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic OrderRejected --from-beginning --max-messages 1 --property print.headers=true
```

**Resolution:**

1. **Fix message format**:
   ```java
   // Add error handling for deserialization
   @KafkaListener(topics = "OrderRejected")
   public void handleOrderRejected(
       @Payload(required = false) OrderRejectedEvent event,
       @Header Map<String, Object> headers,
       ConsumerRecord<String, String> record) {
       
       if (event == null) {
           log.error("Failed to deserialize message: {}", record.value());
           // Handle malformed message
           return;
       }
       
       // Process valid event
   }
   ```

2. **Configure error handling**:
   ```yaml
   spring:
     kafka:
       consumer:
         value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
         properties:
           spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
           spring.json.trusted.packages: "com.arcone.biopro.exception.collector.domain.event"
   ```

3. **Add dead letter queue**:
   ```java
   @RetryableTopic(
       attempts = "3",
       backoff = @Backoff(delay = 1000, multiplier = 2.0),
       dltStrategy = DltStrategy.FAIL_ON_ERROR
   )
   @KafkaListener(topics = "OrderRejected")
   public void handleOrderRejected(OrderRejectedEvent event) {
       // Processing logic
   }
   ```

### Issue 4: Producer Connection Issues

**Symptoms:**
- "Failed to send message" errors
- Outbound events not being published
- Producer timeout errors

**Diagnosis:**

```bash
# Check producer metrics
curl http://localhost:8080/actuator/metrics/kafka.producer.record.send.rate
curl http://localhost:8080/actuator/metrics/kafka.producer.record.error.rate

# Test producer connectivity
kafka-console-producer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic ExceptionCaptured
```

**Resolution:**

1. **Check producer configuration**:
   ```yaml
   spring:
     kafka:
       producer:
         bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
         key-serializer: org.apache.kafka.common.serialization.StringSerializer
         value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
         acks: all
         retries: 3
         properties:
           retry.backoff.ms: 1000
           request.timeout.ms: 30000
   ```

2. **Add retry logic**:
   ```java
   @Retryable(value = {KafkaException.class}, maxAttempts = 3)
   public void publishEvent(BaseEvent event) {
       kafkaTemplate.send(topicName, event);
   }
   ```

3. **Implement circuit breaker**:
   ```java
   @CircuitBreaker(name = "kafka-producer", fallbackMethod = "fallbackPublish")
   public void publishEvent(BaseEvent event) {
       kafkaTemplate.send(topicName, event);
   }
   
   public void fallbackPublish(BaseEvent event, Exception ex) {
       // Store event for later retry or log error
       log.error("Failed to publish event, storing for retry: {}", event, ex);
   }
   ```

### Issue 5: Topic Not Found

**Symptoms:**
- "Topic does not exist" errors
- Consumer group not starting
- Producer failing to send messages

**Diagnosis:**

```bash
# List all topics
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Check if topic exists
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --topic OrderRejected
```

**Resolution:**

1. **Create missing topics**:
   ```bash
   # Create topic with proper configuration
   kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
     --create --topic OrderRejected \
     --partitions 3 --replication-factor 2 \
     --config retention.ms=604800000  # 7 days
   ```

2. **Use topic creation script**:
   ```bash
   ./scripts/create-kafka-topics.sh
   ```

3. **Enable auto-topic creation** (if appropriate):
   ```yaml
   # In Kafka broker configuration
   auto.create.topics.enable=true
   num.partitions=3
   default.replication.factor=2
   ```

### Issue 6: Rebalancing Issues

**Symptoms:**
- Frequent consumer rebalancing
- Processing interruptions
- "Rebalance in progress" messages

**Diagnosis:**

```bash
# Monitor consumer group stability
watch -n 5 "kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector"

# Check for consumer timeouts
kubectl logs deployment/interface-exception-collector | grep -i rebalance
```

**Resolution:**

1. **Tune consumer configuration**:
   ```yaml
   spring:
     kafka:
       consumer:
         session-timeout-ms: 30000      # Increase session timeout
         heartbeat-interval-ms: 10000   # Increase heartbeat interval
         max-poll-interval-ms: 300000   # Increase max poll interval
         max-poll-records: 50           # Reduce batch size
   ```

2. **Optimize message processing**:
   ```java
   @KafkaListener(topics = "OrderRejected", concurrency = "3")
   public void handleOrderRejected(OrderRejectedEvent event) {
       // Ensure fast processing to avoid session timeout
       processEventQuickly(event);
   }
   ```

3. **Add health checks**:
   ```java
   @Component
   public class KafkaHealthIndicator implements HealthIndicator {
       @Override
       public Health health() {
           // Check consumer group health
           return Health.up().build();
       }
   }
   ```

## Performance Monitoring

### Key Metrics

```bash
# Consumer metrics
curl http://localhost:8080/actuator/metrics/kafka.consumer.records.consumed.rate
curl http://localhost:8080/actuator/metrics/kafka.consumer.records.lag.max
curl http://localhost:8080/actuator/metrics/kafka.consumer.fetch.rate

# Producer metrics
curl http://localhost:8080/actuator/metrics/kafka.producer.record.send.rate
curl http://localhost:8080/actuator/metrics/kafka.producer.record.error.rate
curl http://localhost:8080/actuator/metrics/kafka.producer.batch.size.avg
```

### Monitoring Script

```bash
#!/bin/bash
# kafka-monitor.sh

BOOTSTRAP_SERVERS=$1
CONSUMER_GROUP=$2

while true; do
  # Check consumer lag
  LAG=$(kafka-consumer-groups.sh --bootstrap-server $BOOTSTRAP_SERVERS \
    --describe --group $CONSUMER_GROUP | awk '{sum += $5} END {print sum}')
  
  if [ "$LAG" -gt 1000 ]; then
    echo "$(date): High consumer lag detected: $LAG"
  fi
  
  # Check consumer group health
  MEMBERS=$(kafka-consumer-groups.sh --bootstrap-server $BOOTSTRAP_SERVERS \
    --describe --group $CONSUMER_GROUP --members | wc -l)
  
  if [ "$MEMBERS" -lt 2 ]; then
    echo "$(date): Low consumer group membership: $MEMBERS"
  fi
  
  sleep 30
done
```

## Topic Management

### Topic Configuration

```bash
# Update topic configuration
kafka-configs.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --alter --entity-type topics --entity-name OrderRejected \
  --add-config retention.ms=1209600000  # 14 days

# Increase partitions
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --alter --topic OrderRejected --partitions 6

# Check topic metrics
kafka-run-class.sh kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic=OrderRejected
```

### Topic Cleanup

```bash
# Delete old messages (be careful!)
kafka-configs.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --alter --entity-type topics --entity-name OrderRejected \
  --add-config retention.ms=1000

# Wait for cleanup, then restore retention
sleep 60
kafka-configs.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --alter --entity-type topics --entity-name OrderRejected \
  --add-config retention.ms=604800000
```

## Security Issues

### Authentication Problems

```bash
# Check SASL configuration
kafka-configs.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --entity-type brokers

# Test authentication
kafka-console-consumer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic OrderRejected --consumer.config client.properties
```

### SSL/TLS Issues

```bash
# Verify SSL configuration
openssl s_client -connect $KAFKA_BROKER:9093 -servername $KAFKA_BROKER

# Check certificate validity
openssl x509 -in kafka.crt -text -noout
```

## Emergency Procedures

### Consumer Group Reset

```bash
# Stop all consumers
kubectl scale deployment interface-exception-collector --replicas=0

# Reset offsets to beginning
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --group interface-exception-collector --reset-offsets --to-earliest --all-topics --execute

# Restart consumers
kubectl scale deployment interface-exception-collector --replicas=3
```

### Topic Recreation

```bash
# Backup topic data
kafka-console-consumer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic OrderRejected --from-beginning > topic_backup.json

# Delete and recreate topic
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --delete --topic OrderRejected
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --create --topic OrderRejected --partitions 3 --replication-factor 2

# Restore data if needed
kafka-console-producer.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic OrderRejected < topic_backup.json
```

## Contact Information

- **Kafka Team**: kafka-team@biopro.com
- **On-call Engineer**: +1-555-0125
- **Infrastructure Team**: infra-team@biopro.com

## Related Documentation

- [Service Lifecycle Management](service-lifecycle.md)
- [Performance Tuning](performance-tuning.md)
- [Monitoring Setup](monitoring-setup.md)