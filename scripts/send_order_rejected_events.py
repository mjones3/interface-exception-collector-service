#!/usr/bin/env python3
"""
Script to send 10 OrderRejected events to Kafka with realistic blood banking domain data.
This script generates test data for the Interface Exception Collector Service.

This script should be run within a virtual environment.
Use ./send_test_events.sh to automatically handle the virtual environment setup.
"""

import json
import sys
import time
import random
from typing import Dict, Any, List

# Import our utilities
try:
    from kafka_utils import KafkaEventSender, EventGenerator, print_event_summary, check_kafka_connectivity
except ImportError:
    print("❌ Failed to import kafka_utils. Make sure you're running this from the scripts directory.")
    print("💡 Use ./send_test_events.sh to run this script with proper environment setup.")
    sys.exit(1)

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS = ['localhost:29092']
TOPIC_NAME = 'OrderRejected'

# Blood banking domain data
BLOOD_PRODUCTS = [
    {'itemId': 'RBC-O-NEG-001', 'itemType': 'Red Blood Cells', 'unitPrice': 245.50},
    {'itemId': 'RBC-A-POS-002', 'itemType': 'Red Blood Cells', 'unitPrice': 245.50},
    {'itemId': 'PLT-AB-POS-003', 'itemType': 'Platelets', 'unitPrice': 520.75},
    {'itemId': 'FFP-B-NEG-004', 'itemType': 'Fresh Frozen Plasma', 'unitPrice': 185.25},
    {'itemId': 'CRYO-O-POS-005', 'itemType': 'Cryoprecipitate', 'unitPrice': 165.00},
    {'itemId': 'WB-A-NEG-006', 'itemType': 'Whole Blood', 'unitPrice': 425.00},
    {'itemId': 'RBC-B-POS-007', 'itemType': 'Red Blood Cells', 'unitPrice': 245.50},
    {'itemId': 'PLT-O-NEG-008', 'itemType': 'Platelets', 'unitPrice': 520.75}
]

HOSPITAL_LOCATIONS = [
    'HOSP-NYC-001', 'HOSP-LA-002', 'HOSP-CHI-003', 'HOSP-HOU-004', 
    'HOSP-PHX-005', 'HOSP-PHI-006', 'HOSP-SA-007', 'HOSP-SD-008'
]

CUSTOMER_IDS = [
    'CUST-MOUNT-SINAI-001', 'CUST-CEDARS-SINAI-002', 'CUST-MAYO-CLINIC-003',
    'CUST-JOHNS-HOPKINS-004', 'CUST-CLEVELAND-CLINIC-005', 'CUST-MASS-GENERAL-006',
    'CUST-UCLA-MEDICAL-007', 'CUST-STANFORD-HEALTH-008', 'CUST-KAISER-PERM-009',
    'CUST-PRESBYTERIAN-010'
]

REJECTION_REASONS = [
    'Insufficient inventory for blood type O-negative',
    'Product expired - expiration date exceeded by 2 days',
    'Cross-match incompatibility detected for patient',
    'Invalid customer credentials - account suspended',
    'Order quantity exceeds maximum allowed limit',
    'Product recall in effect for lot number BB-2024-0156',
    'Temperature control breach during transport',
    'Duplicate order detected for same patient within 24 hours',
    'Invalid blood type specification in order',
    'Customer payment method declined - insufficient funds'
]

OPERATIONS = [
    'CREATE_BLOOD_ORDER',
    'RESERVE_BLOOD_UNITS',
    'SCHEDULE_DELIVERY',
    'VALIDATE_COMPATIBILITY',
    'PROCESS_URGENT_ORDER',
    'ALLOCATE_INVENTORY',
    'CONFIRM_AVAILABILITY',
    'UPDATE_ORDER_STATUS'
]

