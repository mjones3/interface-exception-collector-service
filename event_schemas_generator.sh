#!/bin/bash

# Event Schema Generator for Interface Exception Collector Service
# Generates individual JSON Schema files for each event type

set -e

SCHEMA_DIR="src/main/resources/schema"

echo "🎯 Generating Event Schemas for Interface Exception Collector Service..."

# Create schema directory
mkdir -p $SCHEMA_DIR

# Generate Inbound Event Schemas
echo "📥 Creating Inbound Event Schemas..."

# OrderRejected-Inbound.json
cat > $SCHEMA_DIR/OrderRejected-Inbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/OrderRejected/v1.0.0",
  "title": "Order Rejected Event",
  "description": "Event published when an order is rejected by the Order Service",
  "type": "object",
  "required": [
    "eventId",
    "eventType", 
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid",
      "description": "Unique event identifier"
    },
    "eventType": {
      "type": "string",
      "const": "OrderRejected",
      "description": "Type of event"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$",
      "description": "Event schema version"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time",
      "description": "When the event occurred"
    },
    "source": {
      "type": "string",
      "const": "order-service",
      "description": "Service that published the event"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid",
      "description": "Correlation ID for request tracing"
    },
    "causationId": {
      "type": "string",
      "format": "uuid",
      "description": "ID of the event that caused this event"
    },
    "payload": {
      "type": "object",
      "required": [
        "transactionId",
        "externalId",
        "operation",
        "rejectedReason"
      ],
      "properties": {
        "transactionId": {
          "type": "string",
          "format": "uuid",
          "description": "Transaction ID from order processing"
        },
        "externalId": {
          "type": "string",
          "description": "Customer's external order ID"
        },
        "operation": {
          "type": "string",
          "enum": ["CREATE_ORDER", "MODIFY_ORDER", "CANCEL_ORDER"],
          "description": "Operation that was attempted"
        },
        "rejectedReason": {
          "type": "string",
          "description": "Human-readable rejection reason"
        },
        "customerId": {
          "type": "string",
          "description": "Customer identifier"
        },
        "locationCode": {
          "type": "string",
          "description": "Location where order was submitted"
        },
        "orderItems": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "bloodType": {
                "type": "string",
                "description": "Blood type for the item"
              },
              "productFamily": {
                "type": "string",
                "description": "Product family classification"
              },
              "quantity": {
                "type": "integer",
                "minimum": 1,
                "description": "Quantity requested"
              }
            },
            "required": ["bloodType", "productFamily", "quantity"]
          }
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440000",
      "eventType": "OrderRejected",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T10:30:00Z",
      "source": "order-service",
      "correlationId": "550e8400-e29b-41d4-a716-446655440001",
      "payload": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440002",
        "externalId": "ORDER-ABC123",
        "operation": "CREATE_ORDER",
        "rejectedReason": "Order already exists for external ID ORDER-ABC123",
        "customerId": "CUST001",
        "locationCode": "LOC123",
        "orderItems": [
          {
            "bloodType": "O+",
            "productFamily": "WHOLE_BLOOD",
            "quantity": 5
          }
        ]
      }
    }
  ]
}
EOF

# OrderCancelled-Inbound.json
cat > $SCHEMA_DIR/OrderCancelled-Inbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/OrderCancelled/v1.0.0",
  "title": "Order Cancelled Event",
  "description": "Event published when an order is cancelled due to processing issues",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion", 
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string", 
      "const": "OrderCancelled"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "order-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "transactionId",
        "externalId", 
        "cancelReason"
      ],
      "properties": {
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "externalId": {
          "type": "string"
        },
        "cancelReason": {
          "type": "string",
          "description": "Reason for cancellation"
        },
        "cancelledBy": {
          "type": "string",
          "description": "User or system that cancelled the order"
        },
        "customerId": {
          "type": "string"
        },
        "orderNumber": {
          "type": "integer",
          "description": "Internal order number"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440003",
      "eventType": "OrderCancelled",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T10:35:00Z",
      "source": "order-service",
      "payload": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440004",
        "externalId": "ORDER-DEF456",
        "cancelReason": "Customer requested cancellation",
        "cancelledBy": "system",
        "customerId": "CUST002",
        "orderNumber": 12345
      }
    }
  ]
}
EOF

