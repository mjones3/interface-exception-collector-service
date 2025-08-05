# Event Schemas

This directory contains JSON Schema definitions for all events used by the Interface Exception Collector Service.

## Directory Structure

```
schema/
├── README.md                           # This file
├── schema-index.json                   # Index of all schemas
├── validate-schemas.js                 # Validation test script
├── Inbound Events (Consumed)
│   ├── OrderRejected-Inbound.json
│   ├── OrderCancelled-Inbound.json
│   ├── CollectionRejected-Inbound.json
│   ├── DistributionFailed-Inbound.json
│   └── ValidationError-Inbound.json
└── Outbound Events (Published)
    ├── ExceptionCaptured-Outbound.json
    ├── ExceptionRetryCompleted-Outbound.json
    ├── ExceptionResolved-Outbound.json
    └── CriticalExceptionAlert-Outbound.json
```

## Inbound Events

Events that the Exception Collector Service consumes from other services:

| Event | Source Service | Description |
|-------|----------------|-------------|
| `OrderRejected` | order-service | Order requests rejected due to business rules or validation |
| `OrderCancelled` | order-service | Orders cancelled during processing |
| `CollectionRejected` | collection-service | Collection requests rejected |
| `DistributionFailed` | distribution-service | Distribution processing failures |
| `ValidationError` | any-service | Schema or data validation errors |

## Outbound Events

Events that the Exception Collector Service publishes:

| Event | Target | Description |
|-------|--------|-------------|
| `ExceptionCaptured` | monitoring-systems | New exception captured notification |
| `ExceptionRetryCompleted` | audit-systems | Retry operation completion |
| `ExceptionResolved` | workflow-systems | Exception resolution notification |
| `CriticalExceptionAlert` | alerting-systems | High-priority exception alerts |

## Schema Validation

To validate all schemas and their examples:

```bash
cd src/main/resources/schema
node validate-schemas.js
```

Requirements:
- Node.js
- `npm install ajv ajv-formats`

## Schema Versioning

All schemas follow semantic versioning:
- **Major version**: Breaking changes to event structure
- **Minor version**: Backward-compatible additions
- **Patch version**: Non-functional changes (documentation, examples)

Current version: `1.0.0`

## Usage in Code

### Spring Boot Event Listeners

```java
@KafkaListener(topics = "OrderRejected")
public void handleOrderRejected(@Payload @Valid OrderRejectedEvent event) {
    // JSON schema validation handled automatically
    exceptionCollectorService.processOrderRejection(event);
}
```

### Event Publishing

```java
@Component
public class ExceptionEventPublisher {
    
    public void publishExceptionCaptured(InterfaceException exception) {
        ExceptionCapturedEvent event = ExceptionCapturedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("ExceptionCaptured")
            .eventVersion("1.0")
            .occurredOn(Instant.now())
            .source("exception-collector-service")
            .payload(createPayload(exception))
            .build();
            
        kafkaTemplate.send("ExceptionCaptured", event);
    }
}
```

## Schema Evolution Guidelines

1. **Backward Compatibility**: New fields must be optional
2. **Field Removal**: Deprecate first, remove in next major version
3. **Type Changes**: Always breaking changes (new major version)
4. **Enum Values**: New values are backward compatible
5. **Required Fields**: Adding required fields is breaking

## Testing

Each schema includes example payloads that are automatically validated. When modifying schemas:

1. Update the schema definition
2. Update or add example payloads
3. Run validation: `node validate-schemas.js`
4. Update integration tests with new examples

## Documentation

Schema documentation is auto-generated from:
- JSON Schema descriptions and titles
- Example payloads
- Schema metadata

The generated documentation is available at: `/api-docs/schemas`
