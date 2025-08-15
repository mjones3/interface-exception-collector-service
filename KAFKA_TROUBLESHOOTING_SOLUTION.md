# Kafka Deserialization Fix for Interface Exception Collector

## Problem
The Interface Exception Collector is consuming OrderRejected events from Kafka but failing to deserialize them from JSON to the OrderRejectedEvent Java object.

## Root Cause
The JSON deserializer is configured to deserialize to `Object.class` which results in a `LinkedHashMap` instead of the expected `OrderRejectedEvent` object.

## Solution

### 1. Update Kafka Consumer Configuration

In `interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/config/KafkaConsumerConfig.java`:

```java
// Change the trusted packages to allow all packages
configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

// Update the VALUE_DEFAULT_TYPE to handle generic objects better
configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.Object");

// Add these properties for better JSON handling
configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
configProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
```

### 2. Alternative Solution: Custom Deserializer

Create a custom deserializer that can handle the specific event format:

```java
@Component
public class OrderRejectedEventDeserializer extends JsonDeserializer<OrderRejectedEvent> {
    
    public OrderRejectedEventDeserializer() {
        super(OrderRejectedEvent.class);
        this.addTrustedPackages("*");
        this.setUseTypeHeaders(false);
        this.setRemoveTypeHeaders(false);
    }
    
    @Override
    public OrderRejectedEvent deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (Exception e) {
            // Fallback: try to deserialize as generic object and convert
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String, Object> eventMap = mapper.readValue(data, Map.class);
                return mapper.convertValue(eventMap, OrderRejectedEvent.class);
            } catch (Exception ex) {
                throw new SerializationException("Failed to deserialize OrderRejectedEvent", ex);
            }
        }
    }
}
```

### 3. Quick Fix: Update OrderRejectedEvent Class

Make the OrderRejectedEvent class more flexible for JSON deserialization:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class OrderRejectedEvent extends BaseEvent {
    // ... existing code
}
```

## Testing the Fix

1. Restart the Interface Exception Collector service
2. Send a test order to the Partner Order Service
3. Check the Interface Exception Collector logs for successful processing
4. Verify no messages are going to the dead letter queue

## Expected Result

After applying the fix, you should see logs like:
```
Processing OrderRejected event for transaction: 8d69f3e6-f622-4114-a627-bdf31455a41e
Successfully processed OrderRejected event. Created/updated exception with ID: 123
```

## Verification Commands

```bash
# Check consumer group status
kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group interface-exception-collector

# Check Interface Exception Collector logs
kubectl logs deployment/interface-exception-collector --tail=50

# Send test order
kubectl exec deployment/partner-order-service -- curl -X POST http://localhost:8090/v1/partner-order-provider/orders -H "Content-Type: application/json" -d '{"externalId":"TEST-FIX-'$(date +%s)'","orderStatus":"OPEN","locationCode":"TEST","shipmentType":"CUSTOMER","productCategory":"BLOOD_PRODUCTS","orderItems":[{"productFamily":"RED_BLOOD_CELLS_LEUKOREDUCED","bloodType":"O-","quantity":1}]}'
```