# CollectionRejected-Inbound.json
cat > $SCHEMA_DIR/CollectionRejected-Inbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/CollectionRejected/v1.0.0",
  "title": "Collection Rejected Event", 
  "description": "Event published when a collection request is rejected",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn", 
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "CollectionRejected"
    },
    "eventVersion": {
      "type": "string", 
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "collection-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "transactionId",
        "collectionId",
        "operation",
        "rejectedReason"
      ],
      "properties": {
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "collectionId": {
          "type": "string",
          "description": "External collection identifier"
        },
        "operation": {
          "type": "string",
          "enum": ["CREATE_COLLECTION", "MODIFY_COLLECTION"],
          "description": "Operation that was attempted"
        },
        "rejectedReason": {
          "type": "string",
          "description": "Human-readable rejection reason"
        },
        "donorId": {
          "type": "string",
          "description": "Donor identifier"
        },
        "locationCode": {
          "type": "string",
          "description": "Collection location"
        },
        "collectionDate": {
          "type": "string",
          "format": "date",
          "description": "Scheduled collection date"
        },
        "collectionType": {
          "type": "string",
          "enum": ["WHOLE_BLOOD", "PLATELET", "PLASMA"],
          "description": "Type of collection"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440005",
      "eventType": "CollectionRejected", 
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T11:00:00Z",
      "source": "collection-service",
      "payload": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440006",
        "collectionId": "COLL-ABC123",
        "operation": "CREATE_COLLECTION",
        "rejectedReason": "Donor not eligible for collection",
        "donorId": "DONOR001",
        "locationCode": "LOC456",
        "collectionDate": "2025-08-05",
        "collectionType": "WHOLE_BLOOD"
      }
    }
  ]
}
EOF

# DistributionFailed-Inbound.json
cat > $SCHEMA_DIR/DistributionFailed-Inbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/DistributionFailed/v1.0.0",
  "title": "Distribution Failed Event",
  "description": "Event published when distribution processing fails",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn",
    "source", 
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "DistributionFailed"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "distribution-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "transactionId",
        "distributionId",
        "operation",
        "failureReason"
      ],
      "properties": {
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "distributionId": {
          "type": "string",
          "description": "External distribution identifier"
        },
        "operation": {
          "type": "string",
          "enum": ["CREATE_DISTRIBUTION", "MODIFY_DISTRIBUTION"],
          "description": "Operation that failed"
        },
        "failureReason": {
          "type": "string",
          "description": "Human-readable failure reason"
        },
        "customerId": {
          "type": "string",
          "description": "Customer identifier"
        },
        "destinationLocation": {
          "type": "string",
          "description": "Destination location code"
        },
        "requestedProducts": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "productCode": {
                "type": "string"
              },
              "quantity": {
                "type": "integer"
              },
              "bloodType": {
                "type": "string"
              }
            }
          }
        },
        "errorCode": {
          "type": "string",
          "description": "Technical error code"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440007",
      "eventType": "DistributionFailed",
      "eventVersion": "1.0", 
      "occurredOn": "2025-08-04T11:30:00Z",
      "source": "distribution-service",
      "payload": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440008",
        "distributionId": "DIST-ABC123",
        "operation": "CREATE_DISTRIBUTION",
        "failureReason": "Insufficient inventory for requested products",
        "customerId": "CUST003",
        "destinationLocation": "LOC789",
        "requestedProducts": [
          {
            "productCode": "WB001",
            "quantity": 10,
            "bloodType": "A+"
          }
        ],
        "errorCode": "INVENTORY_SHORTAGE"
      }
    }
  ]
}
EOF

