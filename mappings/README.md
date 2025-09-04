# Mock RSocket Server Mappings

This directory contains mapping files that configure how the mock RSocket server responds to different order requests.

## Mapping Files

### Core Mappings
- `order-success-mapping.json` - General success responses for valid order patterns
- `order-not-found-mapping.json` - 404 responses for orders that don't exist
- `default-fallback-mapping.json` - Catch-all mapping for unmapped requests

### Test Scenario Mappings
- `test-order-1-mapping.json` - Specific mapping for TEST-ORDER-1 test case
- `test-ord-2025-018-mapping.json` - Specific mapping for TEST-ORD-2025-018 test case
- `bulk-order-mapping.json` - Bulk order scenarios (BULK-ORDER-*, LARGE-*, MULTI-*)
- `urgent-order-mapping.json` - Urgent/emergency orders (URGENT-*, EMERGENCY-*, STAT-*)

### Error Condition Mappings
- `validation-error-mapping.json` - 400 responses for invalid order data
- `server-error-mapping.json` - 500 responses for server error simulation
- `rate-limit-mapping.json` - 429 responses for rate limiting scenarios
- `maintenance-mapping.json` - 503 responses for service maintenance scenarios

### Performance Testing Mappings
- `timeout-simulation-mapping.json` - Simulates slow responses for timeout testing

## Pattern Matching

The mappings use `routePathPattern` with regex patterns to match incoming requests:

### Success Patterns
- `orders\.(TEST-ORDER-1|TEST-ORD-2025-018|[A-Z0-9-]+)` - Valid order IDs
- `orders\.TEST-ORDER-1` - Specific test case 1
- `orders\.TEST-ORD-2025-018` - Specific test case 2

### Error Patterns
- `orders\.(NOTFOUND-.*|ERROR-404-.*)` - Orders that should return 404
- `orders\.(INVALID-.*|ERROR-400-.*)` - Orders that should return 400
- `orders\.(ERROR-500-.*|SERVER-ERROR-.*)` - Orders that should return 500
- `orders\.(RATE-LIMIT-.*|TOO-MANY-.*)` - Orders that should return 429 (rate limit)
- `orders\.(MAINTENANCE-.*|SERVICE-DOWN-.*)` - Orders that should return 503 (maintenance)

### Special Scenario Patterns
- `orders\.(BULK-ORDER-.*|LARGE-.*|MULTI-.*)` - Bulk orders with multiple items
- `orders\.(URGENT-.*|EMERGENCY-.*|STAT-.*)` - Urgent/emergency orders
- `orders\.(TIMEOUT-.*|SLOW-.*)` - Slow responses for timeout testing

### Fallback Pattern
- `.*` - Catches all unmapped requests

## Test Scenarios

### Valid Orders
- `TEST-ORDER-1` - Basic order with 2 items
- `TEST-ORD-2025-018` - Bulk order with special handling
- Any pattern matching `[A-Z0-9-]+` - Returns generic complete order

### Error Conditions
- `NOTFOUND-123` - Returns 404 not found
- `INVALID-ABC` - Returns 400 validation error
- `ERROR-500-XYZ` - Returns 500 server error
- `RATE-LIMIT-001` - Returns 429 rate limit exceeded
- `MAINTENANCE-001` - Returns 503 service maintenance

### Special Scenarios
- `BULK-ORDER-001` - Returns bulk order with multiple items
- `URGENT-TRAUMA-001` - Returns urgent order for trauma scenarios
- `TIMEOUT-TEST-001` - Returns response after 8-second delay

## Response Files

All response files are located in the `../mock-responses/` directory:
- `orders/` - Contains successful order response files
- `errors/` - Contains error response files

## Usage in Tests

Use these patterns in your test cases to simulate different scenarios:

```java
// Success cases
client.getOrderData("TEST-ORDER-1");        // Basic order
client.getOrderData("TEST-ORD-2025-018");   // Bulk order
client.getOrderData("VALID-ORDER-123");     // Generic order

// Special scenarios
client.getOrderData("BULK-ORDER-001");      // Bulk order with many items
client.getOrderData("URGENT-TRAUMA-001");   // Urgent trauma order
client.getOrderData("TIMEOUT-TEST-001");    // Slow response (8s delay)

// Error cases
client.getOrderData("NOTFOUND-123");        // 404
client.getOrderData("INVALID-ABC");         // 400
client.getOrderData("ERROR-500-XYZ");       // 500
client.getOrderData("RATE-LIMIT-001");      // 429
client.getOrderData("MAINTENANCE-001");     // 503
```