#!/usr/bin/env pwsh

# Final Resolution Steps
# This script provides the final steps to complete the end-to-end test

Write-Host "=== Final Resolution Steps ===" -ForegroundColor Green

Write-Host "Both services are running successfully!" -ForegroundColor Green
Write-Host "- Interface Exception Collector: http://localhost:8080" -ForegroundColor Cyan
Write-Host "- Partner Order Service: http://localhost:8090" -ForegroundColor Cyan

Write-Host ""
Write-Host "STEP 1: Fix Database Schema" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "The acknowledgment_notes column is missing. Please run this SQL in your PostgreSQL database:" -ForegroundColor White

$sqlFix = @"
-- Connect to your PostgreSQL database and run this:
-- psql -h localhost -p 5432 -U exception_user -d exception_collector_db

DO `$`$
BEGIN
    -- Add acknowledgment_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes TEXT;
        RAISE NOTICE 'Added acknowledgment_notes column';
    END IF;
    
    -- Add resolution_method column if it doesn't exist  
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_method'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_method VARCHAR(50);
        RAISE NOTICE 'Added resolution_method column';
    END IF;
    
    -- Add resolution_notes column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'resolution_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN resolution_notes TEXT;
        RAISE NOTICE 'Added resolution_notes column';
    END IF;
END
`$`$;
"@

Write-Host $sqlFix -ForegroundColor Cyan

Write-Host ""
Write-Host "STEP 2: Test the API (after running the SQL)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "After running the SQL, test the API:" -ForegroundColor White

$curlCommand = @"
# Test without authentication (should return 403 but no schema error)
curl -X GET http://localhost:8080/api/v1/exceptions

# Test with authentication (generate a new JWT token or disable security temporarily)
curl -X GET http://localhost:8080/api/v1/exceptions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
"@

Write-Host $curlCommand -ForegroundColor Cyan

Write-Host ""
Write-Host "STEP 3: Create Kafka Topics" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Ensure these Kafka topics exist:" -ForegroundColor White

$kafkaTopics = @"
# Create Kafka topics (adjust path to your Kafka installation)
kafka-topics --create --topic OrderRejected --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic OrderCancelled --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic CollectionRejected --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic DistributionFailed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic ValidationError --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify topics exist
kafka-topics --list --bootstrap-server localhost:9092
"@

Write-Host $kafkaTopics -ForegroundColor Cyan

Write-Host ""
Write-Host "STEP 4: Test Order Posting" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Post a test order to trigger the exception flow:" -ForegroundColor White

$orderCurl = @"
# Post a test order
curl -X POST http://localhost:8090/v1/partner-order-provider/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "locationCode": "LOC001", 
    "externalId": "TEST-ORDER-001",
    "orderItems": [
      {
        "bloodType": "O_POSITIVE",
        "productFamily": "RED_BLOOD_CELLS",
        "quantity": 1,
        "unitOfMeasure": "UNITS"
      }
    ],
    "priority": "NORMAL",
    "requestedDeliveryDate": "2025-09-13T10:00:00.000Z"
  }'
"@

Write-Host $orderCurl -ForegroundColor Cyan

Write-Host ""
Write-Host "STEP 5: Verify End-to-End Flow" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "After posting the order, check for exceptions:" -ForegroundColor White

$verifySteps = @"
1. Wait 10-15 seconds for Kafka event processing
2. Check exceptions endpoint: http://localhost:8080/api/v1/exceptions
3. Look for your test order in the exceptions list
4. Verify the exception has the correct externalId (TEST-ORDER-001)
5. Check Kafka consumer groups to ensure 'interface-exception-collector' is registered
"@

Write-Host $verifySteps -ForegroundColor Cyan

Write-Host ""
Write-Host "TROUBLESHOOTING" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host "If issues persist:" -ForegroundColor White
Write-Host "1. Check application logs for errors" -ForegroundColor Gray
Write-Host "2. Verify PostgreSQL is running and accessible" -ForegroundColor Gray
Write-Host "3. Verify Kafka is running on localhost:9092" -ForegroundColor Gray
Write-Host "4. Check that Kafka topics exist" -ForegroundColor Gray
Write-Host "5. Verify consumer group 'interface-exception-collector' is registered" -ForegroundColor Gray
Write-Host "6. Check JWT token configuration if authentication fails" -ForegroundColor Gray

Write-Host ""
Write-Host "FILES CREATED:" -ForegroundColor Green
Write-Host "- fix_schema.sql (database schema fix)" -ForegroundColor Cyan
Write-Host "- This script with all the manual steps" -ForegroundColor Cyan

Write-Host ""
Write-Host "Once you complete these steps, the end-to-end flow should work:" -ForegroundColor Green
Write-Host "Order -> Kafka Event -> Exception Collector -> Database -> API" -ForegroundColor Cyan