# ValidationError-Inbound.json
cat > $SCHEMA_DIR/ValidationError-Inbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/ValidationError/v1.0.0",
  "title": "Validation Error Event",
  "description": "Event published when schema or data validation fails",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "ValidationError"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "description": "Service that detected the validation error"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "transactionId",
        "interfaceType",
        "validationErrors"
      ],
      "properties": {
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "interfaceType": {
          "type": "string",
          "enum": ["ORDER", "COLLECTION", "DISTRIBUTION"],
          "description": "Type of interface that failed validation"
        },
        "validationErrors": {
          "type": "array",
          "minItems": 1,
          "items": {
            "type": "object",
            "required": ["field", "errorCode", "message"],
            "properties": {
              "field": {
                "type": "string",
                "description": "Field that failed validation"
              },
              "errorCode": {
                "type": "string",
                "description": "Validation error code"
              },
              "message": {
                "type": "string",
                "description": "Human-readable error message"
              },
              "rejectedValue": {
                "description": "Value that was rejected"
              }
            }
          }
        },
        "schemaVersion": {
          "type": "string",
          "description": "Version of schema used for validation"
        },
        "endpoint": {
          "type": "string",
          "description": "API endpoint that received the request"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440009",
      "eventType": "ValidationError",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T12:00:00Z",
      "source": "order-service",
      "payload": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440010",
        "interfaceType": "ORDER",
        "validationErrors": [
          {
            "field": "orderItems[0].quantity",
            "errorCode": "INVALID_RANGE",
            "message": "Quantity must be greater than 0",
            "rejectedValue": 0
          },
          {
            "field": "customerId",
            "errorCode": "REQUIRED_FIELD",
            "message": "Customer ID is required"
          }
        ],
        "schemaVersion": "1.2.0",
        "endpoint": "/api/orders"
      }
    }
  ]
}
EOF

echo "📤 Creating Outbound Event Schemas..."

# ExceptionCaptured-Outbound.json
cat > $SCHEMA_DIR/ExceptionCaptured-Outbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/ExceptionCaptured/v1.0.0",
  "title": "Exception Captured Event",
  "description": "Event published when a new exception is captured and stored",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "ExceptionCaptured"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "exception-collector-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "causationId": {
      "type": "string",
      "format": "uuid",
      "description": "ID of the original exception event that caused this"
    },
    "payload": {
      "type": "object",
      "required": [
        "exceptionId",
        "transactionId",
        "interfaceType",
        "severity",
        "category"
      ],
      "properties": {
        "exceptionId": {
          "type": "integer",
          "format": "int64",
          "description": "Unique exception ID in collector database"
        },
        "transactionId": {
          "type": "string",
          "format": "uuid",
          "description": "Original transaction ID"
        },
        "interfaceType": {
          "type": "string",
          "enum": ["ORDER", "COLLECTION", "DISTRIBUTION"],
          "description": "Type of interface that generated the exception"
        },
        "severity": {
          "type": "string",
          "enum": ["LOW", "MEDIUM", "HIGH", "CRITICAL"],
          "description": "Severity level assigned to the exception"
        },
        "category": {
          "type": "string",
          "enum": ["BUSINESS_RULE", "VALIDATION", "SYSTEM_ERROR", "INTEGRATION"],
          "description": "Category of the exception"
        },
        "exceptionReason": {
          "type": "string",
          "description": "Human-readable exception reason"
        },
        "customerId": {
          "type": "string",
          "description": "Customer identifier"
        },
        "externalId": {
          "type": "string",
          "description": "Customer's external reference ID"
        },
        "retryable": {
          "type": "boolean",
          "description": "Whether this exception can be retried"
        },
        "operation": {
          "type": "string",
          "description": "Operation that caused the exception"
        },
        "capturedAt": {
          "type": "string",
          "format": "date-time",
          "description": "When the exception was captured by collector"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440011",
      "eventType": "ExceptionCaptured",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T10:30:05Z",
      "source": "exception-collector-service",
      "correlationId": "550e8400-e29b-41d4-a716-446655440001",
      "causationId": "550e8400-e29b-41d4-a716-446655440000",
      "payload": {
        "exceptionId": 12345,
        "transactionId": "550e8400-e29b-41d4-a716-446655440002",
        "interfaceType": "ORDER",
        "severity": "MEDIUM",
        "category": "BUSINESS_RULE",
        "exceptionReason": "Order already exists for external ID ORDER-ABC123",
        "customerId": "CUST001",
        "externalId": "ORDER-ABC123",
        "retryable": false,
        "operation": "CREATE_ORDER",
        "capturedAt": "2025-08-04T10:30:05Z"
      }
    }
  ]
}
EOF

