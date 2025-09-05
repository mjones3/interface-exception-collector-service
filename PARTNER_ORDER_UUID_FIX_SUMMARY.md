# Partner Order Service UUID Generation Fix

## Problem
When the Interface Exception Collector retries an order by POSTing to the Partner Order Service, the service was reusing the original transaction ID for the `partner_orders.transaction_id` field. This caused unique constraint violations because the `transaction_id` field has a unique constraint in the database.

## Root Cause
The original code was reusing the original transaction ID for retries:
```java
UUID transactionId = isRetry && originalTransactionId != null ? originalTransactionId : UUID.randomUUID();
```

This caused unique constraint violations on the `transaction_id` field when retrying.

## Solution
**Removed the unique constraint on `external_id`** and **always generate new UUIDs** for `transaction_id`:

1. **Database Migration**: Remove unique constraint on `external_id` to allow duplicate orders for retries
2. **Service Logic**: Always create new order records with new `transaction_id`, even for retries
3. **Entity Update**: Remove `unique = true` from `external_id` column annotation

```java
// Always generate a new UUID for transaction_id
UUID transactionId = UUID.randomUUID();

// Always create new order record (duplicates allowed for external_id)
PartnerOrder partnerOrder = PartnerOrder.builder()
        .transactionId(transactionId)  // New UUID every time
        .externalId(request.getExternalId())  // Can be duplicate for retries
        // ... other fields
        .build();
```

## Changes Made

### 1. Database Migration (V002)
- **File**: `partner-order-service/src/main/resources/db/migration/V002__Remove_external_id_unique_constraint.sql`
- **Change**: Remove unique constraint on `external_id` field
- **Reason**: Allow duplicate orders with same external_id but different transaction_ids for retries

### 2. PartnerOrder Entity
- **File**: `partner-order-service/src/main/java/com/arcone/biopro/partner/order/domain/entity/PartnerOrder.java`
- **Change**: Remove `unique = true` from `external_id` column annotation
- **Reason**: Align entity with database schema changes

### 3. PartnerOrderService.java
- **File**: `partner-order-service/src/main/java/com/arcone/biopro/partner/order/application/service/PartnerOrderService.java`
- **Changes**:
  - Always generate new UUID for `transactionId`
  - Always create new order records (no special retry logic)
  - Remove duplicate external_id validation
  - Enhanced logging for retry correlation

### 4. Enhanced Logging
Added conditional logging to clearly show when a retry is being processed:
```java
if (isRetry && originalTransactionId != null) {
    log.info("Processing partner order retry - externalId: {}, originalTransactionId: {}, newTransactionId: {}",
            request.getExternalId(), originalTransactionId, transactionId);
} else {
    log.info("Processing partner order - externalId: {}, isRetry: {}",
            request.getExternalId(), isRetry);
}
```

## How It Works

### Retry Flow
1. Interface Exception Collector receives an `OrderRejectedEvent` with the original transaction ID
2. When retrying, it sends:
   - `X-Retry-Attempt` header with the retry count
   - `X-Original-Transaction-ID` header with the original transaction ID
3. Partner Order Service:
   - Generates a **new** UUID for the `transaction_id` field (avoids constraint violation)
   - **Creates a new order record** with the same `external_id` (duplicates now allowed)
   - Logs the correlation between original and new transaction IDs
   - Returns the new transaction ID in the response

### Correlation Maintained
- The original transaction ID is preserved in the `X-Original-Transaction-ID` header
- Logging shows the relationship between original and new transaction IDs
- The correlation ID provides additional tracing capability

## Database Schema
The `partner_orders` table has a unique constraint on `transaction_id`:
```sql
transaction_id UUID NOT NULL UNIQUE,
```

This constraint is necessary for data integrity but was being violated by retry attempts.

## Impact on Other Components

### Interface Exception Collector
- No changes needed - it continues to send the original transaction ID in headers
- It receives the new transaction ID in the response for any subsequent operations

### Event Publishing
- Kafka events (OrderReceived, OrderRejected) use the new transaction ID
- This is correct behavior as events should reference the actual stored order

### Payload Retrieval
- The PayloadController continues to work with transaction IDs
- Interface Exception Collector would use the new transaction ID (from retry response) for payload retrieval

## Testing
- Created test script: `test-uuid-generation.ps1`
- Existing tests should continue to pass as the interface remains the same
- The change is internal to the service logic

## Benefits
1. **Eliminates unique constraint violations** on `transaction_id`
2. **Allows duplicate orders** with same `external_id` for retry scenarios
3. **Simple and consistent logic** - always create new orders with new transaction IDs
4. **Maintains correlation** between original and new transactions through logging
5. **Preserves data integrity** by ensuring each order has a unique transaction ID
6. **No breaking changes** to external interfaces
7. **Clear audit trail** through enhanced logging
8. **Proper retry semantics** - each retry creates a new attempt record

## Verification
To verify the fix:
1. Start the partner-order-service
2. Make a retry request with `X-Original-Transaction-ID` header
3. Check logs for correlation messages
4. Verify response contains new transaction ID
5. Confirm no database constraint violations occur