#!/usr/bin/env python3

import json
import sys
from confluent_kafka import Producer

def test_kafka_connection():
    """Test direct connection to Kafka"""
    
    # Try different server configurations
    servers_to_try = [
        '127.0.0.1:9092',
        'localhost:9092',
        '0.0.0.0:9092'
    ]
    
    for server in servers_to_try:
        print(f"🔍 Testing connection to {server}...")
        
        try:
            config = {
                'bootstrap.servers': server,
                'socket.timeout.ms': 5000,
                'api.version.request': True,
                'debug': 'broker,topic,metadata'
            }
            
            producer = Producer(config)
            
            # Try to get metadata
            metadata = producer.list_topics(timeout=5)
            
            print(f"✅ Successfully connected to {server}")
            print(f"📋 Available topics: {list(metadata.topics.keys())[:5]}...")
            
            # Try to send a test message
            test_message = {
                "eventId": "test-123",
                "eventType": "OrderRejected",
                "payload": {"test": "data"}
            }
            
            producer.produce(
                topic='OrderRejected',
                value=json.dumps(test_message),
                key='test-key'
            )
            
            producer.flush(timeout=5)
            print(f"✅ Successfully sent test message to {server}")
            return True
            
        except Exception as e:
            print(f"❌ Failed to connect to {server}: {e}")
            continue
    
    return False

if __name__ == "__main__":
    print("🧪 Direct Kafka Connection Test")
    print("=" * 50)
    
    try:
        if test_kafka_connection():
            print("\n🎉 Kafka connection test passed!")
            sys.exit(0)
        else:
            print("\n💥 All connection attempts failed")
            sys.exit(1)
    except ImportError as e:
        print(f"❌ Import error: {e}")
        print("💡 Try: pip install confluent-kafka")
        sys.exit(1)