# ExceptionRetryCompleted-Outbound.json
cat > $SCHEMA_DIR/ExceptionRetryCompleted-Outbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/ExceptionRetryCompleted/v1.0.0", 
  "title": "Exception Retry Completed Event",
  "description": "Event published when a retry operation completes",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "ExceptionRetryCompleted"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "exception-collector-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "exceptionId",
        "transactionId",
        "attemptNumber",
        "retryStatus"
      ],
      "properties": {
        "exceptionId": {
          "type": "integer",
          "format": "int64"
        },
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "attemptNumber": {
          "type": "integer",
          "minimum": 1,
          "description": "Which retry attempt this was"
        },
        "retryStatus": {
          "type": "string",
          "enum": ["SUCCESS", "FAILED"],
          "description": "Result of the retry operation"
        },
        "retryResult": {
          "type": "object",
          "properties": {
            "success": {
              "type": "boolean"
            },
            "message": {
              "type": "string",
              "description": "Response message from target service"
            },
            "responseCode": {
              "type": "integer",
              "description": "HTTP response code"
            },
            "errorDetails": {
              "type": "object",
              "description": "Additional error information if retry failed"
            }
          }
        },
        "initiatedBy": {
          "type": "string",
          "description": "User or system that initiated the retry"
        },
        "targetService": {
          "type": "string",
          "enum": ["order-service", "collection-service", "distribution-service"],
          "description": "Service that processed the retry"
        },
        "completedAt": {
          "type": "string",
          "format": "date-time"
        },
        "processingTimeMs": {
          "type": "integer",
          "description": "Time taken to process retry in milliseconds"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440012",
      "eventType": "ExceptionRetryCompleted",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T14:15:30Z",
      "source": "exception-collector-service",
      "payload": {
        "exceptionId": 12346,
        "transactionId": "550e8400-e29b-41d4-a716-446655440013",
        "attemptNumber": 1,
        "retryStatus": "SUCCESS",
        "retryResult": {
          "success": true,
          "message": "Order processed successfully",
          "responseCode": 200
        },
        "initiatedBy": "user123",
        "targetService": "order-service",
        "completedAt": "2025-08-04T14:15:30Z",
        "processingTimeMs": 250
      }
    }
  ]
}
EOF