def generate_order_rejected_event() -> Dict[str, Any]:
    """Generate a realistic OrderRejected event for blood banking domain."""
    
    # Create base event structure
    event = EventGenerator.create_base_event('OrderRejected', 'order-service')
    
    # Generate transaction identifiers
    transaction_id = f"TXN-{random.randint(100000, 999999)}"
    external_id = f"EXT-ORD-{random.randint(10000, 99999)}"
    
    # Select random data
    customer_id = random.choice(CUSTOMER_IDS)
    location_code = random.choice(HOSPITAL_LOCATIONS)
    operation = random.choice(OPERATIONS)
    rejected_reason = random.choice(REJECTION_REASONS)
    
    # Generate 1-3 order items
    num_items = random.randint(1, 3)
    order_items = []
    
    for _ in range(num_items):
        product = random.choice(BLOOD_PRODUCTS)
        quantity = random.randint(1, 5)
        
        order_items.append({
            'itemId': product['itemId'],
            'itemType': product['itemType'],
            'quantity': quantity,
            'unitPrice': product['unitPrice']
        })
    
    # Add payload to the base event
    event['payload'] = {
        'transactionId': transaction_id,
        'externalId': external_id,
        'operation': operation,
        'rejectedReason': rejected_reason,
        'customerId': customer_id,
        'locationCode': location_code,
        'orderItems': order_items
    }
    
    return event

def send_events_to_kafka() -> bool:
    """Send 10 OrderRejected events to Kafka."""
    
    print(f"🩸 Blood Banking Order Rejection Event Generator")
    print(f"📡 Sending events to Kafka topic: {TOPIC_NAME}")
    print(f"🔗 Bootstrap servers: {KAFKA_BOOTSTRAP_SERVERS}")
    print("-" * 60)
    
    # Check Kafka connectivity first
    if not check_kafka_connectivity(KAFKA_BOOTSTRAP_SERVERS):
        print("❌ Cannot connect to Kafka. Please ensure Kafka is running.")
        print("💡 Try: docker-compose up -d kafka")
        return False
    
    # Send events using context manager
    try:
        with KafkaEventSender(KAFKA_BOOTSTRAP_SERVERS) as sender:
            for i in range(1, 11):
                # Generate event
                event = generate_order_rejected_event()
                
                # Use transaction ID as the message key for partitioning
                message_key = event['payload']['transactionId']
                
                # Send to Kafka
                success = sender.send_event(TOPIC_NAME, event, message_key)
                
                if success:
                    print_event_summary(event, i, 10)
                    print()
                else:
                    print(f"❌ Failed to send event {i}/10")
                    return False
                
                # Small delay between messages
                time.sleep(0.5)
        
        print("🎉 Successfully sent all 10 OrderRejected events!")
        print("💡 Check your application logs to see the events being processed.")
        return True
        
    except KeyboardInterrupt:
        print("\n⏹️  Event sending interrupted by user.")
        return False
    except Exception as e:
        print(f"❌ Unexpected error sending events: {e}")
        return False

def main() -> int:
    """Main function to run the script."""
    print("🚀 Starting Blood Banking Order Rejection Event Generator...")
    
    # Check if we're running in a virtual environment
    if sys.prefix == sys.base_prefix:
        print("⚠️  Warning: Not running in a virtual environment.")
        print("💡 For best results, use: ./send_test_events.sh")
        print()
    
    try:
        success = send_events_to_kafka()
        if success:
            print("\n✨ All events sent successfully!")
            print("🔍 You can now check the exception collector API to see the captured exceptions.")
            print("📊 API endpoint: http://localhost:8080/api/v1/exceptions")
            print("🧪 Test with Bruno collection or curl commands")
            return 0
        else:
            print("\n❌ Failed to send events. Please check your Kafka connection.")
            return 1
            
    except KeyboardInterrupt:
        print("\n⏹️  Script interrupted by user.")
        return 1
    except Exception as e:
        print(f"\n💥 Unexpected error: {e}")
        print("🐛 If this persists, check your Python environment and dependencies.")
        return 1

if __name__ == "__main__":
    exit(main())