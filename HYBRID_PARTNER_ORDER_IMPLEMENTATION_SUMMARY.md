# Hybrid Partner Order Service Implementation Summary

## Overview

This implementation provides a hybrid approach for Partner Order Service integration that uses:
- **RSocket** for retrieving order data after OrderRejected events
- **REST** for submitting retry requests to the partner order service

## Architecture

```
OrderRejected Event → RSocket Retrieval → Store in DB → REST Retry → Partner Order Service
                           ↓                    ↓              ↓
                    Mock RSocket Server    order_received   POST /v1/partner-order-provider/orders
                      (Port 7000)          field in DB      (Port 8090)
```

## Key Components

### 1. PartnerOrderServiceClient (Hybrid Implementation)

**Location**: `interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/client/PartnerOrderServiceClient.java`

**Key Features**:
- Extends `BaseSourceServiceClient` for common functionality
- Uses RSocket for `getOriginalPayload()` method
- Uses REST for `submitRetry()` method
- Automatic data transformation between formats
- Circuit breaker and retry patterns
- Comprehensive error handling

**RSocket Retrieval**:
```java
@Override
public CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception) {
    // Uses RSocket to call: orders.{externalId}
    // Returns order data from mock server or partner service
}
```

**REST Retry**:
```java
@Override
public CompletableFuture<ResponseEntity<Object>> submitRetry(InterfaceException exception, Object payload) {
    // Transforms payload to PartnerOrderRequest format
    // POSTs to: http://localhost:8090/v1/partner-order-provider/orders
    // Includes retry headers: X-Retry-Attempt, X-Original-Transaction-ID
}
```

### 2. Data Transformation

The client automatically transforms stored order data into the Partner Order Service expected format:

**Input Format** (from RSocket/database):
```json
{
  "externalId": "ORDER-123",
  "customerId": "CUST-001",
  "locationCode": "LOC-001",
  "orderItems": [
    {
      "productFamily": "RED_BLOOD_CELLS",
      "bloodType": "O_POS",
      "quantity": 1
    }
  ]
}
```

**Output Format** (for REST retry):
```json
{
  "externalId": "ORDER-123",
  "orderStatus": "OPEN",
  "locationCode": "LOC-001",
  "shipmentType": "CUSTOMER",
  "productCategory": "BLOOD_PRODUCTS",
  "orderItems": [
    {
      "productFamily": "RED_BLOOD_CELLS",
      "bloodType": "O_POS",
      "quantity": 1
    }
  ]
}
```

### 3. Configuration

**Location**: `interface-exception-collector/src/main/resources/application-local.yml`

```yaml
source-services:
  partner-order:
    base-url: http://localhost:8090
    api-key: ${PARTNER_ORDER_API_KEY:}
    auth-header: X-API-Key
    rsocket:
      host: localhost
      port: 7000
      timeout: PT10S
    rest:
      retry-url: http://localhost:8090/v1/partner-order-provider/orders
    timeout: 30s
    retry:
      max-attempts: 3
      delay: 1s

interface-routing:
  PARTNER_ORDER: partner-order-service
  ORDER: partner-order-service  # Routes to hybrid client
```

### 4. Testing

**Unit Tests**: `PartnerOrderServiceClientTest.java`
- Tests RSocket retrieval functionality
- Tests REST retry submission
- Tests data transformation logic
- Tests error handling and fallbacks

**Integration Tests**: `HybridPartnerOrderServiceIntegrationTest.java`
- End-to-end flow testing
- Complex data transformation scenarios
- Circuit breaker behavior
- Error recovery patterns

**Manual Testing**: `test-hybrid-partner-order-flow.ps1`
- Service availability checks
- RSocket connectivity testing
- REST endpoint validation
- Configuration verification

## Flow Details

### 1. Order Rejection Processing

1. **OrderRejected Event** received via Kafka
2. **ExceptionProcessingService** creates InterfaceException
3. **RSocket Retrieval** attempts to get order data:
   ```java
   retrieveAndStoreOrderData(exception);
   ```
