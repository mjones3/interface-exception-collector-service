# Mock RSocket Server Response Files

This directory contains response files that the mock RSocket server returns for different order scenarios.

## Directory Structure

```
mock-responses/
├── orders/                               # Successful order responses
│   ├── complete-order-with-items.json        # Generic complete order
│   ├── TEST-ORDER-1.json                     # Test scenario 1
│   ├── TEST-ORD-2025-018.json               # Test scenario 2
│   ├── bulk-order-response.json             # Bulk order with multiple items
│   └── urgent-order-response.json           # Urgent/emergency order
└── errors/                               # Error responses
    ├── not-found.json                        # 404 order not found
    ├── validation-error.json                 # 400 validation error
    ├── server-error.json                    # 500 internal server error
    ├── rate-limit-error.json                # 429 rate limit exceeded
    └── maintenance-error.json               # 503 service maintenance
```

## Order Response Structure

All successful order responses include:

### Core Order Data
- `externalId` - Unique order identifier
- `customerId` - Customer identifier
- `locationCode` - Location code
- `orderDate` - Order creation timestamp
- `status` - Order status (PENDING, PROCESSING, etc.)
- `totalAmount` - Total order amount
- `currency` - Currency code

### Order Items
Each order contains multiple items with:
- `itemId` - Unique item identifier
- `productCode` - Product code
- `bloodType` - Blood type (O_POS, A_NEG, etc.)
- `productFamily` - Product family (RED_BLOOD_CELLS, PLATELETS, PLASMA)
- `quantity` - Item quantity
- `unitPrice` - Price per unit
- `totalPrice` - Total price for item
- `attributes` - Additional item attributes

### Customer Information
- `customerInfo` - Customer details (name, email, phone, contact person)
- `shippingAddress` - Delivery address information

### Metadata
- `metadata` - Additional order metadata (priority, delivery date, special handling)

## Test Scenarios

### TEST-ORDER-1
- Basic order with 2 items (Red Blood Cells + Plasma)
- Normal priority
- Standard delivery
- Total: $750.00

### TEST-ORD-2025-018
- Bulk order with 3 Red Blood Cells + 2 Platelets
- High priority
- Special handling required
- Total: $2,100.00

### BULK-ORDER-2025-001
- Large hospital network order
- 10 Red Blood Cells (O+), 8 Red Blood Cells (A+), 15 Platelets (O+), 6 Platelets (A-), 5 Plasma (AB+)
- High priority with contract number
- Total: $15,750.00

### URGENT-TRAUMA-001
- Emergency trauma center order
- 4 Universal donor Red Blood Cells (O-), 1 Universal Platelets (O-)
- STAT priority for trauma patient
- Total: $2,250.00

## Error Responses

### 404 Not Found
- Used when order doesn't exist
- Includes error code and suggestions
- Triggered by patterns: `NOTFOUND-*`, `ERROR-404-*`

### 400 Validation Error
- Used for invalid order data
- Includes validation error details
- Triggered by patterns: `INVALID-*`, `ERROR-400-*`

### 500 Server Error
- Used to simulate server failures
- Includes correlation ID for tracking
- Triggered by patterns: `ERROR-500-*`, `SERVER-ERROR-*`

### 429 Rate Limit Error
- Used to simulate rate limiting
- Includes retry-after header and rate limit details
- Triggered by patterns: `RATE-LIMIT-*`, `TOO-MANY-*`

### 503 Service Maintenance
- Used to simulate service maintenance windows
- Includes estimated recovery time and maintenance details
- Triggered by patterns: `MAINTENANCE-*`, `SERVICE-DOWN-*`

## BioPro Domain Compatibility

All order responses are designed to be compatible with BioPro's domain model:
- Blood type enumeration matches BioPro standards
- Product families align with BioPro categories
- Order structure supports retry operations
- Customer and shipping data matches expected formats