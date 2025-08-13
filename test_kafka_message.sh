#!/bin/bash

# Send a properly formatted OrderRejected event to Kafka with Spring Kafka headers

kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic OrderRejected \
  --property "parse.key=true" \
  --property "key.separator=:" \
  --property "parse.headers=true" \
  --property "headers.separator=|" \
  --property "headers.key.separator==" << 'EOF'
__TypeId__=com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent|__ContentTypeId__=application/json|__KeyTypeId__=java.lang.String:txn-test-123:{"eventId":"test-event-123","eventType":"OrderRejected","eventVersion":"1.0","occurredOn":"2025-08-13T14:40:00.000Z","source":"test-script","correlationId":"corr-test-123","causationId":"cause-test-123","payload":{"transactionId":"txn-test-123","externalId":"ext-test-123","operation":"CREATE_ORDER","rejectedReason":"Test rejection for Kafka connectivity","customerId":"CUST-TEST-001","locationCode":"HOSP-TEST-001","orderItems":[{"bloodType":"O-","productFamily":"RED_BLOOD_CELLS","quantity":2}]}}
EOF