4. **Order data stored** in `interface_exceptions.order_received` field
5. **Exception marked** as retryable if data retrieved successfully

### 2. Retry Processing

1. **User/System triggers retry** via API or scheduled job
2. **PartnerOrderServiceClient.submitRetry()** called
3. **Data transformation** from stored format to PartnerOrderRequest
4. **REST POST** to partner order service with retry headers
5. **Response handling** and status updates

## Error Handling

### RSocket Errors
- Connection failures → Fallback response
- Timeout errors → Circuit breaker activation
- Data not found → Mark as non-retryable

### REST Errors
- 400 Bad Request → Log validation errors
- 409 Conflict → Handle duplicate orders
- 500 Internal Error → Trigger retry mechanism
- Network errors → Circuit breaker protection

## Monitoring and Observability

### Metrics
- RSocket connection status
- Retrieval success/failure rates
- REST retry success/failure rates
- Circuit breaker state changes
- Data transformation errors

### Logging
- Structured logging with correlation IDs
- RSocket call tracing
- REST request/response logging
- Error details and stack traces

### Health Checks
- RSocket connectivity
- Partner Order Service availability
- Circuit breaker status
- Configuration validation

## Deployment Considerations

### Prerequisites
1. **Mock RSocket Server** running on port 7000
2. **Partner Order Service** running on port 8090
3. **Database** with interface_exceptions table
4. **Kafka** for OrderRejected events

### Environment Variables
```bash
PARTNER_ORDER_API_KEY=your-api-key-here
SPRING_PROFILES_ACTIVE=local
```

### Service Dependencies
- Interface Exception Collector (port 8080)
- Partner Order Service (port 8090)
- Mock RSocket Server (port 7000)
- PostgreSQL/H2 Database
- Kafka Cluster

## Testing the Implementation

### 1. Run the Test Script
```powershell
./test-hybrid-partner-order-flow.ps1
```

### 2. Manual Testing Steps
1. Start all required services
2. Send OrderRejected event to Kafka
3. Verify order data retrieval via RSocket
4. Trigger retry via API
5. Verify REST submission to partner service

### 3. Verify Data Flow
1. Check `interface_exceptions.order_received` field populated
2. Verify retry headers in REST requests
3. Confirm data transformation accuracy
4. Monitor circuit breaker behavior

## Future Enhancements

### 1. Enhanced Resilience
- Implement bulkhead pattern
- Add rate limiting
- Enhanced fallback strategies

### 2. Performance Optimization
- Connection pooling for RSocket
- Async processing improvements
- Caching strategies

### 3. Monitoring Improvements
- Custom metrics dashboards
- Alerting on failure thresholds
- Performance tracking

### 4. Security Enhancements
- JWT token authentication
- TLS for RSocket connections
- API key rotation

## Troubleshooting

### Common Issues

1. **RSocket Connection Failed**
   - Check if mock server is running on port 7000
   - Verify network connectivity
   - Check firewall settings

2. **REST Retry Failed**
   - Verify partner order service is running
   - Check API key configuration
   - Validate request format

3. **Data Transformation Errors**
   - Check order data format in database
   - Verify JSON parsing logic
   - Review transformation mappings

4. **Circuit Breaker Open**
   - Check error rates and thresholds
   - Verify service health
   - Review timeout configurations

### Debug Commands
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8090/actuator/health

# Test RSocket connectivity
telnet localhost 7000

# Check database records
SELECT * FROM interface_exceptions WHERE interface_type = 'ORDER';

# View application logs
kubectl logs -f deployment/interface-exception-collector
```

## Conclusion

This hybrid implementation provides a robust, scalable solution for Partner Order Service integration that:
- Separates concerns between retrieval and retry operations
- Provides comprehensive error handling and resilience
- Supports automatic data transformation
- Includes extensive testing and monitoring capabilities
- Follows enterprise patterns and best practices

The implementation is production-ready and includes all necessary components for reliable operation in a distributed microservices environment.