# ExceptionResolved-Outbound.json
cat > $SCHEMA_DIR/ExceptionResolved-Outbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/ExceptionResolved/v1.0.0",
  "title": "Exception Resolved Event",
  "description": "Event published when an exception is fully resolved",
  "type": "object",
  "required": [
    "eventId",
    "eventType", 
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "ExceptionResolved"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "exception-collector-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "exceptionId",
        "transactionId",
        "resolutionMethod"
      ],
      "properties": {
        "exceptionId": {
          "type": "integer",
          "format": "int64"
        },
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "resolutionMethod": {
          "type": "string",
          "enum": ["RETRY_SUCCESS", "MANUAL_RESOLUTION", "CUSTOMER_RESOLVED", "AUTOMATED"],
          "description": "How the exception was resolved"
        },
        "resolvedBy": {
          "type": "string",
          "description": "User or system that resolved the exception"
        },
        "resolvedAt": {
          "type": "string",
          "format": "date-time"
        },
        "totalRetryAttempts": {
          "type": "integer",
          "minimum": 0,
          "description": "Total number of retry attempts made"
        },
        "resolutionTimeHours": {
          "type": "number",
          "description": "Hours from exception capture to resolution"
        },
        "resolutionNotes": {
          "type": "string",
          "description": "Optional notes about the resolution"
        },
        "customerNotified": {
          "type": "boolean",
          "description": "Whether customer was notified of resolution"
        },
        "finalStatus": {
          "type": "string",
          "enum": ["RESOLVED", "CLOSED"],
          "description": "Final status of the exception"
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440014",
      "eventType": "ExceptionResolved",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T14:20:00Z",
      "source": "exception-collector-service",
      "payload": {
        "exceptionId": 12346,
        "transactionId": "550e8400-e29b-41d4-a716-446655440013",
        "resolutionMethod": "RETRY_SUCCESS",
        "resolvedBy": "user123",
        "resolvedAt": "2025-08-04T14:20:00Z",
        "totalRetryAttempts": 1,
        "resolutionTimeHours": 3.5,
        "resolutionNotes": "Successfully retried after customer corrected data",
        "customerNotified": true,
        "finalStatus": "RESOLVED"
      }
    }
  ]
}
EOF

# CriticalExceptionAlert-Outbound.json
cat > $SCHEMA_DIR/CriticalExceptionAlert-Outbound.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/events/CriticalExceptionAlert/v1.0.0",
  "title": "Critical Exception Alert Event",
  "description": "High-priority alert for critical exceptions requiring immediate attention",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "occurredOn",
    "source",
    "payload"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid"
    },
    "eventType": {
      "type": "string",
      "const": "CriticalExceptionAlert"
    },
    "eventVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "occurredOn": {
      "type": "string",
      "format": "date-time"
    },
    "source": {
      "type": "string",
      "const": "exception-collector-service"
    },
    "correlationId": {
      "type": "string",
      "format": "uuid"
    },
    "payload": {
      "type": "object",
      "required": [
        "exceptionId",
        "transactionId",
        "alertLevel",
        "alertReason"
      ],
      "properties": {
        "exceptionId": {
          "type": "integer",
          "format": "int64"
        },
        "transactionId": {
          "type": "string",
          "format": "uuid"
        },
        "alertLevel": {
          "type": "string",
          "enum": ["HIGH", "CRITICAL", "EMERGENCY"],
          "description": "Severity level of the alert"
        },
        "alertReason": {
          "type": "string",
          "enum": [
            "CRITICAL_SEVERITY", 
            "MULTIPLE_RETRIES_FAILED",
            "SYSTEM_ERROR",
            "CUSTOMER_IMPACT",
            "SLA_BREACH"
          ],
          "description": "Reason for generating critical alert"
        },
        "interfaceType": {
          "type": "string",
          "enum": ["ORDER", "COLLECTION", "DISTRIBUTION"]
        },
        "exceptionReason": {
          "type": "string",
          "description": "Original exception reason"
        },
        "customerId": {
          "type": "string"
        },
        "customersAffected": {
          "type": "integer",
          "minimum": 1,
          "description": "Number of customers affected by this exception pattern"
        },
        "estimatedImpact": {
          "type": "string",
          "enum": ["LOW", "MEDIUM", "HIGH", "SEVERE"],
          "description": "Estimated business impact"
        },
        "requiresImmediateAction": {
          "type": "boolean",
          "description": "Whether this alert requires immediate response"
        },
        "escalationTeam": {
          "type": "string",
          "enum": ["OPERATIONS", "ENGINEERING", "CUSTOMER_SUCCESS", "MANAGEMENT"],
          "description": "Team that should handle this alert"
        },
        "similarExceptionsCount": {
          "type": "integer",
          "description": "Number of similar exceptions in last hour"
        },
        "recommendedActions": {
          "type": "array",
          "items": {
            "type": "string"
          },
          "description": "Suggested actions to resolve the issue"
        },
        "slaBreach": {
          "type": "object",
          "properties": {
            "breached": {
              "type": "boolean"
            },
            "slaTarget": {
              "type": "string",
              "description": "Target SLA time"
            },
            "actualTime": {
              "type": "string",
              "description": "Actual time elapsed"
            }
          }
        }
      }
    }
  },
  "examples": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440015",
      "eventType": "CriticalExceptionAlert",
      "eventVersion": "1.0",
      "occurredOn": "2025-08-04T10:45:00Z",
      "source": "exception-collector-service",
      "payload": {
        "exceptionId": 12347,
        "transactionId": "550e8400-e29b-41d4-a716-446655440016",
        "alertLevel": "CRITICAL",
        "alertReason": "MULTIPLE_RETRIES_FAILED",
        "interfaceType": "ORDER",
        "exceptionReason": "Database connection timeout",
        "customerId": "CUST001",
        "customersAffected": 15,
        "estimatedImpact": "HIGH",
        "requiresImmediateAction": true,
        "escalationTeam": "ENGINEERING",
        "similarExceptionsCount": 23,
        "recommendedActions": [
          "Check database connectivity",
          "Review connection pool settings",
          "Contact infrastructure team"
        ],
        "slaBreach": {
          "breached": true,
          "slaTarget": "15 minutes",
          "actualTime": "45 minutes"
        }
      }
    }
  ]
}
EOF

