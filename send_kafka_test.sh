#!/bin/bash

echo "🩸 Sending test OrderRejected event to Kafka..."

# Generate a test event JSON
EVENT_JSON='{
  "eventId": "test-'$(date +%s)'",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'",
  "source": "test-script",
  "correlationId": "corr-'$(date +%s)'",
  "causationId": "cause-'$(date +%s)'",
  "payload": {
    "transactionId": "'$(uuidgen | tr '[:upper:]' '[:lower:]')'",
    "externalId": "EXT-TEST-'$RANDOM'",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Test rejection - insufficient inventory for O- blood type",
    "customerId": "CUST-TEST-001",
    "locationCode": "HOSP-TEST-001",
    "orderItems": [
      {
        "bloodType": "O-",
        "productFamily": "RED_BLOOD_CELLS",
        "quantity": 2
      }
    ]
  }
}'

echo "📝 Event JSON:"
echo "$EVENT_JSON" | jq .

echo ""
echo "📡 Sending to Kafka topic 'OrderRejected'..."

# Send the message to Kafka with proper Spring Kafka headers
kubectl exec deployment/kafka -- bash -c "
echo '__TypeId__:com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent,__ContentTypeId__:application/json,__KeyTypeId__:java.lang.String|test-key-$(date +%s)|$EVENT_JSON' | /opt/kafka/bin/kafka-console-producer.sh \\
  --bootstrap-server localhost:9092 \\
  --topic OrderRejected \\
  --property 'parse.key=true' \\
  --property 'key.separator=|' \\
  --property 'parse.headers=true' \\
  --property 'headers.separator=,' \\
  --property 'headers.key.separator=:'
"

if [ $? -eq 0 ]; then
    echo "✅ Message sent successfully!"
    echo ""
    echo "🔍 Check the application logs:"
    echo "kubectl logs -l app=interface-exception-collector -c app --tail=20"
    echo ""
    echo "📊 Check if message was processed:"
    echo "kubectl exec interface-exception-collector-xxx -c app -- curl -s http://localhost:8080/api/v1/exceptions"
else
    echo "❌ Failed to send message"
    exit 1
fi