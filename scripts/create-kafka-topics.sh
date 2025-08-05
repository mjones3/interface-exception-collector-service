#!/bin/bash

echo "Creating Kafka topics..."

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
while ! docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    sleep 2
done

# Inbound topics
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic OrderRejected --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic OrderCancelled --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic CollectionRejected --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic DistributionFailed --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic ValidationError --partitions 3 --replication-factor 1

# Outbound topics  
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic ExceptionCaptured --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic ExceptionRetryCompleted --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic ExceptionResolved --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic CriticalExceptionAlert --partitions 1 --replication-factor 1

# Test topics
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic OrderRejected-test --partitions 1 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic ExceptionCaptured-test --partitions 1 --replication-factor 1
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic test-events --partitions 1 --replication-factor 1

echo "Kafka topics created successfully!"
