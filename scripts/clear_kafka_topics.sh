#!/bin/bash

# Script to clear Kafka topics and reset consumer groups
# This helps resolve deserialization issues by removing problematic messages

set -e

echo "🧹 Kafka Topic Cleanup Utility"
echo "==============================="

# Check if Kafka is running
if ! docker ps | grep -q kafka; then
    echo "❌ Kafka is not running. Starting Kafka..."
    docker-compose up -d kafka
    echo "⏳ Waiting for Kafka to start..."
    sleep 10
fi

# Topics to clear
TOPICS=(
    "OrderRejected"
    "OrderCancelled" 
    "CollectionRejected"
    "DistributionFailed"
    "ValidationError"
)

# Consumer groups to reset
CONSUMER_GROUPS=(
    "interface-exception-collector-local"
    "interface-exception-collector"
)

echo "🗑️  Clearing Kafka topics..."

for topic in "${TOPICS[@]}"; do
    echo "   Clearing topic: $topic"
    
    # Delete and recreate the topic
    docker exec exception-collector-kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic "$topic" 2>/dev/null || echo "   Topic $topic doesn't exist or already deleted"
    
    # Recreate the topic
    docker exec exception-collector-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic "$topic" --partitions 3 --replication-factor 1
    
    echo "   ✅ Topic $topic cleared and recreated"
done

echo ""
echo "🔄 Resetting consumer group offsets..."

for group in "${CONSUMER_GROUPS[@]}"; do
    echo "   Resetting consumer group: $group"
    
    for topic in "${TOPICS[@]}"; do
        docker exec exception-collector-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group "$group" --reset-offsets --to-earliest --topic "$topic" --execute 2>/dev/null || echo "   No offsets to reset for $group on $topic"
    done
    
    echo "   ✅ Consumer group $group reset"
done

echo ""
echo "📋 Current topic status:"
docker exec exception-collector-kafka kafka-topics --bootstrap-server localhost:9092 --list | grep -E "(OrderRejected|OrderCancelled|CollectionRejected|DistributionFailed|ValidationError)" || echo "   No matching topics found"

echo ""
echo "🎉 Kafka cleanup completed!"
echo "💡 You can now restart your application and send new test events."
echo ""
echo "🚀 Next steps:"
echo "   1. Restart the application: ./mvnw spring-boot:run"
echo "   2. Send test events: ./scripts/send_test_events.sh"