echo "📋 Creating Schema Index File..."

# Create schema index file
cat > $SCHEMA_DIR/schema-index.json << 'EOF'
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://schemas.biopro.arcone.com/exception-collector/index",
  "title": "Interface Exception Collector Service - Schema Index",
  "description": "Index of all event schemas used by the Interface Exception Collector Service",
  "type": "object",
  "properties": {
    "inboundEvents": {
      "type": "object",
      "description": "Events consumed by the Exception Collector Service",
      "properties": {
        "OrderRejected": {
          "$ref": "./OrderRejected-Inbound.json",
          "description": "Order rejection events from Order Service"
        },
        "OrderCancelled": {
          "$ref": "./OrderCancelled-Inbound.json",
          "description": "Order cancellation events from Order Service"
        },
        "CollectionRejected": {
          "$ref": "./CollectionRejected-Inbound.json", 
          "description": "Collection rejection events from Collection Service"
        },
        "DistributionFailed": {
          "$ref": "./DistributionFailed-Inbound.json",
          "description": "Distribution failure events from Distribution Service"
        },
        "ValidationError": {
          "$ref": "./ValidationError-Inbound.json",
          "description": "Schema validation errors from any interface service"
        }
      }
    },
    "outboundEvents": {
      "type": "object",
      "description": "Events published by the Exception Collector Service",
      "properties": {
        "ExceptionCaptured": {
          "$ref": "./ExceptionCaptured-Outbound.json",
          "description": "Published when new exception is captured and stored"
        },
        "ExceptionRetryCompleted": {
          "$ref": "./ExceptionRetryCompleted-Outbound.json",
          "description": "Published when retry operation completes"
        },
        "ExceptionResolved": {
          "$ref": "./ExceptionResolved-Outbound.json",
          "description": "Published when exception is fully resolved"
        },
        "CriticalExceptionAlert": {
          "$ref": "./CriticalExceptionAlert-Outbound.json",
          "description": "High-priority alerts for critical exceptions"
        }
      }
    }
  },
  "schemaVersions": {
    "current": "1.0.0",
    "supported": ["1.0.0"],
    "deprecated": []
  },
  "metadata": {
    "service": "interface-exception-collector-service",
    "domain": "exception-management",
    "owner": "architecture-team",
    "lastUpdated": "2025-08-04T12:00:00Z"
  }
}
EOF

echo "🔧 Creating Schema Validation Test..."

# Create validation test file
cat > $SCHEMA_DIR/validate-schemas.js << 'EOF'
#!/usr/bin/env node

/**
 * Schema Validation Test Script
 * Validates all event schemas and example payloads
 */

const fs = require('fs');
const path = require('path');
const Ajv = require('ajv');
const addFormats = require('ajv-formats');

const ajv = new Ajv({ allErrors: true, strict: false });
addFormats(ajv);

const schemaDir = __dirname;
const schemas = [
  'OrderRejected-Inbound.json',
  'OrderCancelled-Inbound.json', 
  'CollectionRejected-Inbound.json',
  'DistributionFailed-Inbound.json',
  'ValidationError-Inbound.json',
  'ExceptionCaptured-Outbound.json',
  'ExceptionRetryCompleted-Outbound.json',
  'ExceptionResolved-Outbound.json',
  'CriticalExceptionAlert-Outbound.json'
];

console.log('🔍 Validating Event Schemas...\n');

let allValid = true;

schemas.forEach(schemaFile => {
  const schemaPath = path.join(schemaDir, schemaFile);
  
  try {
    const schemaContent = fs.readFileSync(schemaPath, 'utf8');
    const schema = JSON.parse(schemaContent);
    
    // Validate schema structure
    const validate = ajv.compile(schema);
    
    // Test with example data if present
    if (schema.examples && schema.examples.length > 0) {
      schema.examples.forEach((example, index) => {
        const valid = validate(example);
        if (valid) {
          console.log(`✅ ${schemaFile} - Example ${index + 1}: Valid`);
        } else {
          console.log(`❌ ${schemaFile} - Example ${index + 1}: Invalid`);
          console.log(`   Errors: ${ajv.errorsText(validate.errors)}`);
          allValid = false;
        }
      });
    } else {
      console.log(`⚠️  ${schemaFile}: No examples to validate`);
    }
    
  } catch (error) {
    console.log(`❌ ${schemaFile}: Failed to parse - ${error.message}`);
    allValid = false;
  }
});

console.log(`\n${allValid ? '🎉' : '💥'} Schema validation ${allValid ? 'completed successfully' : 'failed'}`);
process.exit(allValid ? 0 : 1);
EOF

chmod +x $SCHEMA_DIR/validate-schemas.js

echo "📝 Creating README for Schema Directory..."

# Create README for schema directory
cat > $SCHEMA_DIR/README.md << 'EOF'
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
EOF

echo "✅ Event Schemas Generated Successfully!"
echo ""
echo "📁 Created Schema Files:"
echo "├── Inbound Events:"
echo "│   ├── OrderRejected-Inbound.json"
echo "│   ├── OrderCancelled-Inbound.json"
echo "│   ├── CollectionRejected-Inbound.json"
echo "│   ├── DistributionFailed-Inbound.json"
echo "│   └── ValidationError-Inbound.json"
echo "├── Outbound Events:"
echo "│   ├── ExceptionCaptured-Outbound.json"
echo "│   ├── ExceptionRetryCompleted-Outbound.json"
echo "│   ├── ExceptionResolved-Outbound.json"
echo "│   └── CriticalExceptionAlert-Outbound.json"
echo "├── Utilities:"
echo "│   ├── schema-index.json"
echo "│   ├── validate-schemas.js"
echo "│   └── README.md"
echo ""
echo "🔧 Next Steps:"
echo "1. Run schema validation: cd $SCHEMA_DIR && node validate-schemas.js"
echo "2. Copy API specs to site/ directory for documentation"
echo "3. Configure Maven plugins for code generation"
echo "4. Add schema validation to Spring Boot configuration"
echo ""
echo "🎯 All schemas include:"
echo "• Complete JSON Schema Draft 2020-12 definitions"
echo "• Validation rules and constraints"
echo "• Real-world example payloads"
echo "• Detailed field descriptions"
echo "• Enum value definitions"
echo "• Format specifications (UUID, date-time, etc.)"
          